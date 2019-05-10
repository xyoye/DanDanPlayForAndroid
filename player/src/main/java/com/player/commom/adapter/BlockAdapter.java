package com.player.commom.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.player.ijkplayer.R;

import java.util.List;

/**
 * Created by xyoye on 2019/4/28.
 */

public class BlockAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public BlockAdapter(@LayoutRes int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    public void convert(BaseViewHolder helper, String text) {
        helper.setText(R.id.block_tv, text)
                .addOnClickListener(R.id.remove_block_iv);
    }
}
