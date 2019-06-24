package com.qyt.xinhuoban;

public interface OnResponseListener {
    //分享成功的回调
    void onSuccess();

    // 分享取消的回调
    void onCancel();

    // 分享失败的回调
    void onFail(String message);
}
