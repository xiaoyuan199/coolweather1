package com.coolweather1.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/4/11 0011.
 */

public class SelectPicPopupWindow extends PopupWindow {
    private TextView take_photo,share;
    private View mMenuView;

    public SelectPicPopupWindow(Activity context, View.OnClickListener itemsOnclik){
        super(context);
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView=inflater.inflate(R.layout.take_photo,null);
        take_photo=(TextView) mMenuView.findViewById(R.id.take_photos);
        share=(TextView) mMenuView.findViewById(R.id.share);

        //设置监听事件
        take_photo.setOnClickListener(itemsOnclik);
        share.setOnClickListener(itemsOnclik);


        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(143);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.anim.in_from_bottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw=new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int height=mMenuView.findViewById(R.id.take_layout).getTop();
                int y= (int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                       dismiss();
                    }
                }


                return true;
            }
        });


    }
}
