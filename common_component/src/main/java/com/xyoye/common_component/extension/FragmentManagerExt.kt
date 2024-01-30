package com.xyoye.common_component.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

/**
 * Created by xyoye on 2020/7/29.
 */

fun FragmentManager.hideFragment(fragment: Fragment) {
    beginTransaction()
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
        .hide(fragment)
        .commit()
}

fun FragmentManager.showFragment(fragment: Fragment) {
    beginTransaction()
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .show(fragment)
        .commit()
}

fun FragmentManager.findAndRemoveFragment(vararg tags: String) {
    beginTransaction().run {
        tags.forEach {
            findFragmentByTag(it)?.also { fragment ->
                this.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .remove(fragment)
            }
        }
        commitNow()
    }
}