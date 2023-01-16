package com.example.payapp.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by leesangkeun on 2016. 12. 5..
 */

public class ResultActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SampleApplication myApp    = (SampleApplication)this.getApplication();
        Intent myIntent = getIntent();

        Log.d( SampleApplication.mTAG, "[ResultActivity] launch uri : " + myIntent.getData().toString());

        if ( myIntent.getData().getScheme().equals( SampleApplication.mStrAppScheme ) == true ){
            myApp.mUriResult	 = myIntent.getData();
        }else{
            myApp.mUriResult	 = null;
        }

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(SampleApplication.mTAG, "result onActivityResult");
    }
}