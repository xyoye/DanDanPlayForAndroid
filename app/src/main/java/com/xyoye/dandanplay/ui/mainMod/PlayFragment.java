package com.xyoye.dandanplay.ui.mainMod;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.PixelUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.event.OpenFolderEvent;
import com.xyoye.dandanplay.mvp.impl.PlayFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.ui.folderMod.FolderActivity;
import com.xyoye.dandanplay.utils.permissionchecker.PermissionHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

/**
 * Created by YE on 2018/6/29 0029.
 */

public class PlayFragment extends BaseFragment<PlayFragmentPresenter> implements PlayFragmentView{
    @BindView(R.id.ptr_pull_rv_to_refresh)
    PtrFrameLayout refresh;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;
    private BaseRvAdapter<FolderBean> adapter;

    public static PlayFragment newInstance(){
        return new PlayFragment();
    }

    @NonNull
    @Override
    protected PlayFragmentPresenter initPresenter() {
        return new PlayFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_play;
    }

    @Override
    public void initView() {
        showLoading();
        new PermissionHelper().with(this).request(new PermissionHelper.OnSuccessListener() {
            @Override
            public void onPermissionSuccess() {
                presenter.getVideoList();
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        StoreHouseHeader header = new StoreHouseHeader(getContext());
        header.setPadding(0, PixelUtil.dip2px(this.getContext(), 20) , 0, PixelUtil.dip2px(this.getContext(), 20));
        header.initWithString("dan dan player");
        header.setTextColor(this.getResources().getColor(R.color.theme_color));
        refresh.disableWhenHorizontalMove(true);
        refresh.setDurationToCloseHeader(1500);
        refresh.setHeaderView(header);
        refresh.addPtrUIHandler(header);

        layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initListener() {
        refresh.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return !frame.isRefreshing() && (layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                new PermissionHelper().with(PlayFragment.this).request(new PermissionHelper.OnSuccessListener() {
                    @Override
                    public void onPermissionSuccess() {
                        presenter.getVideoList();
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            }
        });
    }

    @Override
    public void refreshAdapter(List<FolderBean> beans) {
        if (adapter == null){
            adapter = new BaseRvAdapter<FolderBean>(beans) {
                @NonNull
                @Override
                public AdapterItem<FolderBean> onCreateItem(int viewType) {
                    return new FolderItem();
                }
            };
            recyclerView.setAdapter(adapter);
        }else {
            adapter.setData(beans);
            adapter.notifyDataSetChanged();
        }
        hideLoading();
        refresh.refreshComplete();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openFolder(OpenFolderEvent event){
        Intent intent = new Intent(getContext(), FolderActivity.class);
        intent.putExtra(OpenFolderEvent.FOLDERPATH, event.getFolderPath());
        intent.putExtra(OpenFolderEvent.FOLDERTITLE, event.getFolderTitle());
        startActivity(intent);
    }
}
