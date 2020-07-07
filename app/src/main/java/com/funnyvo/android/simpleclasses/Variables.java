package com.funnyvo.android.simpleclasses;

import android.content.SharedPreferences;
import android.os.Environment;

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

    public static String outputfile = app_folder + "output.mp4";
    public static String outputfile2 = app_folder + "output2.mp4";
    public static String OUTPUT_FILE_MOTION = app_folder + "output_file_motions.mp4";
    public static String OUTPUT_FILTER_FILE = app_folder + "output-filtered" + Functions.getRandomString() + ".mp4";

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

    public static final String privacy_policy = "https://www.privacypolicygenerator.info/live.php?token=";


    public static String base_url = "http://api.funnyvo.com/API/";
    public static String domain = base_url + "index.php?p=";


    public static final String SignUp = domain + "signup";
    public static final String uploadVideo = domain + "uploadVideo";
    public static final String showAllVideos = domain + "showAllVideos";
    public static final String showMyAllVideos = domain + "showMyAllVideos";
    public static final String likeDislikeVideo = domain + "likeDislikeVideo";
    public static final String updateVideoView = domain + "updateVideoView";
    public static final String allSounds = domain + "allSounds";
    public static final String fav_sound = domain + "fav_sound";
    public static final String my_FavSound = domain + "my_FavSound";
    public static final String my_liked_video = domain + "my_liked_video";
    public static final String follow_users = domain + "follow_users";
    public static final String discover = domain + "discover";
    public static final String showVideoComments = domain + "showVideoComments";
    public static final String postComment = domain + "postComment";
    public static final String edit_profile = domain + "edit_profile";
    public static final String get_user_data = domain + "get_user_data";
    public static final String get_followers = domain + "get_followers";
    public static final String get_followings = domain + "get_followings";
    public static final String SearchByHashTag = domain + "SearchByHashTag";
    public static final String sendPushNotification = domain + "sendPushNotification";
    public static final String uploadImage = domain + "uploadImage";
    public static final String DeleteVideo = domain + "DeleteVideo";
    public static final String search = domain + "search";
    public static final String getNotifications = domain + "getNotifications";
    public static final String getVerified = domain + "getVerified";


}
