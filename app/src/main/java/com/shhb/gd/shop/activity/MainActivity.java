package com.shhb.gd.shop.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.auth.third.ui.context.CallbackContext;
import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.jaeger.library.StatusBarUtil;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.adapter.MainAdapter;
import com.shhb.gd.shop.adapter.PageAdapter;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.fragment.BaseFragment;
import com.shhb.gd.shop.fragment.Fragment1;
import com.shhb.gd.shop.fragment.Fragment2;
import com.shhb.gd.shop.fragment.Fragment3;
import com.shhb.gd.shop.fragment.Fragment4;
import com.shhb.gd.shop.listener.ShareCallback;
import com.shhb.gd.shop.module.Constants;
import com.shhb.gd.shop.module.NetworkType;
import com.shhb.gd.shop.module.PhoneInfo;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;
import com.shhb.gd.shop.view.CustomViewPager;
import com.umeng.message.PushAgent;
import com.umeng.socialize.UMShareAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.shhb.gd.shop.module.Constants.HTML_REQUEST;

/**
 * Created by superMoon on 2017/3/15.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private long mExitTime;
    private View mViewNeedOffset;
    private boolean flag;
    private RelativeLayout mainView;
    private static RelativeLayout homeView;
    /** 底部按钮的ViewPager */
    private static CustomViewPager buttonView;
    /** 启动页的ViewPager*/
    private CustomViewPager pageView;
    /** 本地保存的次数 */
    private int getFrequency;
    /** 本地保存的版本 */
    private int getOpenver;
    /** 查到的倒计时 */
    private int duration;
    /** 查到的版本 */
    private int openver;
    /** 查到的次数 */
    private int frequency;
    private AlphaAnimation start_anima;
    /** 每隔1000 毫秒执行一次*/
    private static final int delayTime = 1000;
    /** 跳过*/
    private TextView cancel,countdown;
    /** 去掉首页ViewPager点击动画*/
    private boolean isWaitingExit = false;
    private MainAdapter viewPagerAdapter;
    private ArrayList<Fragment> fragments;
    private static Fragment1 fragment1;
    private static Fragment2 fragment2;
    private static Fragment3 fragment3;
    private static Fragment4 fragment4;
    private static String webViewUrl;
    private static BottomNavigationBar navigationBar;
    /** 声明AMapLocationClient类对象*/
    private AMapLocationClient mLocationClient = null;
    /** 声明AMapLocationClientOption对象*/
    private AMapLocationClientOption mLocationOption = null;
    /** 手机位置信息*/
    private String addres = "";
    /** Fragment中单击分享或WebView加载完毕的广播接收器*/
    private ShareOrShowBReceiver shareOrShowBR;
    /** 是否禁止返回键0：不禁止*/
    public static int isBan = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//Android6.0以上的系统
            int sdPermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);//获取手机信息的权限
            if(sdPermission != PackageManager.PERMISSION_GRANTED){//还没有获取获取手机信息的权限
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, Constants.PHONE_CODE);
            } else {
                initLocation();
                findBanner();
            }
        } else {
            initLocation();
            findBanner();
        }
    }

    @Override
    public void onNetConnected(NetworkType networkType) {
        super.onNetConnected(networkType);
        BaseTools.removeNoneImg(mainView);
        buttonView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNetDisconnected() {
        super.onNetDisconnected();
        buttonView.setVisibility(View.GONE);
        BaseTools.addNoneImg(mainView,this);
    }

    /***
     * 如果是6.0的系统就获取权限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PHONE_CODE:
                if(permissions[0].equals(Manifest.permission.READ_PHONE_STATE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意读取手机信息权限
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.SD_CODE);//SD权限
                } else {
                    showAlertDialog(requestCode);
                }
                break;
            case Constants.SD_CODE:
                if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意SD读写权限
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},Constants.LOCATION_CODE);//定位权限
                    findBanner();
                } else {
                    showAlertDialog(requestCode);
                }
                break;
            case Constants.LOCATION_CODE:
                initLocation();
                break;
            default:
                break;
        }
    }

    /**
     * 开始定位
     */
    private void initLocation(){
        long yqTime = PrefShared.getLong(context,"yqTime");
        String current = (System.currentTimeMillis())+"";
        current = current.substring(0,10);
        long xzTime = Long.parseLong(current);
        long s = (xzTime - yqTime) / 60;
        if(s > 10){//十分钟进行一次定位
            mLocationClient = new AMapLocationClient(getApplicationContext());//初始化定位
            mLocationClient.setLocationListener(mLocationListener);//设置定位回调监听
            mLocationOption = new AMapLocationClientOption();//初始化AMapLocationClientOption对象
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式。
            mLocationOption.setOnceLocation(true);//获取一次定位结果：该方法默认为false。
            mLocationOption.setOnceLocationLatest(true);//获取最近3s内精度最高的一次定位结果
            mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
            mLocationClient.startLocation();//启动定位
        }
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    double latitude = aMapLocation.getLatitude();//获取纬度
                    double longitude = aMapLocation.getLongitude();//获取经度
                    float accuracy = aMapLocation.getAccuracy();//获取精度信息
                    String address = aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    String country = aMapLocation.getCountry();//国家信息
                    String province = aMapLocation.getProvince();//省信息
                    String city = aMapLocation.getCity();//城市信息
                    String distric = aMapLocation.getDistrict();//城区信息
                    String street = aMapLocation.getStreet();//街道信息
                    String streetNum = aMapLocation.getStreetNum();//街道门牌号信息
                    String cityCode = aMapLocation.getCityCode();//城市编码
                    String adCode = aMapLocation.getAdCode();//地区编码
                    String aoiName = aMapLocation.getAoiName();//获取当前定位点的AOI信息
                    addres = country + province + city + distric + street + streetNum + aoiName;
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    addres = aMapLocation.getErrorCode()+"";
                }
                PrefShared.saveString(context,"position",addres);
                String current = (System.currentTimeMillis())+"";
                current = current.substring(0,10);
                long xzTime = Long.parseLong(current);
                PrefShared.saveLong(context,"yqTime",xzTime);
            }
        }
    };

    /**
     * 弹出提示框
     * @param requestCode
     */
    private void showAlertDialog(final int requestCode){
        final MaterialDialog mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle("温馨提示");
        mMaterialDialog.setMessage("仅在您同意的情况下，可以使用本应用的所有服务。");
        mMaterialDialog.setPositiveButton("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (requestCode){
                    case Constants.SD_CODE:
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
                        break;
                    case Constants.PHONE_CODE:
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},requestCode);
                        break;
                    default:
                        break;
                }
                mMaterialDialog.dismiss();
            }
        });
        mMaterialDialog.show();
    }

    private void init(){
        homeView.setVisibility(View.VISIBLE);
        initBtn();
        initFragments();
        setCurrentPage(0);
        if(null != PrefShared.getString(context,"token")){
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    sendLoginLog();
                }
            }.start();
        }
        intiShareBReceiver();
    }

    @Override
    protected void createLoading() {
        super.createLoading();
        failureHud = KProgressHUD.create(context).setCustomView(new ImageView(context));
    }

    private void initView() {
        mViewNeedOffset = findViewById(R.id.view_need_offset);
        mainView = (RelativeLayout) findViewById(R.id.main);
        homeView = (RelativeLayout) findViewById(R.id.homeView);
        buttonView = (CustomViewPager) findViewById(R.id.viewPager);
        pageView = new CustomViewPager(context);
        buttonView.setScanScroll(false);
        buttonView.setOffscreenPageLimit(4);
        navigationBar = (BottomNavigationBar) findViewById(R.id.navigation_bar);
    }

    /**
     * 查找欢迎页或者广告
     */
    private void findBanner() {
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        JSONObject jsonObject = new JSONObject();
        if(null != PrefShared.getString(context,"userId")){
            jsonObject.put("user_id",PrefShared.getString(context,"userId"));
        }
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        okHttpUtils.postEnqueue(Constants.FIND_BY_IMG, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mianHandler.sendEmptyMessage(1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject jsObject = new JSONObject();
                try {
                    String json = response.body().string();
                    json = BaseTools.decryptJson(json);
//                    Log.e("查找欢迎页或者广告信息",json);
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    if(status == 1){
                        JSONArray jsArray = jsonObject.getJSONArray("data");
                        PrefShared.saveString(context,"phoneNum",jsonObject.getString("phone"));
                        jsObject.put("countdown",jsonObject.getInteger("countdown"));//倒计时
                        jsObject.put("frequency",jsonObject.getInteger("frequency"));//显示的次数
                        jsObject.put("openver",jsonObject.getInteger("openver"));//版本
                        jsObject.put("data",jsArray);//图片
                        Message msg = new Message();
                        msg.obj = jsObject;
                        msg.what = 0;
                        mianHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    mianHandler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }, parameter);
    }

    Handler mianHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                JSONObject jsObject = (JSONObject) msg.obj;
                addUi(jsObject);
            } else {
                init();
            }
        }
    };

    /**
     * 显示欢迎页或者广告页
     * @param jsObject
     */
    private void addUi(JSONObject jsObject){
        pageView.setScanScroll(false);
        List<ImageView> imgs = new ArrayList<>();
        openver = jsObject.getInteger("openver");
        frequency = jsObject.getInteger("frequency");
        getFrequency = PrefShared.getInt(context,"banner3");
        getOpenver = PrefShared.getInt(context,"openver");
        if(getOpenver != openver){
            runNetMain(jsObject,imgs);
        } else {
            if(getFrequency != frequency){
                runNetMain(jsObject,imgs);
            } else {
                runMain(imgs);
            }
        }
    }

    /**
     *  显示网络图片
     * @param jsObject,imgs
     */
    private void runNetMain(JSONObject jsObject,List<ImageView> imgs){
        duration = jsObject.getInteger("countdown");
        JSONArray jsArray = jsObject.getJSONArray("data");
        final int size = jsArray.size();
        pageView.setOffscreenPageLimit(size);
        for (int i = 0;i < size;i++){
            ImageView imageView = new ImageView(MainActivity.this);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            String url = jsArray.getJSONObject(i).getString("icon_url");
            final int finalI = i;
            Glide.with(context)
                    .load(url)//加载地址
                    .placeholder(R.mipmap.welcome1_1)//加载中的图片
                    .listener(requestListener)
                    .error(R.mipmap.welcome1_1)//加载出错的图片
                    .priority(Priority.HIGH)//优先加载
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//设置缓存策略
                    .into(new GlideDrawableImageViewTarget(imageView){
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            if(finalI == 0){
                                if(size > 1){
                                    flag = true;
                                    pageView.setScanScroll(true);//ViewPager禁止滑动
                                } else {
                                    pageView.setScanScroll(true);//ViewPager可以滑动
                                    countdown = new TextView(MainActivity.this);
                                    countdown.setTextColor(ContextCompat.getColor(context,R.color.white));
                                    countdown.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                                    int countdownWidth = BaseTools.getWindowsWidth(MainActivity.this) / 8;
                                    RelativeLayout.LayoutParams countdownParams = new RelativeLayout.LayoutParams(countdownWidth,countdownWidth);
                                    countdownParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                    countdownParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                    int statusBarHeight = PrefShared.getInt(context,"statusBarHeight");
                                    countdownParams.setMargins(0,statusBarHeight,statusBarHeight,0);
                                    countdown.setLayoutParams(countdownParams);
                                    countdown.setBackgroundResource(R.drawable.dis_view_bg);
                                    countdown.setGravity(Gravity.CENTER);
                                    mainView.addView(countdown);
                                    countdown.setOnClickListener(countdownListener);
                                    setDuration(duration);
                                    handler.postDelayed(timerRunnable, delayTime);
                                }
                                init();
                            }
                        }
                    });
            imgs.add(imageView);
        }
        PageAdapter pageAdapter = new PageAdapter(imgs);
        pageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        pageView.setAdapter(pageAdapter);
        pageView.addOnPageChangeListener(pageViewListener);
        mainView.addView(pageView);
    }

    /**
     *  显示默认图片
     * @param imgs
     */
    private void runMain(List<ImageView> imgs){
        init();
        ImageView imageView = new ImageView(MainActivity.this);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageResource(R.mipmap.welcome1_1);
        imgs.add(imageView);
        PageAdapter pageAdapter = new PageAdapter(imgs);
        pageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        pageView.setAdapter(pageAdapter);
        pageView.addOnPageChangeListener(pageViewListener);
        mainView.addView(pageView);
        intiShowBReceiver();
    }

    /**
     * 创建显示主页面的监听
     */
    private void intiShowBReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.SENDMSG_SHOW);
        shareOrShowBR = new ShareOrShowBReceiver();
        registerReceiver(shareOrShowBR, intentFilter);
    }

    /**
     * 监听图片出错时的处理
     */
    private RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            init();
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    /**
     * 关闭欢迎页的线程
     */
    Handler pageHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mainView.removeView(cancel);
            mainView.removeView(pageView);
            if(msg.what == 1){
                addPrefShared();
            }
        }
    };

    private void addPrefShared(){
        getFrequency = PrefShared.getInt(context,"banner3");
        getOpenver = PrefShared.getInt(context,"openver");
        if(getOpenver != openver){
            PrefShared.saveInt(context,"openver",openver);
            PrefShared.removeData(context,"banner3");
            if(getFrequency != frequency){
                getFrequency++;
                PrefShared.saveInt(context,"banner3",getFrequency);
            }
        } else {
            if(getFrequency != frequency){
                getFrequency++;
                PrefShared.saveInt(context,"banner3",getFrequency);
            }
        }
    }

    /**
     * 倒计时的计时器
     */
    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (1 == duration) {//当及时
                mainView.removeView(countdown);
                mainView.removeView(pageView);
                handler.removeCallbacks(timerRunnable);
                addPrefShared();
                return;
            } else {
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
        countdown.setText(String.format("跳过\n%d s", duration));
    }

    View.OnClickListener countdownListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            duration = 1;
        }
    };

    View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pageHandler.sendEmptyMessage(1);
        }
    };

    ViewPager.OnPageChangeListener pageViewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
//                    flag= false;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
//                    flag = true;
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    if(flag){
                        if (pageView.getCurrentItem() == pageView.getAdapter().getCount() - 1) {
                            cancel = new TextView(MainActivity.this);
                            cancel.setText("进入应用");
                            cancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                            cancel.setTextColor(ContextCompat.getColor(context, R.color.white));
                            int cancelWidth = (int) (BaseTools.getWindowsWidth(MainActivity.this) / 6);
                            RelativeLayout.LayoutParams cancelParams = new RelativeLayout.LayoutParams(cancelWidth, cancelWidth);
                            cancelParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            cancelParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            cancelParams.setMargins(0, 0, (int) BaseTools.pxChangeDp(context, 60), (int) BaseTools.pxChangeDp(context, 60));
                            cancel.setLayoutParams(cancelParams);
                            cancel.setBackgroundResource(R.drawable.dis_view_bg);
                            cancel.setGravity(Gravity.CENTER);
                            mainView.addView(cancel);
                            cancel.setOnClickListener(cancelListener);
                            flag = false;
                        }
                    }
                    break;
            }
        }
    };

    /**
     * 初始化底部菜单按钮
     */
    private void initBtn() {
        navigationBar
                .setInActiveColor(R.color.gray)//设置未选中的Item的颜色，包括图片和文字
                .setActiveColor(R.color.btn_unselect)////设置选中的Item的颜色，包括图片和文字
                .setMode(BottomNavigationBar.MODE_FIXED)//没有切换动画且都有文字（MODE_SHIFTING:换挡模式;MODE_DEFAULT）     如果设置的Mode为MODE_FIXED，将使用BACKGROUND_STYLE_STATIC 。
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)//（RIPPLE：波纹动画、STATIC：没有波纹动画） 如果Mode为MODE_SHIFTING将使用BACKGROUND_STYLE_RIPPLE。
                .setBarBackgroundColor(R.color.white);//设置navigationBar的背景颜色
//        BadgeItem badge = new BadgeItem()
//                .setBorderWidth(2)//Badge的Border(边界)宽度
//                .setBorderColor("#FF0000")//Badge的Border颜色
//                .setBackgroundColor(ContextCompat.getColor(context,R.color.btn_unselect))//Badge背景颜色
//                .setGravity(Gravity.RIGHT| Gravity.TOP)//位置，默认右上角
//                .setText("2")//显示的文本
//                .setTextColor(ContextCompat.getColor(context,R.color.white))//文本颜色
//                .setAnimationDuration(1000)
//                .setHideOnSelect(true);//当选中状态时消失，非选中状态显示
        navigationBar
                .addItem(new BottomNavigationItem(R.mipmap.btn_home_on,"抢购").setInactiveIconResource(R.mipmap.btn_home))
                .addItem(new BottomNavigationItem(R.mipmap.btn_9_on,"9块9").setInactiveIconResource(R.mipmap.btn_9))
                .addItem(new BottomNavigationItem(R.mipmap.btn_share_on,"邀请赚").setInactiveIconResource(R.mipmap.btn_share))
                .addItem(new BottomNavigationItem(R.mipmap.btn_me_on,"我的").setInactiveIconResource(R.mipmap.btn_me))
                .initialise();
        navigationBar.setTabSelectedListener(tabSelectedListener);
    }

    /**
     * 显示或隐藏底部Button的线程
     */
    public static Handler hiddenH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                navigationBar.setVisibility(View.GONE);
                navigationBar.hide();
            } else {
                navigationBar.setVisibility(View.VISIBLE);
                navigationBar.show();
            }
            int item = buttonView.getCurrentItem();
            if (item == 0) {
                webViewUrl = fragment1.webView.getUrl();
            } else if (item == 1) {
                webViewUrl = fragment2.webView.getUrl();
            } else if (item == 2) {
                webViewUrl = fragment3.webView.getUrl();
            } else if (item == 3) {
                webViewUrl = fragment4.webView.getUrl();
            }
        }
    };

    /**
     * 底部菜单按钮滑动事件
     */
    private BottomNavigationBar.OnTabSelectedListener tabSelectedListener = new BottomNavigationBar.OnTabSelectedListener() {
        @Override
        public void onTabSelected(int position) {//未选中 -> 选中
            buttonView.setCurrentItem(position,isWaitingExit);
        }

        @Override
        public void onTabUnselected(int position) {//选中 -> 未选中

        }

        @Override
        public void onTabReselected(int position) {//选中 -> 选中

        }
    };

    /**
     * 初始化Fragment
     */
    private void initFragments() {
        fragments = new ArrayList<>();
        fragment1 = new Fragment1(HTML_REQUEST + "index.html");
        fragment2 = new Fragment2(HTML_REQUEST + "page/9_9.html");
        fragment3 = new Fragment3(HTML_REQUEST + "invitation.html");
        fragment4 = new Fragment4(HTML_REQUEST + "page/my.html");
        fragments.add(fragment1);
        fragments.add(fragment2);
        fragments.add(fragment3);
        fragments.add(fragment4);
        buttonView.addOnPageChangeListener(pageChangeListener);
        viewPagerAdapter = new MainAdapter(getSupportFragmentManager(),fragments);
        buttonView.setAdapter(viewPagerAdapter);
    }

    /**
     * Fragment滑动事件
     */
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            navigationBar.selectTab(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * 发送用户登录日志
     */
    private void sendLoginLog() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone",PrefShared.getString(context,"phoneNum"));
        jsonObject.put("device_token", PushAgent.getInstance(this).getRegistrationId());
        jsonObject.put("token",PrefShared.getString(context,"token"));
        PhoneInfo phoneInfo = new PhoneInfo(context);
        Map<String,Object> map = phoneInfo.getPhoneMsg();
        map.put("address", PrefShared.getString(context,"position"));
        map.put("type","1");
        for(Map.Entry<String, Object> m : map.entrySet()){
            jsonObject.put(m.getKey(),m.getValue());
        }
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.USER_LOGIN, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                json = BaseTools.decryptJson(json);
//                Log.e("用户登录信息",json);
            }
        }, parameter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case 1:
                break;
            default:
                break;
        }
    }

    /**
     * 设置启动首次展示的页面
     * @param i
     */
    private void setCurrentPage(int i) {
        navigationBar.setFirstSelectedPosition(i);
        buttonView.setCurrentItem(i,isWaitingExit);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageView(this, 0, mViewNeedOffset);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isGoBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 捕捉返回按钮事件
     */
    private void isGoBack(){
        int item = buttonView.getCurrentItem();
        if(item == 0){
            if (fragment1.webView.canGoBack()) {
                webViewGoBakc(fragment1);
            } else {
                SySGc();
            }
        } else if(item == 1) {
            if (fragment2.webView.canGoBack()) {
                webViewGoBakc(fragment2);
            } else {
                SySGc();
            }
        } else if(item == 2) {
            if(isBan == 0){
                if (fragment3.webView.canGoBack()) {
                    webViewGoBakc(fragment3);
                } else {
                    SySGc();
                }
            }
        } else if(item == 3) {
            if (fragment4.webView.canGoBack()) {
                webViewGoBakc(fragment4);
            } else {
                SySGc();
            }
        }
        if(isBan == 0){
            showView();
        }
    }

    /**
     * 显示主页面底部按钮
     */
    private void showView(){
        if(null != webViewUrl && !TextUtils.equals(webViewUrl,"")) {
            webViewUrl = webViewUrl.substring(webViewUrl.lastIndexOf("/") + 1);
            if (TextUtils.equals("index.html", webViewUrl) ||
                    TextUtils.equals("9_9.html", webViewUrl) ||
                    TextUtils.equals("invitation.html", webViewUrl) ||
                    TextUtils.equals("my.html", webViewUrl)) {
                hiddenH.sendEmptyMessage(1);
            }
        }
    }

    /**
     * WebView页面的返回
     * @param fragment
     */
    private void webViewGoBakc(final BaseFragment fragment){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragment.webView.goBack();
            }
        });
    }


    /**
     * 退出系统
     */
    private void SySGc(){
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            MainApplication.exit();
        }
    }

    /**
     * 创建分享的监听
     */
    private void intiShareBReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.SENDMSG_SHARE);
        shareOrShowBR = new ShareOrShowBReceiver();
        registerReceiver(shareOrShowBR, intentFilter);
    }

    class ShareOrShowBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String type = intent.getAction();
                if(TextUtils.equals(type,Constants.SENDMSG_SHOW)){
                    pageHandler.sendEmptyMessage(0);
                } else {
                    String result = intent.getStringExtra("result");
                    if (null != result) {
                        String userId = PrefShared.getString(context,"userId");
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        String numId = "",shareTitle = "",shareContent = "",shareImg = "",shareUrl = "";
                        numId = jsonObject.getString("goods_id");
                        shareTitle = jsonObject.getString("title");
                        shareContent = jsonObject.getString("content");
                        shareImg = jsonObject.getString("share_img");
                        if(null != userId){
                            shareUrl = jsonObject.getString("share_url") + "?uid=" + PrefShared.getString(context,"userId");
                        } else {
                            shareUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.shhb.gd.shop";
                        }
                        new ShareCallback(MainActivity.this, hud, failureHud, numId).share(shareTitle, shareContent, shareImg, shareUrl);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
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
    protected void onDestroy() {
        AlibcTradeSDK.destory();
        unregisterReceiver(shareOrShowBR);
        super.onDestroy();
    }
}
