package com.cartoony.allinonevideo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cartoony.adapter.CommentAdapter;
import com.cartoony.item.ItemComment;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.ItemOffsetDecoration;
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

public class ActivityComment extends AppCompatActivity {

    ProgressBar progressBar;
    ArrayList<ItemComment> mCommentList;
    CommentAdapter commentAdapter;
    RecyclerView recyclerViewCommentVideo;
    TextView txt_comment_no;
    JsonUtils jsonUtils;
    MyApplication myApplication;
    EditText edt_comment;
    ImageView image_sent;
    ProgressDialog pDialog;
    String strMessage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mCommentList = new ArrayList<>();
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(this);
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        progressBar = findViewById(R.id.progressBar);
        txt_comment_no = findViewById(R.id.txt_comment_no);
        recyclerViewCommentVideo = findViewById(R.id.rv_comment_video);
        recyclerViewCommentVideo.setHasFixedSize(false);
        recyclerViewCommentVideo.setNestedScrollingEnabled(false);
        recyclerViewCommentVideo.setLayoutManager(new LinearLayoutManager(ActivityComment.this, LinearLayoutManager.VERTICAL, false));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(ActivityComment.this, R.dimen.item_offset);
        recyclerViewCommentVideo.addItemDecoration(itemDecoration);
        edt_comment = findViewById(R.id.edt_comment);
        image_sent = findViewById(R.id.image_sent);

        if (getResources().getString(R.string.isRTL).equals("true")) {
            image_sent.setImageResource(R.drawable.send_right);
        } else {
            image_sent.setImageResource(R.drawable.send);
        }


        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_single_video");
        jsObj.addProperty("video_id", Constant.LATEST_CMT_IDD);
        if (JsonUtils.isNetworkAvailable(ActivityComment.this)) {
            new getVideoDetail(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

        image_sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myApplication.getIsLogin()) {
                    if (edt_comment.length() == 0) {
                        Toast.makeText(ActivityComment.this, getString(R.string.comment_require), Toast.LENGTH_SHORT).show();
                    } else {
                        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                        jsObj.addProperty("method_name", "video_comment");
                        jsObj.addProperty("comment_text", edt_comment.getText().toString());
                        jsObj.addProperty("user_name", myApplication.getUserName());
                        jsObj.addProperty("user_id", myApplication.getUserId());
                        jsObj.addProperty("post_id", Constant.LATEST_CMT_IDD);
                        if (JsonUtils.isNetworkAvailable(ActivityComment.this)) {
                            new MyTaskComment(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                        }

                    }

                } else {
                    final PrettyDialog dialog = new PrettyDialog(ActivityComment.this);
                    dialog.setTitle(getString(R.string.dialog_warning))
                            .setTitleColor(R.color.dialog_text)
                            .setMessage(getString(R.string.login_require))
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
                                    Intent intent_login = new Intent(ActivityComment.this, SignInActivity.class);
                                    intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent_login.putExtra("isfromdetail", true);
                                    intent_login.putExtra("islogid", Constant.LATEST_CMT_IDD);
                                    startActivity(intent_login);
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();

                }

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class getVideoDetail extends AsyncTask<String, Void, String> {

        String base64;

        private getVideoDetail(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewCommentVideo.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            recyclerViewCommentVideo.setVisibility(View.VISIBLE);
            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        JSONArray jsonArrayCmt = objJson.getJSONArray(Constant.COMMENT_ARRAY);
                        JSONObject objComment;
                        for (int j = 0; j < jsonArrayCmt.length(); j++) {
                            objComment = jsonArrayCmt.getJSONObject(j);
                            ItemComment itemComment = new ItemComment();

                            itemComment.setCommentId(objComment.getString(Constant.COMMENT_ID));
                            itemComment.setCommentName(objComment.getString(Constant.COMMENT_NAME));
                            itemComment.setCommentMsg(objComment.getString(Constant.COMMENT_MSG));

                            mCommentList.add(itemComment);

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResultSlider();
            }
        }
    }

    private void setResultSlider() {

        if (mCommentList.size() == 0) {
            txt_comment_no.setVisibility(View.VISIBLE);
        }
        commentAdapter = new CommentAdapter(ActivityComment.this, mCommentList);
        recyclerViewCommentVideo.setAdapter(commentAdapter);

    }

    public void showToast(String msg) {
        Toast.makeText(ActivityComment.this, msg, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskComment extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskComment(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dismissProgressDialog();

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        strMessage = objJson.getString(Constant.MSG);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            showToast(getString(R.string.error_title) + "\n" + strMessage);
        } else {
            onBackPressed();
        }
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }
}
