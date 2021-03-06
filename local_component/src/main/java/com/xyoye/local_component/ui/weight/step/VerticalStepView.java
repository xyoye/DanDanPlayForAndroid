package com.xyoye.local_component.ui.weight.step;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.xyoye.local_component.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 日期：16/6/24 11:48
 * <p/>
 * 描述：
 */
public class VerticalStepView extends LinearLayout implements VerticalStepViewIndicator.OnDrawIndicatorListener {
    private RelativeLayout mTextContainer;
    private VerticalStepViewIndicator mStepsViewIndicator;
    private List<StepInfo> mStepInfoList = new ArrayList<>();
    private int mUnComplectedTextColor = ContextCompat.getColor(getContext(), R.color.text_gray);//定义默认未完成文字的颜色;
    private int mComplectedTextColor = ContextCompat.getColor(getContext(), R.color.text_black);//定义默认完成文字的颜色;
    private int mDescribeTextColor = ContextCompat.getColor(getContext(), R.color.text_gray);//定义描述文字的颜色;
    private int mFailedTextColor = ContextCompat.getColor(getContext(), R.color.text_black);//定义描述文字的颜色;

    private int mTextSize = 14;//default textSize
    private int mDescribeTextSize = 12;//default textSize
    private TextView mTextView;
    private TextView mDescribeTextView;


    public VerticalStepView(Context context) {
        this(context, null);
    }

    public VerticalStepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalStepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.weight_vertical_setpsview, this);
        mStepsViewIndicator = (VerticalStepViewIndicator) rootView.findViewById(R.id.steps_indicator);
        mStepsViewIndicator.setOnDrawListener(this);
        mTextContainer = (RelativeLayout) rootView.findViewById(R.id.rl_text_container);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置初始的步骤信息
     *
     * @param infoList
     * @return
     */
    public VerticalStepView setInitStepInfo(List<StepInfo> infoList) {
        mStepInfoList.clear();
        mStepInfoList.addAll(infoList);
        mStepsViewIndicator.setStepInfo(mStepInfoList);
        return this;
    }

    /**
     * 添加步骤信息
     *
     * @param stepInfo
     * @return
     */
    public VerticalStepView addStepInfo(StepInfo stepInfo) {
        //找到ID相同的，不添加，更新信息
        for (StepInfo info : mStepInfoList) {
            if (stepInfo.getUniqueId() == info.getUniqueId()){
                info.setState(stepInfo.getState());
                info.setDescribe(stepInfo.getDescribe());
                info.setContent(stepInfo.getContent());
                mStepsViewIndicator.setStepInfo(mStepInfoList);
                return this;
            }
        }


        mStepInfoList.add(stepInfo);
        for (int i = 0; i < mStepInfoList.size() - 1; i++) {
            mStepInfoList.get(i).setState(StepState.COMPLETED);
        }
        mStepsViewIndicator.setStepInfo(mStepInfoList);
        return this;
    }

    /**
     * 设置正在进行的position
     *
     * @param position
     * @return
     */
    public VerticalStepView stepToPosition(int position) {
        for (int i = 0; i < mStepInfoList.size(); i++) {
            if (i < position) {
                mStepInfoList.get(i).setState(StepState.COMPLETED);
            } else if (i == position) {
                mStepInfoList.get(i).setState(StepState.ONGOING);
            } else {
                mStepInfoList.get(i).setState(StepState.DEFAULT);
            }
        }
        mStepsViewIndicator.setStepInfo(mStepInfoList);
        return this;
    }

    /**
     * 所有步骤已完成
     *
     * @return
     */
    public VerticalStepView stepComplete() {
        for (int i = 0; i < mStepInfoList.size(); i++) {
            mStepInfoList.get(i).setState(StepState.COMPLETED);
        }
        mStepsViewIndicator.setStepInfo(mStepInfoList);
        return this;
    }

    /**
     * 步骤失败
     *
     * @return
     */
    public VerticalStepView stepFailed(int position, String describe) {
        for (int i = 0; i < mStepInfoList.size(); i++) {
            if (i < position) {
                mStepInfoList.get(i).setState(StepState.COMPLETED);
            } else if (i == position) {
                mStepInfoList.get(i).setDescribe(describe);
                mStepInfoList.get(i).setState(StepState.FAILED);
            } else {
                mStepInfoList.get(i).setState(StepState.DEFAULT);
            }
        }
        mStepsViewIndicator.setStepInfo(mStepInfoList);
        return this;
    }


    /**
     * 设置未完成文字的颜色
     *
     * @param unComplectedTextColor
     * @return
     */
    public VerticalStepView setStepViewUnComplectedTextColor(int unComplectedTextColor) {
        mUnComplectedTextColor = unComplectedTextColor;
        return this;
    }

    /**
     * 设置完成文字的颜色
     *
     * @param complectedTextColor
     * @return
     */
    public VerticalStepView setStepViewComplectedTextColor(int complectedTextColor) {
        this.mComplectedTextColor = complectedTextColor;
        return this;
    }

    /**
     * 设置失败文字的颜色
     *
     * @param failedTextColor
     * @return
     */
    public VerticalStepView setStepViewFailedTextColor(int failedTextColor) {
        this.mFailedTextColor = failedTextColor;
        return this;
    }

    /**
     * 设置描述文字的颜色
     *
     * @return
     */
    public VerticalStepView setStepViewDescribeTextColor(int describeTextColor) {
        this.mDescribeTextColor = describeTextColor;
        return this;
    }

    /**
     * 设置StepsViewIndicator未完成线的颜色
     *
     * @param unCompletedLineColor
     * @return
     */
    public VerticalStepView setStepsViewIndicatorUnCompletedLineColor(int unCompletedLineColor) {
        mStepsViewIndicator.setUnCompletedLineColor(unCompletedLineColor);
        return this;
    }

    /**
     * 设置StepsViewIndicator完成线的颜色
     *
     * @param completedLineColor
     * @return
     */
    public VerticalStepView setStepsViewIndicatorCompletedLineColor(int completedLineColor) {
        mStepsViewIndicator.setCompletedLineColor(completedLineColor);
        return this;
    }

    /**
     * 设置StepsViewIndicator默认图片
     *
     * @param defaultIcon
     */
    public VerticalStepView setStepsViewIndicatorDefaultIcon(Drawable defaultIcon) {
        mStepsViewIndicator.setDefaultIcon(defaultIcon);
        return this;
    }

    /**
     * 设置StepsViewIndicator已完成图片
     *
     * @param completeIcon
     */
    public VerticalStepView setStepsViewIndicatorCompleteIcon(Drawable completeIcon) {
        mStepsViewIndicator.setCompleteIcon(completeIcon);
        return this;
    }

    /**
     * 设置StepsViewIndicator已完成图片
     *
     * @param failedIcon
     */
    public VerticalStepView setStepsViewIndicatorFailedIcon(Drawable failedIcon) {
        mStepsViewIndicator.setFailedIcon(failedIcon);
        return this;
    }

    /**
     * 设置StepsViewIndicator正在进行中的图片
     *
     * @param attentionIcon
     */
    public VerticalStepView setStepsViewIndicatorAttentionIcon(Drawable attentionIcon) {
        mStepsViewIndicator.setAttentionIcon(attentionIcon);
        return this;
    }

    /**
     * is reverse draw 是否倒序画
     *
     * @param isReverSe default is true
     * @return
     */
    public VerticalStepView reverseDraw(boolean isReverSe) {
        this.mStepsViewIndicator.reverseDraw(isReverSe);
        return this;
    }

    /**
     * set linePadding  proportion 设置线间距的比例系数
     *
     * @param linePaddingProportion
     * @return
     */
    public VerticalStepView setLinePaddingProportion(float linePaddingProportion) {
        this.mStepsViewIndicator.setIndicatorLinePaddingProportion(linePaddingProportion);
        return this;
    }


    /**
     * set textSize
     *
     * @param textSize
     * @return
     */
    public VerticalStepView setTextSize(int textSize) {
        if (textSize > 0) {
            mTextSize = textSize;
        }
        return this;
    }

    @Override
    public void ondrawIndicator() {
        if (mTextContainer != null) {
            mTextContainer.removeAllViews();//clear ViewGroup
            List<Float> complectedXPosition = mStepsViewIndicator.getCircleCenterPointPositionList();
            if (mStepInfoList != null && complectedXPosition != null && complectedXPosition.size() > 0) {
                for (int i = 0; i < mStepInfoList.size(); i++) {
                    float textViewY = complectedXPosition.get(i) - mStepsViewIndicator.getCircleRadius() / 2;

                    mTextView = new TextView(getContext());
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
                    mTextView.setText(mStepInfoList.get(i).getContent());
                    mTextView.setY(textViewY);
                    mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    if (mStepInfoList.get(i).getState() == StepState.COMPLETED) {
                        mTextView.setTypeface(null, Typeface.BOLD);
                        mTextView.setTextColor(mComplectedTextColor);
                    } else if (mStepInfoList.get(i).getState() == StepState.FAILED) {
                        mTextView.setTypeface(null, Typeface.BOLD);
                        mTextView.setTextColor(mFailedTextColor);
                    } else {
                        mTextView.setTextColor(mUnComplectedTextColor);
                    }

                    mTextContainer.addView(mTextView);

                    if (!TextUtils.isEmpty(mStepInfoList.get(i).getDescribe())) {
                        float describeViewY = textViewY + dp2px(20);

                        mDescribeTextView = new TextView(getContext());
                        mDescribeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDescribeTextSize);
                        mDescribeTextView.setText(mStepInfoList.get(i).getDescribe());
                        mDescribeTextView.setY(describeViewY);
                        mDescribeTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        mDescribeTextView.setTextColor(mDescribeTextColor);
                        mTextContainer.addView(mDescribeTextView);
                    }

                }
            }
        }
    }

    private float dp2px(int dpValue) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }
}
