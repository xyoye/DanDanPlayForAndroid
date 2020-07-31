package com.xyoye.dandanplay.ui.weight.material;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import skin.support.design.app.SkinMaterialViewInflater;

public class MaterialViewInflater extends SkinMaterialViewInflater {
    @Override
    public View createView(@NonNull Context context, String name, @NonNull AttributeSet attrs) {
        if ("androidx.appcompat.widget.SwitchCompat".equals(name) || name.startsWith("com.google.android.material")) {
            switch (name) {
                case "androidx.appcompat.widget.SwitchCompat":
                case "com.google.android.material.switch.SwitchMaterial":
                    return new MaterialSwitch(context, attrs);
                case "com.google.android.material.bottomnavigation.BottomNavigationView":
                    return new MaterialBottomNavigationView(context, attrs);
                case "com.google.android.material.appbar.MaterialToolbar":
                    return new MaterialToolbar(context, attrs);
                default:
                    return super.createView(context, name, attrs);
            }
        }else {
            return super.createView(context, name, attrs);
        }
    }
}
