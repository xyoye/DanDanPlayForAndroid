package com.xyoye.user_component.ui.activities.feedback

import android.content.Intent
import android.os.Build
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.addToClipboard
import com.xyoye.common_component.extension.startUrlActivity
import com.xyoye.common_component.extension.toResString
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.AppUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityFeedbackBinding
import com.xyoye.user_component.databinding.ItemCommonQuestionBinding

@Route(path = RouteTable.User.Feedback)
class FeedbackActivity : BaseActivity<FeedbackViewModel, ActivityFeedbackBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            FeedbackViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_feedback

    override fun initView() {

        title = "意见反馈"

        initRv()

        dataBinding.copyQqTv.setOnClickListener {
            R.string.text_qq.toResString().addToClipboard()
            ToastCenter.showSuccess("QQ已复制！")
        }

        dataBinding.sendMainTv.setOnClickListener {
            val email = R.string.text_email.toResString()
            val androidVersion = "Android ${Build.VERSION.RELEASE}"
            val phoneVersion = Build.MODEL
            val appVersion = AppUtils.getVersionName()

            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "弹弹play - 反馈")
                putExtra(Intent.EXTRA_TEXT, "$phoneVersion\n$androidVersion\n\n$appVersion")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(intent, "选择邮件客户端"))
        }

        dataBinding.editIssuesTv.setOnClickListener {
            startUrlActivity("https://github.com/xyoye/DanDanPlayForAndroid/issues")
        }
    }

    private fun initRv() {

        val question = mutableListOf(
            Pair(
                "1、视频资源播放失败",
                "1.切换播放资源，由于视频资源并非弹弹所有，所以无法保证视频质量，一般来说较新的资源能播放的机率较大。\n2.切换网络，移动网络与WIFI间相互切换\n注：墙外可能会无法播放"
            ),
            Pair(
                "2、本地视频播放失败",
                "尝试在播放器设置中切换播放器内核，一般选择ijkplayer内核或exoplayer内核。\n\n如果还是不能播放请在确保资源有效的情况下，保留视频资源，并联系开发人员，开发人员可能需要以此视频资源进行测试改进"
            ),
            Pair(
                "3、视频播放卡顿",
                "尝试在播放器设置中开启硬解码或切换像素格式类型，一般选择Yv12或OpenGL ES2。\n\n如果播放依然卡顿请保留视频资源，并联系开发人员，开发人员可能需要以此视频资源进行测试"
            ),
            Pair(
                "4、扫描不到视频",
                "尝试在扫描设置中单独添加该视频，或将该视频文件夹加入扫描目录列表。\n\n视频扫描为了保证体验流畅，采取的视频收集方式是获取系统内部的视频，所以某些视频可能不能及时扫描或无法扫描"
            )
        )

        dataBinding.feedbackRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<Pair<String, String>> {

                initData(question)

                addItem<Pair<String, String>, ItemCommonQuestionBinding>(R.layout.item_common_question) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            answerTitleTv.text = data.first
                            answerTv.text = data.second
                            expandableLayout.setExpansionObserver { expansionFraction, _ ->
                                arrowIv.rotation = expansionFraction * 90
                            }
                            itemLayout.setOnClickListener { expandableLayout.toggle() }
                        }
                    }
                }
            }
        }

    }
}