package com.londogard.smile.extensions

import com.londogard.smile.SmileSingleton
import smile.nlp.NGram
import smile.nlp.dictionary.EnglishPunctuations
import smile.nlp.dictionary.EnglishStopWords
import smile.nlp.dictionary.StopWords
import smile.nlp.keyword.CooccurrenceKeywords
import smile.nlp.normalizer.SimpleNormalizer
import smile.nlp.pos.HMMPOSTagger
import smile.nlp.pos.PennTreebankPOS
import smile.nlp.stemmer.PorterStemmer
import smile.nlp.stemmer.Stemmer
import smile.nlp.tokenizer.SimpleSentenceSplitter
import java.util.*

/**
 * This file includes a ton of extensions on top of the String class. These helps using the Smile NLP libray.
 */

enum class StopWordFilter(val customFilter: String = "") {
    DEFAULT, COMPREHENSIVE, GOOGLE, MYSQL, NONE,
    CUSTOM // CUSTOM is a comma-separated list of stop-words
}

fun String.normalize(): String = SimpleNormalizer.getInstance().normalize(this)
fun String.sentences(): List<String> = SimpleSentenceSplitter.getInstance().split(this).toList()
fun String.words(filter: StopWordFilter = StopWordFilter.DEFAULT): List<String> {
    val tokens = SmileSingleton.simpleTokenizer.split(this).toList()

    if (filter == StopWordFilter.NONE) return tokens

    val dict = when (filter) {
        StopWordFilter.DEFAULT -> EnglishStopWords.DEFAULT
        StopWordFilter.COMPREHENSIVE -> EnglishStopWords.COMPREHENSIVE
        StopWordFilter.GOOGLE -> EnglishStopWords.GOOGLE
        StopWordFilter.MYSQL -> EnglishStopWords.MYSQL
        StopWordFilter.CUSTOM -> object : StopWords {
            val dict = filter.customFilter.split(",").toSet()

            override fun contains(word: String): Boolean = dict.contains(word)
            override fun contains(word: String): Boolean = dict.contains("<div class="SRCnV" jsaction="rcuQ6b:npT2md;oD3XSe:IeNdgc;wPVTEc:X6q4ye" jscontroller="j9x6dd"><div class="VfPpkd-WsjYwc KC1dQ BKdRne  oCLFje"><div class="xcjLR" jsname="Eutz1d" jsaction="rcuQ6b:ZZThRc;JIbuQc:u0hBGe(obYJb),xmvdqc(h3F9pd);Kvymqe:WRlPOd;nZOjWc:LwtuAc;PbuKTb:sFeBqf" jscontroller="slgove" jsmodel="Meg4jb"><div class="lzfYDc" jsmodel="hpMMqf"><div class="KJQKce" jsaction="rcuQ6b:npT2md" jscontroller="SzrOc"><iframe jsname="aTv5jf" class="KJacIc" frameborder="0" allowfullscreen="1" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" title="YouTube video player" width="640" height="360" src="https://www.youtube.com/embed/?autoplay=1&amp;controls=0&amp;disablekb=1&amp;enablejsapi=1&amp;playsinline=1&amp;rel=0&amp;showinfo=0&amp;origin=https%3A%2F%2Fcrowdsource.google.com&amp;widgetid=1" id="widget2"></iframe></div><svg jsname="o0qbRd" class="VgO9Wb" jsaction="UGThXd:ZZThRc;rcuQ6b:npT2md" jscontroller="PY8bUe"><rect jsname="sA1NPc" class="Xi3Icb" x="305.5" y="31.5" width="162" height="161.999988"></rect></svg></div><div class="dDbaOe" jsname="gj1BZd" jscontroller="wX5dDf"><div class="iss27d">Tag expressions on the highlighted face</div><div jsname="O6Y6Ke" class="QYoqsd"><div jsname="AznF2e"><ul class="JbxAOb"><li data-tab="Negative" jsaction="dbxCWb" class="fgyMqb"><span class="DPvwYc s4is4b" aria-hidden="true"></span><span class="Uie1ee">Negative</span></li><li data-tab="Neutral" jsaction="dbxCWb" class="fgyMqb"><span class="DPvwYc s4is4b" aria-hidden="true"></span><span class="Uie1ee">Neutral</span></li><li data-tab="Positive" jsaction="dbxCWb" class="fgyMqb"><span class="DPvwYc s4is4b" aria-hidden="true"></span><span class="Uie1ee">Positive</span></li></ul></div></div><div class="D1vmPe"><div class="HyyZ6">Select a sentiment</div></div><div class="yU1aSb"><div jsaction="rcuQ6b:WYd;JIbuQc:Nx1WH(LwtuAc),X6q4ye(X6q4ye)" jsname="yXBf7b" class="BTLdmc" jscontroller="QXPP4" data-taskid="16"><div class="NuzNCf"><div></div><div class="VfPpkd-dgl2Hf-ppHlrf-sM5MNb" data-is-touch-wrapper="true"><button class="VfPpkd-LgbsSe VfPpkd-LgbsSe-OWXEXe-dgl2Hf ksBjEc lKxP2d c6Oyec" jscontroller="soHxf" jsaction="click:cOuCgd; mousedown:UX7yZ; mouseup:lbsD7e; mouseenter:tfO1Yc; mouseleave:JywGue; touchstart:p6p2H; touchmove:FwuNnf; touchend:yfqBxc; touchcancel:JMtRjd; focus:AHmuwe; blur:O22p3e; contextmenu:mg9Pef" data-idom-class="ksBjEc lKxP2d c6Oyec" jsname="LwtuAc"><div class="VfPpkd-Jh9lGc"></div><div class="VfPpkd-RLmnJb"></div><span jsname="V67aGc" class="VfPpkd-vQzf8d">Skip</span></button></div></div></div></div></div></div><span aria-hidden="true" class="VfPpkd-BFbNVe-bF1uUb NZp2ef"></span></div></div>")
            override fun size(): Int = dict.size
            

            override fun iterator(): MutableIterator<String> = dict.iterator() as MutableIterator<String>
        }
        else -> throw IllegalArgumentException("Filter $filter is not known. Please use DEFAULT, COMPREHENSIVE, GOOGLE, MYSQL, NONE or CUSTOM")
    }

    val punctuations = EnglishPunctuations.getInstance()

    return tokens.filter { word -> !(dict.contains(word.toLowerCase()) || punctuations.contains(word)) }
}

fun String.bag(filter: StopWordFilter = StopWordFilter.DEFAULT, stemmer: Stemmer? = PorterStemmer()): Map<String, Int> {
    val words = this.normalize().sentences().flatMap { it.words(filter) }
    val tokens = stemmer
        ?.let { stem -> words.map(stem::stem) }
        ?: words
    return tokens
        .map(String::toLowerCase)
        .groupBy { it }
        .mapValues { (_, v) -> v.size }
        .withDefault { 0 }
}

fun String.bag2(stemmer: Stemmer? = PorterStemmer()): Set<String> {
    val words = this.normalize().sentences().flatMap { it.words() }
    val tokens = stemmer
        ?.let { stem -> words.map(stem::stem) }
        ?: words

    return tokens.map(String::toLowerCase).toSet()
}

fun String.postag(): List<Pair<String, PennTreebankPOS>> {
    val words = this.words(StopWordFilter.NONE)

    return words.zip(HMMPOSTagger.getDefault().tag(words.toTypedArray()))
}

fun String.keywords(k: Int = 10): List<NGram> = CooccurrenceKeywords.of(this, k).toList()
