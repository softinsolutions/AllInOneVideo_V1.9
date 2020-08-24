package com.cartoony.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cartoony.allinonevideo.ActivityVideoDetails;
import com.cartoony.allinonevideo.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class PopUpAds {

    public static ProgressDialog pDialog;

    public static void ShowInterstitialAds(Context context) {

        if (Constant.SAVE_ADS_FULL_ON_OFF.equals("true")) {
            if (Constant.SAVE_FULL_TYPE.equals("admob")) {
            Constant.AD_COUNT++;
            if (Constant.AD_COUNT == Integer.parseInt(Constant.SAVE_ADS_CLICK)) {
                Constant.AD_COUNT = 0;
                Loading(context);
                final InterstitialAd mInterstitial = new InterstitialAd(context);
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
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        Intent intent_single = new Intent(context, ActivityVideoDetails.class);
                        intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent_single);

                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        pDialog.dismiss();
                        Intent intent_single = new Intent(context, ActivityVideoDetails.class);
                        intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent_single);
                    }
                });
            } else {
                Intent intent_single = new Intent(context, ActivityVideoDetails.class);
                intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent_single);
            }}
            else {
                Constant.AD_COUNT++;
                if (Constant.AD_COUNT == Integer.parseInt(Constant.SAVE_ADS_CLICK)) {
                    Constant. AD_COUNT = 0;
                    Loading(context);
                    com.facebook.ads.InterstitialAd  mInterstitialfb = new com.facebook.ads.InterstitialAd(context, Constant.SAVE_ADS_FULL_ID);
                    mInterstitialfb.loadAd();
                    mInterstitialfb.setAdListener(new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            Intent intent_single = new Intent(context, ActivityVideoDetails.class);
                            intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent_single);
                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            pDialog.dismiss();
                            Intent intent_single = new Intent(context, ActivityVideoDetails.class);
                            intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent_single);
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
                }else {
                    Intent intent_single = new Intent(context, ActivityVideoDetails.class);
                    intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent_single);
                }
            }
        }else{
            Intent intent_single = new Intent(context, ActivityVideoDetails.class);
            intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent_single);
        }
    }

    public static void Loading(Context context) {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getResources().getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();
    }
}
