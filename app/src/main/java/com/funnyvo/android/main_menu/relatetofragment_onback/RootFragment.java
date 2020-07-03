package com.funnyvo.android.main_menu.relatetofragment_onback;

import androidx.fragment.app.Fragment;


public class RootFragment extends Fragment implements OnBackPressListener {

    @Override
    public boolean onBackPressed() {
        return new BackPressImplementation(this).onBackPressed();
    }
}