package com.shhb.gd.shop.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.baichuan.android.trade.page.AlibcPage;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.module.AlibcShow;
import com.shhb.gd.shop.module.JsObject;
import com.shhb.gd.shop.tools.BaseTools;

/**
 * Created by superMoon on 2017/3/15.
 */
public class TailsWindow extends PopupWindow {
    private Activity context;
    /**
     * 优惠券父容器
     */
    private RelativeLayout fatherView;
    /**
     * 显示优惠券的WebView
     */
    public WebView webView;
    private View viewmenu;
    private TWClient twClient;
    private String numId;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public TailsWindow(final Activity context, String url, String numId, final ImageView icon, final TextView content1, final TextView content2) {
        super(context);
        this.numId = numId;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewmenu = inflater.inflate(R.layout.web_activity, null);
        fatherView = (RelativeLayout) viewmenu.findViewById(R.id.fatherView);
        webView = new WebView(context);
        RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(webParams);
        fatherView.addView(webView);

        webView.setBackgroundColor(ContextCompat.getColor(context,R.color.white));
        twClient = new TWClient();
        webView.setWebChromeClient(twClient);
        //禁止长按事件
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
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
        webView.addJavascriptInterface(new JsObject(webView, context), "native_android");
        webView.setWebViewClient(new WebViewClient());
        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);

        AlibcShow.showH5(context, webView, twClient, new AlibcPage(url));

        this.setContentView(viewmenu);
        this.setWidth(BaseTools.getWindowsWidth(context));// 设置窗体的宽
        this.setHeight((int) (BaseTools.getWindowsHeight(context) / 2.5));// 设置窗体的高
        this.setOutsideTouchable(true);// 点击外部可关闭窗口
        this.setBackgroundDrawable(new ColorDrawable());
        this.setFocusable(true);//设置窗体可点击
        this.setTouchable(true);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        this.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);//不被输入法挡住
        this.setAnimationStyle(R.style.AnimBottom);//设置窗体从底部进入的动画效果
        this.update();
        //关闭窗体时
        this.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                String text = content1.getText().toString();
                if(text.equals("立即领劵")){
                    icon.setVisibility(View.GONE);//隐藏icon
                    content2.setVisibility(View.GONE);//隐藏小字
                    content1.setText("继续购物>");//设置文字
                } else {
                    icon.setVisibility(View.VISIBLE);//隐藏icon
                    content2.setVisibility(View.VISIBLE);//隐藏小字
                    content1.setText("立即领劵");//设置文字
                }
//                BaseTools.setBackgroundAlpha(context,1.0f);
            }
        });
    }

    class TWClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress >= 100) {
                if (!TextUtils.equals(numId, "")) {
                    String js = "alert(window.native_android.coupon_msg ? 'y' : 'n');";
                    String wholeJS = "(function(_time,_url){" +
                            "setTimeout(function(){" +
                            "var msg = document.getElementById('J-msg').innerText;" +
                            "window.native_android.coupon_msg(JSON.stringify({msg:msg,url:_url}));" +
                            "},_time);" +
                            "})(4500," + "\"" + numId + "\"" + ");";
                    view.loadUrl("javascript:" + wholeJS);
                }
            }
        }
    }
}