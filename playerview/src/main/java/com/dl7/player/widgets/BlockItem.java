package com.dl7.player.widgets;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dl7.player.R;
import com.dl7.player.adapter.AdapterItem;

/**
 * Created by xyy on 2018/9/13.
 */


public class BlockItem implements AdapterItem<String> {
    private View mView;
    private TextView blockTv;
    private ImageView blockRemoveIv;

    @Override
    public int getLayoutResId() {
        return R.layout.item_block;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {
        blockTv = mView.findViewById(R.id.block_tv);
        blockRemoveIv = mView.findViewById(R.id.remove_block_iv);
    }

    @Override
    public void onUpdateViews(final String model, int position) {
        blockTv.setText(model);

        blockRemoveIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mView.getContext(), model, Toast.LENGTH_LONG).show();
            }
        });
    }
}
