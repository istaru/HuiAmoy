package com.shhb.gd.shop.listener;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.module.Constants;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.ShareBoardConfig;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by superMoon on 2017/3/15.
 */

public class ShareCallback implements UMShareListener {

    private Activity context;
    private KProgressHUD failureHud;
    private KProgressHUD hud;
    private String numId;
    private String type;

    public ShareCallback(Activity context, KProgressHUD hud, KProgressHUD failureHud, String numId){
        this.context = context;
        this.hud = hud;
        this.failureHud = failureHud;
        this.numId = numId;
    }

    public void share(String title,String content,String imgUrl,String shareUrl){
        ShareAction shareAction = new ShareAction(context);
        UMImage imageurl;
        if(TextUtils.equals("",numId)){
            imageurl = new UMImage(context, R.mipmap.ic_launcher);//本地图片
            type = "邀请";
        } else {
            imageurl = new UMImage(context, imgUrl);//网络图片
            type = "分享";
        }
        UMWeb web = new UMWeb(shareUrl);//链接
        web.setTitle(title);//标题
        web.setThumb(imageurl);//图片
        web.setDescription(content);//描述
        shareAction.withMedia(web);
        shareAction.setDisplayList(SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.QQ);;
        shareAction.setCallback(this);//添加回调
        ShareBoardConfig config = new ShareBoardConfig();
        config.setShareboardPostion(ShareBoardConfig.SHAREBOARD_POSITION_BOTTOM);//分享的面板底部显示
        config.setMenuItemBackgroundShape(ShareBoardConfig.BG_SHAPE_NONE);//设置面板的透明度和圆角
        config.setTitleVisibility(true);//隐藏标题
        if(TextUtils.equals("",numId)){//邀请好友
            config.setTitleText("邀请好友轻松赚钱");
        }else {//分享商品
            config.setTitleText("成功分享有返利哟");
        }
        config.setCancelButtonVisibility(true);//底部取消按钮
        config.setShareboardBackgroundColor(ContextCompat.getColor(context, R.color.white));
        shareAction.open(config);
    }

    @Override
    public void onStart(SHARE_MEDIA share_media) {
        showToast(0,"应用启动中");
    }

    @Override
    public void onResult(SHARE_MEDIA share_media) {
        showToast(1,type+"成功");
        new hideThread().start();
        if(null != PrefShared.getString(context,"userId")){
            sendOnResult();
        }
    }

    @Override
    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
        if(throwable.getMessage().contains("没有安装")){//没有安装应用
            showToast(1,"请先安装应用！");
        } else {
            showToast(1,"分享"+type+"！");
        }
        new hideThread().start();
    }

    @Override
    public void onCancel(SHARE_MEDIA share_media) {
        showToast(1,"取消"+type);
        new hideThread().start();
    }

    /**
     * 分享成功的回调
     */
    private void sendOnResult(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id", PrefShared.getString(context,"userId"));
        jsonObject.put("num_iid", numId);
        jsonObject.put("type",1);//表android分享
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.DETAILS_SHARE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Log.e("分享成功回调信息","网络或接口异常");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    json = BaseTools.decryptJson(json);
//                    Log.e("分享成功回调信息",json);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        },parameter);
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
