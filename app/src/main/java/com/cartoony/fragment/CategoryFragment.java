package com.cartoony.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cartoony.adapter.CategoryAdapter;
import com.cartoony.allinonevideo.MainActivity;
import com.cartoony.allinonevideo.R;
import com.cartoony.item.ItemCategory;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.ItemOffsetDecoration;
import com.cartoony.util.JsonUtils;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CategoryFragment extends Fragment {

    ArrayList<ItemCategory> mListItem;
    public RecyclerView recyclerView;
    CategoryAdapter categoryAdapter;
    private ProgressBar progressBar;
    private FragmentManager fragmentManager;
    ItemCategory itemCategory;
    private InterstitialAd mInterstitial;
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);


        mListItem = new ArrayList<>();
        ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_category));
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.rv_video);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(requireActivity(), R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        fragmentManager = getFragmentManager();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (categoryAdapter.getItemViewType(position)) {
                    case 0:
                        return 2;
                    default:
                        return 1;
                }
            }
        });

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_category");
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new getSubCat(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                itemCategory = mListItem.get(position);

                if (itemCategory != null) {
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
                                    fragmentTransaction.hide(CategoryFragment.this);
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
                                    fragmentTransaction.hide(CategoryFragment.this);
                                    fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                                    fragmentTransaction.commit();
                                    ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                                }
                            });
                        } else {
                            CategoryListFragment categoryListFragment = new CategoryListFragment();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.hide(CategoryFragment.this);
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
                                    fragmentTransaction.hide(CategoryFragment.this);
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
                                    fragmentTransaction.hide(CategoryFragment.this);
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
                            fragmentTransaction.hide(CategoryFragment.this);
                            fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                            fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                            fragmentTransaction.commit();
                            ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                        }
                    }
                } else {
                    CategoryListFragment categoryListFragment = new CategoryListFragment();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(CategoryFragment.this);
                    fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
                    fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
                    fragmentTransaction.commit();
                    ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
                }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    private class getSubCat extends AsyncTask<String, Void, String> {

        String base64;

        private getSubCat(String base64) {
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

                        ItemCategory objItem = new ItemCategory();

                        objItem.setCategoryName(objJson.getString(Constant.CATEGORY_NAME));
                        objItem.setCategoryId(objJson.getString(Constant.CATEGORY_CID));
                        objItem.setCategoryImageUrl(objJson.getString(Constant.CATEGORY_IMAGE));

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
        if (Constant.SAVE_ADS_NATIVE_ON_OFF.equals("true")) {
        if (mListItem.size() > 0) {
            for (int i = 0; i < mListItem.size(); i++) {
                if (i % Integer.parseInt(Constant.SAVE_NATIVE_CLICK_OTHER) == 0) {
                    mListItem.add(i, null);
                }
            }
            mListItem.remove(0);
        }}
         if (getActivity() != null) {
             categoryAdapter = new CategoryAdapter(getActivity(), mListItem);
            recyclerView.setAdapter(categoryAdapter);
        }
    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_category));
    }

    private void Loading() {
        pDialog = new ProgressDialog(requireActivity());
        pDialog.setMessage(getResources().getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();
    }
}
