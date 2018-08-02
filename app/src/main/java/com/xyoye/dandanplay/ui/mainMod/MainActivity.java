package com.xyoye.dandanplay.ui.mainMod;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.jaeger.library.StatusBarUtil;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.MainPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.utils.DataGenerator;

import butterknife.BindView;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {
    @BindView(R.id.bottom_tab_layout)
    TabLayout mTabLayout;
    private Fragment[] mFragments;

    private boolean waitExit = true;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_main;
    }

    @NonNull
    @Override
    protected MainPresenterImpl initPresenter() {
        return new MainPresenterImpl(this, this);
    }

    @Override
    public void initView() {
        setTitle("");
        mFragments = DataGenerator.getFragments();
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabItemSelected(tab.getPosition());
                for (int i=0;i<mTabLayout.getTabCount();i++){
                    View view = mTabLayout.getTabAt(i).getCustomView();
                    ImageView icon = view.findViewById(R.id.tab_content_image);
                    TextView text = view.findViewById(R.id.tab_content_text);
                    if(i == tab.getPosition()){
                        icon.setImageResource(DataGenerator.mTabResPressed[i]);
                        text.setTextColor(getResources().getColor(R.color.theme_color));
                    }else{
                        icon.setImageResource(DataGenerator.mTabRes[i]);
                        text.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        for(int i=0;i<3;i++){
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(DataGenerator.getTabView(this,i)));
        }
        mTabLayout.getTabAt(1).select();
    }

    @Override
    public void initListener() {

    }

    private void onTabItemSelected(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = mFragments[0];
                break;
            case 1:
                fragment = mFragments[1];
                break;
            case 2:
                fragment = mFragments[2];
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, fragment).commit();
        }

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() != KeyEvent.ACTION_UP) {
            if (waitExit) {
                waitExit = false;
                ToastUtils.showShort("再按一次退出");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitExit = true;
                    }
                }, 2000);
            } else {
                finish();
                System.exit(0);
            }
        }
        return true;
    }
}
