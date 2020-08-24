package com.cartoony.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cartoony.allinonevideo.MyApplication;
import com.cartoony.allinonevideo.R;
import com.cartoony.allinonevideo.SignInActivity;
import com.cartoony.favorite.DatabaseHelper;
import com.cartoony.fragment.ReportFragment;
import com.cartoony.item.ItemLatest;
import com.cartoony.util.Constant;
import com.cartoony.util.JsonUtils;
import com.cartoony.util.PopUpAds;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class CatListAdapter extends RecyclerView.Adapter {


    private Activity activity;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_Ad = 0;
    private ArrayList<ItemLatest> dataList;
    private DatabaseHelper databaseHelper;
    private MyApplication myApplication;

    public CatListAdapter(Activity context, ArrayList<ItemLatest> dataList) {
        this.dataList = dataList;
        this.activity = context;
        databaseHelper = new DatabaseHelper(activity);
        myApplication = MyApplication.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.row_all_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_Ad) {
            View view = LayoutInflater.from(activity).inflate(R.layout.admob_adapter, parent, false);
            return new AdOption(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;
            final ItemLatest singleItem = dataList.get(position);
            viewHolder.text.setText(singleItem.getLatestVideoName());
            viewHolder.text_view.setText(JsonUtils.Format(Integer.parseInt(singleItem.getLatestVideoView())));

            switch (singleItem.getLatestVideoType()) {
                case "local":
                case "server_url":
                case "vimeo":
                case "embeded_code":
                    Picasso.get().load(singleItem.getLatestVideoImgBig()).into(viewHolder.image);
                    break;
                case "youtube":
                    Picasso.get().load(Constant.YOUTUBE_IMAGE_FRONT + singleItem.getLatestVideoPlayId() + Constant.YOUTUBE_SMALL_IMAGE_BACK).into(viewHolder.image);
                    break;
                case "dailymotion":
                    Picasso.get().load(Constant.DAILYMOTION_IMAGE_PATH + singleItem.getLatestVideoPlayId()).into(viewHolder.image);
                    break;
            }

            viewHolder.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Constant.LATEST_IDD = singleItem.getLatestId();
                    PopUpAds.ShowInterstitialAds(activity);

                }
            });

            viewHolder.image_pop_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(activity, viewHolder.image_pop_up);
                    popup.inflate(R.menu.popup_menu);
                    Menu popupMenu = popup.getMenu();
                    if (databaseHelper.getFavouriteById(singleItem.getLatestId())) {
                        popupMenu.findItem(R.id.option_add_favourite).setVisible(false);
                    } else {
                        popupMenu.findItem(R.id.option_remove_favourite).setVisible(false);
                    }

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.option_add_favourite:
                                    ContentValues fav_list = new ContentValues();
                                    fav_list.put(DatabaseHelper.KEY_ID, singleItem.getLatestId());
                                    fav_list.put(DatabaseHelper.KEY_TITLE, singleItem.getLatestVideoName());
                                    fav_list.put(DatabaseHelper.KEY_IMAGE, singleItem.getLatestVideoImgBig());
                                    fav_list.put(DatabaseHelper.KEY_VIEW, singleItem.getLatestVideoView());
                                    fav_list.put(DatabaseHelper.KEY_TYPE, singleItem.getLatestVideoType());
                                    fav_list.put(DatabaseHelper.KEY_PID, singleItem.getLatestVideoPlayId());
                                    fav_list.put(DatabaseHelper.KEY_TIME, singleItem.getLatestDuration());
                                    fav_list.put(DatabaseHelper.KEY_CNAME, singleItem.getLatestCategoryName());
                                    databaseHelper.addFavourite(DatabaseHelper.TABLE_FAVOURITE_NAME, fav_list, null);
                                    Toast.makeText(activity, activity.getString(R.string.favourite_add), Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.option_remove_favourite:
                                    databaseHelper.removeFavouriteById(singleItem.getLatestId());
                                    Toast.makeText(activity, activity.getString(R.string.favourite_remove), Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.option_share:
                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_msg) + "\n" + "https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                                    sendIntent.setType("text/plain");
                                    activity.startActivity(sendIntent);
                                    break;
                                case R.id.option_report:
                                    if (myApplication.getIsLogin()) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("postId", singleItem.getLatestId());
                                        ReportFragment reportFragment = new ReportFragment();
                                        reportFragment.setArguments(bundle);
                                        reportFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), reportFragment.getTag());

                                    } else {
                                        final PrettyDialog dialog = new PrettyDialog(activity);
                                        dialog.setTitle(activity.getString(R.string.dialog_warning))
                                                .setTitleColor(R.color.dialog_text)
                                                .setMessage(activity.getString(R.string.login_require))
                                                .setMessageColor(R.color.dialog_text)
                                                .setAnimationEnabled(false)
                                                .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                                                    @Override
                                                    public void onClick() {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .addButton(activity.getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                                    @Override
                                                    public void onClick() {
                                                        dialog.dismiss();
                                                        Intent intent_login = new Intent(activity, SignInActivity.class);
                                                        intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        activity.startActivity(intent_login);
                                                    }
                                                });
                                        dialog.setCancelable(false);
                                        dialog.show();

                                    }
                                    break;

                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_Ad) {

            final AdOption adOption = (AdOption) holder;
            if (Constant.SAVE_ADS_NATIVE_ON_OFF.equals("true")) {
                if (Constant.SAVE_NATIVE_TYPE.equals("admob")) {

                    if (adOption.linearLayout.getChildCount() == 0) {

                        View view = activity.getLayoutInflater().inflate(R.layout.admob_ad, null, true);

                        final TemplateView templateView = view.findViewById(R.id.my_template);
                        if (templateView.getParent() != null) {
                            ((ViewGroup) templateView.getParent()).removeView(templateView); // <- fix
                        }
                        adOption.linearLayout.addView(templateView);
                        AdLoader adLoader = new AdLoader.Builder(activity, Constant.SAVE_NATIVE_ID)
                                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                                    @Override
                                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                                        NativeTemplateStyle styles = new
                                                NativeTemplateStyle.Builder()
                                                .build();

                                        templateView.setStyles(styles);
                                        templateView.setNativeAd(unifiedNativeAd);

                                    }
                                })
                                .build();

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
                        adLoader.loadAd(adRequest);
                    }

                } else {
                    if (adOption.linearLayout.getChildCount() == 0) {

                        LayoutInflater inflater = LayoutInflater.from(activity);
                        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, adOption.linearLayout, false);

                        adOption.linearLayout.addView(adView);

                        // Add the AdOptionsView
                        final LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);

                        // Create native UI using the ad metadata.
                        final AdIconView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
                        final TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
                        final MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
                        final TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
                        final TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
                        final TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
                        final Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

                        final NativeAd nativeAd = new NativeAd(activity, Constant.SAVE_NATIVE_ID);
                        //AdSettings.addTestDevice("1035dc69-0d11-45c5-bfaf-8f7f7e76e42a");
                        nativeAd.setAdListener(new NativeAdListener() {
                            @Override
                            public void onMediaDownloaded(Ad ad) {
                                Log.d("status_data", "MediaDownloaded" + " " + ad.toString());
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                Toast.makeText(activity, adError.toString(), Toast.LENGTH_SHORT).show();
                                Log.d("status_data", "error" + " " + adError.toString());
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                // Race condition, load() called again before last ad was displayed
                                if (nativeAd == null || nativeAd != ad) {
                                    return;
                                }
                                // Inflate Native Ad into Container
                                Log.d("status_data", "on load" + " " + ad.toString());

                                NativeAdLayout nativeAdLayout = new NativeAdLayout(activity);
                                AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeAdLayout);
                                adChoicesContainer.removeAllViews();
                                adChoicesContainer.addView(adOptionsView, 0);

                                // Set the Text.
                                nativeAdTitle.setText(nativeAd.getAdvertiserName());
                                nativeAdBody.setText(nativeAd.getAdBodyText());
                                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                                nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                                sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                                // Create a list of clickable views
                                List<View> clickableViews = new ArrayList<>();
                                clickableViews.add(nativeAdTitle);
                                clickableViews.add(nativeAdCallToAction);

                                // Register the Title and CTA button to listen for clicks.
                                nativeAd.registerViewForInteraction(
                                        adOption.linearLayout,
                                        nativeAdMedia,
                                        nativeAdIcon,
                                        clickableViews);

                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                Log.d("status_data", "AdClicked" + " " + ad.toString());
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                Log.d("status_data", "Impression" + " " + ad.toString());
                            }

                        });

                        // Request an ad
                        nativeAd.loadAd();
                    }
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_Ad;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image, image_pop_up;
        private TextView text, text_view;
        private LinearLayout lyt_parent;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            lyt_parent = itemView.findViewById(R.id.rootLayout);
            text_view = itemView.findViewById(R.id.text_view);
            image_pop_up = itemView.findViewById(R.id.image_pop_up);
        }
    }

    public class AdOption extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;

        public AdOption(View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.adView_admob);

        }

    }

}