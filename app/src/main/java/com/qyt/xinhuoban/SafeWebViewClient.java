package com.qyt.xinhuoban;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

public class SafeWebViewClient extends WebViewClient {

    WebActivity.IWebViewInterface iWebViewInterface;
    private Activity activity;

    public SafeWebViewClient(Activity activity, WebActivity.IWebViewInterface iWebViewInterface) {
        this.activity = activity;
        this.iWebViewInterface = iWebViewInterface;
    }

    private static final String TAG = "SafeWebViewClient";

    /**
     * 当WebView得页面Scale值发生改变时回调
     */
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        Log.d(TAG, "onScaleChanged");
        super.onScaleChanged(view, oldScale, newScale);
    }

    /**
     * 是否在 WebView 内加载页面
     *
     * @param view
     * @param url
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        String urlStarts = "xmg://";

        if (url.startsWith(urlStarts)) {

            StringBuilder newStr = new StringBuilder(url.substring(urlStarts.length()));

            Log.d(TAG, url);
            iWebViewInterface.onShareClick(newStr.toString());
            return true;
        }

        if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            activity.startActivity(intent);
            return true;
        }

        if (url.startsWith("weixin://wap/pay?")) {
            //如果return false  就会先提示找不到页面，然后跳转微信
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);
            return true;
        } else {
            //H5微信支付要用，不然说"商家参数格式有误"
            Map<String, String> extraHeaders = new HashMap<String, String>();
//            extraHeaders.put("Referer", "http://wxpay.wxutil.com");
            extraHeaders.put("Referer", "http://pay.xinyigouhuoban.com/");
            view.loadUrl(url, extraHeaders);
        }
        return true;
    }

    /**
     * WebView 开始加载页面时回调，一次Frame加载对应一次回调
     *
     * @param view
     * @param url
     * @param favicon
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG, "onPageStarted");
        super.onPageStarted(view, url, favicon);
    }

    /**
     * WebView 完成加载页面时回调，一次Frame加载对应一次回调
     *
     * @param view
     * @param url
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d(TAG, "onPageFinished");
        super.onPageFinished(view, url);
    }

    /**
     * WebView 加载页面资源时会回调，每一个资源产生的一次网络加载，除非本地有当前 url 对应有缓存，否则就会加载。
     *
     * @param view WebView
     * @param url  url
     */
    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d(TAG, "onLoadResource");
        super.onLoadResource(view, url);
    }

    /**
     * WebView 可以拦截某一次的 request 来返回我们自己加载的数据，这个方法在后面缓存会有很大作用。
     *
     * @param view    WebView
     * @param request 当前产生 request 请求
     * @return WebResourceResponse
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().getScheme();
        Log.d(TAG, "shouldInterceptRequest" + url);
        Log.d(TAG, "shouldInterceptRequest");
        return super.shouldInterceptRequest(view, request);
    }

    /**
     * WebView 访问 url 出错
     *
     * @param view
     * @param request
     * @param error
     */
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        WebBackForwardList webBackForwardList = view.copyBackForwardList();
        view.loadUrl("about:blank");
        iWebViewInterface.onReceivedError();
//        super.onReceivedError(view, request, error);
    }

    /**
     * WebView ssl 访问证书出错，handler.cancel()取消加载，handler.proceed()对然错误也继续加载
     *
     * @param view
     * @param handler
     * @param error
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
    }
}