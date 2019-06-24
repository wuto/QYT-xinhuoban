package com.qyt.xinhuoban;

import android.app.Application;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class MyApplication extends Application {


    private static IWXAPI iwxapi; //第三方app与微信通信的openapi接口

    @Override
    public void onCreate() {

        super.onCreate();

        iwxapi= WXAPIFactory.createWXAPI(this,Constants.WX_APP_ID); //初始化api
        iwxapi.registerApp(Constants.WX_APP_ID); //将APP_ID注册到微信中


    }



}
