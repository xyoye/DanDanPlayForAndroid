package com.xyoye.common_component.extension

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.xyoye.common_component.R

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

fun FragmentManager.addFragment(
    @IdRes viewId: Int,
    fragment: Fragment,
    tag: String
) {
    beginTransaction().apply {
        setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        add(viewId, fragment, tag)
        commit()
    }
}

fun FragmentManager.removeFragment(vararg fragment: Fragment) {
    beginTransaction().apply {
        setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
        fragment.forEach { remove(it) }
        commit()
    }
}

fun FragmentManager.findAndHideFragment(vararg tags: String) {
    beginTransaction().run {
        tags.forEach {
            findFragmentByTag(it)?.also { fragment ->
                this.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .hide(fragment)
            }
        }
        commit()
    }
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