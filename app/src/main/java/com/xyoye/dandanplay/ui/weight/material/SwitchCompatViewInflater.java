package com.xyoye.dandanplay.ui.weight.material;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import skin.support.design.app.SkinMaterialViewInflater;

public class SwitchCompatViewInflater extends SkinMaterialViewInflater {
    @Override
    public View createView(@NonNull Context context, String name, @NonNull AttributeSet attrs) {
        if (name.equals("Switch") || name.equals("android.support.v7.widget.SwitchCompat")) {
            return new MaterialSwitch(context, attrs);
        } else {
            return null;
        }
    }
}
