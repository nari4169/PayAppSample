package com.example.payapp.sample;

import android.app.Application;
import android.net.Uri;

/**
 * Created by leesangkeun on 2016. 12. 5..
 * Define....
 */

// 아래 스키마는 해당 앱 별로 수정을 하여 사용한다.
public class SampleApplication extends Application {
    public Uri mUriResult;
    public  boolean mBtype      = false;

    public static final String mStrAppScheme = "payappSample";
    public static final String mTAG = "payappSample";

    public static final int REQUEST_INTERNAL = 1000;
    public static final int REQUEST_NOTEPAYMENT = 1001;
    public static final int REQUEST_NFCPAYMENT = 1002;
    public static final int REQUEST_SAMSUNGPAYMENT = 1003;
    public static final int REQUEST_FIXEDPERIODPAYMENT = 1004;
    public static final int REQUEST_CASHRECEIPTPAYMENT = 1005;
    public static final int REQUEST_OCRPAYMENT = 1006;
}
