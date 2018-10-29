package com.xyoye.dandanplay.ui.weight;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by YE on 2018/7/21.
 */

public class ExpandableTextView extends LinearLayout implements View.OnClickListener {

    private final int STATE_NOT_OVERFLOW = 1; //文本行数不超过限定行数
    private final int STATE_COLLAPSED = 2; //文本行数超过限定行数,处于折叠状态
    private final int STATE_EXPANDED = 3; //文本行数超过限定行数,被点击全文展开

    /* 默认最高行数 */
    private static final int MAX_COLLAPSED_LINES = 3;
    private Map<Integer, Integer> mCollapsedStatus = new HashMap<>();
    private TextView tv_expandable_content;
    private TextView tv_expand_or_collapse;

    private boolean forceRefresh;//是否每次setText都重新获取文本行数
    private float textViewWidthPx;
    private int position;
    private ExpandStatusChangedListener expandStatusChangedListener;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    @Override
    public void setOrientation(int orientation) {
        if (LinearLayout.HORIZONTAL == orientation) {
            throw new IllegalArgumentException("ExpandableTextView only supports Vertical Orientation.");
        }
        super.setOrientation(orientation);
    }

    public void setCollapsedStatus(HashMap<Integer, Integer> mCollapsedStatus) {
        this.mCollapsedStatus = mCollapsedStatus;
    }

    public void setTextViewWidthPx(float textViewWidthPx) {
        this.textViewWidthPx = textViewWidthPx;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    public void setExpandStatusChangedListener(ExpandStatusChangedListener expandStatusChangedListener) {
        this.expandStatusChangedListener = expandStatusChangedListener;
    }

    /**
     * 初始化属性
     *
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        // enforces vertical orientation
        setOrientation(LinearLayout.VERTICAL);
        // default visibility is gone
        setVisibility(GONE);
    }

    /**
     * 渲染完成时初始化view
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViews();
    }

    /**
     * 初始化viwe
     */
    private void findViews() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_expandable_textview, this);
        tv_expandable_content = findViewById(R.id.tv_expandable_content);
        tv_expand_or_collapse = findViewById(R.id.tv_expand_or_collapse);

        setOnClickListener(this);
    }

    public void setText(String text) {
        this.setText(text, 0);
    }

    public void setText(String text, final int index) {
        if (forceRefresh) {
            mCollapsedStatus.put(index, null);
        }

        this.position = index;
        Integer state = mCollapsedStatus.get(index);

        if (state == null) {
            int lineCount = getLineCount(text);
//            L.i("================================lineCount:" + lineCount + "======================================");
            if (lineCount > MAX_COLLAPSED_LINES) {
                tv_expandable_content.setMaxLines(MAX_COLLAPSED_LINES);
                tv_expand_or_collapse.setVisibility(View.VISIBLE);
                tv_expand_or_collapse.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_down_gray, 0, 0);
                mCollapsedStatus.put(index, STATE_COLLAPSED);

            } else {
                tv_expandable_content.setMaxLines(lineCount);
                tv_expand_or_collapse.setVisibility(View.GONE);
                mCollapsedStatus.put(index, STATE_NOT_OVERFLOW);

            }
            tv_expandable_content.setText(text);
        } else {
            //如果之前已经初始化过了，则使用保存的状态，无需再获取一次
            switch (state) {
                case STATE_NOT_OVERFLOW:
                    tv_expandable_content.setMaxLines(MAX_COLLAPSED_LINES);
                    tv_expand_or_collapse.setVisibility(View.GONE);
                    break;
                case STATE_COLLAPSED:
                    tv_expandable_content.setMaxLines(MAX_COLLAPSED_LINES);
                    tv_expand_or_collapse.setVisibility(View.VISIBLE);
                    tv_expand_or_collapse.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_down_gray, 0, 0);
                    break;
                case STATE_EXPANDED:
                    tv_expandable_content.setMaxLines(Integer.MAX_VALUE);
                    tv_expand_or_collapse.setVisibility(View.VISIBLE);
                    tv_expand_or_collapse.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_up_gray, 0, 0);
                    break;
            }
            tv_expandable_content.setText(text);
        }

        setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }


    @Override
    public void onClick(View v) {
        int state = mCollapsedStatus.get(position);
        //两种状态下对应动作
        if (state == STATE_COLLAPSED) {
            tv_expand_or_collapse.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_up_gray, 0, 0);
            tv_expandable_content.setMaxLines(Integer.MAX_VALUE);
            mCollapsedStatus.put(position, STATE_EXPANDED);

            if (null != expandStatusChangedListener) {
                expandStatusChangedListener.onChanged(true);
            }
        } else if (state == STATE_EXPANDED) {
            tv_expand_or_collapse.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_down_gray, 0, 0);
            tv_expandable_content.setMaxLines(MAX_COLLAPSED_LINES);
            mCollapsedStatus.put(position, STATE_COLLAPSED);

            if (null != expandStatusChangedListener) {
                expandStatusChangedListener.onChanged(false);
            }
        }
    }

    public int getLineCount(String text) {
        Paint paint = new Paint();
        paint.setTextSize(tv_expandable_content.getTextSize());
        paint.setTypeface(tv_expandable_content.getTypeface());

        List<String> strings = splitWordsIntoStringsThatFit(text, textViewWidthPx, paint);

        return strings.size();
    }


    public static List<String> splitWordsIntoStringsThatFit(String source, float maxWidthPx, Paint paint) {
        ArrayList<String> result = new ArrayList<>();

        ArrayList<String> currentLine = new ArrayList<>();

        String[] sources = source.split("\n");
        for (String chunk : sources) {
            if ("".equals(chunk)) {
                chunk = "\n";
            }
            if (paint.measureText(chunk) < maxWidthPx) {
                processFitChunk(maxWidthPx, paint, result, currentLine, chunk);
            } else {
                //the chunk is too big, split it.
                List<String> splitChunk = splitIntoStringsThatFit(chunk, maxWidthPx, paint);
                for (String chunkChunk : splitChunk) {
                    processFitChunk(maxWidthPx, paint, result, currentLine, chunkChunk);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            result.addAll(currentLine);
        }
        return result;
    }

    /**
     * Splits a string to multiple strings each of which does not exceed the width
     * of maxWidthPx.
     */
    private static List<String> splitIntoStringsThatFit(String source, float maxWidthPx, Paint paint) {
        if (TextUtils.isEmpty(source) || paint.measureText(source) <= maxWidthPx) {
            return Arrays.asList(source);
        }

        ArrayList<String> result = new ArrayList<>();
        int start = 0;
        for (int i = 1; i <= source.length(); i++) {
            String substr = source.substring(start, i);
            if (paint.measureText(substr) >= maxWidthPx) {
                //this one doesn't fit, take the previous one which fits
                String fits = source.substring(start, i - 1);
                result.add(fits);
                start = i - 1;
            }
            if (i == source.length()) {
                String fits = source.substring(start, i);
                result.add(fits);
            }
        }

        return result;
    }

    /**
     * Processes the chunk which does not exceed maxWidth.
     */
    private static void processFitChunk(float maxWidth, Paint paint, ArrayList<String> result, ArrayList<String> currentLine, String chunk) {
        currentLine.add(chunk);
        String currentLineStr = TextUtils.join(" ", currentLine);
        if (paint.measureText(currentLineStr) >= maxWidth || "\n".equals(chunk)) {
            //remove chunk
            currentLine.remove(currentLine.size() - 1);
            result.add(TextUtils.join(" ", currentLine));
            currentLine.clear();
            //ok because chunk fits
            currentLine.add(chunk);
        }
    }


    public interface ExpandStatusChangedListener {

        void onChanged(boolean isExpand);

    }
}
