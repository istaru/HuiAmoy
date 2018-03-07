package com.shhb.gd.shop.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import me.drakeet.materialdialog.MaterialDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by superMoon on 2017/3/23.
 */

public class RegisterActivity extends BaseActivity implements View.OnClickListener{
    /** (公共)标题 */
    private String titleText;
    /** (公共)页面的类型0注册 */
    private int type;
    /** (公共)显示标题的View */
    private TextView title;
    /** (公共)获取验证码的View */
    private TextView getCode;
    /** (公共)返回按钮View */
    private LinearLayout onBack;
    /** (公共)输入密码的View */
    private EditText password;
    /** (公共)输入验证码的View */
    private EditText codeEdt;
    /** (公共)验证码父View */
    private RelativeLayout code;
    /** (公共)确认按钮View */
    private Button userBtn;

    /** (注册)忘记密码的View */
    private TextView forPassword;
    /** (注册)去注册的View */
    private TextView goRegister;
    /** （注册）惠淘协议的父View */
    private LinearLayout user_pro;
    /** (注册)惠淘协议View */
    private TextView goProtocol;
    /** (注册)输入手机号码的父View */
    private LinearLayout phone_n_main;
    /** (注册)输入手机号码的View */
    private EditText phoneNumber;
    /** (注册)忘记密码/去注册的父View */
    private RelativeLayout fr_main;

    /** (修改)修改手机号父View */
    private RelativeLayout set_text_main;
    /** (修改)修改手机号的View */
    private EditText set_number;

    /**  获取验证码的倒计时 */
    private int duration;
    /** 每隔1000 毫秒执行一次 */
    private static final int delayTime = 1000;
    private List<Activity> activitys;
    private String PrePhoneNum;
    /** 2是修改密码 1是注册 */
    private int goType;
    /** 手机号、密码、验证码 */
    private String telphoneNum,telPassword,telCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        titleText = getIntent().getStringExtra("title");
        type = Integer.parseInt(getIntent().getStringExtra("type"));
        activitys = MainApplication.getActivitys();
        initView();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.webView_title);
        title.setText(titleText);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);

        set_text_main = (RelativeLayout) findViewById(R.id.set_text_main);
        set_number = (EditText) findViewById(R.id.set_number);
        phone_n_main = (LinearLayout) findViewById(R.id.phone_n_main);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        password = (EditText) findViewById(R.id.password);
        code = (RelativeLayout) findViewById(R.id.code);
        code.setVisibility(View.VISIBLE);
        codeEdt = (EditText) findViewById(R.id.code_edt);
        getCode = (TextView) findViewById(R.id.get_code);
        getCode.setOnClickListener(this);
        userBtn = (Button) findViewById(R.id.userBtn);
        userBtn.setOnClickListener(this);
        user_pro = (LinearLayout) findViewById(R.id.user_pro);
        goProtocol = (TextView) findViewById(R.id.go_protocol);
        goProtocol.setOnClickListener(this);
        fr_main = (RelativeLayout) findViewById(R.id.fr_main);
        forPassword = (TextView) findViewById(R.id.forget_password);
        forPassword.setVisibility(View.GONE);
        goRegister = (TextView) findViewById(R.id.go_register);
        goRegister.setText(R.string.btn_login_text);
        goRegister.setOnClickListener(this);
        if(type == 0){
            goType = 1;
            phone_n_main.setVisibility(View.VISIBLE);
            password.setHint(R.string.set_password_text);
            userBtn.setText(R.string.registered_text);
            user_pro.setVisibility(View.VISIBLE);
            fr_main.setVisibility(View.VISIBLE);
        } else {
            goType = 2;
            phone_n_main.setVisibility(View.GONE);
            set_text_main.setVisibility(View.VISIBLE);
            PrePhoneNum = PrefShared.getString(context,"phoneNum");
            if(null == PrePhoneNum){
                set_number.setEnabled(true);
            } else {
                set_number.setEnabled(false);
                set_number.setText(PrePhoneNum);
            }
            password.setHint("请输入新的密码");
            userBtn.setText("确认");
            user_pro.setVisibility(View.GONE);
            fr_main.setVisibility(View.GONE);
        }
    }

    @Override
    protected void createLoading() {
        super.createLoading();
        failureHud = KProgressHUD.create(context).setCustomView(new ImageView(this));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.onBack:
                showDialog();
                break;
            case R.id.get_code:
                if(goType == 1){
                    telphoneNum = phoneNumber.getText().toString();
                } else {
                    telphoneNum = set_number.getText().toString();
                }
                if(TextUtils.equals(telphoneNum,"")){
                    showToast(1,"请输入手机号码！");
                    new hideThread().start();
                } else {
                    if(BaseTools.isNumeric(telphoneNum) && telphoneNum.length() == 11) {
                        showToast(0,"请求中");
                        if(getCode.isEnabled()){
                            getVCode();
                        }
                    } else {
                        showToast(1,"手机号码不正确！");
                        new hideThread().start();
                    }
                }
                break;
            case R.id.go_register:
                for(int i = 0;i < activitys.size();i++){
                    String activityName = activitys.get(i).toString();
                    activityName = activityName.substring(activityName.lastIndexOf(".")+1,activityName.indexOf("@"));
                    if(TextUtils.equals(activityName,"LoginActivity")){
                        this.finish();
                        break;
                    } else {
                        if(i == activitys.size()-1){
                            startActivity(new Intent(context,LoginActivity.class));
                            this.finish();
                            break;
                        }
                    }
                }
                break;
            case R.id.userBtn:
                if(goType == 1){
                    telphoneNum = phoneNumber.getText().toString();
                } else {
                    telphoneNum = set_number.getText().toString();
                }
                telPassword = password.getText().toString();
                telCode = codeEdt.getText().toString();
                if(TextUtils.equals(telphoneNum,"")){
                    showToast(1,"请输入手机号码！");
                    new hideThread().start();
                } else {
                    if(BaseTools.isNumeric(telphoneNum) && telphoneNum.length() == 11) {
                        if(TextUtils.equals("",telCode)){
                            showToast(1,"请输入验证码！");
                            new hideThread().start();
                        } else {
                            if (TextUtils.equals(telPassword, "")) {
                                showToast(1, "请输入密码！");
                                new hideThread().start();
                            } else {
                                if(6 > telPassword.length()){
                                    showToast(1,"密码不能少于6位！");
                                    new hideThread().start();
                                } else {
                                    showToast(0,"请求中");
                                    new Thread(){
                                        @Override
                                        public void run() {
                                            super.run();
                                            userRegister();
                                        }
                                    }.start();
                                }
                            }
                        }
                    } else {
                        showToast(1,"手机号码不正确！");
                        new hideThread().start();
                    }
                }
                break;
            case R.id.go_protocol:
                startActivity(new Intent(context,ProtocolActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        showDialog();
        return super.onKeyDown(keyCode, event);
    }

    private void showDialog(){
        if(0 != duration){
            final MaterialDialog mMaterialDialog = new MaterialDialog(this);
            mMaterialDialog.setTitle("温馨提示");
            mMaterialDialog.setMessage("获取的验证码会根据网络状况有所延迟，要不再等等？");
            mMaterialDialog.setPositiveButton("再等等", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMaterialDialog.dismiss();
                }
            }).setNegativeButton("算了", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMaterialDialog.dismiss();
                    handler.removeCallbacks(timerRunnable);
                    RegisterActivity.this.finish();
                }
            });
            mMaterialDialog.show();
        } else {
            RegisterActivity.this.finish();
        }
    }

    /**
     * 获取验证码
     */
    private void getVCode() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone",telphoneNum);
        jsonObject.put("type",goType);
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.GET_VCODE, new Callback() {
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
//                    Log.e("获取验证码信息",json);
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    if(status == 1){
                        duration = 300;
                        handler.postDelayed(timerRunnable, delayTime);
                    }
                    msg = jsonObject.getString("msg");
                } catch (Exception e){
                    msg = "获取验证码失败！";
                    e.printStackTrace();
                }
                showToast(1,msg);
                new hideThread().start();
            }
        },parameter);
    }

    /**
     * 用户修改密码或注册
     */
    private void userRegister() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone",telphoneNum);
        jsonObject.put("password",password.getText().toString());
        jsonObject.put("device_token", PushAgent.getInstance(this).getRegistrationId());
        jsonObject.put("id_code",codeEdt.getText().toString());
        PhoneInfo phoneInfo = new PhoneInfo(context);
        Map<String,Object> map = phoneInfo.getPhoneMsg();
        map.put("address", PrefShared.getString(context,"position"));
        map.put("type","1");
        for(Map.Entry<String, Object> m : map.entrySet()){
            jsonObject.put(m.getKey(),m.getValue());
        }
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        String btnUrl;
        if(goType == 1){
            btnUrl = Constants.USER_REGISTER;
        } else {
            btnUrl = Constants.USER_LOGIN;
        }
        okHttpUtils.postEnqueue(btnUrl, new Callback() {
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
//                    Log.e("用户修改密码或注册信息",json);
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    msg = jsonObject.getString("msg");
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
                    if(goType == 1){
                        msg = "注册失败！";
                    } else {
                        msg = "修改密码失败！";
                    }
                    showToast(1,msg);
                    new hideThread().start();
                    e.printStackTrace();
                }
            }
        },parameter);
    }

    /**
     * 倒计时的计时器
     */
    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.e("获取验证码的线程",duration+"");
            if (0 == duration) {
                getCode.setTextColor(ContextCompat.getColor(context,R.color.blue));
                getCode.setText(R.string.get_code_text);
                getCode.setEnabled(true);
                handler.removeCallbacks(timerRunnable);
                duration = 300;
                return;
            } else {
                getCode.setEnabled(false);
                setDuration(--duration);
            }
            handler.postDelayed(timerRunnable, delayTime);
        }
    };

    /**
     * 显示倒计时
     * @param duration
     */
    private void setDuration(Integer duration) {
        getCode.setTextColor(ContextCompat.getColor(context,R.color.btn_select));
        getCode.setText(duration + getResources().getString(R.string.rest_get_code_text));
    }

    class clearThread extends Thread {
        public void run() {
            try {
                sleep(1500);
                if(failureHud.isShowing()){
                    failureHud.dismiss();
                }
                handler.removeCallbacks(timerRunnable);
                RegisterActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
