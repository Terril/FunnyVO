package com.funnyvo.android.SoundLists;

import java.util.ArrayList;

/**
 * Created by AQEEL on 2/22/2019.
 */


public class Sounds_GetSet {

    public String id, sound_name, description, section, thum, date_created, fav;
    public String acc_path;
    //public String mp3_path;
}

class Sound_catagory_Get_Set {
    public String catagory;
    ArrayList<Sounds_GetSet> sound_list = new ArrayList<>();
}
