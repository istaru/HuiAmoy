package com.shhb.gd.shop.listener;

import com.shhb.gd.shop.module.NetworkType;

/**
 * 网络状态变化观察者
 * Created by superMoon on 2017/4/25.
 */

public interface NetStateChangeObserver {
    void onNetDisconnected();
    void onNetConnected(NetworkType networkType);
}
