package com.cartoony.util;



import com.cartoony.allinonevideo.BuildConfig;

import java.io.Serializable;

public class Constant implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	//server url
	public static final String SERVER_URL= BuildConfig.server_url;

	public static final String API_URL = SERVER_URL + "api.php";

	public static final String IMAGE_PATH_URL =SERVER_URL+"images/";

	public static final String YOUTUBE_IMAGE_FRONT="http://img.youtube.com/vi/";
	public static final String YOUTUBE_SMALL_IMAGE_BACK="/mqdefault.jpg";
    public static final String YOUTUBE_SMALL_IMAGE_HD="/maxresdefault.jpg";

	public static final String DAILYMOTION_IMAGE_PATH="http://www.dailymotion.com/thumbnail/video/";

	public static final String LATEST_ARRAY_NAME="ALL_IN_ONE_VIDEO";
	public static final String RELATED_ARRAY="related";
	public static final String COMMENT_ARRAY="user_comments";

	public static final String LATEST_ID="id";
	public static final String LATEST_CATID="cat_id";
	public static final String LATEST_CAT_NAME="category_name";
	public static final String LATEST_VIDEO_URL="video_url";
	public static final String LATEST_VIDEO_ID="video_id";
	public static final String LATEST_VIDEO_DURATION="video_type";
	public static final String LATEST_VIDEO_NAME="video_title";
	public static final String LATEST_VIDEO_DESCRIPTION="video_description";
	public static final String LATEST_IMAGE_URL="video_thumbnail_b";
 	public static final String LATEST_TYPE="video_type";
	public static final String LATEST_VIEW="totel_viewer";
	public static final String LATEST_RATE="rate_avg";

 	public static final String CATEGORY_NAME="category_name";
	public static final String CATEGORY_CID="cid";
	public static final String CATEGORY_IMAGE="category_image";

	public static final String COMMENT_ID="video_id";
	public static final String COMMENT_NAME="user_name";
	public static final String COMMENT_MSG="comment_text";

	//for title display in CategoryItemF
	public static String CATEGORY_TITLEE;
	public static String CATEGORY_IDD;
	public static  String LATEST_IDD;
	public static  String LATEST_CMT_IDD;
	public static int AD_COUNT = 0;

	public static int GET_SUCCESS_MSG;
	public static final String MSG = "msg";
    public static final String MSG2 = "message";
	public static final String SUCCESS = "success";
	public static final String USER_NAME = "name";
	public static final String USER_ID = "user_id";
	public static final String USER_EMAIL = "email";
	public static final String USER_PHONE = "phone";


  	public static final String APP_NAME="app_name";
	public static final String APP_IMAGE="app_logo";
	public static final String APP_VERSION="app_version";
	public static final String APP_AUTHOR="app_author";
	public static final String APP_CONTACT="app_contact";
	public static final String APP_EMAIL="app_email";
	public static final String APP_WEBSITE="app_website";
	public static final String APP_DESC="app_description";
	public static final String APP_PRIVACY="app_privacy_policy";
	public static final String APP_DEVELOP="app_developed_by";
	public static final String APP_PACKAGE_NAME = "package_name";

	public static final String ADS_BANNER_ID="banner_ad_id";
	public static final String ADS_FULL_ID="interstital_ad_id";
	public static final String ADS_BANNER_ON_OFF="banner_ad";
	public static final String ADS_FULL_ON_OFF="interstital_ad";
	public static final String ADS_PUB_ID="publisher_id";
	public static final String ADS_CLICK="interstital_ad_click";
	public static final String NATIVE_AD_ON_OFF = "native_ad";
	public static final String NATIVE_AD_ID = "native_ad_id";
	public static final String BANNER_TYPE="banner_ad_type";
	public static final String FULL_TYPE="interstital_ad_type";
	public static final String NATIVE_TYPE="native_ad_type";
	public static String SAVE_ADS_NATIVE_ON_OFF,SAVE_NATIVE_ID,SAVE_BANNER_TYPE,SAVE_FULL_TYPE,SAVE_NATIVE_TYPE,SAVE_NATIVE_CLICK_OTHER,
			SAVE_ADS_BANNER_ID,SAVE_ADS_FULL_ID,SAVE_ADS_BANNER_ON_OFF="",SAVE_ADS_FULL_ON_OFF,SAVE_ADS_PUB_ID,SAVE_ADS_CLICK;

}
