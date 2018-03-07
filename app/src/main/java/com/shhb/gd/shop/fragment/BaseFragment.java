package com.shhb.gd.shop.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.widget.RelativeLayout;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.tencent.smtt.sdk.WebView;

/**
 * Created by superMoon on 2017/3/15.
 */

public class BaseFragment extends Fragment {
    public Activity context;
    public KProgressHUD hud;
    public KProgressHUD failureHud;
    public String str;
    public RelativeLayout fatherView;
    public WebView webView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = getActivity();
        createLoading();
    }

    /**
     * 创建自定义的loading
     */
    protected void createLoading() {
        hud = KProgressHUD.create(context);
        hud.setCancellable(true);
    }

    /**
     * 显示提示框，0显示普通的loading
     */
    public void showToast(int type,String content){
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
