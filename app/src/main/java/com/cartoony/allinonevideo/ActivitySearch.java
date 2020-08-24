package com.cartoony.allinonevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cartoony.adapter.AllVideoAdapter;
import com.cartoony.item.ItemLatest;
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

public class ActivitySearch extends AppCompatActivity {

    ArrayList<ItemLatest> mListItem;
    public RecyclerView recyclerView;
    AllVideoAdapter allVideoAdapter;
    private ProgressBar progressBar;
    Toolbar toolbar;
    String search;
    TextView txt_no;
    JsonUtils jsonUtils;
    LinearLayout adLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.search_title));
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.RobotoTextViewStyle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        Intent intent = getIntent();
        search = intent.getStringExtra("search");

        mListItem = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rv_video);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(ActivitySearch.this, 2));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(ActivitySearch.this, R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        txt_no = findViewById(R.id.txt_no);
        adLayout = findViewById(R.id.ad_view);
        if (Constant.SAVE_BANNER_TYPE.equals("admob")) {
            if (JsonUtils.personalization_ad) {
                JsonUtils.showPersonalizedAds(adLayout, ActivitySearch.this);
            } else {
                JsonUtils.showNonPersonalizedAds(adLayout, ActivitySearch.this);
            }
        } else {
            JsonUtils.showNonPersonalizedAdsFB(adLayout, ActivitySearch.this);
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_search_videos");
        jsObj.addProperty("search_text", search);
        if (JsonUtils.isNetworkAvailable(ActivitySearch.this)) {
            new getSearch(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        } else {
            showToast(getString(R.string.no_connect));
        }


    }

    @SuppressLint("StaticFieldLeak")
    private class getSearch extends AsyncTask<String, Void, String> {

        String base64;

        private getSearch(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

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

                        mListItem.add(objItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
            }
        }
    }

    private void displayData() {
        if (mListItem.size() == 0) {
            txt_no.setVisibility(View.VISIBLE);
        }
        allVideoAdapter = new AllVideoAdapter(ActivitySearch.this, mListItem);
        recyclerView.setAdapter(allVideoAdapter);
    }

    public void showToast(String msg) {
        Toast.makeText(ActivitySearch.this, msg, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

}
