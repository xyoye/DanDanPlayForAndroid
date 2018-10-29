package com.xyoye.dandanplay.utils.net;

import com.blankj.utilcode.util.NetworkUtils;
import com.xyoye.core.utils.TLog;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by yzd on 2017/5/10 0010.
 */

public class NetworkConsumer implements Consumer<Disposable> {

    private NetworkDisplay display;

    public NetworkConsumer() {

    }

    public NetworkConsumer(NetworkDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(Disposable disposable) {
        if (NetworkUtils.isAvailableByPing()) {
            if (display != null) {
                display.normalNetwork();
            }
            TLog.i("normal network");
        } else {
            if (display != null) {
                display.noNetwork();
            }
            TLog.i("no network");
        }
    }

    interface NetworkDisplay {

        void normalNetwork();

        void noNetwork();
    }
}
