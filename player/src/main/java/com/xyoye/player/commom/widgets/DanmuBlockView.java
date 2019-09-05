package com.xyoye.player.commom.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.player.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xyoye on 2019/5/6.
 */

public class DanmuBlockView extends RelativeLayout implements View.OnClickListener{
    private EditText blockEt;
    private LabelsView labelsView;
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
        labelsView = findViewById(R.id.labels_view);

        findViewById(R.id.block_view_cancel_iv).setOnClickListener(this);
        findViewById(R.id.add_block_bt).setOnClickListener(this);
        findViewById(R.id.delete_block_bt).setOnClickListener(this);

        this.setOnTouchListener((v, event) -> true);
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
                Toast.makeText(getContext(), "当前关键字已在屏蔽范围内", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(getContext(), "添加屏蔽成功", Toast.LENGTH_LONG).show();
                blockEt.setText("");
                KeyboardUtils.hideSoftInput(blockEt);

                if (blockText.endsWith(";")){
                    blockText = blockText.substring(0, blockText.length() - 1);
                }
                if (blockText.startsWith(";")){
                    blockText = blockText.substring(1);
                }
                String[] blockData;
                if (listener != null){
                    if (blockText.contains(";")){
                        blockData = blockText.split(";");
                    }else {
                        blockData = new String[]{blockText};
                    }

                    //添加到显示界面
                    blockList.addAll(Arrays.asList(blockData));
                    labelsView.setLabels(blockList);
                    if (listener != null)
                        listener.addBlock(Arrays.asList(blockData));
                }

            }
        }else if (id == R.id.delete_block_bt){
            List<String> selectLabelList = labelsView.getSelectLabelDatas();
            if (selectLabelList.size() == 0){
                ToastUtils.showShort("未选中屏蔽数据");
                return;
            }
            for (String text : selectLabelList){
                blockList.remove(text);
            }
            labelsView.setLabels(blockList);
            if (listener != null){
                listener.removeBlock(selectLabelList);
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
        labelsView.setLabels(blockList);
    }

    public void setCallBack(DanmuBlockListener listener){
        this.listener = listener;
    }

    public List<String> getBlockList() {
        return blockList;
    }

    public interface DanmuBlockListener{
        void removeBlock(List<String> text);

        void addBlock(List<String> text);

        void onCloseView();
    }
}
