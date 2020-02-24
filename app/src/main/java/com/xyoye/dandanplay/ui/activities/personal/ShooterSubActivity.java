package com.xyoye.dandanplay.ui.activities.personal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.bean.ShooterSubtitleBean;
import com.xyoye.dandanplay.mvp.impl.ShooterSubPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.ShooterSubPresenter;
import com.xyoye.dandanplay.mvp.view.ShooterSubView;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;
import com.xyoye.dandanplay.ui.weight.dialog.ShooterSubDetailDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2020/2/23.
 */

public class ShooterSubActivity extends BaseMvpActivity<ShooterSubPresenter> implements ShooterSubView {

    @BindView(R.id.api_key_status_tv)
    TextView apiKeyStatusTv;
    @BindView(R.id.quota_tv)
    TextView quotaTv;
    @BindView(R.id.search_subtitle_et)
    EditText searchSubtitleEt;
    @BindView(R.id.subtitle_rv)
    RecyclerView subtitleRv;

    private boolean isApiSecretExist = false;
    private List<ShooterSubtitleBean.SubBean.SubsBean> mSubtitleList;
    private ShooterSubtitleAdapter subtitleAdapter;

    private ShooterSubDetailDialog shooterSubDetailDialog;

    private int mPage = 0;

    @NonNull
    @Override
    protected ShooterSubPresenter initPresenter() {
        return new ShooterSubPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_shooter_sub;
    }

    @Override
    public void initView() {
        setTitle("射手（伪）字幕下载");

        mSubtitleList = new ArrayList<>();

        subtitleAdapter = new ShooterSubtitleAdapter(R.layout.item_shooter_subtitle, mSubtitleList);
        subtitleAdapter.setOnItemChildClickListener((adapter, view, position) ->
                presenter.querySubtitleDetail(mSubtitleList.get(position).getId()));

        subtitleAdapter.setEnableLoadMore(true);
        subtitleAdapter.setOnLoadMoreListener(() -> {
            mPage += 1;
            if (isApiSecretExist) {
                String text = searchSubtitleEt.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    presenter.searchSubtitle(text, mPage);
                } else {
                    ToastUtils.showShort("搜索内容不能为空");
                }
            } else {
                ToastUtils.showShort("请先设置API密钥");
            }
        }, subtitleRv);

        subtitleRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        subtitleRv.setAdapter(subtitleAdapter);

        updateApiSecretStatus();
    }

    @Override
    public void initListener() {

    }

    @OnClick({R.id.update_api_key_tv, R.id.update_quota_tv, R.id.search_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.update_api_key_tv:
                new CommonEditTextDialog(this, CommonEditTextDialog.SAVE_SHOOTER_API_SECRET, data -> {
                    AppConfig.getInstance().setShooterApiSecret(data[0]);
                    updateApiSecretStatus();
                }).show();
                break;
            case R.id.update_quota_tv:
                if (isApiSecretExist) {
                    presenter.updateQuota();
                } else {
                    ToastUtils.showShort("请先设置API密钥");
                }
                break;
            case R.id.search_tv:
                KeyboardUtils.hideSoftInput(searchSubtitleEt);
                if (isApiSecretExist) {
                    String text = searchSubtitleEt.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        mPage = 0;
                        presenter.searchSubtitle(text, mPage);
                    } else {
                        ToastUtils.showShort("搜索内容不能为空");
                    }
                } else {
                    ToastUtils.showShort("请先设置API密钥");
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about_download) {
            new CommonDialog.Builder(ShooterSubActivity.this)
                    .setAutoDismiss()
                    .setCancelListener(dialog -> {
                        String link = "https://secure.assrt.net/user/logon.xml";
                        ClipboardManager clipboardManagerMagnet = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipDataMagnet = ClipData.newPlainText("Label", link);
                        if (clipboardManagerMagnet != null) {
                            clipboardManagerMagnet.setPrimaryClip(mClipDataMagnet);
                            ToastUtils.showShort("登录链接已复制");
                        }
                    })
                    .build()
                    .show(getResources().getString(R.string.about_download_subtitle), "关于API", "确定", "复制登录链接");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_subtitle, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void updateApiSecretStatus() {
        String apiSecret = AppConfig.getInstance().getShooterApiSecret();
        if (TextUtils.isEmpty(apiSecret)) {
            apiKeyStatusTv.setText("未设置");
            apiKeyStatusTv.setTextColor(CommonUtils.getResColor(R.color.text_red));
            isApiSecretExist = false;
        } else {
            apiKeyStatusTv.setText("已设置");
            apiKeyStatusTv.setTextColor(CommonUtils.getResColor(R.color.text_theme));
            isApiSecretExist = true;
            presenter.updateQuota();
        }
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @Override
    public void updateQuota(int quota) {
        quotaTv.setText(String.valueOf(quota));
    }

    @Override
    public void updateSubtitleList(List<ShooterSubtitleBean.SubBean.SubsBean> subtitleList, boolean enableLoadMore) {
        subtitleAdapter.loadMoreComplete();
        subtitleAdapter.setEnableLoadMore(enableLoadMore);

        if (mPage == 0) {
            mSubtitleList.clear();
        }

        mSubtitleList.addAll(subtitleList);
        subtitleAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateSubtitleFailed() {
        if (subtitleAdapter.isLoadMoreEnable() && subtitleAdapter.isLoading()) {
            subtitleAdapter.loadMoreFail();
        }
    }

    @Override
    public void showSubtitleDetailDialog(ShooterSubDetailBean.SubBean.SubsBean detailBean) {
        shooterSubDetailDialog = new ShooterSubDetailDialog(this, detailBean, (fileName, link) ->
                        presenter.downloadSubtitleFile(fileName, link));
        shooterSubDetailDialog.show();
    }

    @Override
    public void subtitleDownloadSuccess() {
        if (shooterSubDetailDialog != null){
            shooterSubDetailDialog.dismiss();
        }
    }

    private class ShooterSubtitleAdapter extends BaseQuickAdapter<ShooterSubtitleBean.SubBean.SubsBean, BaseViewHolder> {

        private ShooterSubtitleAdapter(@LayoutRes int layoutResId, @Nullable List<ShooterSubtitleBean.SubBean.SubsBean> data) {
            super(layoutResId, data);
        }

        @Override
        public void convert(BaseViewHolder helper, ShooterSubtitleBean.SubBean.SubsBean model) {
            String uploadTime = model.getUpload_time();
            if (uploadTime.contains(" ")) {
                uploadTime = uploadTime.split(" ")[0];
            }

            String name = TextUtils.isEmpty(model.getNative_name()) ? model.getVideoname() : model.getNative_name();
            String format = "格式: " + model.getSubtype();
            String language = "语言: " + (model.getLang() == null ? "无" : model.getLang().getDesc());
            uploadTime = "上传时间: " + uploadTime;

            helper.setText(R.id.position_tv, String.valueOf(helper.getAdapterPosition() + 1))
                    .setText(R.id.subtitle_name_tv, name)
                    .setText(R.id.subtitle_format_tv, format)
                    .setText(R.id.subtitle_language_tv, language)
                    .setText(R.id.subtitle_time_tv, uploadTime)
                    .addOnClickListener(R.id.item_view);
        }
    }

}
