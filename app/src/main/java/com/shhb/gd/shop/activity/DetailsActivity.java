package com.shhb.gd.shop.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.auth.third.ui.context.CallbackContext;
import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.baichuan.android.trade.page.AlibcDetailPage;
import com.alibaba.baichuan.android.trade.page.AlibcPage;
import com.alibaba.fastjson.JSONObject;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.listener.KitWCClient;
import com.shhb.gd.shop.listener.ShareCallback;
import com.shhb.gd.shop.module.AlibcShow;
import com.shhb.gd.shop.module.Constants;
import com.shhb.gd.shop.module.JsObject;
import com.shhb.gd.shop.module.NetworkType;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;
import com.shhb.gd.shop.view.TailsWindow;
import com.umeng.socialize.UMShareAPI;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by superMoon on 2017/3/15.
 */
public class DetailsActivity extends BaseActivity implements View.OnClickListener{

    private KitWCClient kitWcClient;
    /**
     * 标题的类型 0天猫
     */
    private String titleType;
    /**
     * 商品ID
     */
    private String numId = "";
    /**
     * 优惠券链接
     */
    private String couponUrl = "";
    private String couponType = "";
    /**
     * 分享出去的地址
     */
    private String shareUrl = "";
    /**
     * 分享出去的图片
     */
    private String shareImg = "";
    /**
     * 分享出去的标题
     */
    private String shareTitle = "";
    /**
     * 分享出去的内容
     */
    private String shareContent = "";
    private RelativeLayout fatherView;
    /**
     * 标题的父控件
     */
    private RelativeLayout titleAll;
    private TextView title;
    /**
     * 底部按钮父控件
     */
    private LinearLayout bottomAll;
    private ImageView tailIcon;
    private TextView tailContent1,tailContent2;
    /**
     * 返回按钮
     */
    private LinearLayout onBack;
    /**
     * 分享按钮
     */
    private LinearLayout onShare;
    /**
     * 显示商品的WebView
     */
    private WebView webView;
    private TailsWindow tailsWindow;
    private List<Activity> activitys;

    @Override
    protected boolean needRegisterNetworkChangeObserver() {
        return true;
    }

    @Override
    public void onNetConnected(NetworkType networkType) {
        super.onNetConnected(networkType);
        BaseTools.removeNoneImg(fatherView);
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNetDisconnected() {
        super.onNetDisconnected();
        webView.setVisibility(View.GONE);
        BaseTools.addNoneImg(fatherView,this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tails_activity);
        activitys = MainApplication.getActivitys();
        initData();
        initView();
        showToast(0,"加载中");
    }

    private void initData(){
        try {
            String reulst = getIntent().getStringExtra("result");
            JSONObject jsonObject = JSONObject.parseObject(reulst);
            titleType = jsonObject.getString("store_type");
            numId = jsonObject.getString("goods_id");
            couponUrl = jsonObject.getString("vocher_url");
            shareTitle = jsonObject.getString("title");
            shareContent = jsonObject.getString("content");
            shareImg = jsonObject.getString("share_img");
            shareUrl = jsonObject.getString("share_url");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initView() {
        fatherView = (RelativeLayout) findViewById(R.id.fatherView);
        titleAll = (RelativeLayout) findViewById(R.id.titleAll);
        title = (TextView)findViewById(R.id.webView_title);
        bottomAll = (LinearLayout) findViewById(R.id.bottomAll);
        bottomAll.setOnClickListener(this);

        tailIcon = (ImageView) findViewById(R.id.tail_icon);
        tailContent1 = (TextView) findViewById(R.id.tail_content1);
        tailContent2 = (TextView) findViewById(R.id.tail_content2);

        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        onShare = (LinearLayout) findViewById(R.id.onShare);
        onShare.setVisibility(View.VISIBLE);
        onShare.setOnClickListener(this);
        if(TextUtils.equals(titleType,"1")){
            title.setText("宝贝详情(天猫)");
        } else {
            title.setText("宝贝详情(淘宝)");
        }
        webView = new WebView(context);
        RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        webParams.addRule(RelativeLayout.BELOW,titleAll.getId());
        webParams.addRule(RelativeLayout.ABOVE,bottomAll.getId());
        webView.setLayoutParams(webParams);
        fatherView.addView(webView);

        webView.setBackgroundColor(ContextCompat.getColor(context,R.color.webBg));
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
        loadData();
    }

    @Override
    protected void createLoading() {
        super.createLoading();
        failureHud = KProgressHUD.create(context).setCustomView(new ImageView(this));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void loadData() {
        if(checkNetworkState(this) != false){
            AlibcShow.showH5(DetailsActivity.this,webView, kitWcClient,new AlibcDetailPage(numId));
            findByShareMsg();
        } else {
            hud.dismiss();
            BaseTools.removeNoneImg(fatherView);
            webView.setVisibility(View.VISIBLE);
        }
        if(TextUtils.equals(couponUrl,"")){
            bottomAll.setVisibility(View.GONE);
        } else {
            if(!TextUtils.equals(couponType,"1")){
                tailsWindow = new TailsWindow(
                        DetailsActivity.this,
                        couponUrl,
                        numId,
                        tailIcon,
                        tailContent1,
                        tailContent2);
                bottomAll.setVisibility(View.VISIBLE);
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        showPopupWindow();
                    }
                });
            } else {
                tailContent2.setVisibility(View.GONE);//隐藏小字
                tailIcon.setVisibility(View.VISIBLE);//显示icon
                tailContent1.setText("立即领劵");
            }
        }
    }

    /**
     * 显示优惠券的弹窗
     */
    private void showPopupWindow() {
        // 显示窗口
        tailsWindow.showAtLocation(webView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) BaseTools.dpChangePx(context,50));
        // 打开窗口时设置背景颜色变暗
//        BaseTools.setBackgroundAlpha(this,0.5f);
    }

    /**
     * 查找商品详情的数据
     */
    private void findByShareMsg() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id", PrefShared.getString(context,"userId"));
        jsonObject.put("num_iid", numId);
        jsonObject.put("type", 1);
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.FIND_BY_DETAILS, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                json = BaseTools.decryptJson(json);
//                Log.e("商品详情",json);
            }
        },parameter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.onBack:
                if(webView.canGoBack()){
                    webView.goBack();
                } else {
                    for(int i = 0;i < activitys.size();i++){
                        String activityName = activitys.get(i).toString();
                        activityName = activityName.substring(activityName.lastIndexOf(".")+1,activityName.indexOf("@"));
                        if(TextUtils.equals(activityName,"MainActivity")){
                            this.finish();
                            break;
                        } else {
                            if(i == activitys.size()-1){
                                startActivity(new Intent(context,MainActivity.class));
                                this.finish();
                                break;
                            }
                        }
                    }
                }
                break;
            case R.id.bottomAll:
                if(!TextUtils.equals(couponType,"1")){
                    String text = tailContent1.getText().toString();
                    if(TextUtils.equals(text,"继续购物>")){
                        tailIcon.setVisibility(View.VISIBLE);//显示icon
                        tailContent2.setVisibility(View.VISIBLE);//显示小字
                        tailContent1.setText("立即领劵");
                    } else {
                        tailIcon.setVisibility(View.GONE);//隐藏icon
                        tailContent2.setVisibility(View.GONE);//隐藏小字
                        tailContent1.setText("继续购物>");
                        showPopupWindow();
                    }
                } else {
                    AlibcShow.showNative(DetailsActivity.this, new AlibcPage(couponUrl));
                }
                break;
            case R.id.onShare:
                new ShareCallback(DetailsActivity.this,hud,failureHud,numId).share(shareTitle,shareContent,shareImg,shareUrl);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(webView.canGoBack()){
                webView.goBack();
            } else {
                for(int i = 0;i < activitys.size();i++){
                    String activityName = activitys.get(i).toString();
                    activityName = activityName.substring(activityName.lastIndexOf(".")+1,activityName.indexOf("@"));
                    if(TextUtils.equals(activityName,"MainActivity")){
                        this.finish();
                        break;
                    } else {
                        if(i == activitys.size()-1){
                            startActivity(new Intent(context,MainActivity.class));
                            this.finish();
                            break;
                        }
                    }
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 淘宝回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);//阿里的回调
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);//友盟精简版的回调
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != tailsWindow){
            if(null != tailsWindow.webView){//销毁WebView
                tailsWindow.webView.removeAllViews();
                tailsWindow.webView.destroy();
            }
        }
        webView.removeAllViews();
        webView.destroy();
        AlibcTradeSDK.destory();
    }
}
