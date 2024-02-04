package com.xyoye.anime_component.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.xyoye.anime_component.ui.fragment.anime_episode.AnimeEpisodeFragment
import com.xyoye.anime_component.ui.fragment.anime_intro.AnimeIntroFragment
import com.xyoye.anime_component.ui.fragment.anime_recommend.AnimeRecommendFragment
import com.xyoye.data_component.enums.AnimeDetailTab

/**
 * Created by xyoye on 2024/2/1
 */

class AnimeDetailPageAdapter(
    activity: FragmentActivity,
    val tabs: Array<AnimeDetailTab>
) : FragmentStateAdapter(activity) {

    override fun getItemCount() = tabs.size

    override fun createFragment(position: Int): Fragment {
        return when (tabs[position]) {
            AnimeDetailTab.INFO -> AnimeIntroFragment()
            AnimeDetailTab.EPISODES -> AnimeEpisodeFragment()
            AnimeDetailTab.RECOMMEND -> AnimeRecommendFragment()
            else -> AnimeIntroFragment()
        }
    }
}