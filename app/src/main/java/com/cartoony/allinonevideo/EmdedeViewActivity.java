package com.cartoony.allinonevideo;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EmdedeViewActivity extends AppCompatActivity {

    WebView webView;
    String Iframe;
    ProgressBar bar;
    String Name;
    private final PictureInPictureParams.Builder pictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();
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

    private void pictureInPictureMode() {
        Rational aspectRatio = new Rational(3, 4);
        pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
        enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
    }

    @Override
    public void onUserLeaveHint() {
        if (!isInPictureInPictureMode()) {
            Rational aspectRatio = new Rational(3, 4);
            pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                              Configuration newConfig) {
        if (isInPictureInPictureMode) {

        } else {

        }
    }
    @Override
    public void onBackPressed() {
        //webView.loadUrl("");
        //super.onBackPressed();
        pictureInPictureMode();
    }


}
