package com.xyoye.dandanplay.ui.activities.setting;

import android.support.annotation.NonNull;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.mvp.impl.BlockManagerPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.BlockManagerPresenter;
import com.xyoye.dandanplay.mvp.view.BlockManagerView;
import com.xyoye.player.commom.widgets.LabelsView;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/6/26.
 */

public class DanmuBlockManagerActivity extends BaseMvpActivity<BlockManagerPresenter> implements BlockManagerView {

    @BindView(R.id.labels_view)
    LabelsView labelsView;

    private List<String> blockData;

    @NonNull
    @Override
    protected BlockManagerPresenter initPresenter() {
        return new BlockManagerPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_block_manager;
    }

    @Override
    public void initView() {
        setTitle("弹幕屏蔽管理");
        blockData = new ArrayList<>();
        labelsView.setLabels(blockData);

        presenter.queryBlockData();
    }

    @Override
    public void initListener() {

    }

    @OnClick({R.id.clear_tv, R.id.delete_tv, R.id.add_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clear_tv:
                new CommonDialog.Builder(this)
                        .setOkListener(dialog -> {
                            blockData.clear();
                            labelsView.setLabels(new ArrayList<>());
                            presenter.deleteALl();
                        })
                        .setAutoDismiss()
                        .build()
                        .show("确定清空所有屏蔽数据？");
                break;
            case R.id.delete_tv:
                List<String> selectLabelList = labelsView.getSelectLabelDatas();
                if (selectLabelList.size() == 0){
                    ToastUtils.showShort("未选中屏蔽数据");
                    return;
                }
                for (String text : selectLabelList){
                    blockData.remove(text);
                }
                labelsView.setLabels(blockData);
                presenter.deleteBlock(selectLabelList);
                break;
            case R.id.add_tv:
                new CommonEditTextDialog(this, CommonEditTextDialog.ADD_BLOCK, blockData, data -> {
                    if (data == null || data.length == 0) return;
                    List<String> addSqlData = new ArrayList<>();
                    for (String text : data){
                        if (!blockData.contains(text)){
                            addSqlData.add(text);
                        }
                    }
                    blockData.addAll(addSqlData);
                    presenter.addBlock(addSqlData);
                    labelsView.setLabels(blockData);
                }).show();
                break;
        }
    }

    @Override
    public List<String> updateData(List<String> result) {
        blockData.clear();
        blockData.addAll(result);
        labelsView.setLabels(blockData);
        return null;
    }
}
