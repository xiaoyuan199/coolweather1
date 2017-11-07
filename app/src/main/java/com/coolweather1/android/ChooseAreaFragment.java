package com.coolweather1.android;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather1.android.db.City;
import com.coolweather1.android.db.County;
import com.coolweather1.android.db.Province;
import com.coolweather1.android.util.HttpUtil;
import com.coolweather1.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by Administrator on 2017/4/3 0003.
 */

public class ChooseAreaFragment extends Fragment  {
    public static final int LEVEL_PROVINCE=0;
    public  static final int LEVEL_CITY=1;
    public  static  final int LEVEL_COUNTY=2;
    public  static String TAG="HERE";
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
    public static final int REQUEST_CODE=1;
    /**
     * 省列表
     */
    private List<Province>provincesList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private  List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private  City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText= (TextView) view.findViewById(R.id.title_text1);
        listView= (ListView) view.findViewById(R.id.list_view);
        backButton= (Button) view.findViewById(R.id.back_button);
        //////////////R.layout.simple_list_item_1 表示该布局只显示一行文字
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,
            dataList);
        }
        listView.setAdapter(adapter);
        return  view;

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provincesList.get(position);//知道选中的是哪个省了
                    queryCities();//找到对应的市并将数据显示在界面
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);//知道选中的是哪个市了
                    queryCounties();//找到对应的县并将数据显示在界面
                }else if(currentLevel==LEVEL_COUNTY){

                    String weatherId=countyList.get(position).getWeatherId();

                    if(getActivity()instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();//释放当前活动
                    }else if (getActivity()instanceof WeatherActivity){
                        WeatherActivity activity= (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }


        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();//刚开始初始化页默认面显示的是省的列表
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查到再去服务器查询
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provincesList= DataSupport.findAll(Province.class);
        if(provincesList.size()>0){
            dataList.clear();
            for(Province province:provincesList){
                dataList.add(province.getPrivinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            Log.d("111","here===");
            String address="http://guolin.tech/api/china";
            queryFromSever(address,"Province");
        }
    }


    /**
     * 查询选中省所对应的市，优先从数据库查询，如果没有查到再去服务器查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getPrivinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId()))
                .find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            String addrress="http://guolin.tech/api/china/"+provinceCode;
            queryFromSever(addrress,"city");
        }

    }
    /**
     * 查询选中市所对应的县，优先从数据库查询，如果没有查到再去服务器查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel=LEVEL_COUNTY;

        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromSever(address,"county");
        }
    }

    /**
     *
     * @param address
     * @param type
     * 根据传入的地址和类型从服务器查询省、市、县的数据。
     */
    private void queryFromSever(String address, final String type) {

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        
                        closeProgressDialog();
                        //////////////////
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            String responseText=response.body().string();
                boolean result=false;
                if("Province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,
                    selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }
    //关闭对话框
    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
//显示对话框
    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

    }

}

