package com.funnyvo.android.simpleclasses;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/4/2019.
 */

public interface ApiCallBack {

    void arrayData(ArrayList arrayList);

    void onSuccess(String responce);

    void onFailure(String responce);


}
