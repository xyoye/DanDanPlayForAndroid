package com.xyoye.anime_component.listener

/**
 * Created by xyoye on 2020/10/20.
 */

interface SearchListener {
    fun search(searchText: String)

    fun onTextClear();
}