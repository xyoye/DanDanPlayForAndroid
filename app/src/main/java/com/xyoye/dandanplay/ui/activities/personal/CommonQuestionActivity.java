package com.xyoye.dandanplay.ui.activities.personal;

import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.bean.FeedbackBean;
import com.xyoye.dandanplay.bean.QuestionEntity;
import com.xyoye.dandanplay.ui.weight.expandable.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2020/7/20.
 */

public class CommonQuestionActivity extends BaseMvcActivity {
    @BindView(R.id.question_rv)
    RecyclerView questionRv;

    private List<QuestionEntity> questionList;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_common_question;
    }

    @Override
    public void initPageView() {
        setTitle("常见问题");

        questionList = buildQuestion();

        QuestionAdapter questionAdapter = new QuestionAdapter(questionList);
        questionAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            for (int i = 0; i < questionList.size(); i++) {
                if (questionList.get(i).getItemType() == QuestionEntity.ITEM_HEADER)
                    continue;
                if (i != position) {
                    questionList.get(i).setOpen(false);
                } else {
                    boolean isOpen = questionList.get(i).isOpen();
                    questionList.get(i).setOpen(!isOpen);
                }
            }
            adapter.notifyDataSetChanged();
        });
        questionRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        questionRv.setAdapter(questionAdapter);
    }

    @Override
    public void initPageViewListener() {

    }

    private List<QuestionEntity> buildQuestion() {
        List<QuestionEntity> questionList = new ArrayList<>();

        questionList.add(new QuestionEntity(QuestionEntity.ITEM_HEADER, "视频播放"));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("1、在线视频播放失败", "1.切换播放资源，由于视频资源并非弹弹所有，所以无法保证视频质量，一般来说较新的资源能播放的机率较大。\n2.切换网络，移动网络与WIFI间相互切换\n3.复制磁链到PC端，使用边下边播\n\n注：墙外可能会无法播放")));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("2、本地视频播放失败", "尝试在播放器设置中切换播放器内核，一般选择ijkplayer内核或exoplayer内核。\n\n如果还是不能播放请在确保资源有效的情况下，保留视频资源，并联系开发人员，开发人员可能需要以此视频资源进行测试改进")));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("3、视频播放卡顿", "尝试在播放器设置中开启硬解码或切换像素格式类型，一般选择Yv12或OpenGL ES2。\n\n如果播放依然卡顿请保留视频资源，并联系开发人员，开发人员可能需要以此视频资源进行测试")));

        questionList.add(new QuestionEntity(QuestionEntity.ITEM_HEADER, "资源下载"));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("1、下载速度慢", "1.尝试切换其它下载资源。\n2.个人中心->下载管理->tracker中，增加tracker，具体作用见该页面右上角说明。\n3.尝试复制磁链到PC端下载，或者选择其它BT下载工具下载\n\n下载资源并不属于弹弹，弹弹play 概念版仅提供下载手段，并不保证资源的完整性和有效性。")));

        questionList.add(new QuestionEntity(QuestionEntity.ITEM_HEADER, "文件扫描"));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("1、扫描不到视频", "进入个人中心->文件扫描管理页面\n\n1.点击添加，将选中目录加入扫描目录，后续下拉刷新会重新扫描该文件夹。\n2.点击扫描文件夹\\文件，扫描选中文件夹，后续该文件夹新增、删除视频文件，不会再扫描该文件夹。\n\n视频扫描为了保证体验流畅，采取的视频收集方式是获取系统内部的视频，所以某些视频可能不能及时扫描或无法扫描。")));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("2、隐藏不必要的目录", "进入个人中心->文件扫描管理页面\n\n1.点击屏幕目录，点击添加，将选中目录加入屏蔽目录，后续将不再显示该目录及其子目录下视频内容。\n2.在扫描目录将系统视频删除，添加需要扫描的目录，后续app将只扫描已添加扫描的目录")));

        questionList.add(new QuestionEntity(QuestionEntity.ITEM_HEADER, "弹幕、字幕匹配及绑定"));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("1、本地视频弹(字)幕绑定", "1.在本地视频列表界面，左滑点击绑定弹幕\\字幕，APP将自动匹配视频相关弹幕\\字幕，若匹配失败，可点击搜索，按文件名搜索弹幕\\字幕。\n2.在播放器界面，点击右上角图标“弹(A)”，在弹窗中点击换源，在弹幕\\字幕弹窗选择需要的弹幕\\字幕。\n\n绑定弹幕\\字幕后，可右滑移除绑定的弹幕\\字幕")));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("2、局域网视频弹(字)幕选择", "选择局域网视频，进入播放界面\n\n1.点击右上角图标“弹(A)”，在弹窗中点击换源，在弹幕\\字幕弹窗点击局域网弹幕\\字幕，选择需要的弹幕\\字幕。\n\n此选择只在当前播放生效")));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("3、外部视频弹幕匹配及搜索", "通过第三方APP播放视频，选择弹弹play作为播放器，播放前可在弹窗中选择是否绑定弹幕播放\n\n弹窗可在“个人中心->播放器设置->其它设置->外链弹窗”中打开和关闭。")));

        questionList.add(new QuestionEntity(QuestionEntity.ITEM_HEADER, "其它"));
        questionList.add(new QuestionEntity(QuestionEntity.ITEM_CONTENT,
                new FeedbackBean("APP开源及免费声明", "弹弹play 概念版（以下简称本APP）目前是一个免费、开源的项目，本APP不存在任何收费方式，不存在任何第三方广告，仅在酷安应用市场及QQ群发布，项目代码已开源至Github， 详见个人中心->系统设置->关于我们。\n\n目前有发现一些人，将本APP发布至闲鱼等平台售卖，或将项目代码重新编译修改后发布，请各位用户注意区分正版，以防造成损失。")));
        return questionList;
    }

    public static class QuestionAdapter extends BaseMultiItemQuickAdapter<QuestionEntity, BaseViewHolder> {

        private QuestionAdapter(List<QuestionEntity> data) {
            super(data);
            addItemType(QuestionEntity.ITEM_HEADER, R.layout.item_question_header);
            addItemType(QuestionEntity.ITEM_CONTENT, R.layout.item_feedback);
        }

        @Override
        protected void convert(BaseViewHolder helper, QuestionEntity item) {
            switch (helper.getItemViewType()) {
                case QuestionEntity.ITEM_HEADER:
                    helper.setText(R.id.header_tv, (String) item.getObject());
                    break;
                case QuestionEntity.ITEM_CONTENT:
                    FeedbackBean question = (FeedbackBean) item.getObject();
                    helper.setText(R.id.expandable_layout_header_tv, question.getHeader())
                            .setText(R.id.expandable_layout_content_tv, question.getContent())
                            .addOnClickListener(R.id.item_layout);

                    ExpandableLayout expandableLayout = helper.getView(R.id.expandable_layout);
                    ImageView tipsIv = helper.getView(R.id.expandable_layout_header_iv);

                    expandableLayout.setOnExpansionUpdateListener((expansionFraction, state) ->
                            tipsIv.setRotation(expansionFraction * 90));

                    helper.itemView.setOnClickListener(v -> expandableLayout.toggle());
                    break;
            }
        }
    }
}
