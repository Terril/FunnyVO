package com.funnyvo.android.videorecording;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.appyvet.materialrangebar.RangeBar;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Variables;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingTimeRangeFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    RangeBar seekbar;

    int selected_value = 3;
    int recording_done_time = 0, total_time = 0;
    TextView range_txt;

    public RecordingTimeRangeFragment() {
        // Required empty public constructor
    }

    FragmentCallback fragment_callback;

    public RecordingTimeRangeFragment(FragmentCallback fragment_callback) {
        this.fragment_callback = fragment_callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recording_time_rang, container, false);
        context = getContext();

        Bundle bundle = getArguments();
        if (bundle != null) {
            recording_done_time = bundle.getInt("end_time");
            total_time = bundle.getInt("total_time");
        }
        seekbar = view.findViewById(R.id.seekbar);
        seekbar.setOnlyOnDrag(true);
        seekbar.setTickEnd(total_time);

        seekbar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {

                Log.d(Variables.tag, "" + leftPinIndex);
                Log.d(Variables.tag, "" + rightPinIndex);
                Log.d(Variables.tag, "" + selected_value);


                if (leftPinIndex > 0) {
                    seekbar.setRangePinsByValue(0, rightPinIndex);
                } else if (rightPinIndex < recording_done_time) {
                    seekbar.setRangePinsByValue(0, recording_done_time);
                }

                range_txt.setText(rightPinIndex + "s/" + total_time + "s");
                selected_value = rightPinIndex;

            }

            @Override
            public void onTouchStarted(RangeBar rangeBar) {

            }

            @Override
            public void onTouchEnded(RangeBar rangeBar) {

            }
        });

        range_txt = view.findViewById(R.id.range_txt);


        view.findViewById(R.id.start_recording_layout).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_recording_layout:
                Bundle bundle = new Bundle();
                bundle.putInt("end_time", selected_value);
                fragment_callback.responseCallBackFromFragment(bundle);
                dismiss();
                break;
        }
    }


}
