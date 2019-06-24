package com.qyt.xinhuoban;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.GridLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import static android.widget.Toast.LENGTH_SHORT;

public class WebActivity extends Activity {

    private IWXAPI wxApi;

    private PopupWindow popupWindow;
    private View popupView;

    private WebView mWebView;
    private String url;
    private Activity activity;

    private CustomDialog customDialog;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        wxApi = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID);
        wxApi.registerApp(Constants.WX_APP_ID);


        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_web);

        mWebView = (WebView) findViewById(R.id.webView);
        setWebviewSetting();

        IWebViewInterface click = new IWebViewInterface() {
            @Override
            public void onShareClick(String string) {

                initPopupWindow(string);

            }

            @Override
            public void onReceivedError() {

                showMyDialog();

            }
        };

        mWebView.setWebViewClient(new SafeWebViewClient(this,click));


        url = Constants.WEB_URL;
        mWebView.loadUrl(url);

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setWebviewSetting() {

        WebSettings webSetting = mWebView.getSettings();
        if (webSetting == null) return;

        //https地址加载http图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
//            webSetting.setMixedContentMode(2);
        }

        //支持js
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);

        webSetting.setUseWideViewPort(true);//将图片调整到适合webView的大小
        webSetting.setLoadWithOverviewMode(true);//缩放至屏幕大小

        // 支持 Js 使用
        webSetting.setJavaScriptEnabled(true);
        // 支持缩放
        webSetting.setSupportZoom(true);


    }

    interface IWebViewInterface {
        void onShareClick(String string);

        void onReceivedError();

    }


    @Override
    protected void onStart() {
        super.onStart();
        initReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (netReceiver != null) {
            unregisterReceiver(netReceiver);
        }
    }


    /**
     * 注册网络监听的广播
     */

    private void initReceiver() {

        IntentFilter timeFilter = new IntentFilter();

        timeFilter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGED");
        timeFilter.addAction("android.net.ethernet.STATE_CHANGE");
        timeFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        timeFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        timeFilter.addAction("android.net.wifi.STATE_CHANGE");
        timeFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(netReceiver, timeFilter);

    }


    BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isAvailable()) {
                    WebBackForwardList webBackForwardList = mWebView.copyBackForwardList();
                    if (customDialog != null && customDialog.isShowing()) {
                        customDialog.dismiss();
                        if (mWebView.canGoBack()) {
                            if (mWebView.getUrl().equals("about:blank")) {
                                mWebView.goBack();
                            }
                        }

                    }
                    int type2 = networkInfo.getType();
                    String typeName = networkInfo.getTypeName();

                    switch (type2) {
                        case 0://移动 网络    2G 3G 4G 都是一样的 实测 mix2s 联通卡
                            break;
                        case 1: //wifi网络
                            break;
                        case 9:  //网线连接
                            break;
                    }

                } else {// 无网络
                    showMyDialog();

                    Toast.makeText(WebActivity.this, "暂无网络连接", LENGTH_SHORT).show();

                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        mWebView.destroy();
        super.onDestroy();
        if (netReceiver != null) {
            unregisterReceiver(netReceiver);
            netReceiver = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void initPopupWindow(final String url) {
//        /**
//         * 实例popupWindow对象
//         */
        popupView = View.inflate(this, R.layout.pop_share, null);
        popupWindow = new PopupWindow(popupView, GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        //设置popupWindow中的item可以被点击，这句话是必须要添加的
        popupWindow.setFocusable(true);
//        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);

        popupView.findViewById(R.id.weixinghaoyou).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(WebActivity.this, "微信好友", LENGTH_SHORT).show();
                getImg(url, 0);
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.pengyouquan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(WebActivity.this, "微信朋友圈", LENGTH_SHORT).show();
                getImg(url, 1);
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.share_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.pop_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
            }
        });


        popupWindow.showAtLocation(popupView, Gravity.BOTTOM | Gravity.LEFT, 0,
                0);

    }


    private void showMyDialog() {

        if (customDialog != null && customDialog.isShowing())
            return;

        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage("请检查网络");
        builder.setTitle("提示");

        customDialog = builder.create();
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.setCancelable(false);

        customDialog.show();
    }


    public void sharetext(int flag) {

        //创建一个用于封装待分享文本的WXTextObject对象

        String text = "一个用于封装待分享";
        WXTextObject textObject = new WXTextObject();
        textObject.text = text; //text为String类型
        // 创建WXMediaMessage对象，该对象用于Android客户端向微信发送数据
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObject;
        msg.description = text;//text为String类型，设置描述，可省略

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        //调用api接口，发送数据到微信
        wxApi.sendReq(req);


    }


    public void getImg(final String url, final int i) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // 检查手机或者模拟器是否安装了微信
                    if (!wxApi.isWXAppInstalled()) {
                        Toast.makeText(WebActivity.this, "您还没有安装微信", LENGTH_SHORT).show();
                        return;
                    }

//                    第一步：创建WXImageObject对象，并设置URL地址
//                    String url = "http://www.zzlrpay.com/uploads/fenxiang/F0051557_JCSJ2015810.png";
                    Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream());

                    WXImageObject imgObj = new WXImageObject(bitmap);

//                    设置图片的url
//                    imgObj.imagePath = url;

//                    第二步：创建WXMediaMeaasge对象，包装WXImageObject对象
                    WXMediaMessage msg = new WXMediaMessage(imgObj);
//                    msg.mediaObject = imgObj;
//                    第三步：压缩图片
//                    Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
                    Bitmap thumBitmap = bitmap.createScaledBitmap(bitmap, 120, 150, true);
//                    释放资源
                    bitmap.recycle();
                    msg.thumbData = bmpToByteArray(thumBitmap, true);

//                    第四部：创建SendMessageTo.Req对象，发送数据
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = buildTransaction("img");
                    req.message = msg;
                    req.scene = i == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
                    wxApi.sendReq(req);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }


    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


}