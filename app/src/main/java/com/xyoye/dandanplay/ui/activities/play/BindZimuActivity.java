package com.xyoye.dandanplay.ui.activities.play;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.BindResourceBean;
import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.bean.ShooterSubtitleBean;
import com.xyoye.dandanplay.bean.params.BindResourceParam;
import com.xyoye.dandanplay.mvp.impl.BindZimuPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.BindZimuPresenter;
import com.xyoye.dandanplay.mvp.view.BindZimuView;
import com.xyoye.dandanplay.ui.activities.personal.ShooterSubActivity;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.ShooterSubDetailDialog;
import com.xyoye.dandanplay.ui.weight.item.SubtitleItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.player.commom.bean.SubtitleBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/4.
 * <p>
 * 网络弹幕绑定界面
 */

public class BindZimuActivity extends BaseMvpActivity<BindZimuPresenter> implements BindZimuView {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.current_resource_path_ll)
    LinearLayout currentResourcePathLl;
    @BindView(R.id.current_resource_tips_tv)
    TextView currentResourceTipsTv;
    @BindView(R.id.current_resource_path_tv)
    TextView currentResourcePathTv;

    private BaseRvAdapter<SubtitleBean> shooterAdapter;
    private ShooterSubtitleAdapter subtitleAdapter;
    private BindResourceParam bindResourceParam;

    private int mPage = 0;
    private String searchText;
    private List<ShooterSubtitleBean.SubBean.SubsBean> mSubtitleList;

    private ShooterSubDetailDialog shooterSubDetailDialog;

    @Override
    @SuppressLint("CheckResult")
    public void initView() {
        setTitle("选择网络字幕");

        bindResourceParam = getIntent().getParcelableExtra("bind_param");
        String currentZimuPath = bindResourceParam.getCurrentResourcePath();
        if (!TextUtils.isEmpty(currentZimuPath)) {
            currentResourcePathLl.setVisibility(View.VISIBLE);
            currentResourceTipsTv.setText("当前字幕: ");
            currentResourcePathTv.setText(currentZimuPath);
        }

        shooterAdapter = new BaseRvAdapter<SubtitleBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<SubtitleBean> onCreateItem(int viewType) {
                return new SubtitleItem((fileName, link) ->
                        presenter.downloadSubtitleFile(fileName, link)
                );
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new ItemDecorationSpaces(0, 0, 0, 1));
        recyclerView.setAdapter(shooterAdapter);

        mSubtitleList = new ArrayList<>();
        subtitleAdapter = new ShooterSubtitleAdapter(R.layout.item_shooter_subtitle, mSubtitleList);
        subtitleAdapter.setOnItemChildClickListener((adapter, view, position) ->
                presenter.queryZimuDetail(mSubtitleList.get(position).getId()));
        subtitleAdapter.setEnableLoadMore(true);
        subtitleAdapter.setOnLoadMoreListener(() -> {
            mPage += 1;
            presenter.searchZimu(searchText, mPage);
        }, recyclerView);

        if (StringUtils.isEmpty(bindResourceParam.getVideoPath())) {
            ToastUtils.showShort("无匹配字幕");
            return;
        }
        presenter.matchZimu(bindResourceParam.getVideoPath());
    }

    @Override
    public void initListener() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.local_zimu:
                String openPath = null;
                String currentZimuPath = bindResourceParam.getCurrentResourcePath();
                String videoPath = bindResourceParam.getVideoPath();

                if (currentZimuPath == null) {
                    currentZimuPath = "";
                }
                if (videoPath == null) {
                    videoPath = "";
                }

                File zimuFile = new File(currentZimuPath);
                File videoFile = new File(videoPath);
                if (zimuFile.exists()) {
                    openPath = zimuFile.getParentFile().getAbsolutePath();
                } else if (videoFile.exists()) {
                    openPath = videoFile.getParentFile().getAbsolutePath();
                }
                new FileManagerDialog(this, openPath, FileManagerDialog.SELECT_SUBTITLE, this::finishActivity).show();
                break;
            case R.id.search_zimu:
                String apiSecret = AppConfig.getInstance().getShooterApiSecret();
                if (TextUtils.isEmpty(apiSecret)) {
                    new CommonDialog.Builder(this)
                            .setOkListener(dialog -> launchActivity(ShooterSubActivity.class))
                            .setAutoDismiss()
                            .build()
                            .show("密钥为空无法搜索，请到APP设置->射手字幕下载中设置API密钥", "前往设置", "取消");
                } else {
                    new CommonEditTextDialog(this, CommonEditTextDialog.SEARCH_SUBTITLE, data -> {
                        searchText = data[0];
                        mPage = 0;
                        recyclerView.setAdapter(subtitleAdapter);
                        presenter.searchZimu(searchText, 0);
                    }).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_zimu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @NonNull
    @Override
    protected BindZimuPresenter initPresenter() {
        return new BindZimuPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_bind_resource;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    public void finishActivity(String zimuPath) {
        BindResourceBean danmuBean = new BindResourceBean();
        danmuBean.setZimuPath(zimuPath);
        danmuBean.setItemPosition(bindResourceParam.getItemPosition());

        Intent intent = getIntent();
        intent.putExtra("bind_data", danmuBean);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void refreshZimuAdapter(List<SubtitleBean> beans) {
        shooterAdapter.setData(beans);
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
        shooterSubDetailDialog = new ShooterSubDetailDialog(this, detailBean, (fileName, link, unzip) ->
                presenter.downloadSubtitleFile(fileName, link, unzip));
        shooterSubDetailDialog.show();
    }

    @Override
    public void subtitleDownloadSuccess(String resultFilePath) {
        finishActivity(resultFilePath);
    }

    @Override
    public void subtitleDownloadSuccess(String resultFilePath, boolean unzip) {
        if (shooterSubDetailDialog != null) {
            shooterSubDetailDialog.dismiss();
        }
        String showDirPath;
        if (unzip) {
            showDirPath = resultFilePath;
        } else {
            showDirPath = new File(resultFilePath).getParentFile().getAbsolutePath();
        }
        new FileManagerDialog(this, showDirPath, FileManagerDialog.SELECT_SUBTITLE, this::finishActivity).show();
    }

    private static class ShooterSubtitleAdapter extends BaseQuickAdapter<ShooterSubtitleBean.SubBean.SubsBean, BaseViewHolder> {

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
