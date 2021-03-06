package com.shhb.gd.shop.module;

/**
 * 网络类型
 * Created by superMoon on 2017/4/25.
 */

public enum NetworkType {
    /**
     * WiFi
     */
    NETWORK_WIFI("WiFi"),
    /**
     * 4G
     */
    NETWORK_4G("4G"),
    /**
     * 3G
     */
    NETWORK_3G("3G"),
    /**
     * 2G
     */
    NETWORK_2G("2G"),
    /**
     * Unknown
     */
    NETWORK_UNKNOWN("Unknown"),
    /**
     * No network
     */
    NETWORK_NO("No network");

    private String desc;
    NetworkType(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
