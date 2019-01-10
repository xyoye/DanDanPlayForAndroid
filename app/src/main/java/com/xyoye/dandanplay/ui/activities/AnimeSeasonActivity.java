package com.xyoye.dandanplay.ui.activities;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.mvp.impl.AnimaSeasonPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimaSeasonPresenter;
import com.xyoye.dandanplay.mvp.view.AnimaSeasonView;
import com.xyoye.dandanplay.ui.weight.item.AnimeItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyy on 2019/1/9.
 */

public class AnimeSeasonActivity extends BaseMvpActivity<AnimaSeasonPresenter> implements AnimaSeasonView {
    private int selectYear, selectMonth;
    private int nowYear, nowMonth;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.anima_rv)
    RecyclerView animaRv;
    @BindView(R.id.year_01_tv)
    TextView year01Tv;
    @BindView(R.id.year_02_tv)
    TextView year02Tv;
    @BindView(R.id.year_03_tv)
    TextView year03Tv;
    @BindView(R.id.year_other_tv)
    TextView yearOtherTv;
    @BindView(R.id.month_10_tv)
    TextView month10Tv;
    @BindView(R.id.month_7_tv)
    TextView month7Tv;
    @BindView(R.id.month_4_tv)
    TextView month4Tv;
    @BindView(R.id.month_1_tv)
    TextView month1Tv;

    private BaseRvAdapter<AnimeBeans.BangumiListBean> animaAdapter;
    private List<AnimeBeans.BangumiListBean> animaList;

    @NonNull
    @Override
    protected AnimaSeasonPresenter initPresenter() {
        return new AnimaSeasonPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_seaon_anima;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        setTitle("季度番剧");
        animaRv.setLayoutManager(new GridLayoutManager(this, 3));
        animaList = new ArrayList<>();
        animaAdapter = new BaseRvAdapter<AnimeBeans.BangumiListBean>(animaList) {
            @NonNull
            @Override
            public AdapterItem<AnimeBeans.BangumiListBean> onCreateItem(int viewType) {
                return new AnimeItem();
            }
        };
        animaRv.setAdapter(animaAdapter);

        Calendar calendar = Calendar.getInstance();
        nowYear = calendar.get(Calendar.YEAR);
        nowMonth = calendar.get(Calendar.MONTH) + 1;
        selectYear = nowYear;
        selectMonth = nowMonth;

        year01Tv.setText(selectYear+"年");
        year02Tv.setText(selectYear-1+"年");
        year03Tv.setText(selectYear-2+"年");
        year01Tv.setTextColor(getResources().getColor(R.color.theme_color));

        if (selectMonth >= 10){
            selectMonth = 10;
            month10Tv.setTextColor(getResources().getColor(R.color.theme_color));
        }else if (selectMonth >= 7){
            selectMonth = 7;
            month7Tv.setTextColor(getResources().getColor(R.color.theme_color));
        }else if (selectMonth >= 4){
            selectMonth = 4;
            month4Tv.setTextColor(getResources().getColor(R.color.theme_color));
        } else{
            selectMonth = 1;
            month1Tv.setTextColor(getResources().getColor(R.color.theme_color));
        }

        initNowDate(selectYear);
    }

    //如果选中年份是当前年份，需要判断月份是否可以选择，改变后请求数据
    private void initNowDate(int selectYear){
        month10Tv.setEnabled(true);
        month7Tv.setEnabled(true);
        month4Tv.setEnabled(true);
        month1Tv.setEnabled(true);
        month10Tv.setTextColor(getResources().getColor(R.color.black_color));
        month7Tv.setTextColor(getResources().getColor(R.color.black_color));
        month4Tv.setTextColor(getResources().getColor(R.color.black_color));
        month1Tv.setTextColor(getResources().getColor(R.color.black_color));


        if (selectYear == nowYear){
            if (nowMonth >= 7 && nowMonth < 10){
                if ( selectMonth > 7)
                    selectMonth = 7;
                month10Tv.setEnabled(false);
                month10Tv.setTextColor(getResources().getColor(R.color.gray_color3));
            }else if (nowMonth >= 4){
                if ( selectMonth > 4)
                    selectMonth = 4;
                month10Tv.setEnabled(false);
                month10Tv.setTextColor(getResources().getColor(R.color.gray_color3));
                month7Tv.setEnabled(false);
                month7Tv.setTextColor(getResources().getColor(R.color.gray_color3));
            } else if (nowMonth >= 1){
                if ( selectMonth > 1)
                    selectMonth = 1;
                month10Tv.setEnabled(false);
                month10Tv.setTextColor(getResources().getColor(R.color.gray_color3));
                month7Tv.setEnabled(false);
                month7Tv.setTextColor(getResources().getColor(R.color.gray_color3));
                month4Tv.setEnabled(false);
                month4Tv.setTextColor(getResources().getColor(R.color.gray_color3));
                month1Tv.setEnabled(true);
            }
        }
        switch (selectMonth){
            case 1:
                month1Tv.setTextColor(getResources().getColor(R.color.theme_color));
                break;
            case 4:
                month4Tv.setTextColor(getResources().getColor(R.color.theme_color));
                break;
            case 7:
                month7Tv.setTextColor(getResources().getColor(R.color.theme_color));
                break;
            case 10:
                month10Tv.setTextColor(getResources().getColor(R.color.theme_color));
                break;
        }
        presenter.getSeasonAnima(selectYear, selectMonth);
    }

    //恢复年份按钮状态
    private void clearYearSelect(){
        year01Tv.setTextColor(getResources().getColor(R.color.black_color));
        year02Tv.setTextColor(getResources().getColor(R.color.black_color));
        year03Tv.setTextColor(getResources().getColor(R.color.black_color));
        yearOtherTv.setTextColor(getResources().getColor(R.color.black_color));
        yearOtherTv.setText("其它");
    }

    //恢复月份按钮状态
    private void clearMonthSelect(){
        if (month10Tv.isEnabled())
            month10Tv.setTextColor(getResources().getColor(R.color.black_color));
        else
            month10Tv.setTextColor(getResources().getColor(R.color.gray_color3));

        if (month7Tv.isEnabled())
            month7Tv.setTextColor(getResources().getColor(R.color.black_color));
        else
            month7Tv.setTextColor(getResources().getColor(R.color.gray_color3));

        if (month4Tv.isEnabled())
            month4Tv.setTextColor(getResources().getColor(R.color.black_color));
        else
            month4Tv.setTextColor(getResources().getColor(R.color.gray_color3));

        month1Tv.setTextColor(getResources().getColor(R.color.black_color));
    }

    @Override
    public void initListener() {

    }

    @OnClick({R.id.year_01_tv, R.id.year_02_tv, R.id.year_03_tv, R.id.year_other_tv, R.id.month_10_tv, R.id.month_7_tv, R.id.month_4_tv, R.id.month_1_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.year_01_tv:
                selectYear = nowYear;
                clearYearSelect();
                year01Tv.setTextColor(getResources().getColor(R.color.theme_color));
                initNowDate(selectYear);
                break;
            case R.id.year_02_tv:
                selectYear = nowYear - 1;
                clearYearSelect();
                year02Tv.setTextColor(getResources().getColor(R.color.theme_color));
                initNowDate(selectYear);
                break;
            case R.id.year_03_tv:
                selectYear = nowYear - 2;
                clearYearSelect();
                year03Tv.setTextColor(getResources().getColor(R.color.theme_color));
                initNowDate(selectYear);
                break;
            case R.id.year_other_tv:
                showDatePicker();
                break;
            case R.id.month_10_tv:
                selectMonth = 10;
                clearMonthSelect();
                month10Tv.setTextColor(getResources().getColor(R.color.theme_color));
                presenter.getSeasonAnima(selectYear, selectMonth);
                break;
            case R.id.month_7_tv:
                selectMonth = 7;
                clearMonthSelect();
                month7Tv.setTextColor(getResources().getColor(R.color.theme_color));
                presenter.getSeasonAnima(selectYear, selectMonth);
                break;
            case R.id.month_4_tv:
                selectMonth = 4;
                clearMonthSelect();
                month4Tv.setTextColor(getResources().getColor(R.color.theme_color));
                presenter.getSeasonAnima(selectYear, selectMonth);
                break;
            case R.id.month_1_tv:
                selectMonth = 1;
                clearMonthSelect();
                month1Tv.setTextColor(getResources().getColor(R.color.theme_color));
                presenter.getSeasonAnima(selectYear, selectMonth);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void showDatePicker() {
        Calendar startDate = Calendar.getInstance();
        Calendar showDate = Calendar.getInstance();
        //最初时间为1980-1-1
        startDate.set(1980, 1, 1);
        //第一个选中时间为当前年份-3，因为其它三个已经存在按钮
        showDate.set(selectYear > (nowYear-3) ? (nowYear-3) : selectYear , 1, 1);
        TimePickerView pickerYearDialog = new TimePickerBuilder(this, (date, v) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            //选择年份
            selectYear = calendar.get(Calendar.YEAR);
            //清除旧的选择
            clearYearSelect();
            //3年内选中按钮，其它改变其它按钮
            if (nowYear - selectYear < 3){
                int n = nowYear - selectYear;
                if (n == 0){
                    year01Tv.setTextColor(getResources().getColor(R.color.theme_color));
                }else if (n == 1){
                    year02Tv.setTextColor(getResources().getColor(R.color.theme_color));
                }else {
                    year03Tv.setTextColor(getResources().getColor(R.color.theme_color));
                }
            }else {
                yearOtherTv.setTextColor(getResources().getColor(R.color.theme_color));
                yearOtherTv.setText(selectYear+"年");
            }
            initNowDate(selectYear);
        })
        .setType(new boolean[]{true, false, false, false, false, false})// 默认全部显示
        .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
        .setRangDate(startDate, Calendar.getInstance())//起始终止年月日设定
        .setDate(showDate)//当前时间
        .setSubmitColor(getResources().getColor(R.color.theme_color))//确定按钮文字颜色
        .setCancelColor(getResources().getColor(R.color.bilibili_pink))//取消按钮文字颜色
        .isCyclic(false)//是否循环滚动
        .build();
        pickerYearDialog.show();
    }

    @Override
    public void refreshAnimas(List<AnimeBeans.BangumiListBean> animas) {
        if (animas != null){
            animaList.clear();
            animaList.addAll(animas);
            animaAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }
}
