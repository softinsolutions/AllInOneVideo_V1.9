package com.cartoony.allinonevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cartoony.fragment.AllVideoFragment;
import com.cartoony.fragment.CategoryFragment;
import com.cartoony.fragment.FavoriteFragment;
import com.cartoony.fragment.HomeFragment;
import com.cartoony.fragment.LatestVideoFragment;
import com.cartoony.fragment.ProfileFragment;
import com.cartoony.fragment.SettingFragment;
import com.cartoony.item.ItemAbout;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.JsonUtils;
import com.facebook.login.LoginManager;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    NavigationView navigationView;
    Toolbar toolbar;
    MyApplication App;
    ArrayList<ItemAbout> mListItem;
    JsonUtils jsonUtils;
    LinearLayout adLayout;
    private ConsentForm form;
    String strMessage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.RobotoTextViewStyle);

        App = MyApplication.getInstance();
        mListItem = new ArrayList<>();
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        fragmentManager = getSupportFragmentManager();
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        adLayout = findViewById(R.id.ad_view);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), //Insert your own package name.
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_app_details");
        if (JsonUtils.isNetworkAvailable(MainActivity.this)) {
            new getAdsManage(API.toBase64(jsObj.toString())).execute(Constant.API_URL);

        } else {
            showToast(getString(R.string.no_connect));
        }

        HomeFragment homeFragment = new HomeFragment();
        loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.menu_go_home:
                        HomeFragment homeFragment = new HomeFragment();
                        loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);
                        return true;
                    case R.id.menu_go_latest:
                        LatestVideoFragment latestVideoFragment = new LatestVideoFragment();
                        loadFrag(latestVideoFragment, getString(R.string.menu_latest), fragmentManager);
                        return true;
                    case R.id.menu_go_all:
                        AllVideoFragment allVideoFragment = new AllVideoFragment();
                        loadFrag(allVideoFragment, getString(R.string.menu_all), fragmentManager);
                        return true;
                    case R.id.menu_go_category:
                        CategoryFragment categoryFragment = new CategoryFragment();
                        loadFrag(categoryFragment, getString(R.string.menu_category), fragmentManager);
                        return true;
                    case R.id.menu_go_favourite:
                        FavoriteFragment favoriteFragment = new FavoriteFragment();
                        loadFrag(favoriteFragment, getString(R.string.menu_favorite), fragmentManager);
                        return true;
                    case R.id.menu_go_profile:
                        if (App.getIsLogin()) {
                            ProfileFragment profileFragment = new ProfileFragment();
                            loadFrag(profileFragment, getString(R.string.menu_profile), fragmentManager);
                            toolbar.setTitle(getString(R.string.menu_profile));
                        } else {
                            final PrettyDialog dialog = new PrettyDialog(MainActivity.this);
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
                                            Intent intent_login = new Intent(MainActivity.this, SignInActivity.class);
                                            intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent_login);
                                        }
                                    });
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                        return true;
                    case R.id.menu_go_setting:
                        SettingFragment settingFragment = new SettingFragment();
                        loadFrag(settingFragment, getString(R.string.menu_setting), fragmentManager);
                        toolbar.setTitle(getString(R.string.menu_setting));
                        return true;
                    case R.id.menu_go_login:
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_go_logout:
                        switch (App.getUserType()) {
                            case "Normal":
                                Logout();
                                break;
                            case "Google":
                                logoutG();
                                break;
                            case "Facebook":
                                LoginManager.getInstance().logOut();
                                App.saveIsLogin(false);
                                Intent intent2 = new Intent(getApplicationContext(), SignInActivity.class);
                                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent2);
                                finish();
                                break;
                        }
                        return true;
                    default:
                        return true;
                }
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (App.getIsLogin()) {
            navigationView.getMenu().findItem(R.id.menu_go_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.menu_go_logout).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.menu_go_login).setVisible(true);
            navigationView.getMenu().findItem(R.id.menu_go_logout).setVisible(false);
        }
    }

    public void highLightNavigation(int position, String name) {
        navigationView.getMenu().getItem(position).setChecked(true);
        toolbar.setTitle(name);
    }

    private void Logout() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.menu_logout))
                .setMessage(getString(R.string.logout_msg))
                .setPositiveButton(R.string.exit_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        App.saveIsLogin(false);
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.exit_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.mipmap.app_icon)
                .show();
    }


    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.Container, f1, name);
        ft.commit();
        setToolbarTitle(name);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (fragmentManager.getBackStackEntryCount() != 0) {
            String tag = fragmentManager.getFragments().get(fragmentManager.getBackStackEntryCount() - 1).getTag();
            setToolbarTitle(tag);
            super.onBackPressed();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle(getString(R.string.app_name));
            alert.setIcon(R.mipmap.app_icon);
            alert.setMessage(getString(R.string.exit_msg));
            alert.setPositiveButton(getString(R.string.exit_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    });
            alert.setNegativeButton(getString(R.string.exit_no), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                }
            });
            alert.show();
        }

    }

    public void setToolbarTitle(String Title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Title);
        }
    }

    public void logoutG() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        App.saveIsLogin(false);
                        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MainActivity.this, ActivitySearch.class);
                intent.putExtra("search", arg0);
                startActivity(intent);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("StaticFieldLeak")
    private class getAdsManage extends AsyncTask<String, Void, String> {

        String base64;

        private getAdsManage(String base64) {
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
                        ItemAbout itemAbout = new ItemAbout();

                        itemAbout.setappDevelpoby(objJson.getString(Constant.APP_DEVELOP));
                        itemAbout.setappBannerId(objJson.getString(Constant.ADS_BANNER_ID));
                        itemAbout.setappFullId(objJson.getString(Constant.ADS_FULL_ID));
                        itemAbout.setappBannerOn(objJson.getString(Constant.ADS_BANNER_ON_OFF));
                        itemAbout.setappFullOn(objJson.getString(Constant.ADS_FULL_ON_OFF));
                        itemAbout.setappFullPub(objJson.getString(Constant.ADS_PUB_ID));
                        itemAbout.setappFullAdsClick(objJson.getString(Constant.ADS_CLICK));
                        itemAbout.setAppNativeId(objJson.getString(Constant.NATIVE_AD_ID));
                        itemAbout.setAppNativeType(objJson.getString(Constant.NATIVE_TYPE));
                        itemAbout.setAppNativeOnOff(objJson.getString(Constant.NATIVE_AD_ON_OFF));
                        itemAbout.setAppBannerType(objJson.getString(Constant.BANNER_TYPE));
                        itemAbout.setAppFullType(objJson.getString(Constant.FULL_TYPE));
                        Constant.SAVE_NATIVE_CLICK_OTHER = objJson.getString("native_position");
                        mListItem.add(itemAbout);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    private void setResult() {

        ItemAbout itemAbout = mListItem.get(0);
        Constant.SAVE_ADS_BANNER_ID = itemAbout.getappBannerId();
        Constant.SAVE_ADS_FULL_ID = itemAbout.getappFullId();
        Constant.SAVE_ADS_BANNER_ON_OFF = itemAbout.getappBannerOn();
        Constant.SAVE_ADS_FULL_ON_OFF = itemAbout.getappFullOn();
        Constant.SAVE_ADS_PUB_ID = itemAbout.getappFullPub();
        Constant.SAVE_ADS_CLICK = itemAbout.getappFullAdsClick();
        Constant.SAVE_ADS_NATIVE_ON_OFF = itemAbout.getAppNativeOnOff();
        Constant.SAVE_NATIVE_ID = itemAbout.getAppNativeId();
        Constant.SAVE_BANNER_TYPE = itemAbout.getAppBannerType();
        Constant.SAVE_FULL_TYPE = itemAbout.getAppFullType();
        Constant.SAVE_NATIVE_TYPE = itemAbout.getAppNativeType();

        if (App.getIsLogin()) {
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty("method_name", "user_status");
            jsObj.addProperty("user_id", App.getUserId());
            if (JsonUtils.isNetworkAvailable(MainActivity.this)) {
                new MyTaskLoginStatus(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
            }

        }
        if (Constant.SAVE_BANNER_TYPE.equals("admob")) {
            checkForConsent();
        } else {
            JsonUtils.showNonPersonalizedAdsFB(adLayout, MainActivity.this);
        }
    }

    public void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskLoginStatus extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskLoginStatus(String base64) {
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
                        strMessage = objJson.getString(Constant.MSG2);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResultStatus();
            }
        }
    }

    public void setResultStatus() {
        if (Constant.GET_SUCCESS_MSG == 0) {
            showToast(getString(R.string.error_login_failed));
            App.saveIsLogin(false);
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        }
    }

    public void checkForConsent() {

        // ConsentInformation.getInstance(MainActivity.this).addTestDevice("FA0F55855A8169A47EB9D713413B9FE9");
        // Geography appears as in EEA for test devices.
        // ConsentInformation.getInstance(MainActivity.this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        // Geography appears as not in EEA for debug devices.
        ConsentInformation consentInformation = ConsentInformation.getInstance(MainActivity.this);
        String[] publisherIds = {Constant.SAVE_ADS_PUB_ID};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d("consentStatus", consentStatus.toString());
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        JsonUtils.personalization_ad = true;
                        JsonUtils.showPersonalizedAds(adLayout, MainActivity.this);
                        break;
                    case NON_PERSONALIZED:
                        JsonUtils.personalization_ad = false;
                        JsonUtils.showNonPersonalizedAds(adLayout, MainActivity.this);
                        break;
                    case UNKNOWN:
                        if (ConsentInformation.getInstance(getBaseContext())
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        } else {
                            JsonUtils.personalization_ad = true;
                            JsonUtils.showPersonalizedAds(adLayout, MainActivity.this);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });
    }

    public void requestConsent() {
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL(getString(R.string.privacy_gdpr_url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(MainActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        showForm();
                        // Consent form loaded successfully.
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d("consentStatus_form", consentStatus.toString());
                        switch (consentStatus) {
                            case PERSONALIZED:
                                JsonUtils.personalization_ad = true;
                                JsonUtils.showPersonalizedAds(adLayout, MainActivity.this);
                                break;
                            case NON_PERSONALIZED:
                                JsonUtils.personalization_ad = false;
                                JsonUtils.showNonPersonalizedAds(adLayout, MainActivity.this);
                                break;
                            case UNKNOWN:
                                JsonUtils.personalization_ad = false;
                                JsonUtils.showNonPersonalizedAds(adLayout, MainActivity.this);
                        }
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d("errorDescription", errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

    private void showForm() {
        if (form != null) {
            form.show();
        }
    }
}

