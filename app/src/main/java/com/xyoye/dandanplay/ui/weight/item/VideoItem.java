package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;
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
    @BindView(R.id.tips_ll)
    LinearLayout tipsLl;
    @BindView(R.id.danmu_tips_iv)
    ImageView danmuTipsIv;
    @BindView(R.id.zimu_tips_tv)
    TextView zimuTipsTv;
    @BindView(R.id.video_info_rl)
    RelativeLayout videoInfoRl;
    @BindView(R.id.bind_danmu_iv)
    ImageView bindDanmuIv;
    @BindView(R.id.bind_danmu_tv)
    TextView bindDanmuTv;
    @BindView(R.id.bind_zimu_iv)
    ImageView bindZimuIv;
    @BindView(R.id.bind_zimu_tv)
    TextView bindZimuTv;
    @BindView(R.id.delete_action_ll)
    LinearLayout deleteActionLl;
    @BindView(R.id.unbind_danmu_action_ll)
    LinearLayout unbindDanmuActionLl;
    @BindView(R.id.unbind_zimu_action_ll)
    LinearLayout unbindZimuActionLl;
    @BindView(R.id.video_action_ll)
    LinearLayout videoActionLl;
    @BindView(R.id.close_action_ll)
    LinearLayout closeActionLl;

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
            io.reactivex.Observable
                    .create((ObservableOnSubscribe<Bitmap>) emitter ->
                            emitter.onNext(getBitmap(model.get_id())))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> coverIv.setImageBitmap(bitmap));
        } else {
            coverIv.setImageBitmap(BitmapFactory.decodeResource(mView.getResources(), R.mipmap.ic_smb_video));
        }

        if (!StringUtils.isEmpty(model.getDanmuPath())) {
            danmuTipsIv.setImageResource(R.mipmap.ic_danmu_exists);
            bindDanmuIv.setImageResource(R.mipmap.ic_download_bind_danmu);
            bindDanmuTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_white));
            unbindDanmuActionLl.setEnabled(true);
        } else {
            danmuTipsIv.setImageResource(R.mipmap.ic_danmu_unexists);
            bindDanmuIv.setImageResource(R.mipmap.ic_cant_unbind_danmu);
            bindDanmuTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            unbindDanmuActionLl.setEnabled(false);
        }

        if (!StringUtils.isEmpty(model.getZimuPath())) {
            zimuTipsTv.setVisibility(View.VISIBLE);
            zimuTipsTv.setText(FileUtils.getFileExtension(model.getZimuPath()).toUpperCase());
            bindZimuIv.setImageResource(R.mipmap.ic_download_bind_danmu);
            bindZimuTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_white));
            unbindZimuActionLl.setEnabled(true);
        } else {
            zimuTipsTv.setVisibility(View.GONE);
            bindZimuIv.setImageResource(R.mipmap.ic_cant_unbind_danmu);
            bindZimuTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            unbindZimuActionLl.setEnabled(false);
        }

        //是否为上次播放的视频
        boolean isLastPlayVideo = false;
        String lastVideoPath = AppConfig.getInstance().getLastPlayVideo();
        if (!StringUtils.isEmpty(lastVideoPath)) {
            isLastPlayVideo = lastVideoPath.equals(model.getVideoPath());
        }

        titleTv.setText(FileUtils.getFileNameNoExtension(model.getVideoPath()));
        titleTv.setTextColor(isLastPlayVideo
                ? CommonUtils.getResColor(R.color.immutable_text_theme)
                : CommonUtils.getResColor(R.color.text_black));

        durationTv.setText(CommonUtils.formatDuring(model.getVideoDuration()));
        if (model.getVideoDuration() == 0) durationTv.setVisibility(View.GONE);

        videoActionLl.setVisibility(View.GONE);

        tipsLl.setOnClickListener(v -> listener.bindDanmu(position));
        videoInfoRl.setOnClickListener(v -> listener.openVideo(position));
        unbindDanmuActionLl.setOnClickListener(v -> {
            listener.unBindDanmu(position);
            videoActionLl.setVisibility(View.GONE);
        });
        unbindZimuActionLl.setOnClickListener(v -> {
            listener.unBindZimu(position);
            videoActionLl.setVisibility(View.GONE);
        });
        deleteActionLl.setOnClickListener(v -> {
            listener.onDelete(position);
            videoActionLl.setVisibility(View.GONE);
        });
        closeActionLl.setOnClickListener(v ->
                videoActionLl.setVisibility(View.GONE));

        videoInfoRl.setOnLongClickListener(v -> {
            videoActionLl.setVisibility(View.VISIBLE);
            return true;
        });

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

    public interface VideoItemEventListener {
        void bindDanmu(int position);

        void openVideo(int position);

        void unBindDanmu(int position);

        void unBindZimu(int position);

        void onDelete(int position);
    }
}
