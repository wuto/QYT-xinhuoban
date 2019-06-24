package com.qyt.xinhuoban.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.qyt.xinhuoban.Constants;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    static IWXAPI iwxapi;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        iwxapi = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID, false);
        iwxapi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        iwxapi.handleIntent(intent, this);
    }


    @Override
    public void onReq(BaseReq baseReq) {

    }

    //请求回调结果处理
    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK://分享成功
//                Log.d("ERR_OK", "ERR_OK");
//                Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show();

                break;

            case BaseResp.ErrCode.ERR_USER_CANCEL:
//                Log.d("ERR_USER_CANCEL", "ERR_USER_CANCEL");
//                Toast.makeText(this, "分享取消", Toast.LENGTH_LONG).show();
                break;

            case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                Log.d("ERR_AUTH_DENIED", "ERR_AUTH_DENIED");
//                Toast.makeText(this, "分享拒绝", Toast.LENGTH_LONG).show();
                break;

        }
        this.finish();

    }


}
