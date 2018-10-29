package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.SearchDanmuEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/10/24.
 */

public class SearchDanmuDialog extends Dialog {

    @BindView(R.id.episode_type_rg)
    RadioGroup episodeTypeRg;
    @BindView(R.id.episode_rb)
    RadioButton episodeRb;
    @BindView(R.id.ova_rb)
    RadioButton ovaRb;
    @BindView(R.id.episode_et)
    EditText episodeEt;
    @BindView(R.id.anime_et)
    EditText animeEt;
    @BindView(R.id.search_danmu_bt)
    Button searchDanmuBt;

    public SearchDanmuDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search_danmu);
        ButterKnife.bind(this);

        episodeTypeRg.check(episodeRb.getId());

        episodeRb.setOnClickListener(v -> {
            episodeEt.setEnabled(true);
            ovaRb.setChecked(false);
        });

        ovaRb.setOnClickListener(v -> {
            episodeEt.setEnabled(false);
            episodeRb.setChecked(false);
        });
    }

    @OnClick(R.id.search_danmu_bt)
    public void onViewClicked() {
        String anime = animeEt.getText().toString().trim();
        String episode;
        if (episodeRb.isChecked()){
            episode = episodeEt.getText().toString().trim();
        }else {
            episode = "movie";
        }
        SearchDanmuDialog.this.dismiss();
        EventBus.getDefault().post(new SearchDanmuEvent(anime, episode));
    }
}
