package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import butterknife.BindView;

/**
 * Created by xyy on 2019/3/6.
 */

public class TorrentFileCheckItem implements AdapterItem<Torrent.TorrentFile> {

    @BindView(R.id.check_cb)
    CheckBox checkCb;
    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.file_size_tv)
    TextView fileSizeTv;

    private TorrentFileCheckListener listener;

    public TorrentFileCheckItem(TorrentFileCheckListener listener){
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_torrent_file_check;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(Torrent.TorrentFile model, int position) {
        checkCb.setChecked(model.isCheck());
        fileNameTv.setText(model.getName());
        fileSizeTv.setText(CommonUtils.convertFileSize(model.getLength()));

        checkCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null)
                listener.onCheck(position, isChecked);
        });
    }

    public interface TorrentFileCheckListener{
        void onCheck(int position, boolean isChecked);
    }
}
