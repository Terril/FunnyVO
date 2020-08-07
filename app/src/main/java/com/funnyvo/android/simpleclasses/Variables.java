package com.funnyvo.android.simpleclasses;

import android.content.SharedPreferences;
import android.os.Environment;

import com.funnyvo.android.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Variables {
    public static final String APP_NAME = "FunnyVO";
    public static final String device = "android";

    public static int screen_width;
    public static int screen_height;

    public static final String SelectedAudio_MP3 = "SelectedAudio.mp3";
    public static final String SelectedAudio_AAC = "SelectedAudio.aac";

    public static final String ROOT = Environment.getExternalStorageDirectory().toString();
    public static final String APP_FOLDER = ROOT + "/FunnyVO/";
    public static final String draft_app_folder = APP_FOLDER + "Draft/";

    public static int max_recording_duration = 18000;
    public static int recording_duration = 18000;

    public static SharedPreferences sharedPreferences;
    public static final String pref_name = "pref_name";
    public static final String u_id = "u_id";
    public static final String u_name = "u_name";
    public static final String u_pic = "u_pic";
    public static final String f_name = "f_name";
    public static final String l_name = "l_name";
    public static final String gender = "u_gender";
    public static final String islogin = "is_login";
    public static final String device_token = "device_token";
    public static final String api_token = "api_token";
    public static final String device_id = "device_id";
    public static final String IS_FIRST_TIME = "is_first_time";
    public static final String USER_PREF_VIDEO_VISIBILITY = "user_video_visibility";
    public static final String SHOW_ADS = "show_ads";
    public static final String PAGE_COUNT_SHOW_ADS_AFTER_VIEWS = "show_ads_after_views";

    public static String outputfile = APP_FOLDER + "output.mp4";
    public static String outputfile2 = APP_FOLDER + "output2.mp4";
    public static String OUTPUT_FILE_MOTION = APP_FOLDER + "output_file_motions.mp4";
    public static String OUTPUT_FILE_MESSAGE = APP_FOLDER + "output_file_message.mp4";
    public static String OUTPUT_FILTER_FILE = APP_FOLDER + "output-filtered" + Functions.getRandomString() + ".mp4";
    public static String OUTPUT_FILE_TRIMMED = APP_FOLDER + "output_trimmed.mp4";

    public static String gallery_trimed_video = APP_FOLDER + "gallery_trimed_video.mp4";
    public static String gallery_resize_video = APP_FOLDER + "gallery_resize_video.mp4";

    public static String user_id;
    public static String user_name;
    public static String user_pic;

    public static String tag = "FunnyVO";

    public static String Selected_sound_id = "null";


    public static final String gif_firstpart = "https://media.giphy.com/media/";
    public static final String gif_secondpart = "/100w.gif";

    public static final String gif_firstpart_chat = "https://media.giphy.com/media/";
    public static final String gif_secondpart_chat = "/200w.gif";


    public static final SimpleDateFormat df =
            new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH);

    public static final SimpleDateFormat df2 =
            new SimpleDateFormat("dd-MM-yyyy HH:mmZZ", Locale.ENGLISH);


    public static final boolean is_secure_info = BuildConfig.logsEnabled;
    public static final boolean is_remove_ads = false;

    public final static int permission_camera_code = 786;
    public final static int permission_write_data = 788;
    public final static int permission_Read_data = 789;
    public final static int permission_Recording_audio = 790;
    public final static int Pick_video_from_gallery = 791;
    public final static int PICK_VIDEO_FROM_GALLERY = 793;

    public static String gif_api_key1 = "giphy_api_key_here";

    public static final String privacy_policy = "https://funnyvo.com/privacy_policy.html";

    public static String API_SUCCESS_CODE = "200";

    public static String base_url =  BuildConfig.hostAPI ;
    public static String DOMAIN = base_url + "index.php?p=";

    public static final String FETCH_SETTINGS = DOMAIN + "getSetting";
    public static final String SIGN_UP = DOMAIN + "signup";
    public static final String UPLOAD_VIDEO = DOMAIN + "uploadVideo";
    public static final String SHOW_ALL_VIDEOS = DOMAIN + "showAllVideos";
    public static final String SHOW_ALL_VIDEOS_WITH_ADS = DOMAIN + "showAllVideosWithAD";
    public static final String SHOW_MY_ALL_VIDEOS = DOMAIN + "showMyAllVideos";
    public static final String LIKE_DISLIKE_VIDEO = DOMAIN + "likeDislikeVideo";
    public static final String UPDATE_VIDEO_VIEW = DOMAIN + "updateVideoView";
    public static final String ALL_SOUNDS = DOMAIN + "allSounds";
    public static final String FAV_SOUND = DOMAIN + "fav_sound";
    public static final String MY_FAV_SOUND = DOMAIN + "my_FavSound";
    public static final String MY_LIKED_VIDEO = DOMAIN + "my_liked_video";
    public static final String FOLLOW_USERS = DOMAIN + "follow_users";
    public static final String DISCOVER = DOMAIN + "discover";
    public static final String SHOW_VIDEO_COMMENTS = DOMAIN + "showVideoComments";
    public static final String POST_COMMENT = DOMAIN + "postComment";
    public static final String EDIT_PROFILE = DOMAIN + "edit_profile";
    public static final String GET_USER_DATA = DOMAIN + "get_user_data";
    public static final String GET_FOLLOWERS = DOMAIN + "get_followers";
    public static final String GET_FOLLOWINGS = DOMAIN + "get_followings";
    public static final String SEARCH_BY_HASH_TAG = DOMAIN + "SearchByHashTag";
    public static final String SEND_PUSH_NOTIFICATION = DOMAIN + "sendPushNotification";
    public static final String UPLOAD_IMAGE = DOMAIN + "uploadImage";
    public static final String DELETE_VIDEO = DOMAIN + "DeleteVideo";
    public static final String SEARCH = DOMAIN + "search";
    public static final String GET_NOTIFICATIONS = DOMAIN + "getNotifications";
    public static final String GET_VERIFIED = DOMAIN + "getVerified";
    public static final String UPDATE_APP_VERSION = DOMAIN + "update_version";
    public static final String LOGOUT = DOMAIN + "logout";
}
