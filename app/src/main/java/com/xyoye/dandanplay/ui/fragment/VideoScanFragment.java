package com.xyoye.dandanplay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.ScanFolderBean;
import com.xyoye.dandanplay.mvp.impl.VideoScanFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.VideoScanFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.VideoScanFragmentView;
import com.xyoye.dandanplay.ui.activities.VideoScanActivity;
import com.xyoye.dandanplay.ui.weight.item.VideoScanItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/5/14.
 */

public class VideoScanFragment extends BaseFragment<VideoScanFragmentPresenter> implements VideoScanFragmentView {

    @BindView(R.id.folder_rv)
    RecyclerView folderRv;

    private boolean isScanType;

    private BaseRvAdapter<ScanFolderBean> adapter;
    private List<ScanFolderBean> folderList;
    private VideoScanActivity.OnFragmentItemCheckListener itemCheckListener;

    public static VideoScanFragment newInstance(boolean isScanType){
        VideoScanFragment videoScanFragment = new VideoScanFragment();
        Bundle args = new Bundle();
        args.putSerializable("is_scan_type", isScanType);
        videoScanFragment.setArguments(args);
        return videoScanFragment;
    }

    @NonNull
    @Override
    protected VideoScanFragmentPresenter initPresenter() {
        return new VideoScanFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_video_scan;
    }

    @Override
    public void initView() {
        Bundle args = getArguments();
        if (args == null) return;
        isScanType = getArguments().getBoolean("is_scan_type");

        folderList = new ArrayList<>();
        adapter = new BaseRvAdapter<ScanFolderBean>(folderList) {
            @NonNull
            @Override
            public AdapterItem<ScanFolderBean> onCreateItem(int viewType) {
                return new VideoScanItem((isCheck, position) -> {
                    folderList.get(position).setCheck(isCheck);
                    if (isCheck){
                        itemCheckListener.onChecked(true);
                    }else {
                        for (ScanFolderBean bean : folderList){
                            if (bean.isCheck()){
                                itemCheckListener.onChecked(true);
                                return;
                            }
                        }
                        itemCheckListener.onChecked(false);
                    }
                });
            }
        };
        folderRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        folderRv.setNestedScrollingEnabled(false);
        folderRv.setItemViewCacheSize(10);
        folderRv.setAdapter(adapter);

        presenter.queryScanFolderList(isScanType);
    }

    @Override
    public void initListener() {

    }

    public void addPath(String path){
        for (ScanFolderBean bean : folderList){
            if (path.contains(bean.getFolder())){
                ToastUtils.showShort("已在扫描范围内");
                return;
            }
        }
        presenter.addScanFolder(path, isScanType);
    }

    public void deleteChecked(){
        for (ScanFolderBean bean : folderList) {
            if (bean.isCheck()) {
                presenter.deleteScanFolder(bean.getFolder(), isScanType);
            }
        }
        presenter.queryScanFolderList(isScanType);
    }

    public boolean hasChecked(){
        for (ScanFolderBean bean : folderList) {
            if (bean.isCheck()) {
                return true;
            }
        }
        return false;
    }

    public void setOnItemCheckListener(VideoScanActivity.OnFragmentItemCheckListener itemCheckListener){
        this.itemCheckListener = itemCheckListener;
    }

    @Override
    public void updateFolderList(List<ScanFolderBean> folderList) {
        this.folderList.clear();
        this.folderList.addAll(folderList);
        adapter.notifyDataSetChanged();
    }
}
