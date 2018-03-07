package com.shhb.gd.shop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ali.auth.third.ui.context.CallbackContext;
import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.module.AlibcUser;
import com.shhb.gd.shop.module.Constants;
import com.shhb.gd.shop.tools.PrefShared;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by superMoon on 2017/3/24.
 */

public class SetActivity extends BaseActivity implements View.OnClickListener{
    private LinearLayout setMain;
    private TextView title;
    private LinearLayout onBack;
    private RelativeLayout set1,set2,set3,set4;
    private static TextView set1_1;
    private static TextView set1_2;
    private static TextView set2_1,set2_2;
    private TextView set3_1;
    private TextView set3_2;
    private static String phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_activity);
        initView();
    }

    private void initView() {
        setMain = (LinearLayout) findViewById(R.id.setMain);
        title = (TextView) findViewById(R.id.webView_title);
        title.setText(R.string.set_text);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        set1 = (RelativeLayout) findViewById(R.id.set1);
        set1.setOnClickListener(this);
        set2 = (RelativeLayout) findViewById(R.id.set2);
        set2.setOnClickListener(this);
        set3 = (RelativeLayout) findViewById(R.id.set3);
        set3.setOnClickListener(this);
        set4 = (RelativeLayout) findViewById(R.id.set4);
        set4.setOnClickListener(this);
        set1_1 = (TextView) findViewById(R.id.set1_1);
        set1_2 = (TextView) findViewById(R.id.set1_2);
        set2_1 = (TextView) findViewById(R.id.set2_1);
        set2_2 = (TextView) findViewById(R.id.set2_2);
        tbhandler.sendEmptyMessage(1);
        set3_1 = (TextView) findViewById(R.id.set3_1);
        set3_2 = (TextView) findViewById(R.id.set3_2);
    }

    /**
     * 设置淘宝登录的文字和颜色
     */
    public static Handler tbhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                phoneNum = PrefShared.getString(context,"phoneNum");
                String nickName = PrefShared.getString(context,"nick");
                if(null != nickName && !TextUtils.equals(nickName,"")){
                    set2_1.setText("解除绑定");
                    set2_1.setTextColor(Color.parseColor("#FB3636"));
                    set2_2.setText("淘宝账号(" + nickName + ")");
                } else {
                    set2_1.setText("绑定淘宝");
                    set2_1.setTextColor(Color.parseColor("#4D4D4D"));
                    set2_2.setText("请绑定淘宝");
                }
                if(null != phoneNum && !TextUtils.equals(phoneNum,"")){
                    set1_1.setText("已登录");
                    set1_2.setText(phoneNum);
                } else {
                    set1_1.setText("登录");
                    set1_2.setText("");
                }
            }
        }
    };

    @Override
    protected void createLoading() {
        super.createLoading();
        hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.onBack:
                this.finish();
                break;
            case R.id.set1:
                phoneNum = PrefShared.getString(context,"phoneNum");
                if(null == phoneNum || TextUtils.equals(phoneNum,"")){
                    set1.setEnabled(true);
                    startActivity(new Intent(this,LoginActivity.class));
                }
                break;
            case R.id.set2:
                String userId = PrefShared.getString(context,"userId");
                if(null != userId && !TextUtils.equals(userId,"")){
                    String nickName = PrefShared.getString(context,"nick");
                    if(null != nickName && !TextUtils.equals(nickName,"")){
                        new AlibcUser(this).unLogin();
                    } else {
                        new AlibcUser(this).login();
                    }
                } else {
                    startActivity(new Intent(this,LoginActivity.class));
                }
                break;
            case R.id.set3:
                Intent intent = new Intent(this,RegisterActivity.class);
                intent.putExtra("title","修改密码");
                intent.putExtra("type","1");
                startActivity(intent);
                break;
            case R.id.set4:
                unLogin();
                break;
            default:
                break;
        }
    }

    /**
     * 退出用户和淘宝登录
     */
    private void unLogin() {
        final MaterialDialog mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle("温馨提示");
        mMaterialDialog.setMessage("确认退出当前账号的所有信息？");
        mMaterialDialog.setPositiveButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        PrefShared.removeData(context,"phoneNum");
                        PrefShared.removeData(context,"userId");
                        PrefShared.removeData(context,"nick");
                        PrefShared.removeData(context,"position");
                        tbhandler.sendEmptyMessage(1);
                        Intent intent = new Intent(Constants.SENDMSG_LOGIN);
                        intent.putExtra("userId","");
                        context.sendBroadcast(intent);
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
        mMaterialDialog.show();
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
    }

    @Override
    protected void onDestroy() {
        AlibcTradeSDK.destory();
        super.onDestroy();
    }
}
