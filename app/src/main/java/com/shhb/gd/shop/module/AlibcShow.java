package com.shhb.gd.shop.module;

import android.app.Activity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.alibaba.baichuan.android.trade.AlibcTrade;
import com.alibaba.baichuan.android.trade.constants.AlibcConstants;
import com.alibaba.baichuan.android.trade.model.AlibcShowParams;
import com.alibaba.baichuan.android.trade.model.AlibcTaokeParams;
import com.alibaba.baichuan.android.trade.model.OpenType;
import com.alibaba.baichuan.android.trade.page.AlibcBasePage;
import com.alibaba.baichuan.android.trade.page.AlibcPage;
import com.shhb.gd.shop.listener.TradeCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by superMoon on 2017/3/15.
 */
public class AlibcShow {

    /**
     * 使用H5打开
     * @param activity
     * @param alibcPage
     */
    public static void showNative(Activity activity, AlibcPage alibcPage){
        AlibcShowParams alibcShowParams = new AlibcShowParams(OpenType.Native, false);
        Map<String, String> exParams = new HashMap<>();
        exParams.put(AlibcConstants.ISV_CODE, "appisvcode");
        AlibcTaokeParams alibcTaokeParams = new AlibcTaokeParams("mm_120894240_21124824_71244797", null, null); // 若非淘客taokeParams设置为null即可
        AlibcTrade.show(activity, alibcPage, alibcShowParams, alibcTaokeParams, exParams,new TradeCallback(activity));
    }

    /**
     * 使用H5打开
     * @param activity
     * @param webView
     * @param webChromeClient
     * @param alibcPage
     */
    public static void showH5(Activity activity, WebView webView, WebChromeClient webChromeClient, AlibcBasePage alibcPage){
        AlibcShowParams alibcShowParams = new AlibcShowParams(OpenType.H5, false);
        Map<String, String> exParams = new HashMap<>();
        exParams.put(AlibcConstants.ISV_CODE, "appisvcode");
        AlibcTaokeParams alibcTaokeParams = new AlibcTaokeParams("mm_120894240_21124824_71244797", null, null); // 若非淘客taokeParams设置为null即可
        AlibcTrade.show(activity, webView, null, webChromeClient, alibcPage,alibcShowParams, alibcTaokeParams, exParams, new TradeCallback(activity));
    }

}
