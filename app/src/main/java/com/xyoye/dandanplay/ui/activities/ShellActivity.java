package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.SkinAppCompatDelegateImpl;
import android.text.TextUtils;
import android.widget.Toast;

import com.xyoye.dandanplay.R;

import java.lang.reflect.InvocationTargetException;

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
                    Class<? extends Fragment> clz = (Class<? extends Fragment>) Class.forName(className);
                    Fragment fragment = clz.getConstructor().newInstance();
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(R.id.frame_shell,fragment,className);
                    transaction.setPrimaryNavigationFragment(fragment);
                    transaction.commit();
                }catch (ClassNotFoundException e) {
                    Toast.makeText(this, "无法打开此页面", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }catch ( InvocationTargetException | IllegalAccessException| InstantiationException | NoSuchMethodException | ClassCastException e) {
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
