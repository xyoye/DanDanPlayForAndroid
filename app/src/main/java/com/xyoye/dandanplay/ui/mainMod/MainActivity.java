package com.xyoye.dandanplay.ui.mainMod;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.MainPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.ui.homeMod.HomeFragment;
import com.xyoye.dandanplay.ui.personalMod.PersonalFragment;
import com.xyoye.dandanplay.ui.playMod.PlayFragment;
import com.xyoye.dandanplay.utils.DataGenerator;

import butterknife.BindView;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView, View.OnClickListener {
    @BindView(R.id.main_home)
    TextView mainHome;
    @BindView(R.id.main_play)
    TextView mainPlay;
    @BindView(R.id.main_personal)
    TextView mainPersonal;

    private Fragment fromFragment;
    private static HomeFragment homeFragment = HomeFragment.newInstance();
    private PlayFragment playFragment = PlayFragment.newInstance();
    private PersonalFragment personalFragment = PersonalFragment.newInstance();

    public TextView[] tabView;
    private long touchTime = 0;

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
        tabView = new TextView[]{mainHome, mainPlay, mainPersonal};
        fromFragment = homeFragment;
        selected(0);
        switchFragment(R.id.fragment_container, fromFragment, homeFragment, HomeFragment.TAG);
    }

    @Override
    public void initListener() {
        mainHome.setOnClickListener(this);
        mainPlay.setOnClickListener(this);
        mainPersonal.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_home:
                switchFragment(R.id.fragment_container, fromFragment, homeFragment,
                        HomeFragment.TAG);
                fromFragment = homeFragment;
                initFooterStyle();
                selected(0);
                break;
            case R.id.main_play:
                switchFragment(R.id.fragment_container, fromFragment, playFragment,
                        HomeFragment.TAG);
                fromFragment = playFragment;
                initFooterStyle();
                selected(1);
                break;
            case R.id.main_personal:
                switchFragment(R.id.fragment_container, fromFragment, personalFragment,
                        HomeFragment.TAG);
                fromFragment = personalFragment;
                initFooterStyle();
                selected(2);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - touchTime > 1500) {
                ToastUtils.showShort("再按一次退出应用");
                touchTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return false;
    }

    // 初始化底部导航的数据
    public void initFooterStyle() {
        int[] normalList = DataGenerator.mTabRes;
        String[] tabTitleList = DataGenerator.mTabTitle;
        for (int i = 0; i < normalList.length; i++) {
            Drawable img = ContextCompat.getDrawable(this,normalList[i]);
            assert img != null;
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            tabView[i].setCompoundDrawables(null, img, null, null);
            tabView[i].setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tabView[i].setText(tabTitleList[i]);
        }
    }

    // 底部导航被选中的修改其样式
    public void selected(int position) {
        int[] selectedList = DataGenerator.mTabResPressed;
        if (selectedList != null && position < 5) {
            Drawable img = ContextCompat.getDrawable(this, selectedList[position]);
            assert img != null;
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            tabView[position].setCompoundDrawables(null, img, null, null);
            tabView[position].setTextColor(ContextCompat.getColor(this, R.color.theme_color));
        }
    }
}
