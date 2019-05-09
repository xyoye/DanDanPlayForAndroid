package com.player.ijkplayer.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.KeyboardUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.player.ijkplayer.R;
import com.player.ijkplayer.adapter.BlockAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/5/6.
 */

public class DanmuBlockView extends RelativeLayout implements View.OnClickListener{
    private EditText blockEt;
    private RecyclerView blockRv;

    private BlockAdapter blockAdapter;
    private List<String> blockList;

    private DanmuBlockListener listener;

    public DanmuBlockView(Context context) {
        this(context, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public DanmuBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.layout_danmu_block, this);

        blockList = new ArrayList<>();

        blockEt = findViewById(R.id.block_input_et);
        blockRv = findViewById(R.id.block_recycler);

        blockRv.setLayoutManager(new GridLayoutManager(getContext(), 5));
        blockAdapter = new BlockAdapter(R.layout.item_block, blockList);
        blockAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (listener != null)
                    listener.removeBlock(blockList.get(position));
                blockAdapter.remove(position);
            }
        });
        blockRv.setAdapter(blockAdapter);

        findViewById(R.id.block_view_cancel_iv).setOnClickListener(this);
        findViewById(R.id.add_block_bt).setOnClickListener(this);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.block_view_cancel_iv){
            if (listener != null)
                listener.onCloseView();
        }else if (id == R.id.add_block_bt){
            String blockText = blockEt.getText().toString().trim();
            if (TextUtils.isEmpty(blockText)){
                Toast.makeText(getContext(), "屏蔽关键字不能为空", Toast.LENGTH_LONG).show();
            }else if (blockList.contains(blockText)){
                Toast.makeText(getContext(), "当前关键字已屏蔽", Toast.LENGTH_LONG).show();
            }else if (traverseBlock(blockText)){
                Toast.makeText(getContext(), "当前关键字已屏蔽", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(getContext(), "添加屏蔽成功", Toast.LENGTH_LONG).show();
                blockEt.setText("");
                KeyboardUtils.hideSoftInput(blockEt);
                //添加到显示界面
                blockList.add(blockText);
                blockAdapter.notifyDataSetChanged();
                if (listener != null)
                    listener.addBlock(blockText);
            }
        }
    }

    private boolean traverseBlock(String blockText){
        boolean isContains = false;
        for (String text : blockList){
            if (text.contains(blockText)){
                isContains = true;
                break;
            }
        }
        return isContains;
    }

    public void setBlockList(List<String> blocks){
        blockList.clear();
        blockList.addAll(blocks);
        blockAdapter.notifyDataSetChanged();
    }

    public void setCallBack(DanmuBlockListener listener){
        this.listener = listener;
    }

    public List<String> getBlockList() {
        return blockList;
    }

    public interface DanmuBlockListener{
        void removeBlock(String text);

        void addBlock(String text);

        void onCloseView();
    }
}
