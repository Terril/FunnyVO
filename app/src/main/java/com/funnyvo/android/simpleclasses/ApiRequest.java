package com.funnyvo.android.simpleclasses;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.funnyvo.android.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.security.cert.CertificateException;

public class ApiRequest {

    public static void callApi(final Context context, final String url, JSONObject jsonObject,
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
                            callback.response(response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (!Variables.is_secure_info) {
                    final String[] urlsplit = url.split("/");
                    Log.d(Variables.tag + urlsplit[urlsplit.length - 1], error.toString());
                }

                if (callback != null)
                    callback.response(error.toString());

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

        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory(context));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context, hurlStack);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjReq);
    }

    static SSLSocketFactory getSSLSocketFactory(Context context)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        AssetManager assetManager =  context.getResources().getAssets();
        InputStream is = assetManager.open("STAR_funnyvo_com.crt");
        InputStream caInput = new BufferedInputStream(is);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            // System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }
}
