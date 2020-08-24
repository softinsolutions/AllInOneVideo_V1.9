package com.cartoony.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class HomeLatestAdapter extends RecyclerView.Adapter<HomeLatestAdapter.ItemRowHolder> {

    private ArrayList<ItemLatest> dataList;
    private Context mContext;
    private DatabaseHelper databaseHelper;
    private MyApplication myApplication;

    public HomeLatestAdapter(Context context, ArrayList<ItemLatest> dataList) {
        this.dataList = dataList;
        this.mContext = context;
        databaseHelper = new DatabaseHelper(mContext);
        myApplication = MyApplication.getInstance();
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home_latest_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemRowHolder holder, final int position) {
        final ItemLatest singleItem = dataList.get(position);

        holder.text.setText(singleItem.getLatestVideoName());
        holder.txt_cat_name.setText(singleItem.getLatestCategoryName());
        holder.text_view.setText(JsonUtils.Format(Integer.parseInt(singleItem.getLatestVideoView())));

        switch (singleItem.getLatestVideoType()) {
            case "local":
            case "server_url":
            case "vimeo":
            case "embeded_code":
                Picasso.get().load(singleItem.getLatestVideoImgBig()).into(holder.image);
                break;
            case "youtube":
                Picasso.get().load(Constant.YOUTUBE_IMAGE_FRONT + singleItem.getLatestVideoPlayId() + Constant.YOUTUBE_SMALL_IMAGE_BACK).into(holder.image);
                break;
            case "dailymotion":
                Picasso.get().load(Constant.DAILYMOTION_IMAGE_PATH + singleItem.getLatestVideoPlayId()).into(holder.image);
                break;
        }

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.LATEST_IDD = singleItem.getLatestId();
                PopUpAds.ShowInterstitialAds(mContext);
            }
        });

        holder.image_pop_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, holder.image_pop_up);
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
                                Toast.makeText(mContext, mContext.getString(R.string.favourite_add), Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.option_remove_favourite:
                                databaseHelper.removeFavouriteById(singleItem.getLatestId());
                                Toast.makeText(mContext, mContext.getString(R.string.favourite_remove), Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.option_share:
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.share_msg) + "\n" + "https://play.google.com/store/apps/details?id=" + mContext.getPackageName());
                                sendIntent.setType("text/plain");
                                mContext.startActivity(sendIntent);
                                break;
                            case R.id.option_report:
                                if (myApplication.getIsLogin()) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("postId", singleItem.getLatestId());
                                    ReportFragment reportFragment = new ReportFragment();
                                    reportFragment.setArguments(bundle);
                                    reportFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), reportFragment.getTag());

                                } else {
                                    final PrettyDialog dialog = new PrettyDialog(mContext);
                                    dialog.setTitle(mContext.getString(R.string.dialog_warning))
                                            .setTitleColor(R.color.dialog_text)
                                            .setMessage(mContext.getString(R.string.login_require))
                                            .setMessageColor(R.color.dialog_text)
                                            .setAnimationEnabled(false)
                                            .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                                                @Override
                                                public void onClick() {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .addButton(mContext.getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                                @Override
                                                public void onClick() {
                                                    dialog.dismiss();
                                                    Intent intent_login = new Intent(mContext, SignInActivity.class);
                                                    intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    mContext.startActivity(intent_login);
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

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        public ImageView image, image_pop_up;
        private TextView text, txt_cat_name, text_view;
        private LinearLayout lyt_parent;

        private ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            lyt_parent = itemView.findViewById(R.id.rootLayout);
            txt_cat_name = itemView.findViewById(R.id.text_category);
            text_view = itemView.findViewById(R.id.text_view);
            image_pop_up = itemView.findViewById(R.id.image_pop_up);
        }
    }
}
