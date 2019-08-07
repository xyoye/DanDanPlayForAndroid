package com.xyoye.dandanplay.ui.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.FeedbackBean;
import com.xyoye.dandanplay.ui.weight.item.FeedbackItem;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/8/7.
 */

public class FeedbackActivity extends BaseMvcActivity {

    @BindView(R.id.common_feedback_rv)
    RecyclerView commonFeedbackRv;
    @BindView(R.id.qq_tv)
    TextView qqTv;
    @BindView(R.id.qq_group_tv)
    TextView qqGroupTv;

    @Override
    protected int initPageLayoutID() {
        return R.layout.acitivty_feedback;
    }

    @Override
    public void initPageView() {
        setTitle("意见反馈");

        List<FeedbackBean> commonFeedbackList = new ArrayList<>();
        commonFeedbackList.add(new FeedbackBean("1、视频播放失败", "尝试在播放器设置中切换播放器内核，一般选择ijkplayer内核或exoplayer内核。\n\n如果还是不能播放请在确保资源有效的情况下，保留视频资源，并联系开发人员，开发人员可能需要以此视频资源进行测试改进"));
        commonFeedbackList.add(new FeedbackBean("2、视频播放卡顿", "尝试在播放器设置中开启硬解码或切换像素格式类型，一般选择Yv12或OpenGL ES2。\n\n如果播放依然卡顿请保留视频资源，并联系开发人员，开发人员可能需要以此视频资源进行测试"));
        commonFeedbackList.add(new FeedbackBean("3、下载速度慢", "尝试切换其它下载资源，或在tracker设置中增加tracker。\n\n下载资源并不属于弹弹，弹弹play 概念版仅提供下载手段，并不保证资源的完整性和有效性。"));
        commonFeedbackList.add(new FeedbackBean("4、扫描不到视频", "尝试在扫描设置中单独添加该视频，或将该视频文件夹加入扫描目录列表。\n\n视频扫描为了保证体验流畅，采取的视频收集方式是获取系统内部的视频，所以某些视频可能不能及时扫描或无法扫描。"));

        BaseRvAdapter<FeedbackBean> feedbackAdapter = new BaseRvAdapter<FeedbackBean>(commonFeedbackList) {
            @NonNull
            @Override
            public AdapterItem<FeedbackBean> onCreateItem(int viewType) {
                return new FeedbackItem(position -> {
                    for (int i = 0; i < commonFeedbackList.size(); i++) {
                        if (i != position) {
                            commonFeedbackList.get(i).setOpen(false);
                        } else {
                            boolean isOpen = commonFeedbackList.get(i).isOpen();
                            commonFeedbackList.get(i).setOpen(!isOpen);
                        }
                    }
                    this.notifyDataSetChanged();
                });
            }
        };
        commonFeedbackRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        commonFeedbackRv.setAdapter(feedbackAdapter);
    }

    @Override
    public void initPageViewListener() {

    }

    @OnClick({R.id.feedback_by_qq_rl, R.id.feedback_by_qq_group_rl, R.id.feedback_more_rl})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.feedback_by_qq_rl:
                String qq = qqTv.getText().toString();
                ClipboardManager qqCM = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData qqCD = ClipData.newPlainText("Label", qq);
                if (qqCM != null) {
                    qqCM.setPrimaryClip(qqCD);
                    ToastUtils.showShort("已复制QQ号：" + qq);
                }
                break;
            case R.id.feedback_by_qq_group_rl:
                String qqGroup = qqGroupTv.getText().toString();
                ClipboardManager qqGCM = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData qqGCD = ClipData.newPlainText("Label", qqGroup);
                if (qqGCM != null) {
                    qqGCM.setPrimaryClip(qqGCD);
                    ToastUtils.showShort("已复制QQ群号：" + qqGroup);
                }
                break;
            case R.id.feedback_more_rl:
                showDialog();
                break;
        }
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择反馈方式");
        final String[] ways = {"邮件", "Github Issue"};
        builder.setItems(ways, (dialog, which) -> {
            if (ways[which].equals("邮件")) {
                builder.show();
                String android_version = "Android "+android.os.Build.VERSION.RELEASE;
                String phone_version = android.os.Build.MODEL;
                String version = CommonUtils.getLocalVersion(this);
                String app_version = getResources().getString(R.string.app_name)+" 版本"+version;

                Intent mail_intent = new Intent(Intent.ACTION_SEND);
                mail_intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"xyoye1997@outlook.com"});
                mail_intent.putExtra(Intent.EXTRA_SUBJECT, "弹弹play - 反馈");
                mail_intent.putExtra(Intent.EXTRA_TEXT, phone_version+"\n"+android_version+"\n\n"+app_version);
                mail_intent.setType("text/plain");
                startActivity(Intent.createChooser(mail_intent, "选择邮件客户端"));
            }
            else if (ways[which].equals("Github Issue")){
                Uri uri = Uri.parse("https://github.com/xyoye/DanDanPlayForAndroid/issues");
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent1);
            }
        });
        builder.show();
    }
}
