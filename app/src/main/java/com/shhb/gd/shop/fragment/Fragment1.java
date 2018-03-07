package com.shhb.gd.shop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.shhb.gd.shop.R;
import com.shhb.gd.shop.module.Constants;
import com.shhb.gd.shop.module.JsObject;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/**
 * Created by superMoon on 2017/3/15.
 */

public class Fragment1 extends BaseFragment {

    public Fragment1(String str) {
        super();
        this.str = str;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.web_activity, container, false);
        fatherView = (RelativeLayout) view.findViewById(R.id.fatherView);
        webView = new WebView(context);
        RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(webParams);

        fatherView.addView(webView);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(str);
            }
        });
        //精致长按事件
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);//支持JavaScript
        webView.getSettings().setAllowFileAccess(true);//允许访问文件
        webView.getSettings().setAllowFileAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript读取其他的本地文件
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源
        //图片显示
        webView.getSettings().setLoadsImagesAutomatically(true);
        //自适应屏幕
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);
                if (progress >= 100) {
                    Intent intent = new Intent(Constants.SENDMSG_SHOW);
                    context.sendBroadcast(intent);
                }
//                if (progress >= 100) {
//                    if (hud.isShowing()) {
//                        hud.dismiss();
//                    }
//                } else {
//                    hud.setLabel("玩命加载中");
//                    hud.show();
//                }
            }
        });
        webView.addJavascriptInterface(new JsObject(1,webView,context), "native_android");
        webView.setWebViewClient(new WebViewClient());
        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        IX5WebViewExtension ix5 = webView.getX5WebViewExtension();
        if (null != ix5) {
            ix5.setScrollBarFadingEnabled(false);
        }
        //给H5读写内存的权限
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = context.getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);//允许访问文件
        webView.getSettings().setAppCacheEnabled(true);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webView.removeAllViews();
        webView.destroy();
    }
}
