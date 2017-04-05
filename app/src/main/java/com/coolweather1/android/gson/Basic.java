package com.coolweather1.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/4/4 0004.
 */

/**
 * 由于JSON中的一些字段可能不太适合直接作为java字段来命名，因此这里使用了@SerializedName注解方式来让JSON
 * 字段和java字段之间建立一种映射关系。
 */
public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public  String weatherId;

    public  Update update;

    public class  Update{
        @SerializedName("loc")
        public  String updateTime;
    }
}
