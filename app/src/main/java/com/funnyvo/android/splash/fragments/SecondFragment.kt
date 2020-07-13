package com.funnyvo.android.splash.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.funnyvo.android.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_splash_first.*

class SecondFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_first, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.with(activity).load(R.drawable.funnyvo_second_pg_min).fit().into(fragmentBackground)
    }

}