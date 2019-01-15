package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.xyoye.dandanplay.bean.event.OpenDanmuSettingEvent;
import com.xyoye.dandanplay.bean.event.OpenVideoEvent;
import com.xyoye.dandanplay.bean.event.VideoActionEvent;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class VideoItem implements AdapterItem<VideoBean> {
    @BindView(R.id.cover_iv)
    ImageView coverIv;
    @BindView(R.id.duration_tv)
    TextView durationTv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.danmu_tips_iv)
    ImageView danmuTipsIv;
    @BindView(R.id.danmu_setting_rl)
    RelativeLayout danmuSetting;
    @BindView(R.id.video_info_rl)
    RelativeLayout videoInfoRl;
    @BindView(R.id.delete_action_ll)
    LinearLayout deleteActionLl;
    @BindView(R.id.bind_danmu_iv)
    ImageView bindDanmuIv;
    @BindView(R.id.bind_danmu_tv)
    TextView bindDanmuTv;
    @BindView(R.id.unbind_danmu_action_ll)
    LinearLayout unbindDanmuActionLl;
    @BindView(R.id.video_action_ll)
    LinearLayout videoActionLl;
    @BindView(R.id.close_action_ll)
    LinearLayout closeActionLl;
    private View mView;

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
        if (!model.isNotCover()){
            io.reactivex.Observable
                    .create((ObservableOnSubscribe<Bitmap>) emitter ->
                            emitter.onNext(getBitmap(model.get_id())))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> coverIv.setImageBitmap(bitmap));
        }else {
            coverIv.setImageBitmap(BitmapFactory.decodeResource(mView.getResources(), R.mipmap.ic_smb_video));
        }

        if (!StringUtils.isEmpty(model.getDanmuPath())){
            bindDanmuIv.setImageResource(R.mipmap.ic_download_bind_danmu);
            bindDanmuTv.setTextColor(mView.getContext().getResources().getColor(R.color.white));
            unbindDanmuActionLl.setEnabled(true);
        }else{
            bindDanmuIv.setImageResource(R.mipmap.id_cant_unbind_danmu);
            bindDanmuTv.setTextColor(mView.getContext().getResources().getColor(R.color.gray_color4));
            unbindDanmuActionLl.setEnabled(false);
        }

        titleTv.setText(FileUtils.getFileNameNoExtension(model.getVideoPath()));

        durationTv.setText(CommonUtils.formatDuring(model.getVideoDuration()));
        if (model.getVideoDuration()  == 0) durationTv.setVisibility(View.GONE);

        if (StringUtils.isEmpty(model.getDanmuPath())) {
            danmuTipsIv.setImageResource(R.drawable.ic_danmaku_inexist);
        } else {
            danmuTipsIv.setImageResource(R.drawable.ic_danmaku_exists);
        }

        danmuSetting.setOnClickListener(v -> {
            OpenDanmuSettingEvent event = new OpenDanmuSettingEvent(model.getVideoPath(), position);
            EventBus.getDefault().post(event);
        });

        videoInfoRl.setOnClickListener(view -> {
            OpenVideoEvent event = new OpenVideoEvent(model, position);
            EventBus.getDefault().post(event);
        });

        videoInfoRl.setOnLongClickListener(v -> {
            videoActionLl.setVisibility(View.VISIBLE);
            return true;
        });

        closeActionLl.setOnClickListener(v -> videoActionLl.setVisibility(View.GONE));

        unbindDanmuActionLl.setOnClickListener(v -> {
            EventBus.getDefault().post(new VideoActionEvent(VideoActionEvent.UN_BIND, position));
            videoActionLl.setVisibility(View.GONE);
        });


        deleteActionLl.setOnClickListener(v -> {
            EventBus.getDefault().post(new VideoActionEvent(VideoActionEvent.DELETE, position));
            videoActionLl.setVisibility(View.GONE);
        });
    }

    private Bitmap getBitmap(int _id){
        Bitmap bitmap;
        if (_id == 0){
            bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            bitmap.eraseColor(Color.parseColor("#000000"));
        }else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(IApplication.get_context().getContentResolver(), _id, MediaStore.Images.Thumbnails.MICRO_KIND, options);;
        }
        if (bitmap == null){
            bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            bitmap.eraseColor(Color.parseColor("#000000"));
        }
        return bitmap;
    }
}
