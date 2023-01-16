package com.example.payapp.sample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;
import java.util.List;

import kr.co.kcp.util.PackageState;

/**
 * Created by leesangkeun on 2016. 12. 5..
 *
 * 다날-KCP를 통한 카드 결제
 */
@SuppressLint("JavascriptInterface")
public class PayAppCardActivity extends AppCompatActivity {

    public static final int PROGRESS_STAT_NOT_START = 1;
    public static final int PROGRESS_STAT_IN = 2;
    public static final int PROGRESS_DONE = 3;

    public static String CARD_CD  = "";
    public static String QUOTA  = "";

    private final Handler  handler = new Handler();
    public int m_nStat  = PROGRESS_STAT_NOT_START;

    private WebView mWebView;
    private ProgressBar progressBar;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mContext = this;

        Intent i = getIntent();
        String url = i.getStringExtra("url");

        mWebView = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

//        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled( true );
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically( true );
        mWebView.addJavascriptInterface(new PayAppCardActivity.KCPPayBridge(), "KCPPayApp");
        // 하나SK 카드 선택시 User가 선택한 기본 정보를 가지고 오기위해 사용
        mWebView.addJavascriptInterface(new PayAppCardActivity.KCPPayCardInfoBridge(), "KCPPayCardInfo");
        mWebView.addJavascriptInterface(new PayAppCardActivity.KCPPayPinInfoBridge() , "KCPPayPinInfo" ); // 페이핀 기능 추가
        mWebView.addJavascriptInterface(new PayAppCardActivity.KCPPayPinReturn()     , "KCPPayPinRet"  ); // 페이핀 기능 추가

        mWebView.getSettings().setUseWideViewPort(true);

        mWebView.setWebChromeClient(new PayAppCardActivity.CustomWebChromeClient());
        mWebView.setWebViewClient(new PayAppCardActivity.mWebViewClient());

        mWebView.loadUrl(url);
    }


    private boolean urlScheme( WebView view, String url )
    {
        Log.d( SampleApplication.mTAG, "[PayAppCardActivity] called test - url : " + url);

        //chrome 버젼 방식 : 2014.01 추가
        if ( url.startsWith( "intent" ) )
        {
            //ILK 용
            if( false /*url.contains( "com.lotte.lottesmartpay" )*/ ) //150327 롯데카드 앱 호출 수정
            {
                try{
                    startActivity( Intent.parseUri(url, Intent.URI_INTENT_SCHEME) );
                } catch ( URISyntaxException e ) {
                    Log.d( SampleApplication.mTAG, e.getMessage());
                    return false;
                } catch ( ActivityNotFoundException e ) {
                    Log.d( SampleApplication.mTAG, e.getMessage());
                    return false;
                }
            }
            //폴라리스 용
            else
            {
                Intent intent = null;

                try {
                    intent = Intent.parseUri( url, Intent.URI_INTENT_SCHEME );
                } catch ( URISyntaxException ex ) {
                    Log.d( SampleApplication.mTAG, ex.getMessage());
                    return false;
                }

                // 앱설치 체크를 합니다.
                if ( getPackageManager().resolveActivity( intent, 0 ) == null )
                {
                    String packagename = intent.getPackage();

                    if ( packagename != null )
                    {
                        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "market://search?q=pname:" + packagename ) ) );

                        return true;
                    }
                }

                intent = new Intent( Intent.ACTION_VIEW, Uri.parse( intent.getDataString() ) );

                try{
                    startActivity( intent );
                }catch( ActivityNotFoundException e ) {
                    Log.d( SampleApplication.mTAG, "[PayAppCardActivity] ActivityNotFoundException=[" + e.getMessage() + "]" );
                    return false;
                }
            }
        }
        // 기존 방식
        else
        {
            /*
            if ( url.startsWith( "ispmobile" ) )
            {
                if( !new PackageState( this ).getPackageDownloadInstallState( "kvp.jjy.MispAndroid" ) )
                {
                    startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse( "market://details?id=kvp.jjy.MispAndroid320" ) ) );

                    return true;
                }
            }
            else if ( url.startsWith( "paypin" ) )
            {
                if( !new PackageState( this ).getPackageDownloadInstallState( "com.skp.android.paypin" ) )
                {
                    if( !urlScheme( "tstore://PRODUCT_VIEW/0000284061/0" ) )
                    {
                        urlScheme( "market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ.k" );
                    }

                    return true;
                }
            }
            */

            // 삼성과 같은 경우 어플이 없을 경우 마켓으로 이동 할수 있도록 넣은 샘플 입니다.
            // 실제 구현시 업체 구현 여부에 따라 삭제 처리 하시는것이 좋습니다.
            if ( url.startsWith( "mpocket.online.ansimclick" ) )
            {
                if( !new PackageState( this ).getPackageDownloadInstallState( "kr.co.samsungcard.mpocket" ) )
                {
                    Toast.makeText(this, getString(R.string.installPlease), Toast.LENGTH_LONG).show();
                    startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse( "market://details?id=kr.co.samsungcard.mpocket" ) ) );

                    return true;
                }
            }

            try
            {
                startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( url ) ) );
            }
            catch(Exception e)
            {
                // 어플이 설치 안되어 있을경우 오류 발생. 해당 부분은 업체에 맞게 구현
                Toast.makeText(this, getString(R.string.installPlease), Toast.LENGTH_LONG).show();
                if( url.contains( "tstore://" ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    public class mWebViewClient extends WebViewClient
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
            Toast.makeText(PayAppCardActivity.this, description, Toast.LENGTH_SHORT).show();
        };

        @Override
        public boolean shouldOverrideUrlLoading( WebView view, String url )
        {
            Log.d( SampleApplication.mTAG, "[PayAppCardActivity] called shouldOverrideUrlLoading - url : " + url);

            if (url != null && !url.equals("about:blank")){
                if( url.startsWith("http://") || url.startsWith("https://")){
                    if (url.contains("http://market.android.com") || url.contains("http://m.ahnlab.com/kr/site/download") ||
                            url.endsWith(".apk") ){
                        return urlScheme( view, url );
                    }else{
                        view.loadUrl( url );
                        return false;
                    }
                }else if(url.startsWith("mailto:")){
                    return false;
                }else if(url.startsWith("tel:")){
                    return false;
                }else{
                    return urlScheme( view, url );
                }
            }

            return true;
        }
    }

    // 하나SK 카드 선택시 User가 선택한 기본 정보를 가지고 오기위해 사용
    private class KCPPayCardInfoBridge
    {
        @JavascriptInterface
        public void getCardInfo( final String card_cd, final String quota )
        {
            handler.post( new Runnable() {
                public void run(){
                    Log.d( SampleApplication.mTAG, "[PayAppCardActivity] KCPPayCardInfoBridge : " + card_cd + ", " + quota);

                    CARD_CD = card_cd;
                    QUOTA   = quota;

                    boolean check = false;
                    PackageManager packMgr = getPackageManager();
                    List<ApplicationInfo> installedAppList = packMgr.getInstalledApplications( PackageManager.GET_UNINSTALLED_PACKAGES);

                    for (ApplicationInfo appInfo : installedAppList){
                        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                            if(appInfo.packageName.indexOf("com.skt.at") > -1){
                                check = true;
                                break;
                            }
                        }
                    }

                    if(!check){
                        alertToNext();
                    }
                }
            });
        }

        private void alertToNext()
        {
            AlertDialog.Builder  dlgBuilder = new AlertDialog.Builder(PayAppCardActivity.this );
            AlertDialog          alertDlg;

            dlgBuilder.setMessage( "HANA SK 모듈이 설이 되어있지 않습니다.\n설치 하시겠습니까?" );
            dlgBuilder.setCancelable( false );
            dlgBuilder.setPositiveButton( "예",
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse( "http://cert.hanaskcard.com/Ansim/HanaSKPay.apk" ) );

                            m_nStat = PROGRESS_STAT_IN;

                            startActivity( intent );
                        }
                    }
            );

            dlgBuilder.setNegativeButton( "아니오",
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    }
            );

            alertDlg = dlgBuilder.create();
            alertDlg.show();
        }
    }

    private class KCPPayPinReturn
    {
        public String getConfirm()
        {
            SampleApplication myApp = (SampleApplication) mContext;

            if( myApp.mBtype ){
                myApp.mBtype = false;

                return "true";
            }else{
                return "false";
            }
        }
    }

    private class KCPPayPinInfoBridge
    {
        public void getPaypinInfo(final String url)
        {
            handler.post( new Runnable() {
                public void run(){
                    Log.d( SampleApplication.mTAG, "[PayDemoActivity] KCPPayPinInfoBridge  : getPaypinInfo" );

                    PackageState ps = new PackageState( PayAppCardActivity.this );

                    if(!ps.getPackageAllInstallState( "com.skp.android.paypin" )){
                        paypinConfim();
                    }else{
                        urlScheme( null, url );
                    }
                }
            });
        }

        private void paypinConfim()
        {
            AlertDialog.Builder  dlgBuilder = new AlertDialog.Builder( PayAppCardActivity.this );
            AlertDialog          alertDlg;

            dlgBuilder.setTitle( "확인" );
            dlgBuilder.setMessage( "PayPin 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다." );
            dlgBuilder.setCancelable( false );
            dlgBuilder.setPositiveButton( "설치",
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();

                            if(
                                //urlScheme( "https://play.google.com/store/apps/details?id=com.skp.android.paypin&feature=nav_result#?t=W10." );
                                //urlScheme( "market://details?id=com.skp.android.paypin&feature=nav_result#?t=W10." );
                                    !urlScheme( null, "tstore://PRODUCT_VIEW/0000284061/0" ) ){
                                urlScheme( null, "market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ.k" );
                            }
                        }
                    }
            );
            dlgBuilder.setNegativeButton( "취소",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();

                            Toast.makeText(PayAppCardActivity.this, "결제를 취소 하셨습니다." , Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            alertDlg = dlgBuilder.create();
            alertDlg.show();
        }
    }

    private class KCPPayBridge
    {
        public void launchMISP( final String arg )
        {
            handler.post( new Runnable() {
                public void run()
                {
                    String  strUrl;
                    String  argUrl;

                    PackageState ps = new PackageState( PayAppCardActivity.this );

                    argUrl = arg;

                    if(!arg.equals("Install"))
                    {
                        if(!ps.getPackageDownloadInstallState( "kvp.jjy.MispAndroid" ))
                        {
                            argUrl = "Install";
                        }
                    }

                    strUrl = ( argUrl.equals( "Install" ) == true )
                            ? "market://details?id=kvp.jjy.MispAndroid320" //"http://mobile.vpay.co.kr/jsp/MISP/andown.jsp"
                            : argUrl;

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse( strUrl ) );

                    m_nStat = PROGRESS_STAT_IN;

                    startActivity( intent );
                }
            });
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        Log.d( SampleApplication.mTAG, "[PayAppCardActivity] called onResume + INPROGRESS : " + m_nStat);

        SampleApplication myApp = (SampleApplication)this.getApplication();


        // 하나 SK 모듈로 결제 이후 해당 카드 정보를 가지고 오기위해 사용
        if(myApp.mUriResult != null){
            if( myApp.mUriResult.getQueryParameter("realPan") != null &&
                    myApp.mUriResult.getQueryParameter("cavv")    != null &&
                    myApp.mUriResult.getQueryParameter("xid")     != null &&
                    myApp.mUriResult.getQueryParameter("eci")     != null ){
                Log.d( SampleApplication.mTAG,
                        "[PayAppCardActivity] HANA SK Result = javascript:hanaSK('"     + myApp.mUriResult.getQueryParameter("realPan") +
                                "', '" + myApp.mUriResult.getQueryParameter("cavv")    +
                                "', '" + myApp.mUriResult.getQueryParameter("xid")     +
                                "', '" + myApp.mUriResult.getQueryParameter("eci")     +
                                "', '" + CARD_CD                                        +
                                "', '" + QUOTA                                          + "');" );

                // 하나 SK 모듈로 인증 이후 승인을 하기위해 결제 함수를 호출 (주문자 페이지)
                mWebView.loadUrl( "javascript:hanaSK('"     + myApp.mUriResult.getQueryParameter("realPan")  +
                        "', '" + myApp.mUriResult.getQueryParameter("cavv")     +
                        "', '" + myApp.mUriResult.getQueryParameter("xid")      +
                        "', '" + myApp.mUriResult.getQueryParameter("eci")      +
                        "', '" + CARD_CD                                         +
                        "', '" + QUOTA                                           + "');" );
            }

            if( (myApp.mUriResult.getQueryParameter("res_cd") == null? "":
                    myApp.mUriResult.getQueryParameter("res_cd") ).equals("999")) {
                Log.d( SampleApplication.mTAG,
                        "[PayAppCardActivity] HANA SK Result = cancel" );

                m_nStat = 9;
            }
        }

        if ( m_nStat == PROGRESS_STAT_IN ) {
            checkFrom();
        }

        myApp.mUriResult = null;
    }

    private void checkFrom()
    {
        try
        {
            SampleApplication myApp = (SampleApplication)this.getApplication();

            if ( myApp.mUriResult != null ) {
                m_nStat = PROGRESS_DONE;

                String	strResultInfo = myApp.mUriResult.getQueryParameter( "approval_key" );

                if ( strResultInfo == null || strResultInfo.length() <= 4 ){
                    finishActivity( "ISP 결제 오류" );
                }

                String  strResCD = strResultInfo.substring( strResultInfo.length() - 4 );
                Log.d( SampleApplication.mTAG, "[PayAppCardActivity] result=[" + strResultInfo + "]+" + "res_cd=[" + strResCD + "]" );

                if ( strResCD.equals( "0000" ) == true ){

                    String	strApprovalKey = "";
                    strApprovalKey = strResultInfo.substring( 0, strResultInfo.length() - 4  );

                    Log.d( SampleApplication.mTAG, "[PayAppCardActivity] approval_key : " + strApprovalKey);

                    // 원천사 결제 확인 및 결제엔진 호출
                    mWebView.loadUrl( "https://pggw.kcp.co.kr/app.do?ActionResult=app&approval_key=" + strApprovalKey );
                }else if ( strResCD.equals( "3001" ) == true )
                {
                    finishActivity( "ISP 결제 사용자 취소" );
                }else
                {
                    finishActivity( "ISP 결제 기타 오류" );
                }
            }
        }catch ( Exception e ){
        }finally{
        }
    }

    public void finishActivity( String strFinishMsg )
    {
        if ( strFinishMsg != null ) {
            Toast.makeText(mContext, strFinishMsg, Toast.LENGTH_SHORT).show();
        }

        finish();
    }


    private class CustomWebChromeClient extends WebChromeClient
    {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
        {

            Log.d(SampleApplication.mTAG, "onJsAlert : " + message);
            new AlertDialog.Builder(PayAppCardActivity.this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }

                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result)
        {

            Log.d(SampleApplication.mTAG, "onJsConfirm : " + message);
            new AlertDialog.Builder(PayAppCardActivity.this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }

                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }

                            })
                    .create()
                    .show();
            return true;
        }
    }
}
