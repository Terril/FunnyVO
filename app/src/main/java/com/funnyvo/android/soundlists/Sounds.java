package com.funnyvo.android.soundlists;

import java.util.ArrayList;

public class Sounds {

    public String id, sound_name, description, section, thum, date_created, fav;
    public String acc_path, soundUrl;
    //public String mp3_path;
}

class SoundCategory {
    public String catagory;
    ArrayList<Sounds> sound_list = new ArrayList<>();
}
