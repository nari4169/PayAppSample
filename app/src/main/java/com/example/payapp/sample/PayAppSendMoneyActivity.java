package com.example.payapp.sample;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;


/**
 * Created by leesangkeun on 2016. 12. 5..
 *
 * KCP를 통한 계좌이체
 */
@SuppressLint("JavascriptInterface")
public class PayAppSendMoneyActivity extends AppCompatActivity {

    private final Handler handler  = new Handler();
    public WebView mWebView;

    private boolean mAuthFlag = false;
    private String  mBankpayCode      = "";
    private String  mBankpayValue     = "";

    private ProgressBar progressBar;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d( SampleApplication.mTAG, "[PayAppSendMoneyActivity] called onCreate" );

        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_webview );

        mContext = this;

        Intent i = getIntent();
        String url = i.getStringExtra("url");

        mWebView = (WebView)findViewById( R.id.webview );
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mWebView.getSettings().setJavaScriptEnabled( true );
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically( true );
        mWebView.getSettings().setSupportMultipleWindows(false);

        mWebView.addJavascriptInterface(new KCPPayBridge(), "KCPPayApp");

        mWebView.setWebViewClient( new mWebViewClient() );
        mWebView.setWebChromeClient( new WebChromeClient() );

        mWebView.loadUrl(url);

    }

    private class mWebViewClient extends WebViewClient
    {

        @Override
        public void onPageStarted(WebView view, String url,
                                  android.graphics.Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        };

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE);
        };

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Toast.makeText(PayAppSendMoneyActivity.this, description, Toast.LENGTH_SHORT).show();
        };

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url )
        {
            Log.d( SampleApplication.mTAG,
                    "[PayAppSendMoneyActivity] called shouldOverrideUrlLoading - url : " + url);

            if (url != null && ( url.contains("kftc-bankpay://") || url.contains("market://")  ) )
            {
                Intent intent = null;

                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }

                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);

                try {
                    if (startActivityIfNeeded(intent, -1)) {
                        return true;
                    }
                } catch (ActivityNotFoundException ex) {
                }
                return false;
            }
            else if(url.contains("tel:"))
            {
                return false;
            }else
            {
                view.loadUrl( url );
                return false;
            }
        }
    }

    private class KCPPayBridge
    {
        public void launchAcnt( final String arg )
        {
            handler.post( new Runnable() {
                public void run()
                {
                    Log.d( SampleApplication.mTAG, "[PayAppSendMoneyActivity] KCPPayBridge : " + arg);
                    Intent intent = new Intent(Intent.ACTION_MAIN);

                    intent.setComponent(new ComponentName(
                            "com.kftc.bankpay.android",
                            "com.kftc.bankpay.android.activity.MainActivity"));

                    intent.putExtra("requestInfo", arg);

                    startActivityForResult(intent, 1);

                    mAuthFlag = true;
                }
            });
        }
    }

    @Override
    protected void onResume()
    {
        super.onRestart();

        if(mAuthFlag)
        {
            mAuthFlag = false;

            checkFromACNT();
        }
    }

    private void checkFromACNT()
    {
        try
        {
            Log.d( SampleApplication.mTAG,
                    "[PayAppSendMoneyActivity] called onResume mBankpayCode : " + mBankpayCode + ", mBankpayValue : " + mBankpayValue);

            mWebView.loadUrl("javascript:KCP_App_script('"+mBankpayCode+"','"+mBankpayValue+"')" );
        }
        catch ( Exception e )
        {
            Log.d("msKim", e.getMessage());
        }
        finally
        {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d( SampleApplication.mTAG, "[PayAppSendMoneyActivity] requestCode : " + requestCode);
        Log.d( SampleApplication.mTAG, "[PayAppSendMoneyActivity] resultCode : " + resultCode);

        if(data != null)
        {
            Log.d( SampleApplication.mTAG, "[PayAppSendMoneyActivity] dataC : " + data.getExtras().getString("mBankpayCode"));
            Log.d( SampleApplication.mTAG, "[PayAppSendMoneyActivity] dataV : " + data.getExtras().getString("mBankpayValue"));

            mBankpayCode  = data.getExtras().getString("mBankpayCode");
            mBankpayValue = data.getExtras().getString("mBankpayValue");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}