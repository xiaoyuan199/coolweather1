package com.coolweather1.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.telecom.Call;

import com.coolweather1.android.gson.Weather;
import com.coolweather1.android.util.HttpUtil;
import com.coolweather1.android.util.Utility;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateWeather();
        updateBingPic();

        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int oneHour=60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+oneHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */

    private void updateWeather() {

        SharedPreferences pres= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=pres.getString("weather",null);
        if(weatherString!=null){

            //缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            String weatherId=weather.basic.weatherId;

            String weatherUrl="http://guolin.tech/api/weather?cityid="+
                    weatherId+"&key=7100df8af5ad44498e2032e083356c31";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {

                    String responText=response.body().string();
                    Weather weather=Utility.handleWeatherResponse(responText);
                    if(weather!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor=PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responText);
                        editor.apply();
                    }

                }
            });


        }

    }

    /**
     * 更新每日一图
     */

    private void updateBingPic(){

        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();

            }
        });

    }
}
