package com.cartoony.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.onesignal.OneSignal;
import com.cartoony.allinonevideo.ActivityAboutUs;
import com.cartoony.allinonevideo.ActivityPrivacy;
import com.cartoony.allinonevideo.MyApplication;
import com.cartoony.allinonevideo.R;


public class SettingFragment extends Fragment {

    MyApplication MyApp;
    SwitchCompat notificationSwitch,notificationSwitchMode;
    LinearLayout lytAbout, lytPrivacy, lytMoreApp, layRateApp,layShareApp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        MyApp = MyApplication.getInstance();
        notificationSwitch = rootView.findViewById(R.id.switch_notification);
        lytAbout = rootView.findViewById(R.id.lytAbout);
        lytPrivacy = rootView.findViewById(R.id.lytPrivacy);
        lytMoreApp = rootView.findViewById(R.id.lytMoreApp);
        layRateApp = rootView.findViewById(R.id.lytRateApp);
        layShareApp=rootView.findViewById(R.id.lytShareApp);
        notificationSwitch.setChecked(MyApp.getNotification());
        notificationSwitchMode=rootView.findViewById(R.id.switch_notification_night);

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            notificationSwitchMode.setChecked(true);

        notificationSwitchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyApplication.getInstance().setIsNightModeEnabled(isChecked);
                MyApplication.getInstance().onCreate();
                Intent intent = requireActivity().getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                requireActivity().finish();
                startActivity(intent);
            }
        });

        layRateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + requireActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
                }
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyApp.saveIsNotification(isChecked);
                OneSignal.setSubscription(isChecked);
            }
        });

        lytAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_ab = new Intent(requireActivity(), ActivityAboutUs.class);
                startActivity(intent_ab);
            }
        });

        lytPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_pri = new Intent(requireActivity(), ActivityPrivacy.class);
                startActivity(intent_pri);
            }
        });

        lytMoreApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
            }
        });

        layShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_msg) + "\n" + "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
