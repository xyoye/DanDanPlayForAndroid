package com.xyoye.common_component.utils

/**
 * Created by xyoye on 2021/3/2.
 */

object KeywordHelper {

    private val startSymbol =
        arrayOf('(', '[', '{', '（', '【', '〖', '『', '「', '〔', '《', '〈', '‹', '<')
    private val endSymbol =
        arrayOf(')', ']', '}', '）', '】', '〗', '』', '」', '〕', '》', '〉', '›', ">")
    private val divider = arrayOf('/', "|", "\\")
    private val spaces = arrayOf(' ', '\t', '\n')

    fun extract(text: String): MutableList<String> {
        //关键字集合
        val keywordList = mutableListOf<String>()
        //候选关键字
        val candidate = StringBuilder()
        //是否在括号内
        var isInBrackets = false

        text.toCharArray().forEach {
            when {
                //括号开头，已在括号内不提取（“[a[”）
                startSymbol.contains(it) && !isInBrackets -> {
                    if (candidate.isNotBlank()) {
                        keywordList.add(candidate.toString())
                        candidate.clear()
                    }
                    isInBrackets = true
                }
                //括号结尾
                endSymbol.contains(it) -> {
                    if (candidate.isNotBlank()) {
                        keywordList.add(candidate.toString())
                        candidate.clear()
                    }
                    isInBrackets = false
                }
                //分隔符
                divider.contains(it) -> {
                    if (candidate.isNotBlank()) {
                        keywordList.add(candidate.toString())
                        candidate.clear()
                    }
                }
                //空格，已在括号内不提取（“[a b]”）
                spaces.contains(it) && !isInBrackets -> {
                    if (candidate.isNotBlank()) {
                        keywordList.add(candidate.toString())
                        candidate.clear()
                    }
                }
                //将字符加入候选关键字，不可见字符跳过
                else -> if (it != '\u200B') candidate.append(it)
            }
        }
        if (candidate.isNotBlank()) {
            keywordList.add(candidate.toString())
        }
        return keywordList
    }
}