package com.xyoye.dandanplay.ui.weight.material;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.xyoye.dandanplay.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;
import skin.support.widget.SkinCompatTextHelper;

import static skin.support.widget.SkinCompatHelper.INVALID_ID;

public class MaterialSwitch extends SwitchMaterial implements SkinCompatSupportable {

    private int trackTintId = INVALID_ID;
    private int thumbTintId = INVALID_ID;
    private SkinCompatTextHelper textHelper = null;
    private SkinCompatBackgroundHelper backgroundHelper = null;

    public MaterialSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchStyle);
    }

    public MaterialSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwitchCompat, defStyleAttr, 0);
        if (array.hasValue(R.styleable.SwitchCompat_trackTint)) {
            trackTintId = array.getResourceId(R.styleable.SwitchCompat_trackTint, trackTintId);
        }
        if (array.hasValue(R.styleable.SwitchCompat_thumbTint)) {
            thumbTintId = array.getResourceId(R.styleable.SwitchCompat_thumbTint, thumbTintId);
        }
        textHelper = SkinCompatTextHelper.create(this);
        backgroundHelper = new SkinCompatBackgroundHelper(this);

        textHelper.loadFromAttributes(attrs, defStyleAttr);
        backgroundHelper.loadFromAttributes(attrs, defStyleAttr);

        applySkin();
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        if (backgroundHelper != null) {
            backgroundHelper.onSetBackgroundResource(resid);
        }
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(int start, int top, int end, int bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        if (textHelper != null) {
            textHelper.onSetCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        }
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        if (textHelper != null) {
            textHelper.onSetCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        }
    }

    @Override
    public void applySkin() {
        if (textHelper != null) {
            textHelper.applySkin();
        }

        if (backgroundHelper != null) {
            backgroundHelper.applySkin();
        }

        if (trackTintId != INVALID_ID) {
            setTrackTintList(SkinCompatResources.getColorStateList(getContext(), trackTintId));
        }

        if (thumbTintId != INVALID_ID) {
            setThumbTintList(SkinCompatResources.getColorStateList(getContext(), thumbTintId));
        }
    }
}
