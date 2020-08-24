package com.cartoony.dailymotion;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.cartoony.allinonevideo.R;

public class DailyMotionPlayNoPip extends Activity {

    WebView mVideoView;
    String Id;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.dailymotion_no_pip);
        Intent i=getIntent();
        Id=i.getStringExtra("id");
   
        mVideoView = findViewById(R.id.web);
        bar = findViewById(R.id.progressBar1);
        WebSettings webSettings = mVideoView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mVideoView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    bar.setVisibility(View.GONE);

                } else {
                    bar.setVisibility(View.VISIBLE);
                }


            }

        });
        mVideoView.loadUrl("https://www.dailymotion.com/embed/video/"+Id);

        }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
