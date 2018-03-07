package com.shhb.gd.shop.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.module.Constants;
import com.shhb.gd.shop.module.PhoneInfo;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;
import com.umeng.message.PushAgent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by superMoon on 2017/3/22.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private TextView title,forPassword,goRegister;
    private LinearLayout onBack;
    private EditText phoneNumber,password;
    private Button userBtn;
    private List<Activity> activitys;
    private String telphoneNum,telPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        activitys = MainApplication.getActivitys();
        initView();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.webView_title);
        title.setText(R.string.login_text);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        password = (EditText) findViewById(R.id.password);
        userBtn = (Button) findViewById(R.id.userBtn);
        userBtn.setOnClickListener(this);
        forPassword = (TextView) findViewById(R.id.forget_password);
        forPassword.setOnClickListener(this);
        goRegister = (TextView) findViewById(R.id.go_register);
        goRegister.setOnClickListener(this);
    }

    @Override
    protected void createLoading() {
        super.createLoading();
        failureHud = KProgressHUD.create(context).setCustomView(new ImageView(this));
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.onBack:
                this.finish();
                break;
            case R.id.userBtn:
                telphoneNum = phoneNumber.getText().toString();
                telPassword = password.getText().toString();
                if(TextUtils.equals(telphoneNum,"")){
                    showToast(1,"请输入手机号码！");
                    new hideThread().start();
                } else {
                    if(BaseTools.isNumeric(telphoneNum) && telphoneNum.length() == 11) {
                        if (TextUtils.equals(telPassword, "")) {
                            showToast(1, "请输入密码！");
                            new hideThread().start();
                        } else {
                            if(6 > telPassword.length()){
                                showToast(1,"密码不能少于6位！");
                                new hideThread().start();
                            } else {
                                showToast(0,"登录中");
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        userLogin();
                                    }
                                }.start();
                            }
                        }
                    } else {
                        showToast(1,"手机号码不正确！");
                        new hideThread().start();
                    }
                }
                break;
            case R.id.forget_password:
                intent = new Intent(this,RegisterActivity.class);
                intent.putExtra("title","修改密码");
                intent.putExtra("type","1");
                startActivity(intent);
                break;
            case R.id.go_register:
                intent = new Intent(this,RegisterActivity.class);
                intent.putExtra("title","注册");
                intent.putExtra("type","0");
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 用户登录
     */
    private void userLogin() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone",telphoneNum);
        jsonObject.put("password",telPassword);
        jsonObject.put("device_token", PushAgent.getInstance(this).getRegistrationId());
        PhoneInfo phoneInfo = new PhoneInfo(context);
        Map<String,Object> map = phoneInfo.getPhoneMsg();
        map.put("address", PrefShared.getString(context,"position"));
        map.put("type","1");
        for(Map.Entry<String, Object> m : map.entrySet()){
            jsonObject.put(m.getKey(),m.getValue());
        }
//        Log.e("登录参数",jsonObject.toString());
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.USER_LOGIN, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast(1,"网络异常！");
                new hideThread().start();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    json = BaseTools.decryptJson(json);
//                    Log.e("用户登录信息",json);
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    String msg = jsonObject.getString("msg");
                    showToast(1,msg);
                    if(status == 1){
                        PrefShared.saveString(context,"phoneNum",telphoneNum);
                        PrefShared.saveString(context,"userId",jsonObject.getString("user_id"));
                        PrefShared.saveString(context,"token",jsonObject.getString("token"));
                        for (int i = 0; i < activitys.size(); i++) {
                            String activityName = activitys.get(i).toString();
                            activityName = activityName.substring(activityName.lastIndexOf(".") + 1, activityName.indexOf("@"));
                            if (TextUtils.equals(activityName, "SetActivity")){
                                SetActivity.tbhandler.sendEmptyMessage(1);
                                Intent intent = new Intent(Constants.SENDMSG_LOGIN);
                                intent.putExtra("userId",jsonObject.getString("user_id"));
                                context.sendBroadcast(intent);
                                break;
                            }
                        }
                        new clearThread().start();
                    } else {
                        new hideThread().start();
                    }
                } catch (Exception e){
                    showToast(1,"登录失败！");
                    new hideThread().start();
                    e.printStackTrace();
                }
            }
        },parameter);
    }

    class clearThread extends Thread {
        public void run() {
            try {
                sleep(1500);
                if(failureHud.isShowing()){
                    failureHud.dismiss();
                }
                LoginActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
