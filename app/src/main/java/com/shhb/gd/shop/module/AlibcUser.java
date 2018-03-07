package com.shhb.gd.shop.module;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.ali.auth.third.core.model.Session;
import com.ali.auth.third.login.callback.LogoutCallback;
import com.alibaba.baichuan.android.trade.adapter.login.AlibcLogin;
import com.alibaba.baichuan.android.trade.callback.AlibcLoginCallback;
import com.alibaba.fastjson.JSONObject;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shhb.gd.shop.activity.SetActivity;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by superMoon on 2017/3/15.
 */

public class AlibcUser {
    private Activity context;
    private static KProgressHUD hud;
    private static KProgressHUD failureHud;
    private List<Activity> activitys;

    public AlibcUser(Activity context){
        this.context = context;
        hud = KProgressHUD.create(context);
        failureHud = KProgressHUD.create(context);
        failureHud.setCustomView(new ImageView(context));
        activitys = MainApplication.getActivitys();
    }

    /**
     * 淘宝授权
     */
    public void login(){
        showToast(0,"正在授权");
        AlibcLogin.getInstance().showLogin(context, new AlibcLoginCallback() {
            @Override
            public void onSuccess() {
                Session user = AlibcLogin.getInstance().getSession();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("imei",new PhoneInfo(context).getIMEI());
                jsonObject.put("user_id",PrefShared.getString(context,"userId"));
                jsonObject.put("user_name",user.nick);
                jsonObject.put("user_head_img",user.avatarUrl);
                jsonObject.put("taobao_id",user.openId);
                sendLoginMsg(jsonObject,0);
            }

            @Override
            public void onFailure(int code, String msg) {
                showToast(1,"授权失败！");
                new hideThread().start();
            }
        });
    }

    /**
     * 退出淘宝授权
     */
    public void unLogin() {
        showToast(0,"授权解除中");
        AlibcLogin.getInstance().logout(context, new LogoutCallback() {
            @Override
            public void onSuccess() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user_id", PrefShared.getString(context, "userId"));
                sendLoginMsg(jsonObject, 1);
            }

            @Override
            public void onFailure(int code, String msg) {
                showToast(1,"解除授权失败！");
                new hideThread().start();
            }
        });
    }

    /**
     * 判断是否淘宝授权过
     * @return
     */
    public boolean isLogin(){
        Session session = com.alibaba.baichuan.android.trade.adapter.login.AlibcLogin.getInstance().getSession();
        if(TextUtils.equals(session.openId,"") && TextUtils.equals(session.nick,"") && TextUtils.equals(session.avatarUrl,"")){
            return false;
        } else {
            return true;
        }
    }

    /**
     * 发送淘宝授权、退出授权成功的信息
     * @param jsons
     * @param type 0授权 1退出授权
     */
    private void sendLoginMsg(final JSONObject jsons, final int type){
        final String parameter = BaseTools.encodeJson(jsons.toString());
//        Log.e("授权的参数",parameter);
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.SEND_LOGIN_MSG, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast(1,"网络异常！");
                new hideThread().start();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String msg = "";
                try {
                    String json = response.body().string();
                    json = BaseTools.decryptJson(json);
//                    Log.e("发送淘宝授权、退出授权成功的信息",json);
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    msg = jsonObject.getString("msg");
                    String nick = JSONObject.parseObject(jsons.toString()).getString("user_name");
                    if(status == 1){
                        if(type == 0){
                            PrefShared.saveString(context,"nick",nick);
                        } else {
                            PrefShared.removeData(context,"nick");
                        }
                        Intent intent = new Intent(Constants.SENDMSG_LOGIN);
                        intent.putExtra("userId",PrefShared.getString(context,"userId"));
                        context.sendBroadcast(intent);
                        for (int i = 0; i < activitys.size(); i++) {
                            String activityName = activitys.get(i).toString();
                            activityName = activityName.substring(activityName.lastIndexOf(".") + 1, activityName.indexOf("@"));
                            if (TextUtils.equals(activityName, "SetActivity")){
                                SetActivity.tbhandler.sendEmptyMessage(1);
                                break;
                            }
                        }
                    }
                } catch (Exception e){
                    msg = "授权失败！";
                    e.printStackTrace();
                }
                showToast(1,msg);
                new hideThread().start();
            }
        },parameter);
    }

    /**
     * 显示提示框，0显示普通的loading
     */
    private void showToast(int type,String content){
        if (type == 0) {
            hud.setLabel(content);
            hud.show();
        } else {
            hud.dismiss();
            Message message = new Message();
            message.what = 1;
            message.obj = content;
            showHandler.sendMessage(message);
        }
    }

    /**
     * 显示1秒就消失的提示框
     */
    Handler showHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String content = (String) msg.obj;
            try {
                if(msg.what == 1){
                    failureHud.setLabel(content);
                    failureHud.show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 1.5秒之后关掉提示
     */
    protected class hideThread extends Thread {
        public void run() {
            try {
                sleep(1000);
                failureHud.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
