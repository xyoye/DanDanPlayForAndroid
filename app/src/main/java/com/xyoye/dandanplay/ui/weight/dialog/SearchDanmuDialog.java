package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.xyoye.dandanplay.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/10/24.
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
    private onSearchClickListener listener;

    public SearchDanmuDialog(@NonNull Context context, onSearchClickListener listener) {
        super(context,  R.style.Dialog);
        this.listener = listener;
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

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.confirm_tv) {
            String anime = animeEt.getText().toString().trim();
            String episode;
            if (episodeRb.isChecked()) {
                episode = episodeEt.getText().toString().trim();
            } else {
                episode = "movie";
            }
            listener.onSearch(anime, episode);
        }
        SearchDanmuDialog.this.dismiss();
    }

    public interface onSearchClickListener{
        void onSearch(String anime, String episode);
    }
}
