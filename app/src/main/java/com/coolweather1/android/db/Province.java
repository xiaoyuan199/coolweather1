package com.coolweather1.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/4/3 0003.
 */

public class Province extends DataSupport {
    private  int id;
    private  String privinceName;
    private int provinceCode;

    public int getProvinceCode() {
        return provinceCode;
    }

    public int getId() {
        return id;
    }

    public String getPrivinceName() {
        return privinceName;
    }
    public void setId(int id){
        this.id=id;
    }

    public void setPrivinceName(String privinceName) {
        this.privinceName = privinceName;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}

