package com.funnyvo.android.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchMainFragment extends RootFragment {

    View view;
    Context context;

    public static EditText search_edit;
    TextView search_btn;

    protected TabLayout tabLayout;
    protected ViewPager menu_pager;
    ViewPagerAdapter adapter;

    public SearchMainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search_main, container, false);
        context = getContext();

        search_edit = view.findViewById(R.id.search_edit);

        search_btn = view.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menu_pager != null) {
                    menu_pager.removeAllViews();
                }
                setTabs();
            }
        });


        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (search_edit.getText().toString().length() > 0) {
                    search_btn.setVisibility(View.VISIBLE);
                } else {
                    search_btn.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        search_edit.setFocusable(true);
        UIUtil.showKeyboard(context, search_edit);

        return view;
    }


    public void setTabs() {

        adapter = new ViewPagerAdapter(getChildFragmentManager());
        menu_pager = (ViewPager) view.findViewById(R.id.viewpager);
        menu_pager.setOffscreenPageLimit(3);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        adapter.addFrag(new SearchFragment("users"), "Users");
        adapter.addFrag(new SearchFragment("video"), "Videos");
        adapter.addFrag(new SoundListFragment("sound"), "Sounds");

        menu_pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(menu_pager);

    }


}
