package com.xyoye.dandanplay.ui.weight.material;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.xyoye.dandanplay.R;

import skin.support.design.widget.SkinMaterialBottomNavigationView;
import skin.support.widget.SkinCompatBackgroundHelper;

public class MaterialBottomNavigationView extends SkinMaterialBottomNavigationView {
    private SkinCompatBackgroundHelper backgroundHelper;
    public MaterialBottomNavigationView(Context context) {
        this(context,null);
    }

    public MaterialBottomNavigationView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs, R.attr.bottomNavigationStyle);
    }

    public MaterialBottomNavigationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

    private void applyBackground() {
        if (backgroundHelper != null) {
            backgroundHelper.applySkin();
        }
    }

    @Override
    public void applySkin() {
        super.applySkin();
        applyBackground();
    }
}
