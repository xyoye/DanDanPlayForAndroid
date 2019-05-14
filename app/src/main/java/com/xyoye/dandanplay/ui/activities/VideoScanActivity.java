package com.xyoye.dandanplay.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.RefreshFolderEvent;
import com.xyoye.dandanplay.mvp.impl.VideoScanPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.VideoScanPresenter;
import com.xyoye.dandanplay.mvp.view.VideoScanView;
import com.xyoye.dandanplay.ui.fragment.VideoScanFragment;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.indicator.LinePagerIndicator;
import com.xyoye.dandanplay.ui.weight.indicator.MagicIndicator;
import com.xyoye.dandanplay.ui.weight.indicator.abs.CommonNavigatorAdapter;
import com.xyoye.dandanplay.ui.weight.indicator.abs.IPagerIndicator;
import com.xyoye.dandanplay.ui.weight.indicator.abs.IPagerTitleView;
import com.xyoye.dandanplay.ui.weight.indicator.navigator.CommonNavigator;
import com.xyoye.dandanplay.ui.weight.indicator.title.ColorTransitionPagerTitleView;
import com.xyoye.dandanplay.ui.weight.indicator.title.SimplePagerTitleView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class VideoScanActivity extends BaseMvpActivity<VideoScanPresenter> implements VideoScanView {

    @BindView(R.id.indicator)
    MagicIndicator magicIndicator;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.delete_tv)
    TextView deleteTv;

    private List<VideoScanFragment> fragmentList;
    private int selectedPosition = 0;

    @Override
    public void initView() {
        setTitle("扫描管理");
        fragmentList = new ArrayList<>();
        VideoScanFragment scanFragment = VideoScanFragment.newInstance(true);
        VideoScanFragment blockFragment = VideoScanFragment.newInstance(false);
        fragmentList.add(scanFragment);
        fragmentList.add(blockFragment);

        VideoScanActivity.OnFragmentItemCheckListener itemCheckListener = hasChecked -> {
            if (hasChecked){
                deleteTv.setTextColor(VideoScanActivity.this.getResources().getColor(R.color.theme_color));
                deleteTv.setClickable(true);
            }else{
                deleteTv.setTextColor(VideoScanActivity.this.getResources().getColor(R.color.text_gray));
                deleteTv.setClickable(false);
            }
        };

        scanFragment.setOnItemCheckListener(itemCheckListener);
        blockFragment.setOnItemCheckListener(itemCheckListener);

        initIndicator();

        initViewPager();
    }

    @Override
    public void initListener() {

    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_video_scan;
    }

    @NonNull
    @Override
    protected VideoScanPresenter initPresenter() {
        return new VideoScanPresenterImpl(this, this);
    }

    private void initIndicator() {
        List<String> titleList = new ArrayList<>();
        titleList.add("扫描目录");
        titleList.add("屏蔽目录");
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return titleList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setText(titleList.get(index));
                simplePagerTitleView.setNormalColor(ContextCompat.getColor(context, R.color.text_black));
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(context, R.color.theme_color));
                simplePagerTitleView.setOnClickListener(v -> viewPager.setCurrentItem(index));
                return simplePagerTitleView;
            }

            @SuppressLint("ResourceType")
            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setColors(ContextCompat.getColor(context, R.color.theme_color));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        magicIndicator.onPageSelected(selectedPosition);
    }

    private void initViewPager() {
        VideoScanFragmentAdapter fragmentAdapter = new VideoScanFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setCurrentItem(selectedPosition);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                magicIndicator.onPageSelected(position);
                selectedPosition = position;
                resetButtonStatus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
    }

    @OnClick({R.id.scan_folder_tv, R.id.scan_file_tv, R.id.delete_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_folder_tv:
                new FileManagerDialog(VideoScanActivity.this, FileManagerDialog.SELECT_FOLDER, path -> presenter.listFolder(path)).show();
                break;
            case R.id.scan_file_tv:
                new FileManagerDialog(VideoScanActivity.this, FileManagerDialog.SELECT_VIDEO, path -> {
                    VideoBean videoBean = new VideoBean();
                    presenter.queryFormSystem(videoBean, path);
                    boolean added = presenter.saveNewVideo(videoBean);
                    if (added)
                        EventBus.getDefault().post(new RefreshFolderEvent(true));
                    ToastUtils.showShort(added ? "扫描成功" : "文件已存在");
                }).show();
                break;
            case R.id.delete_tv:
                fragmentList.get(selectedPosition).deleteChecked();
                break;
        }
    }

    private void resetButtonStatus(){
        VideoScanFragment videoScanFragment = fragmentList.get(selectedPosition);
        if (videoScanFragment.hasChecked()){
            deleteTv.setTextColor(VideoScanActivity.this.getResources().getColor(R.color.theme_color));
            deleteTv.setClickable(true);
        }else {
            deleteTv.setTextColor(VideoScanActivity.this.getResources().getColor(R.color.text_gray));
            deleteTv.setClickable(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.add_scan:
                new FileManagerDialog(VideoScanActivity.this, FileManagerDialog.SELECT_FOLDER, path ->
                        fragmentList.get(selectedPosition).addPath(path)).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public Context getContext() {
        return this;
    }

    public interface OnFragmentItemCheckListener{
        void onChecked( boolean hasChecked);
    }

    private class VideoScanFragmentAdapter extends FragmentPagerAdapter {
        private List<VideoScanFragment> list;

        private VideoScanFragmentAdapter(FragmentManager supportFragmentManager, List<VideoScanFragment> list) {
            super(supportFragmentManager);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
