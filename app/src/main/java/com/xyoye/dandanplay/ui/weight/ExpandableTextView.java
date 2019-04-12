package com.xyoye.dandanplay.ui.weight;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
    private static final int MAX_COLLAPSED_LINES = 1;
    private Map<Integer, Integer> mCollapsedStatus = new HashMap<>();
    private TextView contentTv, extraTv;

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
        contentTv = findViewById(R.id.content_tv);
        extraTv = findViewById(R.id.extra_tv);
        setOnClickListener(this);
    }

    public void setText(String text, int maxWith) {
        if (forceRefresh) {
            mCollapsedStatus.put(0, null);
        }

        SpannableString spanText;

        this.position = 0;
        //获取状态
        Integer state = mCollapsedStatus.get(0);
        if (state == null) {
            //获取行数
            int lineCount = getLineCount(text + "[收起]", maxWith);
            //大于最大行数
            if (lineCount > MAX_COLLAPSED_LINES) {
                //添加“收起”后缀
                text += "[收起]";
                spanText = new SpannableString(text);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.theme_color));
                spanText.setSpan(colorSpan, text.length()-4, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                //设置最大行数现在
                contentTv.setMaxLines(MAX_COLLAPSED_LINES);
                //显示“展开”
                extraTv.setVisibility(VISIBLE);
                //保存当前状态
                mCollapsedStatus.put(0, STATE_COLLAPSED);
            } else {
                spanText = new SpannableString(text);
                contentTv.setMaxLines(lineCount);
                extraTv.setVisibility(GONE);
                mCollapsedStatus.put(0, STATE_NOT_OVERFLOW);
            }
            contentTv.setText(spanText);
        } else {
            switch (state) {
                case STATE_NOT_OVERFLOW:
                    spanText = new SpannableString(text);

                    extraTv.setVisibility(GONE);
                    contentTv.setMaxLines(MAX_COLLAPSED_LINES);
                    break;
                case STATE_COLLAPSED:
                    text += "[收起]";
                    spanText = new SpannableString(text);
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.theme_color));
                    spanText.setSpan(colorSpan, text.length()-4, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    extraTv.setVisibility(VISIBLE);
                    contentTv.setMaxLines(MAX_COLLAPSED_LINES);
                    break;
                case STATE_EXPANDED:
                default:
                    spanText = new SpannableString(text);

                    extraTv.setVisibility(GONE);
                    contentTv.setMaxLines(Integer.MAX_VALUE);
                    break;
            }
            contentTv.setText(spanText);
        }

        setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }


    @Override
    public void onClick(View v) {
        int state = mCollapsedStatus.get(position);
        if (state == STATE_COLLAPSED) {
            contentTv.setMaxLines(Integer.MAX_VALUE);
            extraTv.setVisibility(GONE);
            mCollapsedStatus.put(position, STATE_EXPANDED);

            if (null != expandStatusChangedListener) {
                expandStatusChangedListener.onChanged(true);
            }
        } else if (state == STATE_EXPANDED) {
            contentTv.setMaxLines(MAX_COLLAPSED_LINES);
            extraTv.setVisibility(VISIBLE);
            mCollapsedStatus.put(position, STATE_COLLAPSED);

            if (null != expandStatusChangedListener) {
                expandStatusChangedListener.onChanged(false);
            }
        }
    }

    public int getLineCount(String text, int maxWidth) {
        Paint paint = new Paint();
        paint.setTextSize(contentTv.getTextSize());
        paint.setTypeface(contentTv.getTypeface());

        TextPaint mTextPaint = contentTv.getPaint();
        mTextPaint.setTextSize(contentTv.getTextSize());
        int mTextViewWidth = (int) mTextPaint.measureText(text);

        int lineCount = mTextViewWidth / maxWidth;
        int ext = mTextViewWidth % maxWidth;

        if (ext > 0) lineCount++;

        return lineCount;
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
