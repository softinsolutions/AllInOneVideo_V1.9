package com.cartoony.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bosphere.fadingedgelayout.FadingEdgeLayout;
import com.cartoony.adapter.HomeAllAdapter;
import com.cartoony.adapter.HomeCatAdapter;
import com.cartoony.adapter.HomeLatestAdapter;
import com.cartoony.allinonevideo.ActivityRecent;
import com.cartoony.allinonevideo.MainActivity;
import com.cartoony.allinonevideo.R;
import com.cartoony.favorite.DatabaseHelperRecent;
import com.cartoony.item.ItemCategory;
import com.cartoony.item.ItemLatest;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.EnchantedViewPager;
import com.cartoony.util.ItemOffsetDecoration;
import com.cartoony.util.JsonUtils;
import com.cartoony.util.PopUpAds;
import com.cartoony.util.RecyclerTouchListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;


public class HomeFragment extends Fragment {

    RecyclerView recyclerViewLatestVideo, recyclerViewAllVideo, recyclerViewCatVideo, rv_cat_video_rec;
    EnchantedViewPager mViewPager;
    CustomViewPagerAdapter mAdapter;
    NestedScrollView mScrollView;
    ProgressBar mProgressBar;
    ArrayList<ItemLatest> mSliderList;
    CircleIndicator circleIndicator;
    Button btnAll, btnLatest, btnCategory, btn_cat_video_rec;
    int currentCount = 0;
    ArrayList<ItemLatest> mLatestList, mAllList, mRecent;
    ArrayList<ItemCategory> mCatList;
    HomeCatAdapter homeCatAdapter;
    HomeLatestAdapter homeLatestAdapter;
    HomeAllAdapter homeAllAdapter;
    private FragmentManager fragmentManager;
    private InterstitialAd mInterstitial;
    ItemCategory itemCategory;
    TextView txt_latest_video_no, txt_all_video_no, txt_cat_video_no, txt_cat_video_no_rec;
    private ProgressDialog pDialog;
    LinearLayout lay_main;
    FadingEdgeLayout fadingEdgeLayout1, fadingEdgeLayout2, fadingEdgeLayout3, fad_shadow1_rec;
    LinearLayout ad_view;
    RelativeLayout lay_cat_rec;
    HomeLatestAdapter allVideoAdapterRecent;
    DatabaseHelperRecent databaseHelperRecent;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout circleLatest, circleCategory, circleAll, circleRecent, circleFav;
    boolean isRefresh = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mSliderList = new ArrayList<>();
        mAllList = new ArrayList<>();
        mLatestList = new ArrayList<>();
        mCatList = new ArrayList<>();
        mRecent = new ArrayList<>();
        databaseHelperRecent = new DatabaseHelperRecent(requireActivity());

        fadingEdgeLayout1 = rootView.findViewById(R.id.fad_shadow1);
        fadingEdgeLayout2 = rootView.findViewById(R.id.fad_shadow2);
        fadingEdgeLayout3 = rootView.findViewById(R.id.fad_shadow3);
        fad_shadow1_rec = rootView.findViewById(R.id.fad_shadow1_rec);

        circleLatest = rootView.findViewById(R.id.circleLatest);
        circleCategory = rootView.findViewById(R.id.circleCategory);
        circleAll = rootView.findViewById(R.id.circleAll);
        circleRecent = rootView.findViewById(R.id.circleRecent);
        circleFav = rootView.findViewById(R.id.circleFav);

        JsonUtils.changeShadowInRtl(requireActivity(), fadingEdgeLayout1);
        JsonUtils.changeShadowInRtl(requireActivity(), fadingEdgeLayout2);
        JsonUtils.changeShadowInRtl(requireActivity(), fadingEdgeLayout3);
        JsonUtils.changeShadowInRtl(requireActivity(), fad_shadow1_rec);

        ad_view = rootView.findViewById(R.id.ad_view);

        fragmentManager = requireActivity().getSupportFragmentManager();
        mProgressBar = rootView.findViewById(R.id.progressBar);
        mScrollView = rootView.findViewById(R.id.scrollView);
        mViewPager = rootView.findViewById(R.id.viewPager);
        circleIndicator = rootView.findViewById(R.id.indicator_unselected_background);
        lay_main = rootView.findViewById(R.id.lay_main);
        recyclerViewLatestVideo = rootView.findViewById(R.id.rv_latest_video);
        recyclerViewAllVideo = rootView.findViewById(R.id.rv_all_video);
        recyclerViewCatVideo = rootView.findViewById(R.id.rv_cat_video);
        txt_cat_video_no_rec = rootView.findViewById(R.id.txt_cat_video_no_rec);
        rv_cat_video_rec = rootView.findViewById(R.id.rv_cat_video_rec);
        lay_cat_rec = rootView.findViewById(R.id.lay_cat_rec);
        mScrollView.setNestedScrollingEnabled(false);

        btnLatest = rootView.findViewById(R.id.btn_latest_video);
        btnAll = rootView.findViewById(R.id.btn_all_video);
        btnCategory = rootView.findViewById(R.id.btn_cat_video);
        btn_cat_video_rec = rootView.findViewById(R.id.btn_cat_video_rec);

        txt_latest_video_no = rootView.findViewById(R.id.txt_latest_video_no);
        txt_all_video_no = rootView.findViewById(R.id.txt_all_video_no);
        txt_cat_video_no = rootView.findViewById(R.id.txt_cat_video_no);

        swipeRefreshLayout = rootView.findViewById(R.id.homeSwipe);

        recyclerViewLatestVideo.setHasFixedSize(false);
        recyclerViewLatestVideo.setNestedScrollingEnabled(false);
        recyclerViewLatestVideo.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(requireActivity(), R.dimen.item_offset);
        recyclerViewLatestVideo.addItemDecoration(itemDecoration);

        recyclerViewAllVideo.setHasFixedSize(false);
        recyclerViewAllVideo.setNestedScrollingEnabled(false);
        recyclerViewAllVideo.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewAllVideo.addItemDecoration(itemDecoration);

        recyclerViewCatVideo.setHasFixedSize(false);
        recyclerViewCatVideo.setNestedScrollingEnabled(false);
        recyclerViewCatVideo.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCatVideo.addItemDecoration(itemDecoration);

        rv_cat_video_rec.setHasFixedSize(false);
        rv_cat_video_rec.setNestedScrollingEnabled(false);
        rv_cat_video_rec.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rv_cat_video_rec.addItemDecoration(itemDecoration);
        mScrollView.setNestedScrollingEnabled(false);

        btnLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity) requireActivity()).highLightNavigation(1, getString(R.string.menu_latest));
                LatestVideoFragment latestVideoFragment = new LatestVideoFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide(HomeFragment.this);
                fragmentTransaction.add(R.id.Container, latestVideoFragment, getString(R.string.menu_latest));
                fragmentTransaction.addToBackStack(getString(R.string.menu_latest));
                fragmentTransaction.commit();
                ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_latest));

            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity) requireActivity()).highLightNavigation(2, getString(R.string.menu_video));
                AllVideoFragment allVideoFragment = new AllVideoFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide(HomeFragment.this);
                fragmentTransaction.add(R.id.Container, allVideoFragment, getString(R.string.menu_video));
                fragmentTransaction.addToBackStack(getString(R.string.menu_video));
                fragmentTransaction.commit();
                ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_video));

            }
        });


        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity) requireActivity()).highLightNavigation(3, getString(R.string.menu_category));
                CategoryFragment categoryFragment = new CategoryFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide(HomeFragment.this);
                fragmentTransaction.add(R.id.Container, categoryFragment, getString(R.string.menu_category));
                fragmentTransaction.addToBackStack(getString(R.string.menu_category));
                fragmentTransaction.commit();
                ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_category));

            }
        });

        btn_cat_video_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_rec = new Intent(requireActivity(), ActivityRecent.class);
                startActivity(intent_rec);
            }
        });

        //absLayout
        circleLatest.setOnClickListener(view -> {
                    ((MainActivity) requireActivity()).highLightNavigation(1, getString(R.string.menu_latest));
                    LatestVideoFragment latestVideoFragment = new LatestVideoFragment();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(HomeFragment.this);
                    fragmentTransaction.add(R.id.Container, latestVideoFragment, getString(R.string.menu_latest));
                    fragmentTransaction.addToBackStack(getString(R.string.menu_latest));
                    fragmentTransaction.commit();
                    ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_latest));
                }
        );

        circleCategory.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).highLightNavigation(3, getString(R.string.menu_category));
            CategoryFragment categoryFragment = new CategoryFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(HomeFragment.this);
            fragmentTransaction.add(R.id.Container, categoryFragment, getString(R.string.menu_category));
            fragmentTransaction.addToBackStack(getString(R.string.menu_category));
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_category));
        });

        circleAll.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).highLightNavigation(2, getString(R.string.menu_video));
            AllVideoFragment allVideoFragment = new AllVideoFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(HomeFragment.this);
            fragmentTransaction.add(R.id.Container, allVideoFragment, getString(R.string.menu_video));
            fragmentTransaction.addToBackStack(getString(R.string.menu_video));
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_video));
        });
        circleRecent.setOnClickListener(view -> {
                    Intent intent_rec = new Intent(requireActivity(), ActivityRecent.class);
                    startActivity(intent_rec);
                }
        );
        circleFav.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).highLightNavigation(2, getString(R.string.menu_favorite));
            FavoriteFragment favoriteFragment = new FavoriteFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(HomeFragment.this);
            fragmentTransaction.add(R.id.Container, favoriteFragment, getString(R.string.menu_favorite));
            fragmentTransaction.addToBackStack(getString(R.string.menu_favorite));
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_favorite));
        });
        //absLayout end


        swipeRefreshLayout.setOnRefreshListener(() ->

        {

            isRefresh = true;
            loadVideos();
            swipeRefreshLayout.setRefreshing(false);
        });

        loadVideos();

        mViewPager.useScale();
        mViewPager.removeAlpha();
        return rootView;
    }

    private void loadVideos() {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_home_video");
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new HomeVideo(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }
    }

    private class CustomViewPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;

        private CustomViewPagerAdapter() {
            // TODO Auto-generated constructor stub
            inflater = requireActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mSliderList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View imageLayout = inflater.inflate(R.layout.row_slider_item, container, false);
            assert imageLayout != null;

            ImageView image = imageLayout.findViewById(R.id.image);
            TextView text = imageLayout.findViewById(R.id.text);
            LinearLayout lyt_parent = imageLayout.findViewById(R.id.rootLayout);

            text.setText(mSliderList.get(position).getLatestVideoName());

            switch (mSliderList.get(position).getLatestVideoType()) {
                case "local":
                case "server_url":
                case "vimeo":
                case "embeded_code":
                    Picasso.get().load(mSliderList.get(position).getLatestVideoImgBig()).into(image);
                    break;
                case "youtube":
                    Picasso.get().load(Constant.YOUTUBE_IMAGE_FRONT + mSliderList.get(position).getLatestVideoPlayId() + Constant.YOUTUBE_SMALL_IMAGE_BACK).into(image);
                    break;
                case "dailymotion":
                    Picasso.get().load(Constant.DAILYMOTION_IMAGE_PATH + mSliderList.get(position).getLatestVideoPlayId()).into(image);
                    break;
            }

            imageLayout.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
            lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Constant.LATEST_IDD = mSliderList.get(position).getLatestId();
                    PopUpAds.ShowInterstitialAds(requireActivity());
                }
            });

            container.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
        }
    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void autoPlay(final ViewPager viewPager) {

        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mAdapter != null && viewPager.getAdapter().getCount() > 0) {
                        int position = currentCount % mAdapter.getCount();
                        currentCount++;
                        viewPager.setCurrentItem(position);
                        autoPlay(viewPager);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "auto scroll pager error.", e);
                }
            }
        }, 3000);
    }

    @SuppressLint("StaticFieldLeak")
    private class HomeVideo extends AsyncTask<String, Void, String> {

        String base64;

        private HomeVideo(String base64) {
            this.base64 = base64;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (isAdded()) {
                getResources().getString(R.string.app_name);
            }
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
                mScrollView.setVisibility(View.GONE);
            } else {
                mSliderList.clear();
                mLatestList.clear();
                mAllList.clear();
                mCatList.clear();

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONObject mainJsonob = mainJson.getJSONObject(Constant.LATEST_ARRAY_NAME);
                    JSONArray jsonArray = mainJsonob.getJSONArray("featured_video");
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

                        mSliderList.add(objItem);
                    }

                    JSONArray jsonArrayla = mainJsonob.getJSONArray("latest_video");
                    JSONObject objJsonla;
                    for (int i = 0; i < jsonArrayla.length(); i++) {
                        objJsonla = jsonArrayla.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();

                        objItem.setLatestId(objJsonla.getString(Constant.LATEST_ID));
                        objItem.setLatestCategoryName(objJsonla.getString(Constant.LATEST_CAT_NAME));
                        objItem.setLatestCategoryId(objJsonla.getString(Constant.LATEST_CATID));
                        objItem.setLatestVideoUrl(objJsonla.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setLatestVideoPlayId(objJsonla.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setLatestVideoName(objJsonla.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setLatestDuration(objJsonla.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setLatestDescription(objJsonla.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setLatestVideoImgBig(objJsonla.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setLatestVideoType(objJsonla.getString(Constant.LATEST_TYPE));
                        objItem.setLatestVideoRate(objJsonla.getString(Constant.LATEST_RATE));
                        objItem.setLatestVideoView(objJsonla.getString(Constant.LATEST_VIEW));

                        mLatestList.add(objItem);
                    }

                    JSONArray jsonArraymost = mainJsonob.getJSONArray("all_video");
                    JSONObject objJsonmost;
                    for (int i = 0; i < jsonArraymost.length(); i++) {
                        objJsonmost = jsonArraymost.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();

                        objItem.setLatestId(objJsonmost.getString(Constant.LATEST_ID));
                        objItem.setLatestCategoryName(objJsonmost.getString(Constant.LATEST_CAT_NAME));
                        objItem.setLatestCategoryId(objJsonmost.getString(Constant.LATEST_CATID));
                        objItem.setLatestVideoUrl(objJsonmost.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setLatestVideoPlayId(objJsonmost.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setLatestVideoName(objJsonmost.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setLatestDuration(objJsonmost.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setLatestDescription(objJsonmost.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setLatestVideoImgBig(objJsonmost.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setLatestVideoType(objJsonmost.getString(Constant.LATEST_TYPE));
                        objItem.setLatestVideoRate(objJsonmost.getString(Constant.LATEST_RATE));
                        objItem.setLatestVideoView(objJsonmost.getString(Constant.LATEST_VIEW));

                        mAllList.add(objItem);
                    }
                    JSONArray jsonArray2 = mainJsonob.getJSONArray("category");
                    JSONObject objJson2 = null;
                    for (int i = 0; i < jsonArray2.length(); i++) {
                        objJson2 = jsonArray2.getJSONObject(i);

                        ItemCategory objItem = new ItemCategory();

                        objItem.setCategoryId(objJson2.getString(Constant.CATEGORY_CID));
                        objItem.setCategoryImageUrl(objJson2.getString(Constant.CATEGORY_IMAGE));
                        objItem.setCategoryName(objJson2.getString(Constant.CATEGORY_NAME));

                        mCatList.add(objItem);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setHomeVideo();
            }
        }

    }

    private void setHomeVideo() {

        if (getActivity() != null) {
            if (Constant.SAVE_BANNER_TYPE.equals("admob")) {
                if (JsonUtils.personalization_ad) {
                    JsonUtils.showPersonalizedAds(ad_view, requireActivity());
                } else {
                    JsonUtils.showNonPersonalizedAds(ad_view, requireActivity());
                }
            } else {
                JsonUtils.showNonPersonalizedAdsFB(ad_view, requireActivity());
            }
        }

        if (!mSliderList.isEmpty() && !isRefresh) {
            mAdapter = new CustomViewPagerAdapter();
            mViewPager.setAdapter(mAdapter);
            circleIndicator.setViewPager(mViewPager);
            autoPlay(mViewPager);
        }

        if (mSliderList.isEmpty()) {
            mScrollView.setVisibility(View.GONE);

        } else {
            mScrollView.setVisibility(View.VISIBLE);

        }

        txt_latest_video_no.setText(String.valueOf(mLatestList.size()) + "\u0020" + getResources().getString(R.string.total_video));
        txt_all_video_no.setText(String.valueOf(mAllList.size()) + "\u0020" + getResources().getString(R.string.total_video));
        txt_cat_video_no.setText(String.valueOf(mCatList.size()) + "\u0020" + getResources().getString(R.string.total_category));

        if (getActivity() != null) {
            homeLatestAdapter = new HomeLatestAdapter(getActivity(), mLatestList);
            recyclerViewLatestVideo.setAdapter(homeLatestAdapter);
        }
        if (getActivity() != null) {
            homeAllAdapter = new HomeAllAdapter(getActivity(), mAllList);
            recyclerViewAllVideo.setAdapter(homeAllAdapter);
        }
        if (getActivity() != null) {
            homeCatAdapter = new HomeCatAdapter(getActivity(), mCatList);
            recyclerViewCatVideo.setAdapter(homeCatAdapter);
        }

        recyclerViewCatVideo.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerViewCatVideo, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                itemCategory = mCatList.get(position);
                Constant.CATEGORY_IDD = itemCategory.getCategoryId();
                Constant.CATEGORY_TITLEE = itemCategory.getCategoryName();

                if (Constant.SAVE_ADS_FULL_ON_OFF.equals("true")) {
                    if (Constant.SAVE_FULL_TYPE.equals("admob")) {
                        Constant.AD_COUNT++;
                        if (Constant.AD_COUNT == Integer.parseInt(Constant.SAVE_ADS_CLICK)) {
                            Constant.AD_COUNT = 0;
                            Loading();
                            mInterstitial = new InterstitialAd(requireActivity());
                            mInterstitial.setAdUnitId(Constant.SAVE_ADS_FULL_ID);
                            AdRequest adRequest;
                            if (JsonUtils.personalization_ad) {
                                adRequest = new AdRequest.Builder()
                                        .build();
                            } else {
                                Bundle extras = new Bundle();
                                extras.putString("npa", "1");
                                adRequest = new AdRequest.Builder()
                                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                        .build();
                            }
                            mInterstitial.loadAd(adRequest);
                            mInterstitial.setAdListener(new AdListener() {
                                @Override
                                public void onAdLoaded() {
                                    // TODO Auto-generated method stub
                                    super.onAdLoaded();
                                    pDialog.dismiss();
                                    if (mInterstitial.isLoaded()) {
                                        mInterstitial.show();
                                    }
                                }

                                public void onAdClosed() {
                                    CategoryListFragment categoryListFragment = new CategoryListFragment();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.hide(HomeFragment.this);
                                    fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.commit();
                                    ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                                }

                                @Override
                                public void onAdFailedToLoad(int errorCode) {
                                    pDialog.dismiss();
                                    CategoryListFragment categoryListFragment = new CategoryListFragment();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.hide(HomeFragment.this);
                                    fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.commit();
                                    ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                                }
                            });
                        } else {
                            CategoryListFragment categoryListFragment = new CategoryListFragment();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.hide(HomeFragment.this);
                            fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                            fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                            fragmentTransaction.commit();
                            ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                        }
                    } else {
                        Constant.AD_COUNT++;
                        if (Constant.AD_COUNT == Integer.parseInt(Constant.SAVE_ADS_CLICK)) {
                            Constant.AD_COUNT = 0;
                            Loading();
                            com.facebook.ads.InterstitialAd mInterstitialfb = new com.facebook.ads.InterstitialAd(requireActivity(), Constant.SAVE_ADS_FULL_ID);
                            mInterstitialfb.loadAd();
                            mInterstitialfb.setAdListener(new InterstitialAdListener() {
                                @Override
                                public void onInterstitialDisplayed(Ad ad) {

                                }

                                @Override
                                public void onInterstitialDismissed(Ad ad) {
                                    CategoryListFragment categoryListFragment = new CategoryListFragment();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.hide(HomeFragment.this);
                                    fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.commit();
                                    ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                                }

                                @Override
                                public void onError(Ad ad, AdError adError) {
                                    pDialog.dismiss();
                                    CategoryListFragment categoryListFragment = new CategoryListFragment();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.hide(HomeFragment.this);
                                    fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.commit();
                                    ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                                }

                                @Override
                                public void onAdLoaded(Ad ad) {
                                    pDialog.dismiss();
                                    mInterstitialfb.show();
                                }

                                @Override
                                public void onAdClicked(Ad ad) {
                                }

                                @Override
                                public void onLoggingImpression(Ad ad) {
                                }
                            });
                        } else {
                            CategoryListFragment categoryListFragment = new CategoryListFragment();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.hide(HomeFragment.this);
                            fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                            fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                            fragmentTransaction.commit();
                            ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                        }
                    }
                } else {
                    CategoryListFragment categoryListFragment = new CategoryListFragment();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(HomeFragment.this);
                    fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                    fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                    fragmentTransaction.commit();
                    ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    private void Loading() {
        pDialog = new ProgressDialog(requireActivity());
        pDialog.setMessage(getResources().getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecent = databaseHelperRecent.getFavourite();
        displayDataRecent();
        ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_home));
    }

    private void displayDataRecent() {

        if (mRecent.size() >= 2) {
            lay_cat_rec.setVisibility(View.VISIBLE);
            fad_shadow1_rec.setVisibility(View.VISIBLE);

        } else {
            lay_cat_rec.setVisibility(View.GONE);
            fad_shadow1_rec.setVisibility(View.GONE);
        }

        txt_cat_video_no_rec.setText(String.valueOf(mRecent.size()) + "\u0020" + getResources().getString(R.string.total_video));

        allVideoAdapterRecent = new HomeLatestAdapter(getActivity(), mRecent);
        rv_cat_video_rec.setAdapter(allVideoAdapterRecent);
    }
}