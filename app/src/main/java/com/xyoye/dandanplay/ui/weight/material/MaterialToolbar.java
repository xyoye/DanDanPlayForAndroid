package com.xyoye.dandanplay.ui.weight.material;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.xyoye.dandanplay.R;

import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;

public class MaterialToolbar extends com.google.android.material.appbar.MaterialToolbar implements SkinCompatSupportable {
    private SkinCompatBackgroundHelper backgroundHelper;
    public MaterialToolbar(Context context) {
        this(context,null);
    }

    public MaterialToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs, R.attr.toolbarStyle);
    }

    public MaterialToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        backgroundHelper = new SkinCompatBackgroundHelper(this);
        backgroundHelper.loadFromAttributes(attrs, defStyleAttr);
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        if (backgroundHelper != null) {
            backgroundHelper.onSetBackgroundResource(resid);
        }
    }

    public void applyBackground() {
        if (backgroundHelper != null) {
            backgroundHelper.applySkin();
        }
    }

    @Override
    public void applySkin() {
        applyBackground();
    }
}
