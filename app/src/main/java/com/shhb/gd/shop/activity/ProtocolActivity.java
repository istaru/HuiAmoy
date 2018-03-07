package com.shhb.gd.shop.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shhb.gd.shop.R;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import static com.shhb.gd.shop.module.Constants.HTML_REQUEST;

/**
 * Created by superMoon on 2017/4/19.
 */

public class ProtocolActivity extends BaseActivity implements View.OnClickListener{

    /**
     * 标题的父控件
     */
    private RelativeLayout titleAll,titleView;
    /**
     * 返回按钮
     */
    private LinearLayout onBack;
    private TextView title;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title_activity);
        showToast(0,"加载中");
        initView();
    }

    private void initView() {
        titleAll = (RelativeLayout) findViewById(R.id.titleAll);
        titleView = (RelativeLayout) findViewById(R.id.titleView);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        title = (TextView)findViewById(R.id.webView_title);
        title.setText("惠淘服务协议");

        webView = new WebView(context);
        webView.setBackgroundColor(ContextCompat.getColor(context,R.color.webBg));
        RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        webParams.addRule(RelativeLayout.BELOW,titleView.getId());
        webView.setLayoutParams(webParams);
        titleAll.addView(webView);

        webView.loadUrl(HTML_REQUEST + "page/agreement.html");
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
            public void onProgressChanged(WebView webView, int newProgress) {
                super.onProgressChanged(webView, newProgress);
                if (newProgress >= 100) {
                    if(hud.isShowing()){
                        hud.dismiss();
                    }
                }
            }
        });
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
    }

    /**
     * 返回键结束当前Activity
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(null != webView){
                    if(webView.canGoBack()){
                        webView.goBack();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.onBack:
                if(null != webView){
                    if(webView.canGoBack()){
                        webView.goBack();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
        }
    }
}
