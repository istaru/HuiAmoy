package com.shhb.gd.shop.activity;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.jaeger.library.StatusBarUtil;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.listener.NetStateChangeObserver;
import com.shhb.gd.shop.listener.NetStateChangeReceiver;
import com.shhb.gd.shop.module.NetworkType;
import com.shhb.gd.shop.view.BlackStatusBar;

/**
 * Created by superMoon on 2017/3/15.
 */

public class BaseActivity extends AppCompatActivity implements NetStateChangeObserver {

    public static Activity context;
    public boolean processFlag = true; //默认可以点击
    public KProgressHUD hud;
    public KProgressHUD failureHud;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
        setBlackStatusBar();
        createLoading();
    }

    /**
     * 设置透明状态栏
     */
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, ContextCompat.getColor(context,R.color.white), 0);
    }

    /**
     * 设置状态栏为黑色图标
     */
    protected void setBlackStatusBar() {
        BlackStatusBar.StatusBarLightMode(this);
    }

    /**
     * 创建自定义的loading
     */
    protected void createLoading() {
        hud = KProgressHUD.create(context);
        hud.setCancellable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        MainApplication.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needRegisterNetworkChangeObserver()) {
            NetStateChangeReceiver.registerObserver(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (needRegisterNetworkChangeObserver()) {
            NetStateChangeReceiver.unregisterObserver(this);
        }
    }

    /**
     * 检测网络是否连接
     * @return
     */
    public boolean checkNetworkState(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 是否需要注册网络变化的Observer,如果不需要监听网络变化,则返回false;否则返回true.默认返回false
     */
    protected boolean needRegisterNetworkChangeObserver() {
        return true;
    }

    @Override
    public void onNetDisconnected() {
    }

    @Override
    public void onNetConnected(NetworkType networkType) {
    }

    /**
     * 设置按钮在短时间内被重复点击的有效标识（true表示点击有效，false表示点击无效）
     */
    protected synchronized void setProcessFlag() {
        processFlag = false;
    }

    /**
     * 计时线程（防止在一定时间段内重复点击按钮）
     */
    protected class TimeThread extends Thread {
        public void run() {
            try {
                sleep(1000);
                processFlag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 显示提示框，0显示普通的loading
     */
    public void showToast(int type,String content){
        if (type == 0) {
            hud.setLabel(content);
            hud.show();
        } else {
            if(hud.isShowing()){
                hud.dismiss();
            }
            Message message = new Message();
            message.what = 1;
            message.obj = content;
            showHandler.sendMessage(message);
        }
    }

    /**
     * 关闭普通的loading
     */
    public void closeToast(){
        if(hud.isShowing()){
            hud.dismiss();
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
    public class hideThread extends Thread {
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
