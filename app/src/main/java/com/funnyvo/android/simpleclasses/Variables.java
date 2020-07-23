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

    public static final String root = Environment.getExternalStorageDirectory().toString();
    public static final String app_folder = root + "/FunnyVO/";
    public static final String draft_app_folder = app_folder + "Draft/";

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
    public static final String isFirstTIme = "is_first_time";

    public static String outputfile = app_folder + "output.mp4";
    public static String outputfile2 = app_folder + "output2.mp4";
    public static String OUTPUT_FILE_MOTION = app_folder + "output_file_motions.mp4";
    public static String OUTPUT_FILE_MESSAGE = app_folder + "output_file_message.mp4";
    public static String OUTPUT_FILTER_FILE = app_folder + "output-filtered" + Functions.getRandomString() + ".mp4";
    public static String OUTPUT_FILE_TRIMMED = app_folder + "output_trimmed.mp4";

    public static String gallery_trimed_video = app_folder + "gallery_trimed_video.mp4";
    public static String gallery_resize_video = app_folder + "gallery_resize_video.mp4";

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


    public static final boolean is_secure_info = false;
    public static final boolean is_remove_ads = false;

    public final static int permission_camera_code = 786;
    public final static int permission_write_data = 788;
    public final static int permission_Read_data = 789;
    public final static int permission_Recording_audio = 790;
    public final static int Pick_video_from_gallery = 791;

    public static String gif_api_key1 = "giphy_api_key_here";

    public static final String privacy_policy = "https://funnyvo.com/privacy_policy.html";

    public static String API_SUCCESS_CODE = "200";

    public static String base_url =  BuildConfig.hostAPI ;
    public static String DOMAIN = base_url + "index.php?p=";

    public static final String SignUp = DOMAIN + "signup";
    public static final String uploadVideo = DOMAIN + "uploadVideo";
    public static final String showAllVideos = DOMAIN + "showAllVideos";
    public static final String showMyAllVideos = DOMAIN + "showMyAllVideos";
    public static final String likeDislikeVideo = DOMAIN + "likeDislikeVideo";
    public static final String updateVideoView = DOMAIN + "updateVideoView";
    public static final String allSounds = DOMAIN + "allSounds";
    public static final String fav_sound = DOMAIN + "fav_sound";
    public static final String my_FavSound = DOMAIN + "my_FavSound";
    public static final String my_liked_video = DOMAIN + "my_liked_video";
    public static final String follow_users = DOMAIN + "follow_users";
    public static final String discover = DOMAIN + "discover";
    public static final String showVideoComments = DOMAIN + "showVideoComments";
    public static final String postComment = DOMAIN + "postComment";
    public static final String edit_profile = DOMAIN + "edit_profile";
    public static final String get_user_data = DOMAIN + "get_user_data";
    public static final String get_followers = DOMAIN + "get_followers";
    public static final String get_followings = DOMAIN + "get_followings";
    public static final String SearchByHashTag = DOMAIN + "SearchByHashTag";
    public static final String sendPushNotification = DOMAIN + "sendPushNotification";
    public static final String uploadImage = DOMAIN + "uploadImage";
    public static final String DeleteVideo = DOMAIN + "DeleteVideo";
    public static final String search = DOMAIN + "search";
    public static final String getNotifications = DOMAIN + "getNotifications";
    public static final String getVerified = DOMAIN + "getVerified";
    public static final String Logout = DOMAIN + "logout";
}
