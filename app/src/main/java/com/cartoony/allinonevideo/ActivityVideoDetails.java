package com.cartoony.allinonevideo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bosphere.fadingedgelayout.FadingEdgeLayout;
import com.cartoony.adapter.CommentAdapter;
import com.cartoony.adapter.RelatedAdapter;
import com.cartoony.dailymotion.DailyMotionPlay;
import com.cartoony.dailymotion.DailyMotionPlayNoPip;
import com.cartoony.favorite.DatabaseHelper;
import com.cartoony.favorite.DatabaseHelperRecent;
import com.cartoony.fragment.ReportFragment;
import com.cartoony.item.ItemComment;
import com.cartoony.item.ItemLatest;
import com.cartoony.serverlocal.NoPipServerActivity;
import com.cartoony.serverlocal.PipServerActivity;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.ItemOffsetDecoration;
import com.cartoony.util.JsonUtils;
import com.cartoony.vimeo.Vimeo;
import com.cartoony.vimeo.VimeoNoPip;
import com.cartoony.youtube.YoutubePlay;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class ActivityVideoDetails extends AppCompatActivity {

    Toolbar toolbar;
    WebView webViewDesc;
    TextView textViewTitle, text_view_all, txt_comment_no;
    ImageView imageViewVideo, imageViewPlay;
    LinearLayout linearLayoutMain;
    ProgressBar progressBar;
    ArrayList<ItemLatest> mVideoList, mVideoListRelated;
    ItemLatest itemVideo;
    Menu menu;
    LinearLayout adLayout;
    boolean isWhichScreenNotification;
    JsonUtils jsonUtils;
    DatabaseHelper databaseHelper;
    RelatedAdapter relatedAdapter;
    ArrayList<ItemComment> mCommentList;
    CommentAdapter commentAdapter;
    RecyclerView recyclerViewRelatedVideo, recyclerViewCommentVideo;
    MyApplication myApplication;
    LinearLayout lay_detail;
    FadingEdgeLayout fad_shadow1;
    DatabaseHelperRecent databaseHelperRecent;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.RobotoTextViewStyle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        mVideoList = new ArrayList<>();
        mVideoListRelated = new ArrayList<>();
        mCommentList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(ActivityVideoDetails.this);
        myApplication = MyApplication.getInstance();
        databaseHelperRecent = new DatabaseHelperRecent(ActivityVideoDetails.this);

        webViewDesc = findViewById(R.id.web_desc);
        webViewDesc.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        textViewTitle = findViewById(R.id.text);
        imageViewPlay = findViewById(R.id.image_play);
        imageViewVideo = findViewById(R.id.image);
        linearLayoutMain = findViewById(R.id.lay_main);
        progressBar = findViewById(R.id.progressBar);
        adLayout = findViewById(R.id.ad_view);
        recyclerViewRelatedVideo = findViewById(R.id.rv_most_video);
        recyclerViewRelatedVideo.setHasFixedSize(false);
        recyclerViewRelatedVideo.setNestedScrollingEnabled(false);
        recyclerViewRelatedVideo.setLayoutManager(new LinearLayoutManager(ActivityVideoDetails.this, LinearLayoutManager.HORIZONTAL, false));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(ActivityVideoDetails.this, R.dimen.item_offset);
        recyclerViewRelatedVideo.addItemDecoration(itemDecoration);
        text_view_all = findViewById(R.id.txt_comment_all);
        txt_comment_no = findViewById(R.id.txt_comment_no);
        lay_detail = findViewById(R.id.lay_detail);

        fad_shadow1 = findViewById(R.id.fad_shadow1);
        JsonUtils.changeShadowInRtl(ActivityVideoDetails.this, fad_shadow1);

        recyclerViewCommentVideo = findViewById(R.id.rv_comment_video);
        recyclerViewCommentVideo.setHasFixedSize(false);
        recyclerViewCommentVideo.setNestedScrollingEnabled(false);
        recyclerViewCommentVideo.setLayoutManager(new LinearLayoutManager(ActivityVideoDetails.this, LinearLayoutManager.VERTICAL, false));
        recyclerViewCommentVideo.addItemDecoration(itemDecoration);

        Intent intent = getIntent();
        isWhichScreenNotification = intent.getBooleanExtra("isNotification", false);
        if (Constant.SAVE_BANNER_TYPE.equals("admob")) {
            if (!isWhichScreenNotification) {
                if (JsonUtils.personalization_ad) {
                    JsonUtils.showPersonalizedAds(adLayout, ActivityVideoDetails.this);
                } else {
                    JsonUtils.showNonPersonalizedAds(adLayout, ActivityVideoDetails.this);
                }
            }
        } else {
            JsonUtils.showNonPersonalizedAdsFB(adLayout, ActivityVideoDetails.this);
        }


        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_single_video");
        jsObj.addProperty("video_id", Constant.LATEST_IDD);
        if (JsonUtils.isNetworkAvailable(ActivityVideoDetails.this)) {
            new getVideoDetail(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

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
            linearLayoutMain.setVisibility(View.GONE);
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
                        ItemLatest objItem = new ItemLatest();

                        objItem.setLatestId(objJson.getString(Constant.LATEST_ID));
                        objItem.setLatestCategoryName(objJson.getString(Constant.LATEST_CAT_NAME));
                        objItem.setLatestCategoryId(objJson.getString(Constant.LATEST_CATID));
                        objItem.setLatestVideoUrl(objJson.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setLatestVideoPlayId(objJson.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setLatestVideoName(objJson.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setLatestDuration(objJson.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setLatestDescription(objJson.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setLatestVideoImgBig(objJson.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setLatestVideoType(objJson.getString(Constant.LATEST_TYPE));
                        objItem.setLatestVideoRate(objJson.getString(Constant.LATEST_RATE));
                        objItem.setLatestVideoView(objJson.getString(Constant.LATEST_VIEW));

                        mVideoList.add(objItem);

                        JSONArray jsonArrayChild = objJson.getJSONArray(Constant.RELATED_ARRAY);

                        for (int j = 0; j < jsonArrayChild.length(); j++) {
                            JSONObject objChild = jsonArrayChild.getJSONObject(j);
                            ItemLatest objItem2 = new ItemLatest();

                            objItem2.setLatestId(objChild.getString(Constant.LATEST_ID));
                            objItem2.setLatestCategoryName(objChild.getString(Constant.LATEST_CAT_NAME));
                            objItem2.setLatestCategoryId(objChild.getString(Constant.LATEST_CATID));
                            objItem2.setLatestVideoUrl(objChild.getString(Constant.LATEST_VIDEO_URL));
                            objItem2.setLatestVideoPlayId(objChild.getString(Constant.LATEST_VIDEO_ID));
                            objItem2.setLatestVideoName(objChild.getString(Constant.LATEST_VIDEO_NAME));
                            objItem2.setLatestDuration(objChild.getString(Constant.LATEST_VIDEO_DURATION));
                            objItem2.setLatestDescription(objChild.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                            objItem2.setLatestVideoImgBig(objChild.getString(Constant.LATEST_IMAGE_URL));
                            objItem2.setLatestVideoType(objChild.getString(Constant.LATEST_TYPE));
                            objItem2.setLatestVideoRate(objChild.getString(Constant.LATEST_RATE));
                            objItem2.setLatestVideoView(objChild.getString(Constant.LATEST_VIEW));

                            mVideoListRelated.add(objItem2);
                        }

                        JSONArray jsonArrayCmt = objJson.getJSONArray(Constant.COMMENT_ARRAY);

                        int k = jsonArrayCmt.length() >= 3 ? 3 : jsonArrayCmt.length();
                        for (int j = 0; j < k; j++) {
                            JSONObject objComment = jsonArrayCmt.getJSONObject(j);
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

    @SuppressLint("SetJavaScriptEnabled")
    private void setResultSlider() {

        itemVideo = mVideoList.get(0);

        textViewTitle.setText(itemVideo.getLatestVideoName());
        webViewDesc.setBackgroundColor(0);
        webViewDesc.setFocusableInTouchMode(false);
        webViewDesc.setFocusable(false);
        WebSettings webSettings = webViewDesc.getSettings();
        webSettings.setDefaultFontSize(14);
        webViewDesc.getSettings().setDefaultTextEncodingName("UTF-8");
        webSettings.setJavaScriptEnabled(true);
        String color;
        if (MyApplication.getInstance().isNightModeEnabled()) {
            color = "#FFFFFF;";
        } else {
            color = "#797979;";
        }
        boolean isRTL = Boolean.parseBoolean(getResources().getString(R.string.isRTL));
        String direction = isRTL ? "rtl" : "ltr";
        String text = "<html dir=" + direction + "><head>" + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/Montserrat-Medium_0.otf\")}body,* {font-family: MyFont; color:" + color + "; font-size: 14px;line-height:1.6}img{max-width:100%;height:auto; border-radius: 3px;}</style></head></html>";
        webViewDesc.loadDataWithBaseURL(" ", text + "<div>" + itemVideo.getLatestDescription() + "</div>", "text/html", "utf-8", null);

        switch (itemVideo.getLatestVideoType()) {
            case "local":
            case "server_url":
            case "vimeo":
            case "embeded_code":
                Picasso.get().load(itemVideo.getLatestVideoImgBig()).into(imageViewVideo);
                break;
            case "youtube":
                Picasso.get().load(Constant.YOUTUBE_IMAGE_FRONT + itemVideo.getLatestVideoPlayId() + Constant.YOUTUBE_SMALL_IMAGE_HD).into(imageViewVideo);
                break;
            case "dailymotion":
                Picasso.get().load(Constant.DAILYMOTION_IMAGE_PATH + itemVideo.getLatestVideoPlayId()).into(imageViewVideo);
                break;
        }

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (itemVideo.getLatestVideoType()) {
                    case "local":
                    case "server_url": {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent i = new Intent();
                            i.setClass(ActivityVideoDetails.this, PipServerActivity.class);
                            i.putExtra("id", itemVideo.getLatestVideoUrl());
                            i.putExtra("title", itemVideo.getLatestVideoName());
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else {
                            Toast.makeText(ActivityVideoDetails.this, getString(R.string.pip_not_support), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent();
                            i.setClass(ActivityVideoDetails.this, NoPipServerActivity.class);
                            i.putExtra("id", itemVideo.getLatestVideoUrl());
                            i.putExtra("title", itemVideo.getLatestVideoName());
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }

                        break;
                    }
                    case "embeded_code": {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent i = new Intent();
                            i.setClass(ActivityVideoDetails.this, EmdedeViewActivity.class);
                            i.putExtra("id", itemVideo.getLatestVideoUrl());
                            i.putExtra("title", itemVideo.getLatestVideoName());
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else {
                            Toast.makeText(ActivityVideoDetails.this, getString(R.string.pip_not_support), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent();
                            i.setClass(ActivityVideoDetails.this, NoPipEmdedeViewActivity.class);
                            i.putExtra("id", itemVideo.getLatestVideoUrl());
                            i.putExtra("title", itemVideo.getLatestVideoName());
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                        break;
                    }
                    case "youtube": {
                        Intent i = new Intent(ActivityVideoDetails.this, YoutubePlay.class);
                        i.putExtra("id", itemVideo.getLatestVideoPlayId());
                        startActivity(i);
                        break;
                    }
                    case "dailymotion": {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent i = new Intent(ActivityVideoDetails.this, DailyMotionPlay.class);
                            i.putExtra("id", itemVideo.getLatestVideoPlayId());
                            startActivity(i);
                        } else {
                            Toast.makeText(ActivityVideoDetails.this, getString(R.string.pip_not_support), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(ActivityVideoDetails.this, DailyMotionPlayNoPip.class);
                            i.putExtra("id", itemVideo.getLatestVideoPlayId());
                            startActivity(i);
                        }
                        break;
                    }
                    case "vimeo": {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent i = new Intent(ActivityVideoDetails.this, Vimeo.class);
                            i.putExtra("id", itemVideo.getLatestVideoPlayId());
                            startActivity(i);
                        } else {
                            Toast.makeText(ActivityVideoDetails.this, getString(R.string.pip_not_support), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(ActivityVideoDetails.this, VimeoNoPip.class);
                            i.putExtra("id", itemVideo.getLatestVideoPlayId());
                            startActivity(i);
                        }
                        break;
                    }
                }
            }
        });

        relatedAdapter = new RelatedAdapter(ActivityVideoDetails.this, mVideoListRelated);
        recyclerViewRelatedVideo.setAdapter(relatedAdapter);

        ContentValues fav_rec = new ContentValues();
        if (databaseHelperRecent.getFavouriteById(Constant.LATEST_IDD)) {
            databaseHelperRecent.removeFavouriteById(Constant.LATEST_IDD);
            fav_rec.put(DatabaseHelperRecent.KEY_ID, Constant.LATEST_IDD);
            fav_rec.put(DatabaseHelperRecent.KEY_TITLE, itemVideo.getLatestVideoName());
            fav_rec.put(DatabaseHelperRecent.KEY_IMAGE, itemVideo.getLatestVideoImgBig());
            fav_rec.put(DatabaseHelperRecent.KEY_VIEW, itemVideo.getLatestVideoView());
            fav_rec.put(DatabaseHelperRecent.KEY_TYPE, itemVideo.getLatestVideoType());
            fav_rec.put(DatabaseHelperRecent.KEY_PID, itemVideo.getLatestVideoPlayId());
            fav_rec.put(DatabaseHelperRecent.KEY_TIME, itemVideo.getLatestDuration());
            fav_rec.put(DatabaseHelperRecent.KEY_CNAME, itemVideo.getLatestCategoryName());
            databaseHelperRecent.addFavourite(DatabaseHelperRecent.TABLE_FAVOURITE_NAME, fav_rec, null);
        } else {
            fav_rec.put(DatabaseHelperRecent.KEY_ID, Constant.LATEST_IDD);
            fav_rec.put(DatabaseHelperRecent.KEY_TITLE, itemVideo.getLatestVideoName());
            fav_rec.put(DatabaseHelperRecent.KEY_IMAGE, itemVideo.getLatestVideoImgBig());
            fav_rec.put(DatabaseHelperRecent.KEY_VIEW, itemVideo.getLatestVideoView());
            fav_rec.put(DatabaseHelperRecent.KEY_TYPE, itemVideo.getLatestVideoType());
            fav_rec.put(DatabaseHelperRecent.KEY_PID, itemVideo.getLatestVideoPlayId());
            fav_rec.put(DatabaseHelperRecent.KEY_TIME, itemVideo.getLatestDuration());
            fav_rec.put(DatabaseHelperRecent.KEY_CNAME, itemVideo.getLatestCategoryName());
            databaseHelperRecent.addFavourite(DatabaseHelperRecent.TABLE_FAVOURITE_NAME, fav_rec, null);
        }

        if (mCommentList.size() == 0) {
            txt_comment_no.setVisibility(View.VISIBLE);
        }
        commentAdapter = new CommentAdapter(ActivityVideoDetails.this, mCommentList);
        recyclerViewCommentVideo.setAdapter(commentAdapter);

        text_view_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.LATEST_CMT_IDD = itemVideo.getLatestId();
                skipActivity(ActivityComment.class);
            }
        });
        progressBar.setVisibility(View.GONE);
        linearLayoutMain.setVisibility(View.VISIBLE);
    }

    private void skipActivity(Class<?> classOf) {
        Intent intent = new Intent(getApplicationContext(), classOf);
        startActivity(intent);
    }

    public void showToast(String msg) {
        Toast.makeText(ActivityVideoDetails.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        this.menu = menu;
        isFavourite();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_fav:
                ContentValues fav = new ContentValues();
                if (databaseHelper.getFavouriteById(Constant.LATEST_IDD)) {
                    databaseHelper.removeFavouriteById(Constant.LATEST_IDD);
                    menu.getItem(0).setIcon(R.drawable.ic_fav);
                    Toast.makeText(ActivityVideoDetails.this, getString(R.string.favourite_remove), Toast.LENGTH_SHORT).show();
                } else {
                    fav.put(DatabaseHelper.KEY_ID, Constant.LATEST_IDD);
                    fav.put(DatabaseHelper.KEY_TITLE, itemVideo.getLatestVideoName());
                    fav.put(DatabaseHelper.KEY_IMAGE, itemVideo.getLatestVideoImgBig());
                    fav.put(DatabaseHelper.KEY_VIEW, itemVideo.getLatestVideoView());
                    fav.put(DatabaseHelper.KEY_TYPE, itemVideo.getLatestVideoType());
                    fav.put(DatabaseHelper.KEY_PID, itemVideo.getLatestVideoPlayId());
                    fav.put(DatabaseHelper.KEY_TIME, itemVideo.getLatestDuration());
                    fav.put(DatabaseHelper.KEY_CNAME, itemVideo.getLatestCategoryName());
                    databaseHelper.addFavourite(DatabaseHelper.TABLE_FAVOURITE_NAME, fav, null);
                    menu.getItem(0).setIcon(R.drawable.ic_fav_hov);
                    Toast.makeText(ActivityVideoDetails.this, getString(R.string.favourite_add), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_overflow:
                return true;

            case R.id.menu_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, itemVideo.getLatestVideoName() + "\n" + itemVideo.getLatestVideoUrl() + "\n" + getResources().getString(R.string.share_msg) + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            case R.id.menu_report:
                if (myApplication.getIsLogin()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("postId", itemVideo.getLatestId());
                    ReportFragment reportFragment = new ReportFragment();
                    reportFragment.setArguments(bundle);
                    reportFragment.show(((FragmentActivity) ActivityVideoDetails.this).getSupportFragmentManager(), reportFragment.getTag());

                } else {
                    final PrettyDialog dialog = new PrettyDialog(ActivityVideoDetails.this);
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
                                    Intent intent_login = new Intent(ActivityVideoDetails.this, SignInActivity.class);
                                    intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent_login.putExtra("isfromdetail", true);
                                    intent_login.putExtra("islogid", Constant.LATEST_CMT_IDD);
                                    startActivity(intent_login);
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();

                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void isFavourite() {
        if (databaseHelper.getFavouriteById(Constant.LATEST_IDD)) {
            menu.getItem(0).setIcon(R.drawable.ic_fav_hov);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_fav);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isWhichScreenNotification) {
            super.onBackPressed();

        } else {
            Intent intent = new Intent(ActivityVideoDetails.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
