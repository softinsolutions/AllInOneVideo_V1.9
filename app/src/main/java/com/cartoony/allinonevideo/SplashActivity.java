package com.cartoony.allinonevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cartoony.item.ItemAbout;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class SplashActivity extends AppCompatActivity {

    boolean mIsBackButtonPressed;
    MyApplication myApplication;
    String str_package;
    ArrayList<ItemAbout> mListItem;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        myApplication = MyApplication.getInstance();
        mListItem = new ArrayList<>();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_app_details");
        if (JsonUtils.isNetworkAvailable(SplashActivity.this)) {
            new MyTaskDev(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
            Log.e("daa", "" + API.toBase64(jsObj.toString()));
        } else {
            showToast(getString(R.string.no_connect));
        }
       // MyApplication.getInstance().setIsNightModeEnabled(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskDev extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskDev(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        if (objJson.has("status")) {
                            final PrettyDialog dialog = new PrettyDialog(SplashActivity.this);
                            dialog.setTitle(getString(R.string.dialog_error))
                                    .setTitleColor(R.color.dialog_text)
                                    .setMessage(getString(R.string.restart_msg))
                                    .setMessageColor(R.color.dialog_text)
                                    .setAnimationEnabled(false)
                                    .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                                        @Override
                                        public void onClick() {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                        @Override
                                        public void onClick() {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            str_package = objJson.getString(Constant.APP_PACKAGE_NAME);
                            if (str_package.equals(getPackageName())) {

                                if (myApplication.getFirstIsLogin()) {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                            } else {
                                final PrettyDialog dialog = new PrettyDialog(SplashActivity.this);
                                dialog.setTitle(getString(R.string.dialog_error))
                                        .setTitleColor(R.color.dialog_text)
                                        .setMessage(getString(R.string.license_msg))
                                        .setMessageColor(R.color.dialog_text)
                                        .setAnimationEnabled(false)
                                        .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                                            @Override
                                            public void onClick() {
                                                dialog.dismiss();
                                            }
                                        })
                                        .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                            @Override
                                            public void onClick() {
                                                dialog.dismiss();
                                                finish();
                                            }
                                        });
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    private void setResult() {

    }

    public void showToast(String msg) {
        Toast.makeText(SplashActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        // set the flag to true so the next activity won't start up
        mIsBackButtonPressed = true;
        super.onBackPressed();

    }
}
