package com.xyoye.dandanplay.ui.temp;

/**
 * Title: VideoViewPresenter <br>
 * Description: <br>
 * Copyright (c) 传化物流版权所有 2016 <br>
 * Created DateTime: 2016/10/29 0029 9:44
 * Created by Wentao.Shi.
 */
public class VideoViewPresenter implements VideoViewContract.Present {
    private VideoViewContract.View mView;

    public VideoViewPresenter(VideoViewContract.View view) {
        mView = view;
    }

    @Override
    public void sendDanmu(int episodeId, double time, int type, int color, String msg) {
//        SendCommentBean sendCommentBean=new SendCommentBean();
//        sendCommentBean.setTime(time);
//        sendCommentBean.setMode(type);
//        sendCommentBean.setColor(color);
//        sendCommentBean.setMessage(msg);
//        String commentJsonstr=GsonManager.getInstance().toJson(sendCommentBean);
//        String encryptedText="\""+ EncryptUtils.getEncryptDanmu(commentJsonstr)+"\"";
//        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),encryptedText);
//        //LogUtils.i("识别到视频id"+episodeId+"  弹幕内容："+encryptedText);
//        RetrofitManager retrofitManager = RetrofitManager.getInstance();
//        APIService apiService = retrofitManager.create();
//        retrofitManager.enqueue(apiService.sendComment(String.valueOf(episodeId), "ddplayandroid", body), new Callback<SendCommentResponse>() {
//            @Override
//            public void onResponse(Call<SendCommentResponse> call, Response<SendCommentResponse> response) {
//                if (response.isSuccessful()) {
//                    SendCommentResponse sendCommentResponse = response.body();
//                    if (!sendCommentResponse.isSuccess()){
//                        mView.error("弹幕发送失败，失败原因："+sendCommentResponse.getError());
//                    }
//                }else {
//                    mView.error("弹幕发送失败，失败原因："+response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(Call call, Throwable t) {
//                LogUtils.e("VideoViewPresenter", "sendcomment Error", t);
//                mView.error("弹幕发送失败，请检查网络");
//            }
//        });
    }
}
