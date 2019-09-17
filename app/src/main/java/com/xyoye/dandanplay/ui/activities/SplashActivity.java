package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.mvp.impl.SplashPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SplashPresenter;
import com.xyoye.dandanplay.mvp.view.SplashView;
import com.xyoye.dandanplay.ui.weight.anim.path.TextPathAnimView;
import com.xyoye.dandanplay.ui.weight.anim.svg.AnimatedSvgView;
import com.xyoye.dandanplay.utils.AppConfig;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/15.
 */

public class SplashActivity extends BaseMvpActivity<SplashPresenter> implements SplashView {

    @BindView(R.id.icon_svg_view)
    AnimatedSvgView iconSvgView;
    @BindView(R.id.text_path_view)
    TextPathAnimView textPathView;
    @BindView(R.id.address_ll)
    LinearLayout addressLl;


    @Override
    protected void process(Bundle savedInstanceState) {
        super.process(savedInstanceState);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_splash;
    }

    @Override
    public void initView() {
        presenter.checkToken();

        //是否关闭启动页
        if (AppConfig.getInstance().isCloseSplashPage()) {
            launchActivity();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setRepeatCount(Animation.ABSOLUTE);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setDuration(2000);

        textPathView.setAnimListener(new TextPathAnimView.AnimListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                textPathView.postDelayed(() -> launchActivity(), 350);
            }

            @Override
            public void onLoop() {

            }
        });

        textPathView.startAnim();
        iconSvgView.start();
        addressLl.startAnimation(alphaAnimation);
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected SplashPresenter initPresenter() {
        return new SplashPresenterImpl(this, this);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private void launchActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_activity_enter, R.anim.anim_activity_exit);
        this.finish();
    }
}
