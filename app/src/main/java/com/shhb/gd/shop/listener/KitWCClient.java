package com.shhb.gd.shop.listener;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by superMoon on 2017/3/15.
 */
public class KitWCClient extends WebChromeClient {
    private KProgressHUD hud;

    public KitWCClient(KProgressHUD hud) {
        this.hud = hud;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (newProgress >= 100) {
            if(hud.isShowing()){
                hud.dismiss();
            }
        }
    }
}
