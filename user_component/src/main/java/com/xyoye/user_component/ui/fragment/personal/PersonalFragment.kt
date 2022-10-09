package com.xyoye.user_component.ui.fragment.personal

import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.bridge.LoginObserver
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.utils.UserInfoHelper
import com.xyoye.data_component.data.LoginData
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.FragmentPersonalBinding
import com.xyoye.user_component.ui.dialog.UserCoverDialog
import com.xyoye.user_component.utils.FeedbackHelper

/**
 * Created by xyoye on 2020/7/28.
 */

@Route(path = RouteTable.User.PersonalFragment)
class PersonalFragment : BaseFragment<PersonalFragmentViewModel, FragmentPersonalBinding>() {

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        PersonalFragmentViewModel::class.java
    )

    override fun getLayoutId() = R.layout.fragment_personal

    override fun initView() {
        dataBinding.userCoverIv.setImageResource(getDefaultCoverResId())

        FeedbackHelper.init(requireActivity().application)

        initClick()

        applyLoginData(null)

        viewModel.relationLiveData.observe(this) {
            dataBinding.followAnimeTv.text = it.first.toString()
            dataBinding.followAnimeTv.setTextColorRes(R.color.text_pink)
            dataBinding.cloudHistoryTv.text = it.second.toString()
        }

        UserInfoHelper.loginLiveData.observe(this) {
            applyLoginData(it)
        }

        if (mAttachActivity is LoginObserver) {
            (mAttachActivity as LoginObserver).getLoginLiveData().observe(this) {
                applyLoginData(it)
            }
        }
    }

    private fun applyLoginData(loginData: LoginData?) {
        if (loginData != null) {
            dataBinding.userNameTv.text = loginData.screenName
            dataBinding.tipsLoginBt.isVisible = false
            viewModel.getUserRelationInfo()
        } else {
            dataBinding.userNameTv.text = "登录账号"
            dataBinding.tipsLoginBt.isVisible = true
            dataBinding.followAnimeTv.text = resources.getText(R.string.text_default_count)
            dataBinding.followAnimeTv.setTextColorRes(R.color.text_black)
            dataBinding.cloudHistoryTv.text = resources.getText(R.string.text_default_count)
        }
    }

    private fun getDefaultCoverResId(): Int {
        val coverArray = resources.getIntArray(R.array.cover)
        var coverIndex = UserConfig.getUserCoverIndex()
        if (coverIndex == -1) {
            coverIndex = coverArray.indices.random()
            UserConfig.putUserCoverIndex(coverIndex)
        }
        val typedArray = resources.obtainTypedArray(R.array.cover)
        val coverResId = typedArray.getResourceId(coverIndex, 0)
        typedArray.recycle()
        return coverResId
    }

    private fun initClick() {

        dataBinding.userCoverIv.setOnClickListener {
            UserCoverDialog(requireActivity()) {
                val typedArray = resources.obtainTypedArray(R.array.cover)
                val coverResId = typedArray.getResourceId(it, 0)
                typedArray.recycle()
                UserConfig.putUserCoverIndex(it)
                dataBinding.userCoverIv.setImageResource(coverResId)
            }.show()
        }

        dataBinding.userAccountCl.setOnClickListener {
            if (!checkLoggedIn())
                return@setOnClickListener

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                mAttachActivity, dataBinding.userCoverIv, dataBinding.userCoverIv.transitionName
            )

            ARouter.getInstance()
                .build(RouteTable.User.UserInfo)
                .withOptionsCompat(options)
                .navigation(mAttachActivity)
        }

        dataBinding.followAnimeLl.setOnClickListener {
            if (!checkLoggedIn())
                return@setOnClickListener

            ARouter.getInstance()
                .build(RouteTable.Anime.AnimeFollow)
                .withParcelable("followData", viewModel.followData)
                .navigation()
        }

        dataBinding.cloudHistoryLl.setOnClickListener {
            if (!checkLoggedIn())
                return@setOnClickListener

            ARouter.getInstance()
                .build(RouteTable.Anime.AnimeHistory)
                .withParcelable("historyData", viewModel.historyData)
                .navigation()
        }

        dataBinding.playerSettingLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.User.SettingPlayer)
                .navigation()
        }

        dataBinding.scanManagerLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.User.ScanManager)
                .navigation()
        }

        dataBinding.cacheManagerLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.User.CacheManager)
                .navigation()
        }

        dataBinding.commonlyManagerLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.User.CommonManager)
                .navigation()
        }

        dataBinding.bilibiliDanmuLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.Local.BiliBiliDanmu)
                .navigation()
        }

        dataBinding.shooterSubtitleLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.Local.ShooterSubtitle)
                .navigation()
        }

        dataBinding.feedbackLl.setOnClickListener {
            FeedbackAPI.openFeedbackActivity()
        }

        dataBinding.appSettingLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.User.SettingApp)
                .navigation()
        }
    }

    /**
     * 检查是否已登录
     */
    private fun checkLoggedIn(): Boolean {
        if (!UserConfig.isUserLoggedIn()) {
            ARouter.getInstance()
                .build(RouteTable.User.UserLogin)
                .navigation()
            return false
        }
        return true
    }
}