package com.cartoony.dailymotion;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.util.Rational;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.cartoony.allinonevideo.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DailyMotionPlay extends Activity {

    private WebView mVideoView;
    String Id;
    private Button pip;
    private final PictureInPictureParams.Builder pictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.dailymotion);

        mVideoView = findViewById(R.id.web);
        setVideoView(getIntent());

        pip = findViewById(R.id.pip);
        pip.setVisibility(View.GONE);
        pip.setOnClickListener(onClickListener);

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

    private final View.OnClickListener onClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.pip:
                            pictureInPictureMode();
                            break;
                    }
                }
            };

    private void setVideoView(Intent i) {
        Id = i.getStringExtra("id");
        mVideoView.loadUrl("https://www.dailymotion.com/embed/video/"+Id);

    }

    private void pictureInPictureMode() {
        Rational aspectRatio = new Rational(mVideoView.getWidth(), mVideoView.getHeight());
        pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
        enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
    }

    @Override
    public void onUserLeaveHint() {
        if (!isInPictureInPictureMode()) {
            Rational aspectRatio = new Rational(mVideoView.getWidth(), mVideoView.getHeight());
            pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                              Configuration newConfig) {
        if (isInPictureInPictureMode) {
            pip.setVisibility(View.GONE);
        } else {
            pip.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNewIntent(Intent i) {
        setVideoView(i);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();

    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            pip.setVisibility(View.GONE);
        } else {
            // In portrait
            pip.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {
        pictureInPictureMode();
    }

}
