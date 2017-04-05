package com.coolweather1.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/4/4 0004.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;


    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public  String info;
    }
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
