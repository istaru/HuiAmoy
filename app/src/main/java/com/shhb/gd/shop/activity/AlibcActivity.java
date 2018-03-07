package com.shhb.gd.shop.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.baichuan.android.trade.page.AlibcMyCartsPage;
import com.alibaba.baichuan.android.trade.page.AlibcMyOrdersPage;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.listener.KitWCClient;
import com.shhb.gd.shop.module.AlibcShow;
import com.shhb.gd.shop.module.JsObject;

/**
 * Created by superMoon on 2017/3/31.
 */

public class AlibcActivity extends BaseActivity implements View.OnClickListener{
    private String type = "";
    /**
     * 标题的父控件
     */
    private RelativeLayout titleAll,titleView;
    /**
     * 返回按钮
     */
    private LinearLayout onBack;
    private TextView title;
    /**
     * 显示商品的WebView
     */
    private WebView webView;
    private KitWCClient kitWcClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title_activity);
        type = getIntent().getStringExtra("type");
        initView();
        showToast(0,"加载中");
    }

    private void initView() {
        titleAll = (RelativeLayout) findViewById(R.id.titleAll);
        titleView = (RelativeLayout) findViewById(R.id.titleView);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        title = (TextView)findViewById(R.id.webView_title);

        webView = new WebView(context);
        webView.setBackgroundColor(ContextCompat.getColor(context,R.color.webBg));
        RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        webParams.addRule(RelativeLayout.BELOW,titleView.getId());
        webView.setLayoutParams(webParams);
        titleAll.addView(webView);

        kitWcClient = new KitWCClient(hud);
        webView.setWebChromeClient(kitWcClient);
        //禁止长按事件
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
        webView.addJavascriptInterface(new JsObject(webView, this), "native_android");
        webView.setWebViewClient(new WebViewClient());
        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        showAlibcH5();
    }

    /**
     * 显示淘宝购物车、淘宝订单
     */
    private void showAlibcH5() {
        if(TextUtils.equals("cart",type)){
            title.setText("淘宝购物车");
            AlibcShow.showH5(this, webView, kitWcClient, new AlibcMyCartsPage());
        } else {
            title.setText("淘宝订单");
            AlibcShow.showH5(this, webView, kitWcClient, new AlibcMyOrdersPage(0, true));
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
        AlibcTradeSDK.destory();
    }
}
