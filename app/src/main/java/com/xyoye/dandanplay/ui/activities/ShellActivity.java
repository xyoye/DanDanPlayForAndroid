package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.SkinAppCompatDelegateImpl;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xyoye.dandanplay.R;

/**
 * 用来加载指定的Fragment
 */
public class ShellActivity extends AppCompatActivity {
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);

        Intent intent = getIntent();
        if (intent != null) {
            String className = intent.getStringExtra("fragment");
            if (!TextUtils.isEmpty(className)) {
                try {
                    FragmentManager manager = getSupportFragmentManager();
                    Fragment fragment = manager.getFragmentFactory().instantiate(getClassLoader(),className);
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(R.id.frame_shell,fragment,className);
                    transaction.setPrimaryNavigationFragment(fragment);
                    transaction.commit();
                }catch ( ClassCastException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        return SkinAppCompatDelegateImpl.get(ShellActivity.this,this);
    }
}
