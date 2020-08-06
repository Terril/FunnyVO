package com.funnyvo.android.home.datamodel;

import java.io.Serializable;


public class Home implements Serializable {
    public String fb_id = "", username, first_name, last_name, profile_pic, verified;
    public String video_id, video_description = "", video_url = "", gif = "", thum = "", created_date = "";
    public String sound_id = "", sound_name = "", sound_pic = "", soundUrl = "";
    public String liked = "", like_count = "", video_comment_count = "", views = "";
    public boolean isMute;
}
