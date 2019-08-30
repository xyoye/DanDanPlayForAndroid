package com.xyoye.dandanplay.ui.activities.play;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.RemoteScanBean;
import com.xyoye.dandanplay.bean.RemoteVideoBean;
import com.xyoye.dandanplay.mvp.impl.RemotePresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.RemotePresenter;
import com.xyoye.dandanplay.mvp.view.RemoteView;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;
import com.xyoye.dandanplay.ui.weight.item.RemoteVideoItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/7/11.
 */

public class RemoteActivity extends BaseMvpActivity<RemotePresenter> implements RemoteView {

    @BindView(R.id.remote_video_rv)
    RecyclerView remoteVideoRv;

    private List<RemoteVideoBean> remoteVideoList;
    private BaseRvAdapter<RemoteVideoBean> remoteVideoAdapter;

    private RemoteScanBean remoteData;
    private String checkedIP;

    @NonNull
    @Override
    protected RemotePresenter initPresenter() {
        return new RemotePresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_remote;
    }

    @Override
    public void initView() {
        remoteVideoList = new ArrayList<>();
        remoteVideoAdapter = new BaseRvAdapter<RemoteVideoBean>(remoteVideoList) {
            @NonNull
            @Override
            public AdapterItem<RemoteVideoBean> onCreateItem(int viewType) {
                return new RemoteVideoItem(position -> {
                    String videoHash = remoteVideoList.get(position).getHash();
                    String videoName = remoteVideoList.get(position).getName();
                    if (!StringUtils.isEmpty(videoHash)){
                        presenter.bindRemoteDanmu(videoHash, FileUtils.getFileNameNoExtension(videoName));
                    }else {
                        ToastUtils.showShort("当前视频不支持绑定弹幕");
                    }
                });
            }
        };
        remoteVideoRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        remoteVideoRv.setAdapter(remoteVideoAdapter);

        remoteData = getIntent().getParcelableExtra("remote_data");
        checkedIP = remoteData.getIp().get(0);
        if (remoteData.isTokenRequired() && StringUtils.isEmpty(remoteData.getAuthorization())){
            new CommonEditTextDialog(this, CommonEditTextDialog.REMOTE_TOKEN, new CommonEditTextDialog.CommonEditTextFullListener() {
                @Override
                public void onConfirm(String... data) {
                    remoteData.setAuthorization(data[0]);
                    presenter.getVideoList(checkedIP, remoteData.getPort(), data[0]);
                }

                @Override
                public void onCancel() {
                    RemoteActivity.this.finish();
                }
            });
        }else {
            presenter.getVideoList(checkedIP, remoteData.getPort(), remoteData.getAuthorization());
        }

        String title = remoteData.getMachineName();
        setTitle(StringUtils.isEmpty(title) ? "远程访问" : title);
    }

    @Override
    public void initListener() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        MenuItem menuSwitchIPItem = menu.findItem(R.id.menu_item_switch_ip);
        menuSwitchIPItem.setVisible(remoteData.getIp().size() > 0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_switch_ip) {
            AlertDialog.Builder builder = new AlertDialog.Builder(RemoteActivity.this);
            builder.setTitle("选择IP");
            builder.setItems(remoteData.getIp().toArray(new String[]{}), (dialog, which) -> {
                checkedIP = remoteData.getIp().get(which);
                presenter.getVideoList(checkedIP, remoteData.getPort(), remoteData.getAuthorization());
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshVideoList(List<RemoteVideoBean> videoList) {
        remoteVideoList.clear();
        remoteVideoList.addAll(videoList);
        remoteVideoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDanmuBind(String hash, String danmuPath) {
        if (remoteVideoList == null)
            return;
        for (int i = 0; i < remoteVideoList.size(); i++) {
            RemoteVideoBean remoteVideoBean = remoteVideoList.get(i);
            if (hash.equals(remoteVideoBean.getHash())){
                remoteVideoBean.setDanmuPath(danmuPath);
                remoteVideoAdapter.notifyItemChanged(i);
                break;
            }
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
}
