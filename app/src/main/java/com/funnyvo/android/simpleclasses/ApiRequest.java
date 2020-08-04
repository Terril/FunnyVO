package com.funnyvo.android.simpleclasses;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.funnyvo.android.BuildConfig;
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
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ApiRequest {

    public static void callApi(final Context context, final String url, JSONObject jsonObject,
                               final Callback callback) {

        if (Variables.is_secure_info) {
            final String[] urlsplit = url.split("/");
            Log.d(Variables.tag, url);

            if (jsonObject != null)
                Log.d(Variables.tag + urlsplit[urlsplit.length - 1], jsonObject.toString());
        }

        if (checkIfNetworkAvailable(context)) {
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            if (Variables.is_secure_info) {
                                final String[] urlsplit = url.split("/");
                                Log.d(Variables.tag + urlsplit[urlsplit.length - 1], response.toString());
                            }
                            if (callback != null)
                                callback.response(response.toString());
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (Variables.is_secure_info) {
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
                    headers.put("version", BuildConfig.VERSION_NAME);
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
    }

    public static SSLSocketFactory getSSLSocketFactory(Context context)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        AssetManager assetManager = context.getResources().getAssets();
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

    private static boolean checkIfNetworkAvailable(Context context) {
        if (!isNetworkAvailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.no_network)
                    .setMessage(R.string.mind_checking_network)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            return false;
        }
        return true;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                }
            } else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        return true;
                    }
                } catch (Exception e) {
                }
            }
        }
        return false;
    }
}
