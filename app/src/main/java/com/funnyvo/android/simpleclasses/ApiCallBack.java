package com.funnyvo.android.simpleclasses;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/4/2019.
 */

public interface ApiCallBack {

    void ArrayData(ArrayList arrayList);

    void OnSuccess(String responce);

    void OnFail(String responce);


}
