package com.example.payapp.sample;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 payapp://default?mode=                         mode는 request, note, nfcCard, nfcSamsungPay, regular, cashReceipt
 &phoneNumber=                                  phoneNumber는 구매자 핸드폰 번호
 &goodPrice=                                    상품가격
 &goodName=                                     상품명
 &callback_url=                                 결과를 받을 Activity
 &application_id=your.app.package.name          개발사 앱의 패키지명
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int PERMISSION_REQUEST = 7000;
    private final String LOGIN_ID = "payrequest";
    private final String URL = "http://api.payapp.kr/oapi/apiLoad.html";
    private final int THREAD_COMPLETE = 0;
    private final int THREAD_ERROR = 1;
    private final int HTTP_TIMEOUT = 5000;

    private CheckBox chkUsePhonePay;
    private CheckBox chkUseCardPay;
    private CheckBox chkUseRBankPay;
    private CheckBox chkUseVBankPay;


    private RadioButton mRbTax;
    private RadioButton mRbDutyFree;

    private Button mBtnRemotePayment;
    private Button mBtnNotePayment;
    private Button mBtnOcrPayment;
    private Button mBtnNfcCardPayment;
    private Button mBtnNfcSamsungPayment;
    private Button mBtnRegularPayment;
    private Button mBtnCashReceipt;


    private EditText mEtPhoneNumber;
    private EditText mEtGoodName;
    private EditText mEtGoodPrice;

    private Button mRequestPayappCard;
    private Button mRequestSendMoney;


    private Context mContext;

    private boolean isCardPay = false;


    private String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE};

    private ArrayList<String> notGranted = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        mEtPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        mEtGoodName = (EditText) findViewById(R.id.etGoodName);
        mEtGoodPrice = (EditText) findViewById(R.id.etGoodPrice);

        mEtPhoneNumber.setText("01055559999");

        chkUsePhonePay = (CheckBox) findViewById(R.id.chkPhone);
        chkUseCardPay = (CheckBox) findViewById(R.id.chkCard);
        chkUseRBankPay = (CheckBox) findViewById(R.id.chkRbank);
        chkUseVBankPay = (CheckBox) findViewById(R.id.chkVbank);


        mRbTax = (RadioButton) findViewById(R.id.rb_tax);
        mRbDutyFree = (RadioButton) findViewById(R.id.rb_tax);

        mRequestPayappCard = (Button) findViewById(R.id.btnPayapp);
        mRequestPayappCard.setOnClickListener(this);

        mRequestSendMoney = (Button) findViewById(R.id.btnSendMoney);
        mRequestSendMoney.setOnClickListener(this);


        mBtnRemotePayment = (Button) findViewById(R.id.btnRemotePayment);
        mBtnRemotePayment.setOnClickListener(this);
        mBtnOcrPayment = (Button) findViewById(R.id.btn_ocr_payment);
        mBtnOcrPayment.setOnClickListener(this);
        mBtnNotePayment = (Button) findViewById(R.id.btnNotePayment);
        mBtnNotePayment.setOnClickListener(this);
        mBtnNfcCardPayment = (Button) findViewById(R.id.btnNfcCardPayment);
        mBtnNfcCardPayment.setOnClickListener(this);
        mBtnNfcSamsungPayment = (Button) findViewById(R.id.btnNfcSamsungPayment);
        mBtnNfcSamsungPayment.setOnClickListener(this);
        mBtnRegularPayment = (Button) findViewById(R.id.btnFixedPeriodPayment);
        mBtnRegularPayment.setOnClickListener(this);
        mBtnCashReceipt = (Button) findViewById(R.id.btnCashReceipt);
        mBtnCashReceipt.setOnClickListener(this);

        checkPermission();

    }

    /**
     * 1. 결과는  csturl값으로 확인한다. 성공시 영수증 url이 내려온다. 그 이외에는 실패
     * 2. 또는 state 는 fail일 경우 결제 실패(서버와의 통신이 정상적이지 못한 경우)를 뜻한다.
     * 3. 예외 발생 항목은 페이앱이 비정상 종료시를 뜻한다.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            String exception = data.getData().getQueryParameter("exception");
            if (exception != null && !exception.isEmpty()) {
                Toast.makeText(mContext, "예외 발생", Toast.LENGTH_SHORT).show();
                return;
            }

            if (requestCode == SampleApplication.REQUEST_INTERNAL) {                                // 원격결제 요청시 리턴 값
                String mul_no = data.getData().getQueryParameter("mul_no");
                String payappurl = data.getData().getQueryParameter("payurl");
                String state = data.getData().getQueryParameter("state");

                Log.d(SampleApplication.mTAG, "mul_no : " + mul_no +
                        ", payappurl : " + payappurl +
                        ", state : " + state);

                Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();

            } else if (requestCode == SampleApplication.REQUEST_NOTEPAYMENT) {                      // 수기결제 요청시 리턴
                String csturl = data.getData().getQueryParameter("csturl");
                String state = data.getData().getQueryParameter("state");
                String errorMessage = data.getData().getQueryParameter("errorMessage");
                String errorCode = data.getData().getQueryParameter("errorCode");
                String mul_no = data.getData().getQueryParameter("mul_no");
                String cardName = data.getData().getQueryParameter("cardName");
                String cardNum = data.getData().getQueryParameter("cardNum");
                String date = data.getData().getQueryParameter("date");
                String installment = data.getData().getQueryParameter("installment");
                String goodName = data.getData().getQueryParameter("goodName");
                String cardAuthNumber = data.getData().getQueryParameter("cardAuthNumber");
                String goodPrice = data.getData().getQueryParameter("goodPrice");


                Log.d(SampleApplication.mTAG, "mul_no : " + mul_no +
                        ", csturl : " + csturl +
                        ", state : " + state);

                Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();

            } else if (requestCode == SampleApplication.REQUEST_NFCPAYMENT) {                        // NFC 결제 요청 시 리턴 값
                String csturl = data.getData().getQueryParameter("csturl");
                String state = data.getData().getQueryParameter("state");
                String errorMessage = data.getData().getQueryParameter("errorMessage");
                String errorCode = data.getData().getQueryParameter("errorCode");
                String mul_no = data.getData().getQueryParameter("mul_no");
                String cardName = data.getData().getQueryParameter("cardName");
                String cardNum = data.getData().getQueryParameter("cardNum");
                String date = data.getData().getQueryParameter("date");
                String installment = data.getData().getQueryParameter("installment");
                String goodName = data.getData().getQueryParameter("goodName");
                String cardAuthNumber = data.getData().getQueryParameter("cardAuthNumber");
                String goodPrice = data.getData().getQueryParameter("goodPrice");

                Log.d(SampleApplication.mTAG, "mul_no : " + mul_no +
                        ", csturl : " + csturl +
                        ", state : " + state);

                Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();

            } else if (requestCode == SampleApplication.REQUEST_SAMSUNGPAYMENT) {                     // 삼성페이 결제 요청시 리턴 값
                String csturl = data.getData().getQueryParameter("csturl");
                String state = data.getData().getQueryParameter("state");
                String errorMessage = data.getData().getQueryParameter("errorMessage");
                String errorCode = data.getData().getQueryParameter("errorCode");
                String mul_no = data.getData().getQueryParameter("mul_no");
                String cardName = data.getData().getQueryParameter("cardName");
                String cardNum = data.getData().getQueryParameter("cardNum");
                String date = data.getData().getQueryParameter("date");
                String installment = data.getData().getQueryParameter("installment");
                String goodName = data.getData().getQueryParameter("goodName");
                String cardAuthNumber = data.getData().getQueryParameter("cardAuthNumber");
                String goodPrice = data.getData().getQueryParameter("goodPrice");

                Log.d(SampleApplication.mTAG, "mul_no : " + mul_no +
                        ", csturl : " + csturl +
                        ", state : " + state);

                Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();

            } else if (requestCode == SampleApplication.REQUEST_FIXEDPERIODPAYMENT) {                 // 정기결제 요청시 리턴 값
                String mul_no = data.getData().getQueryParameter("rebill_no");
                String payappurl = data.getData().getQueryParameter("payurl");
                String state = data.getData().getQueryParameter("state");

                Log.d(SampleApplication.mTAG, "mul_no : " + mul_no +
                        ", payappurl : " + payappurl +
                        ", state : " + state);

                Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();

            } else if (requestCode == SampleApplication.REQUEST_CASHRECEIPTPAYMENT) {                 // 현금영수증 발행 요청
                String state = data.getData().getQueryParameter("state");


                Log.d(SampleApplication.mTAG, "state : " + state);

                Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();

            } else if (requestCode == SampleApplication.REQUEST_OCRPAYMENT) {                         // 카메라 결제 요청시 리턴 값
                String csturl = data.getData().getQueryParameter("csturl");
                String state = data.getData().getQueryParameter("state");
                String errorMessage = data.getData().getQueryParameter("errorMessage");
                String errorCode = data.getData().getQueryParameter("errorCode");
                String mul_no = data.getData().getQueryParameter("mul_no");
                String cardName = data.getData().getQueryParameter("cardName");
                String cardNum = data.getData().getQueryParameter("cardNum");
                String date = data.getData().getQueryParameter("date");
                String installment = data.getData().getQueryParameter("installment");
                String goodName = data.getData().getQueryParameter("goodName");
                String cardAuthNumber = data.getData().getQueryParameter("cardAuthNumber");
                String goodPrice = data.getData().getQueryParameter("goodPrice");

                Log.d(SampleApplication.mTAG, "mul_no : " + mul_no +
                        ", csturl : " + csturl +
                        ", state : " + state);

                Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();

            } else {
                try {
                    String csturl = data.getData().getQueryParameter("csturl");
                    String state = data.getData().getQueryParameter("state");
                    String errorMessage = data.getData().getQueryParameter("errorMessage");
                    String errorCode = data.getData().getQueryParameter("errorCode");
                    String mul_no = data.getData().getQueryParameter("mul_no");
                    String cardName = data.getData().getQueryParameter("cardName");
                    String cardNum = data.getData().getQueryParameter("cardNum");
                    String date = data.getData().getQueryParameter("date");
                    String installment = data.getData().getQueryParameter("installment");
                    String goodName = data.getData().getQueryParameter("goodName");
                    String cardAuthNumber = data.getData().getQueryParameter("cardAuthNumber");
                    String goodPrice = data.getData().getQueryParameter("goodPrice");


                    Log.d(SampleApplication.mTAG, "mul_no : " + mul_no +
                            ", csturl : " + csturl +
                            ", state : " + state);

                    Toast.makeText(mContext, data.getData().toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPayapp:                    // 카드결제
                isCardPay = true;
                getPayAppUrl();
                break;

            case R.id.btnSendMoney:                 // 계좌이체
                isCardPay = false;
                getPayAppUrl();
                break;

            case R.id.btnRemotePayment:             // 원격결제
                sendRemotePayment();
                break;
            case R.id.btn_ocr_payment:               // 카메라 결제
                sendOcrPayment();
                break;
            case R.id.btnNotePayment:               // 수기 결제
                sendNotePayment();
                break;
            case R.id.btnNfcCardPayment:            // NFC 카드 결제
                sendNfcPayment();
                break;
            case R.id.btnNfcSamsungPayment:         // 삼성페이 결제
                sendSamsungPayment();
                break;
            case R.id.btnFixedPeriodPayment:        // 정기결제
                sendFixedPeriodPayment();
                break;
            case R.id.btnCashReceipt:               // 현금영수증
                sendCashReceiptPayment();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void checkPermission() {
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            for (String permission : permissions) {
                int result = PermissionChecker.checkSelfPermission(mContext, permission);
                if (result == PermissionChecker.PERMISSION_DENIED) {
                    notGranted.add(permission);
                }
            }
        }

        if (0 < notGranted.size()) {
            ActivityCompat.requestPermissions(this, notGranted.toArray(new String[]{}), PERMISSION_REQUEST);
        } else {
            getMyPhoneNumber();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;

        if (requestCode == PERMISSION_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] < 0) {
                    isAllGranted = false;
                }
            }
        }

        if (!isAllGranted) {
            Toast.makeText(mContext, mContext.getString(R.string.str_permission_denied), Toast.LENGTH_SHORT).show();
            finish();

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + mContext.getPackageName()));
            mContext.startActivity(intent);
        } else {
            getMyPhoneNumber();
        }
    }

    private void getPayAppUrl() {
        new Thread() {
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");

                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    conn.setConnectTimeout(HTTP_TIMEOUT);
                    conn.setReadTimeout(HTTP_TIMEOUT);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    HashMap<String, String> cryptParam = new HashMap<String, String>();

                    // 파라미터를 설정
                    cryptParam.put("cmd", LOGIN_ID);
                    cryptParam.put("userid", "payapptest");
                    cryptParam.put("goodname", mEtGoodName.getText().toString());
                    cryptParam.put("price", mEtGoodPrice.getText().toString());
                    cryptParam.put("recvphone", mEtPhoneNumber.getText().toString());
                    cryptParam.put("smsuse", "n");
                    // "Scheme + ://결제방법" 를 appurl 에 추가
                    if (isCardPay) {     //  카드결제시 : card_pay
                        cryptParam.put("appurl", SampleApplication.mStrAppScheme + "://card_pay");
                    } else {              // 계좌이체시 : acnt_pay
                        cryptParam.put("appurl", SampleApplication.mStrAppScheme + "://acnt_pay");
                    }

                    writer.write(convertParam(cryptParam));
                    writer.flush();
                    writer.close();
                    os.close();

                    conn.connect();

                    BufferedReader br = null;
                    if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {       // 성공

                        br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                        String line = br.readLine();
                        String[] splitString = line.split("&");
                        String[] result = splitString[0].split("=");

                        if (result[1].equals("0")) {
                            String[] errorMsg = splitString[1].split("=");
                            Message msg = new Message();
                            msg.what = THREAD_ERROR;
                            msg.obj = URLDecoder.decode(errorMsg[1], "UTF-8");
                            handler.sendMessage(msg);

                        } else {
                            for (int i = 0; i < splitString.length; i++) {
                                if (splitString[i].contains("payurl")) {
                                    String[] payappUrl = splitString[i].split("=");
                                    Message msg = new Message();
                                    msg.what = THREAD_COMPLETE;
                                    msg.obj = (Object) URLDecoder.decode(payappUrl[1], "UTF-8");
                                    handler.sendMessage(msg);
                                }
                            }

                        }
                    } else {        // 실패
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Message data = msg;
            if (data.what == THREAD_COMPLETE) {
                if (isCardPay) {
                    requestPayApp(msg.obj.toString());
                } else {
                    requestPayAppSendMoney(msg.obj.toString());
                }
            } else if (data.what == THREAD_ERROR) {
                Toast.makeText(mContext, String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
            }
        }
    };

    // 페이앱 계좌이체 URL
    private void requestPayAppSendMoney(String a_url) {
        Log.d(SampleApplication.mTAG, "payapp send money url : " + a_url);

        Intent i = new Intent(this, PayAppSendMoneyActivity.class);
        i.putExtra("url", a_url);
        startActivity(i);
    }

    // 페이앱 카드결제 URL
    private void requestPayApp(String a_url) {
        Log.d(SampleApplication.mTAG, "payapp url : " + a_url);

        Intent i = new Intent(this, PayAppCardActivity.class);
        i.putExtra("url", a_url);
        startActivity(i);
    }

    private String convertParam(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (HashMap.Entry<String, String> pair : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    private void getMyPhoneNumber() {
        try {
            TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            String phoneNum = telManager.getLine1Number();
            if (phoneNum.startsWith("+82")) {
                mEtPhoneNumber.setText(phoneNum.replace("+82", "0"));
            } else {
                mEtPhoneNumber.setText(phoneNum);
            }
        }catch (Exception e){

        }
    }


    private void sendRemotePayment(){
        /**
         *
         * 페이앱의 원격결제 이용방법
         *
         * payappRequest : 페이앱의 scheme
         * default : 페이앱의 host
         * mode : request
         * phoneNumber : 구매자 번호
         * goodPrice : 구매 금액
         * goodName : 상품명
         * callback_url : 페이앱을 호출 후 받을 앱의 host
         * scheme : 페이앱을 호출 후 받을 앱의 Scheme
         * application_id : 페이앱을 호출 하는 앱의 package name
         */


        String hasTax = "true";
        if(mRbTax.isChecked()){
            hasTax = "true";
        }else{
            hasTax = "false";
        }

        String url = "payapprequest://default?mode=request&phoneNumber=" +  mEtPhoneNumber.getText().toString() +
                        "&goodPrice=" + mEtGoodPrice.getText().toString() +
                        "&goodName=" + mEtGoodName.getText().toString() +  "&hasTax=" + hasTax + "&callback_url=result&scheme=payappsample&application_id=com.example.payapp";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, SampleApplication.REQUEST_INTERNAL);

    }

    private void sendOcrPayment(){
        /**
         *
         * 페이앱의 카메라 결제 시용 방법
         *
         * payappOcrPayment : 페이앱의 scheme
         * default : 페이앱의 host
         * mode : request
         * phoneNumber : 구매자 번호
         * goodPrice : 구매 금액
         * goodName : 상품명
         * callback_url : 페이앱을 호출 후 받을 앱의 host
         * scheme : 페이앱을 호출 후 받을 앱의 Scheme
         * application_id : 페이앱을 호출 하는 앱의 package name
         */

        String hasTax = "true";
        if(mRbTax.isChecked()){
            hasTax = "true";
        }else{
            hasTax = "false";
        }


        String url = "payappOcrPayment://default?mode=request&phoneNumber=" +  mEtPhoneNumber.getText().toString() +
                "&goodPrice=" + mEtGoodPrice.getText().toString() +
                "&goodName=" + mEtGoodName.getText().toString() + "&hasTax=" + hasTax +"&callback_url=result&scheme=payappsample&application_id=com.example.payapp";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, SampleApplication.REQUEST_OCRPAYMENT);
    }

    private void sendNotePayment(){
        /**
         *
         * 페이앱의 수기 결제 시용 방법
         *
         * payappNotePayment : 페이앱의 scheme
         * default : 페이앱의 host
         * mode : request
         * phoneNumber : 구매자 번호
         * goodPrice : 구매 금액
         * goodName : 상품명
         * callback_url : 페이앱을 호출 후 받을 앱의 host
         * scheme : 페이앱을 호출 후 받을 앱의 Scheme
         * application_id : 페이앱을 호출 하는 앱의 package name
         */

        String hasTax = "true";
        if(mRbTax.isChecked()){
            hasTax = "true";
        }else{
            hasTax = "false";
        }


        String url = "payappNotePayment://default?mode=request&phoneNumber=" +  mEtPhoneNumber.getText().toString() +
                "&goodPrice=" + mEtGoodPrice.getText().toString() +
                "&goodName=" + mEtGoodName.getText().toString() + "&hasTax=" + hasTax +"&callback_url=result&scheme=payappsample&application_id=com.example.payapp";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, SampleApplication.REQUEST_NOTEPAYMENT);
    }


    private void sendNfcPayment(){
        /**
         *
         * 페이앱의 NFC 카드 결제시 시용 방법
         *
         * payappNfcPayment : 페이앱의 scheme
         * default : 페이앱의 host
         * mode : request
         * phoneNumber : 구매자 번호
         * goodPrice : 구매 금액
         * goodName : 상품명
         * callback_url : 페이앱을 호출 후 받을 앱의 host
         * scheme : 페이앱을 호출 후 받을 앱의 Scheme
         * application_id : 페이앱을 호출 하는 앱의 package name
         */



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage("NFC 결제시 페이앱이 실행중이면 핸드폰에서 NFC 태그가 정상적으로 동작하지 않을 수 있습니다.\n\n페이앱을 종료 후 사용해 주세요.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String hasTax = "true";
                        if(mRbTax.isChecked()){
                            hasTax = "true";
                        }else{
                            hasTax = "false";
                        }

                        String url = "payappNfcPayment://default?mode=request&phoneNumber=" +  mEtPhoneNumber.getText().toString() +
                                "&goodPrice=" + mEtGoodPrice.getText().toString() +
                                "&goodName=" + mEtGoodName.getText().toString() + "&hasTax=" + hasTax + "&callback_url=result&scheme=payappsample&application_id=com.example.payapp";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivityForResult(intent, SampleApplication.REQUEST_NFCPAYMENT);
                    }
                });
        builder.show();

    }


    private void sendSamsungPayment(){
        /**
         *
         * 페이앱의 삼성페이 결제시 시용 방법
         *
         * payappSamsungPayment : 페이앱의 scheme
         * default : 페이앱의 host
         * mode : request
         * phoneNumber : 구매자 번호
         * goodPrice : 구매 금액
         * goodName : 상품명
         * callback_url : 페이앱을 호출 후 받을 앱의 host
         * scheme : 페이앱을 호출 후 받을 앱의 Scheme
         * application_id : 페이앱을 호출 하는 앱의 package name
         */




        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage("삼성페이 결제시 페이앱이 실행중이면 핸드폰에서 NFC 태그가 정상적으로 동작하지 않을 수 있습니다.\n\n페이앱을 종료 후 사용해 주세요.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String hasTax = "true";
                        if(mRbTax.isChecked()){
                            hasTax = "true";
                        }else{
                            hasTax = "false";
                        }

                        String url = "payappSamsungPayment://default?mode=request&phoneNumber=" +  mEtPhoneNumber.getText().toString() +
                                "&goodPrice=" + mEtGoodPrice.getText().toString() +
                                "&goodName=" + mEtGoodName.getText().toString() + "&hasTax=" + hasTax + "&callback_url=result&scheme=payappsample&application_id=com.example.payapp";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivityForResult(intent, SampleApplication.REQUEST_SAMSUNGPAYMENT);
                    }
                });
        builder.show();

    }

    private void sendFixedPeriodPayment(){
        /**
         *
         * 페이앱의 정기결제 시용 방법
         *
         * payappFixedPeriodPayment : 페이앱의 scheme
         * default : 페이앱의 host
         * mode : request
         * phoneNumber : 구매자 번호
         * goodPrice : 구매 금액
         * goodName : 상품명
         * callback_url : 페이앱을 호출 후 받을 앱의 host
         * scheme : 페이앱을 호출 후 받을 앱의 Scheme
         * application_id : 페이앱을 호출 하는 앱의 package name
         */

        String hasTax = "true";
        if(mRbTax.isChecked()){
            hasTax = "true";
        }else{
            hasTax = "false";
        }

        String url = "payappFixedPeriodPayment://default?mode=request&phoneNumber=" +  mEtPhoneNumber.getText().toString() +
                "&goodPrice=" + mEtGoodPrice.getText().toString() +
                "&goodName=" + mEtGoodName.getText().toString() + "&hasTax=" + hasTax + "&callback_url=result&scheme=payappsample&application_id=com.example.payapp";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, SampleApplication.REQUEST_FIXEDPERIODPAYMENT);
    }

    private void sendCashReceiptPayment(){
        /**
         *
         * 페이앱의 현금영수증 시용 방법
         *
         * payappCashReceiptPayment : 페이앱의 scheme
         * default : 페이앱의 host
         * mode : request
         * phoneNumber : 구매자 번호
         * goodPrice : 구매 금액
         * goodName : 상품명
         * callback_url : 페이앱을 호출 후 받을 앱의 host
         * scheme : 페이앱을 호출 후 받을 앱의 Scheme
         * application_id : 페이앱을 호출 하는 앱의 package name
         */

        String hasTax = "true";
        if(mRbTax.isChecked()){
            hasTax = "true";
        }else{
            hasTax = "false";
        }

        String url = "payappCashReceiptPayment://default?mode=request&phoneNumber=" +  mEtPhoneNumber.getText().toString() +
                "&goodPrice=" + mEtGoodPrice.getText().toString() +
                "&goodName=" + mEtGoodName.getText().toString() + "&hasTax=" + hasTax + "&callback_url=result&scheme=payappsample&application_id=com.example.payapp";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, SampleApplication.REQUEST_CASHRECEIPTPAYMENT);
    }


}
