package com.xyoye.dandanplay.ui.activities.anime;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.mvp.impl.AnimeSeasonPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimeSeasonPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeSeasonView;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.item.AnimeItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/1/9.
 */

public class AnimeSeasonActivity extends BaseMvpActivity<AnimeSeasonPresenter> implements AnimeSeasonView {
    public static final int SORT_FOLLOW = 0;
    public static final int SORT_NAME = 1;
    public static final int SORT_RETA = 2;
    private int selectYear, selectMonth;
    private int nowYear, nowMonth;

    private int sortType = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.anime_rv)
    RecyclerView animeRv;
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

    private BaseRvAdapter<AnimeBean> animeAdapter;
    private List<AnimeBean> animeList;

    @NonNull
    @Override
    protected AnimeSeasonPresenter initPresenter() {
        return new AnimeSeasonPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_seaon_anime;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        setTitle("季度番剧");
        animeRv.addItemDecoration(new ItemDecorationSpaces(ConvertUtils.dp2px(5)));
        animeRv.setLayoutManager(new GridLayoutManager(this, 3));
        animeList = new ArrayList<>();
        animeAdapter = new BaseRvAdapter<AnimeBean>(animeList) {
            @NonNull
            @Override
            public AdapterItem<AnimeBean> onCreateItem(int viewType) {
                return new AnimeItem();
            }
        };
        animeRv.setAdapter(animeAdapter);

        Calendar calendar = Calendar.getInstance();
        nowYear = calendar.get(Calendar.YEAR);
        nowMonth = calendar.get(Calendar.MONTH) + 1;
        selectYear = nowYear;
        selectMonth = nowMonth;

        year01Tv.setText(selectYear+"年");
        year02Tv.setText(selectYear-1+"年");
        year03Tv.setText(selectYear-2+"年");
        year01Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));

        if (selectMonth >= 10){
            selectMonth = 10;
            month10Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
        }else if (selectMonth >= 7){
            selectMonth = 7;
            month7Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
        }else if (selectMonth >= 4){
            selectMonth = 4;
            month4Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
        } else{
            selectMonth = 1;
            month1Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
        }

        initNowDate(selectYear);
    }

    //如果选中年份是当前年份，需要判断月份是否可以选择，改变后请求数据
    private void initNowDate(int selectYear){
        month10Tv.setEnabled(true);
        month7Tv.setEnabled(true);
        month4Tv.setEnabled(true);
        month1Tv.setEnabled(true);
        month10Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        month7Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        month4Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        month1Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));


        if (selectYear == nowYear){
            if (nowMonth >= 7 && nowMonth < 10){
                if ( selectMonth > 7)
                    selectMonth = 7;
                month10Tv.setEnabled(false);
                month10Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            }else if (nowMonth >= 4){
                if ( selectMonth > 4)
                    selectMonth = 4;
                month10Tv.setEnabled(false);
                month10Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                month7Tv.setEnabled(false);
                month7Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            } else if (nowMonth >= 1){
                if ( selectMonth > 1)
                    selectMonth = 1;
                month10Tv.setEnabled(false);
                month10Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                month7Tv.setEnabled(false);
                month7Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                month4Tv.setEnabled(false);
                month4Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                month1Tv.setEnabled(true);
            }
        }
        switch (selectMonth){
            case 1:
                month1Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                break;
            case 4:
                month4Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                break;
            case 7:
                month7Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                break;
            case 10:
                month10Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                break;
        }
        presenter.getSeasonAnime(selectYear, selectMonth);
    }

    //恢复年份按钮状态
    private void clearYearSelect(){
        year01Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        year02Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        year03Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        yearOtherTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        yearOtherTv.setText("其它");
    }

    //恢复月份按钮状态
    private void clearMonthSelect(){
        if (month10Tv.isEnabled())
            month10Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        else
            month10Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));

        if (month7Tv.isEnabled())
            month7Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        else
            month7Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));

        if (month4Tv.isEnabled())
            month4Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        else
            month4Tv.setTextColor(CommonUtils.getResColor(R.color.text_gray));

        month1Tv.setTextColor(CommonUtils.getResColor(R.color.text_black));
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
                year01Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                initNowDate(selectYear);
                break;
            case R.id.year_02_tv:
                selectYear = nowYear - 1;
                clearYearSelect();
                year02Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                initNowDate(selectYear);
                break;
            case R.id.year_03_tv:
                selectYear = nowYear - 2;
                clearYearSelect();
                year03Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                initNowDate(selectYear);
                break;
            case R.id.year_other_tv:
                showDatePicker();
                break;
            case R.id.month_10_tv:
                selectMonth = 10;
                clearMonthSelect();
                month10Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                presenter.getSeasonAnime(selectYear, selectMonth);
                break;
            case R.id.month_7_tv:
                selectMonth = 7;
                clearMonthSelect();
                month7Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                presenter.getSeasonAnime(selectYear, selectMonth);
                break;
            case R.id.month_4_tv:
                selectMonth = 4;
                clearMonthSelect();
                month4Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                presenter.getSeasonAnime(selectYear, selectMonth);
                break;
            case R.id.month_1_tv:
                selectMonth = 1;
                clearMonthSelect();
                month1Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                presenter.getSeasonAnime(selectYear, selectMonth);
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
                    year01Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                }else if (n == 1){
                    year02Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                }else {
                    year03Tv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                }
            }else {
                yearOtherTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                yearOtherTv.setText(selectYear+"年");
            }
            initNowDate(selectYear);
        })
        .setType(new boolean[]{true, false, false, false, false, false})// 默认全部显示
        .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
        .setRangDate(startDate, Calendar.getInstance())//起始终止年月日设定
        .setDate(showDate)//当前时间
        .setSubmitColor(CommonUtils.getResColor(R.color.immutable_text_theme))//确定按钮文字颜色
        .setCancelColor(CommonUtils.getResColor(R.color.immutable_text_pink))//取消按钮文字颜色
        .setBgColor(CommonUtils.getResColor(R.color.layout_bg_color))
        .setTitleBgColor(CommonUtils.getResColor(R.color.item_bg_color))
        .setTextColorCenter(CommonUtils.getResColor(R.color.text_black))
        .isCyclic(false)//是否循环滚动
        .build();
        pickerYearDialog.show();
    }

    @Override
    public void refreshAnime(List<AnimeBean> anime) {
        if (anime != null){
            animeList.clear();
            animeList.addAll(anime);
            animeAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_season_sort, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_by_follow:
                if (AppConfig.getInstance().isLogin()){
                    sortType = SORT_FOLLOW;
                    AppConfig.getInstance().saveSeasonSortType(SORT_FOLLOW);
                } else {
                    ToastUtils.showShort("请先登录再进行此操作");
                }
                break;
            case R.id.sort_by_name:
                sortType = SORT_NAME;
                AppConfig.getInstance().saveSeasonSortType(SORT_NAME);
                break;
            case R.id.sort_by_rate:
                sortType = SORT_RETA;
                AppConfig.getInstance().saveSeasonSortType(SORT_RETA);
                break;
        }
        presenter.sortAnime(animeList, sortType);
        animeAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }
}
