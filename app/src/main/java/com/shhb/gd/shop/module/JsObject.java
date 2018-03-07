package com.shhb.gd.shop.module;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.shhb.gd.shop.activity.AlibcActivity;
import com.shhb.gd.shop.activity.DetailsActivity;
import com.shhb.gd.shop.activity.LoginActivity;
import com.shhb.gd.shop.activity.MainActivity;
import com.shhb.gd.shop.activity.SetActivity;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;
import com.tencent.smtt.sdk.WebView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by superMoon on 2017/3/15.
 */
public class JsObject {
    private WebView x5WebView;
    private android.webkit.WebView kitWebView;
    private Activity context;
    private int type;

    public JsObject(int type, WebView webView, Activity context) {
        this.type = type;
        this.x5WebView = webView;
        this.context = context;
        if(type == 3 || type == 4){
            initBroadcastReceiver();
        }
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.SENDMSG_LOGIN);
        broadcastReceiver receiver = new broadcastReceiver();
        context.registerReceiver(receiver, intentFilter);
    }

    class broadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId",intent.getStringExtra("userId"));
                callBack("js_returnUserId",jsonObject.toString());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public JsObject(android.webkit.WebView webView, Activity context) {
        this.kitWebView = webView;
        this.context = context;
    }

    /**
     * 加密
     * @param result
     */
    @JavascriptInterface
    public void encode(String result){
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String content = jsonObject.getString("data");
            String callBack = jsonObject.getString("callback");
            callBack(callBack,BaseTools.encodeJson(content));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 解密
     * @param result
     */
    @JavascriptInterface
    public void decode(String result){
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String content = jsonObject.getString("data");
            String callBack = jsonObject.getString("callback");
            callBack(callBack,BaseTools.decryptJson(content));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * JS调登录
     * @param result
     */
    @JavascriptInterface
    public void js_login(String result){
        String userId = PrefShared.getString(context,"userId");
        if(null == userId || TextUtils.equals(userId,"")){
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        }
    }

    /**
     * 显示或隐藏首页的底部Button
     * @param result
     */
    @JavascriptInterface
    public void js_hiddenBottom(String result){
        String type = JSONObject.parseObject(result).getString("type");
        int num;
        if(TextUtils.equals("0",type)){
            num = 0;
        } else {
            num = 1;
        }
        MainActivity.hiddenH.sendEmptyMessage(num);
    }

    /**
     * 是否禁止返回键
     */
    @JavascriptInterface
    public void js_hideswiper_return(String result){
        int type = JSONObject.parseObject(result).getInteger("type");
        MainActivity.isBan = type;
    }

    /**
     * 获取用户ID
     */
    @JavascriptInterface
    public void js_getUserId(String result){
        String callBack = JSONObject.parseObject(result).getString("name");
        String userId = PrefShared.getString(context,"userId");
        JSONObject jsonObject = new JSONObject();
        if(null == userId){
            jsonObject.put("userId","");
        } else {
            jsonObject.put("userId",userId);
        }
        callBack(callBack,jsonObject.toString());
    }

    /**
     * 所有方法的回调
     */
    public void callBack(final String callBack,final String result){
        x5WebView.post(new Runnable() {
            @Override
            public void run() {
                x5WebView.loadUrl("javascript:"+callBack+"(" + result + ");");
            }
        });
    }

    /**
     * 进入设置页面
     */
    @JavascriptInterface
    public void js_set(String result){
        context.startActivity(new Intent(context,SetActivity.class));
    }

    /**
     * 常用工具
     * @param result
     */
    @JavascriptInterface
    public void js_tools(String result){
//        Log.e("进入常用工具",result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String type = jsonObject.getString("type");
        String userId = PrefShared.getString(context,"userId");
        String nick = PrefShared.getString(context,"nick");
        if(TextUtils.equals(type,"orders")){
            Intent intent;
            if(null != userId && !TextUtils.equals(userId,"")){
                if(null != nick && !TextUtils.equals(nick,"")){
                    intent = new Intent(context, AlibcActivity.class);
                    intent.putExtra("type", "order");
                    context.startActivity(intent);
                } else {
                    new AlibcUser(context).login();
                }
            } else {
                intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        } else if(TextUtils.equals(type,"shopping_cart")){
            Intent intent;
            if(null != userId && !TextUtils.equals(userId,"")){
                if(null != nick && !TextUtils.equals(nick,"")){
                    intent = new Intent(context, AlibcActivity.class);
                    intent.putExtra("type", "cart");
                    context.startActivity(intent);
                } else {
                    new AlibcUser(context).login();
                }
            } else {
                intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        } else if(TextUtils.equals(type,"wechat")){
            try {
                Intent intent = new Intent();
                ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");// 报名该有activity
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(cmp);
                context.startActivityForResult(intent, 0);
            } catch (Exception e){
                e.printStackTrace();
                if (e.toString().contains("ActivityNotFoundException")) {
                    Toast.makeText(context,"请先安装微信客户端",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,"无法打开微信客户端",Toast.LENGTH_SHORT).show();
                }
            }
        } else if(TextUtils.equals(type,"QQ")){
            try {
                String qqNum = jsonObject.getString("QQ_num");
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.OPEN_QQ + qqNum)));
            } catch (Exception e) {
                e.printStackTrace();
                if (e.toString().contains("ActivityNotFoundException")) {
                    Toast.makeText(context,"请先安装QQ客户端",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,"无法打开QQ客户端",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 进入商品详情
     * @param result
     */
    @JavascriptInterface
    public void js_startActivity(String result){
//        Log.e("进入商品详情",result);
        Intent intent;
        String userId = PrefShared.getString(context,"userId");
        String nick = PrefShared.getString(context,"nick");
        try {
            if(null != userId && !TextUtils.equals(userId,"")){
                if(null != nick && !TextUtils.equals(nick,"")){
                    intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("result", result);
                    context.startActivity(intent);
                } else {
                    new AlibcUser(context).login();
                }
            } else {
                intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * JS注入拿到购物卷是否失效的值
     * @param result
     */
    @JavascriptInterface
    public void coupon_msg(String result){
        JSONObject json = JSONObject.parseObject(result);
        result = json.getString("msg");
        if(TextUtils.equals(result,"")){//优惠券可以领取
//            Log.e("优惠卷","可用");
        } else {//优惠券不能领取
//            Log.e("优惠卷","失效");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("num_iid", json.getString("url"));
            String parameter = BaseTools.encodeJson(jsonObject.toString());
            OkHttpUtils okHttpUtils = new OkHttpUtils(20);
            okHttpUtils.postEnqueue(Constants.SEND_VOLUME, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    Log.e("发送失效优惠券成功回调","网络或接口异常");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String json = response.body().string();
                        json = BaseTools.decryptJson(json);
//                        Log.e("发送失效优惠券成功回调",json);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            },parameter);
        }
    }

    /**
     * 分享商品或邀请好友
     * @param result
     */
    @JavascriptInterface
    public void js_share(String result){
//        Log.e("分享商品或邀请好友",result);
        Intent intent = new Intent(Constants.SENDMSG_SHARE);
        intent.putExtra("result",result);
        context.sendBroadcast(intent);
    }
}
