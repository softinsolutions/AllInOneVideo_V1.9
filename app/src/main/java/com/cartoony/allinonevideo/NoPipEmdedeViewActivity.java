package com.cartoony.allinonevideo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class NoPipEmdedeViewActivity extends AppCompatActivity {

    WebView webView;
    String Iframe;
    ProgressBar bar;
    String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emdedeview_activity);

        Intent i = getIntent();
        Iframe = i.getStringExtra("id");
        Name = i.getStringExtra("title");
        webView = findViewById(R.id.vwebView);
        bar = findViewById(R.id.progressBar1);
        webView.setBackgroundColor(0);
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setJavaScriptEnabled(true);

        String mimeType = "text/html";
        String encoding = "utf-8";

        String text = "<html><head>"
                + "<style type=\"text/css\">body{color: #000000;}"
                + "</style></head>"
                + "<body>"
                + Iframe
                + "</body></html>";

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    bar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                } else {
                    bar.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                }

            }
        });

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);


    }


    @Override
    public void onBackPressed() {
        webView.loadUrl("");
        super.onBackPressed();
    }


}
