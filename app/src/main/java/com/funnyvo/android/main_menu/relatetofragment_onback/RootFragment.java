package com.funnyvo.android.main_menu.relatetofragment_onback;

import androidx.fragment.app.Fragment;

/**
 * Created by AQEEL on 3/30/2018.
 */

public class RootFragment extends Fragment implements OnBackPressListener {

    @Override
    public boolean onBackPressed() {
        return new BackPressImplementation(this).onBackPressed();
    }
}