package com.coolweather1.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather1.android.gson.Forecast;
import com.coolweather1.android.gson.Weather;
import com.coolweather1.android.service.AutoUpdateService;
import com.coolweather1.android.util.HttpUtil;
import com.coolweather1.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;






public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comWashText;

    private TextView comfortText;

    private TextView sportText;

    private Button back_Button1;

    static final String TAG = "WO";

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;

    public DrawerLayout drawerLayout;

    private Button nav_Button;

    private Button setting_Button;

    private FloatingActionButton float_Button;
    Intent intent1;

    SelectPicPopupWindow selectPicPopupWindow;

    String degree;
    String weatherInfo;
    String mweatherInfo;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            //传入View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和View.SYSTEM_UI_FLAG_LAYOUT_STABLE，
            //表示活动的布局会显示在状态栏上面
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //将状态栏设置成透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        //初始化各种控件
        init();


        //查看缓存区是否有数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        final String weatherId;
        if (weatherString != null) {
            //有就直接显示
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //没有缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);//先不显示
            requestWeather(weatherId);
        }

        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        /**
         * 滑动菜单
         *
         */
        nav_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        /**
         * 设置按钮
         */
        setting_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(WeatherActivity.this);
                dialog.setTitle("设置");

                dialog.setMessage("是否开启后台自动更新天气！");
                dialog.setCancelable(false);

                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startService(intent1);
                    }
                });

                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopService(intent1);
                    }
                });
                dialog.show();
            }
        });
        /**
         * 悬浮按钮
         */
        //为弹出窗口实现监听类
        final View.OnClickListener itemsOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicPopupWindow.dismiss();
                switch (v.getId()) {
                    case R.id.take_photos:
                        //启用照相机
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        startActivity(intent);break;
                    case R.id.share:
                        //启用分享功能
                        showShare();
                        break;
                    default:
                        break;

                }
            }
        };

        float_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //实例化SelectPicPopupWindow
                selectPicPopupWindow = new SelectPicPopupWindow(WeatherActivity.this,
                        itemsOnClick);

                //显示窗口
                selectPicPopupWindow.showAtLocation(WeatherActivity.this.findViewById(R.id.weather_main),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });


    }

    /**
     * 初始化各控件
     */
    private void init() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.api_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        comWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        nav_Button = (Button) findViewById(R.id.nav_button);
        setting_Button = (Button) findViewById(R.id.setting);
        float_Button = (FloatingActionButton) findViewById(R.id.fab);
    }


    private void showShare() {
        if(degree!=null||weatherInfo!=null){
            String mdegree="当前气温："+degree+"\n";
             mweatherInfo=mdegree+"天气概况："+weatherInfo;
        }
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_SEND).setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,mweatherInfo);
        startActivity(Intent.createChooser(intent,"请选择要分享的应用"));
    }


    /**
     * 加载每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);

                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     */

    public void requestWeather(final String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=7100df8af5ad44498e2032e083356c31";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        });
        loadBingPic();

    }


    /**
     * 处理并展示Weather实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
         degree = weather.now.temperature + "℃";
         weatherInfo = weather.now.more.info;

        titleCity.setText(cityName);

        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout,
                    false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);

        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        comWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        intent1 = new Intent(this, AutoUpdateService.class);
    }



}
