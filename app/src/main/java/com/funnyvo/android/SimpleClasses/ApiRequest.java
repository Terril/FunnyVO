package com.funnyvo.android.SimpleClasses;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.funnyvo.android.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiRequest {

    public static void Call_Api(final Context context, final String url, JSONObject jsonObject,
                                final Callback callback) {


        if (!Variables.is_secure_info) {
            final String[] urlsplit = url.split("/");
            Log.d(Variables.tag, url);

            if (jsonObject != null)
                Log.d(Variables.tag + urlsplit[urlsplit.length - 1], jsonObject.toString());
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (!Variables.is_secure_info) {
                            final String[] urlsplit = url.split("/");
                            Log.d(Variables.tag + urlsplit[urlsplit.length - 1], response.toString());
                        }

                        if (callback != null)
                            callback.Responce(response.toString());

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (!Variables.is_secure_info) {
                    final String[] urlsplit = url.split("/");
                    Log.d(Variables.tag + urlsplit[urlsplit.length - 1], error.toString());
                }

                if (callback != null)
                    callback.Responce(error.toString());

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("fb-id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
                headers.put("version", context.getResources().getString(R.string.version));
                headers.put("device", context.getResources().getString(R.string.device));
                headers.put("tokon", Variables.sharedPreferences.getString(Variables.api_token, ""));
                headers.put("deviceid", Variables.sharedPreferences.getString(Variables.device_id, ""));
                Log.d(Variables.tag, headers.toString());
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjReq);
    }
}
