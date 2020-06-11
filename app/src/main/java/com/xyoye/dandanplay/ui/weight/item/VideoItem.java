package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.io.File;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/6/30 0030.
 */

public class VideoItem implements AdapterItem<VideoBean> {

    @BindView(R.id.cover_iv)
    ImageView coverIv;
    @BindView(R.id.duration_tv)
    TextView durationTv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.item_layout)
    ConstraintLayout itemLayout;
    @BindView(R.id.bind_danmu_tv)
    TextView bindDanmuTv;
    @BindView(R.id.bind_zimu_tv)
    TextView bindZimuTv;
    @BindView(R.id.delete_video_tv)
    TextView deleteVideoTv;
    @BindView(R.id.danmu_tips_tv)
    TextView danmuTipsTv;
    @BindView(R.id.zimu_tips_tv)
    TextView zimuTipsTv;
    @BindView(R.id.swipe_menu_layout)
    EasySwipeMenuLayout swipeMenuLayout;
    @BindView(R.id.remove_danmu_tv)
    TextView removeDanmuTv;
    @BindView(R.id.remove_zimu_tv)
    TextView removeZimuTv;

    private View mView;
    private VideoItemEventListener listener;

    public VideoItem(VideoItemEventListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_video;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @SuppressLint("CheckResult")
    @Override
    public void onUpdateViews(final VideoBean model, final int position) {
        coverIv.setScaleType(ImageView.ScaleType.FIT_XY);
        if (!model.isNotCover()) {
            Observable
                    .create((ObservableOnSubscribe<Bitmap>) emitter ->
                            emitter.onNext(getBitmap(model.get_id())))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> coverIv.setImageBitmap(bitmap));
        } else {
            coverIv.setImageBitmap(BitmapFactory.decodeResource(mView.getResources(), R.mipmap.ic_smb_video));
        }

        //是否为上次播放的视频
        boolean isLastPlayVideo = false;
        String lastVideoPath = AppConfig.getInstance().getLastPlayVideo();
        if (!StringUtils.isEmpty(lastVideoPath)) {
            isLastPlayVideo = lastVideoPath.equals(model.getVideoPath());
        }

        boolean isBoundDanmu = isBoundDanmu(model.getDanmuPath());
        boolean isBoundZimu = isBoundZimu(model.getZimuPath());
        //是否已绑定弹幕
        danmuTipsTv.setVisibility(isBoundDanmu ? View.VISIBLE : View.GONE);
        removeDanmuTv.setVisibility(isBoundDanmu ? View.VISIBLE : View.GONE);
        //是否已经绑定字幕
        zimuTipsTv.setVisibility(isBoundZimu ? View.VISIBLE : View.GONE);
        removeZimuTv.setVisibility(isBoundZimu ? View.VISIBLE : View.GONE);
        //是否启用左部布局
        swipeMenuLayout.setCanRightSwipe(isBoundDanmu || isBoundZimu);

        titleTv.setText(FileUtils.getFileNameNoExtension(model.getVideoPath()));
        titleTv.setTextColor(isLastPlayVideo
                ? CommonUtils.getResColor(R.color.immutable_text_theme)
                : CommonUtils.getResColor(R.color.text_black));

        durationTv.setText(CommonUtils.formatDuring(model.getVideoDuration()));
        if (model.getVideoDuration() == 0) durationTv.setVisibility(View.GONE);

        bindDanmuTv.setOnClickListener(v -> listener.onBindDanmu(position));
        bindZimuTv.setOnClickListener(v -> listener.onBindZimu(position));
        removeDanmuTv.setOnClickListener(v -> listener.onRemoveDanmu(position));
        removeZimuTv.setOnClickListener(v -> listener.onRemoveZimu(position));
        deleteVideoTv.setOnClickListener(v -> listener.onVideoDelete(position));
        itemLayout.setOnClickListener(v -> listener.onOpenVideo(position));
    }

    private Bitmap getBitmap(int _id) {
        Bitmap bitmap;
        if (_id == 0) {
            bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            bitmap.eraseColor(CommonUtils.getResColor(R.color.video_item_image_default_color));
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(IApplication.get_context().getContentResolver(), _id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            bitmap.eraseColor(CommonUtils.getResColor(R.color.video_item_image_default_color));
        }
        return bitmap;
    }

    private boolean isBoundDanmu(String danmuPath) {
        if (!TextUtils.isEmpty(danmuPath)) {
            File danmuFile = new File(danmuPath);
            return danmuFile.exists();
        }
        return false;
    }

    private boolean isBoundZimu(String zimuPath) {
        if (!TextUtils.isEmpty(zimuPath)) {
            File zimuFile = new File(zimuPath);
            return zimuFile.exists();
        }
        return false;
    }

    public interface VideoItemEventListener {
        void onBindDanmu(int position);

        void onBindZimu(int position);

        void onRemoveDanmu(int position);

        void onRemoveZimu(int position);

        void onVideoDelete(int position);

        void onOpenVideo(int position);
    }
}
