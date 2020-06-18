package com.xyoye.dandanplay.utils;

import android.os.Environment;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.dandanplay.utils.net.service.SubtitleRetrofitService;
import com.xyoye.player.commom.bean.SubtitleBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2020/6/17.
 */

public class SubtitleRequester {

    public static void querySubtitle(String videoPath, CommOtherDataObserver<List<SubtitleBean>> subtitleObserver) {
        //只查找本地视频字幕
        String localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!videoPath.startsWith(localPath))
            return;
        String thunderHash = HashUtils.getFileSHA1(videoPath);
        String shooterHash = HashUtils.getFileHash(videoPath);
        if (!StringUtils.isEmpty(thunderHash) && !StringUtils.isEmpty(shooterHash)) {
            Map<String, String> shooterParams = new HashMap<>();
            shooterParams.put("filehash", shooterHash);
            shooterParams.put("pathinfo", FileUtils.getFileName(videoPath));
            shooterParams.put("format", "json");
            shooterParams.put("lang", "Chn");
            SubtitleRetrofitService service = RetroFactory.getSubtitleInstance();

            Observable<List<SubtitleBean.Shooter>> shooterObservable = service.queryShooter(shooterParams)
                    .onErrorReturnItem(new ArrayList<>());

            service.queryThunder(thunderHash)
                    .onErrorReturnItem(new SubtitleBean.Thunder())
                    .zipWith(shooterObservable, (thunder, shooters) ->
                            SubtitleConverter.transform(thunder, shooters, videoPath))
                    .doOnSubscribe(new NetworkConsumer())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subtitleObserver);
        }
    }
}
