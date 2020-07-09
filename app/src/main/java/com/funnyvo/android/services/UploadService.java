package com.funnyvo.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.funnyvo.android.apirequest.MultipartRequest;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.videorecording.AnimatedGifEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.funnyvo.android.simpleclasses.Variables.APP_NAME;

// this the background service which will upload the video into database
public class UploadService extends Service {

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public UploadService getService() {
            return UploadService.this;
        }
    }

    boolean mAllowRebind;
    private ServiceCallback Callback;
    private Uri uri;
    private String video_base64 = "", thumb_base_64 = "", Gif_base_64 = "";
    private String description;
    private SharedPreferences sharedPreferences;
    private File gifFilePath;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    public UploadService() {
        super();
    }

    public UploadService(ServiceCallback serviceCallback) {
        Callback = serviceCallback;
    }

    public void setCallbacks(ServiceCallback serviceCallback) {
        Callback = serviceCallback;
    }

    @Override
    public void onCreate() {
        sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction().equals("startservice")) {
                showNotification();

                String uri_string = intent.getStringExtra("uri");
                uri = Uri.parse(uri_string);
                description = intent.getStringExtra("desc");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bmThumbnail;
                        bmThumbnail = ThumbnailUtils.createVideoThumbnail(uri.getPath(),
                                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                        Bitmap bmThumbnailResized = Bitmap.createScaledBitmap(bmThumbnail, (int) (bmThumbnail.getWidth() * 0.4), (int) (bmThumbnail.getHeight() * 0.4), true);
                        thumb_base_64 = bitmapToBase64(bmThumbnailResized);

                        File videoFile = new File(uri.getPath());
                        File thumbNail = saveBitmapInFile(bmThumbnailResized);

                        try {
                            video_base64 = encodeFileToBase64Binary(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Uri myVideoUri = Uri.parse(videoFile.toString());

                        final MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
                        mmRetriever.setDataSource(videoFile.getAbsolutePath());

                        final MediaPlayer mp = MediaPlayer.create(getBaseContext(), myVideoUri);

                        final ArrayList<Bitmap> frames = new ArrayList<Bitmap>();

                        for (int i = 1000000; i < 2000 * 1000; i += 100000) {
                            Bitmap bitmap = mmRetriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                            Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true);
                            frames.add(resized);
                        }

                        Gif_base_64 = Base64.encodeToString(generateGIF(frames), Base64.DEFAULT);
                        // + Functions.getRandomString()

                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("fb_id", sharedPreferences.getString(Variables.u_id, "0"));
                        headers.put("version", getResources().getString(R.string.version));
                        headers.put("device", getResources().getString(R.string.device));
                        headers.put("tokon", sharedPreferences.getString(Variables.api_token, ""));
                        headers.put("deviceid", sharedPreferences.getString(Variables.device_id, ""));

                        HashMap<String, String> stringRequest = new HashMap<String, String>();
                        stringRequest.put("fb_id", sharedPreferences.getString(Variables.u_id, ""));
                        stringRequest.put("sound_id", Variables.Selected_sound_id);
                        stringRequest.put("description", description);

                        HashMap<String, File> fileRequest = new HashMap<String, File>();
                        fileRequest.put("video", videoFile);
                        fileRequest.put("thum", thumbNail);
                        fileRequest.put("gif", gifFilePath);
                        MultipartRequest multipartRequest = new MultipartRequest(Variables.uploadVideo, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (!Variables.is_secure_info)
                                    Log.e(APP_NAME, error.getLocalizedMessage());

                                stopForeground(true);
                                stopSelf();

                                Callback.showResponse("Their is some kind of problem from Server side Please Try Later");
                            }

                        }, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                if (!Variables.is_secure_info)
                                    Log.d(APP_NAME, response);

                                stopForeground(true);
                                stopSelf();

                                Callback.showResponse("Your Video is uploaded Successfully");
                            }

                        }, fileRequest, stringRequest, headers);
                        RequestQueue request = Volley.newRequestQueue(UploadService.this);
                        request.getCache().clear();
                        request.add(multipartRequest);

                    }
                }
                ).start();


            } else if (intent.getAction().equals("stopservice")) {
                stopForeground(true);
                stopSelf();
            }

        }

        return Service.START_STICKY;
    }

    private File saveBitmapInFile(Bitmap bmp) {
        // + Functions.getRandomString()
        File fileName = new File(Variables.app_folder, "thumbnail" + Functions.getRandomString() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;
    }

    // this will show the sticky notification during uploading video
    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        final String CHANNEL_ID = "default";
        final String CHANNEL_NAME = "Default";

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        androidx.core.app.NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle("Uploading Video")
                .setContentText("Please wait! Video is uploading....")
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        android.R.drawable.stat_sys_upload))
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        startForeground(101, notification);
    }


    // for thumbnail
    public String bitmapToBase64(Bitmap imagebitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagebitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] byteArray = baos.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }


    // for video base64
    private String encodeFileToBase64Binary(Uri fileName)
            throws IOException {

        File file = new File(fileName.getPath());
        byte[] bytes = loadFile(file);
        String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encodedString;
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    //for video gif image
    public byte[] generateGIF(ArrayList<Bitmap> bitmaps) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out);
            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

            encoder.addFrame(decoded);

        }

        encoder.finish();


        gifFilePath = new File(Variables.app_folder, "upload" + Functions.getRandomString() + ".gif");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(gifFilePath);
            outputStream.write(bos.toByteArray());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return bos.toByteArray();
    }
}