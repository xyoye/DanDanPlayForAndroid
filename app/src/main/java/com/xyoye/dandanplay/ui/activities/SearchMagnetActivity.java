package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.bean.event.SelectInfoEvent;
import com.xyoye.dandanplay.mvp.impl.SearchMagnetPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SearchMagnetPresenter;
import com.xyoye.dandanplay.mvp.view.SearchMagnetView;
import com.xyoye.dandanplay.ui.weight.dialog.SelectInfoDialog;
import com.xyoye.dandanplay.ui.weight.item.MagnetItem;
import com.xyoye.dandanplay.utils.AppConfigShare;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by YE on 2018/10/13.
 */

public class SearchMagnetActivity extends BaseActivity<SearchMagnetPresenter> implements SearchMagnetView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_magnet_subgroup_tv)
    TextView searchMagnetSubgroupTv;
    @BindView(R.id.search_magnet_type_tv)
    TextView searchMagnetTypeTv;
    @BindView(R.id.search_magnet_et)
    EditText searchMagnetEt;
    @BindView(R.id.search_magnet_bt)
    Button searchMagnetBt;
    @BindView(R.id.magnet_rv)
    RecyclerView magnetRv;

    private BaseRvAdapter<MagnetBean.ResourcesBean> adapter;
    private AnimeTypeBean.TypesBean typesBean;
    private SubGroupBean.SubgroupsBean subgroupsBean;

    private List<AnimeTypeBean.TypesBean> typeList;
    private List<SubGroupBean.SubgroupsBean> subgroupList;

    private String oldSearchTerm;
    private String animeTitle;

    @Override
    public void initView() {
        EventBus.getDefault().register(this);
        setTitle("播放资源列表");

        magnetRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        magnetRv.setNestedScrollingEnabled(false);
        magnetRv.setItemViewCacheSize(10);

        animeTitle = getIntent().getStringExtra("anime_title");
        String animeName = getIntent().getStringExtra("anime");
        String episode = getIntent().getStringExtra("episode_title");
        oldSearchTerm = animeName + " " + episode;
        searchMagnetEt.setText(oldSearchTerm);

        presenter.searchMagnet(oldSearchTerm, -1,-1);
        typeList = presenter.getTypeList();
        subgroupList = presenter.getSubGroupList();
    }

    @Override
    public void initListener() {

    }

    @OnClick({R.id.search_magnet_subgroup_tv, R.id.search_magnet_type_tv, R.id.search_magnet_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.search_magnet_type_tv:
                if (typeList.size() > 0){
                    SelectInfoDialog<AnimeTypeBean.TypesBean> selectTypeDialog = new SelectInfoDialog<>(SearchMagnetActivity.this, R.style.Dialog, SelectInfoEvent.TYPE, typeList);
                    selectTypeDialog.show();
                }
                break;
            case R.id.search_magnet_subgroup_tv:
                if (subgroupList.size() > 0){
                    SelectInfoDialog<SubGroupBean.SubgroupsBean> selectSubgroupDialog = new SelectInfoDialog<>(SearchMagnetActivity.this, R.style.Dialog, SelectInfoEvent.SUBGROUP, subgroupList);
                    selectSubgroupDialog.show();
                }
                break;
            case R.id.search_magnet_bt:
                searchMagnet();
                break;
        }
    }

    @NonNull
    @Override
    protected SearchMagnetPresenter initPresenter() {
        return new SearchMagnetPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_search_magnet;
    }

    @Override
    public void refreshAdapter(List<MagnetBean.ResourcesBean> beanList) {
        if (adapter == null){
            adapter = new BaseRvAdapter<MagnetBean.ResourcesBean>(beanList) {
                @NonNull
                @Override
                public AdapterItem<MagnetBean.ResourcesBean> onCreateItem(int viewType) {
                    return new MagnetItem();
                }
            };
            magnetRv.setAdapter(adapter);
        }else {
            adapter.setData(beanList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getEpisodeId() {
        return getIntent().getIntExtra("episode_id", -1);
    }

    @Override
    public void downloadTorrentOver(String torrentPath, String magnet) {
        Intent intent = new Intent(this, DownloadMangerActivity.class);
        for (Torrent torrent : IApplication.torrentList){
            if (torrentPath.equals(torrent.getPath())){
                startActivity(intent);
                return;
            }
        }
        intent.putExtra("episode_id", getIntent().getIntExtra("episode_id", -1));
        intent.putExtra("torrent_path", torrentPath);
        intent.putExtra("anime_folder", animeTitle);
        intent.putExtra("torrent_magnet", magnet);
        startActivity(intent);
    }

    @Override
    public void showLoading(String text) {
        showLoadingDialog(text, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SelectInfoEvent event) {
        if (event.getType() == SelectInfoEvent.TYPE){
            typesBean = new AnimeTypeBean.TypesBean();
            if (event.getSelectId() == -1){
                typesBean = null;
                searchMagnetTypeTv.setText("全部");
            }else {
                typesBean.setId(event.getSelectId());
                typesBean.setName(event.getSelectName());
                searchMagnetTypeTv.setText(event.getSelectName());
            }
        }else if (event.getType() == SelectInfoEvent.SUBGROUP){
            subgroupsBean = new SubGroupBean.SubgroupsBean();
            if (event.getSelectId() == -1){
                subgroupsBean = null;
                searchMagnetSubgroupTv.setText("全部");
            }else {
                subgroupsBean.setId(event.getSelectId());
                subgroupsBean.setName(event.getSelectName());
                searchMagnetSubgroupTv.setText(event.getSelectName());
            }
        }
        searchMagnet();
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MagnetBean.ResourcesBean model){
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        presenter.downloadTorrent(animeTitle, model.getMagnet());
                    }
                });
    }

    private void searchMagnet(){
        String searchText = searchMagnetEt.getText().toString().trim();
        if (StringUtils.isEmpty(searchText)){
            ToastUtils.showShort("请输入搜索条件");
        }else {
            int typesId = typesBean == null ? -1 : typesBean.getId();
            int subgroupsId = subgroupsBean == null ? -1 : subgroupsBean.getId();
            presenter.searchMagnet(searchText, typesId, subgroupsId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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

    }
}
