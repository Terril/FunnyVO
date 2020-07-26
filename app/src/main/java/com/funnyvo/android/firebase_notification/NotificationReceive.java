package com.funnyvo.android.firebase_notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentTransaction;

import com.funnyvo.android.chat.ChatActivity;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.main_menu.MainMenuFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Variables;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.funnyvo.android.simpleclasses.Variables.APP_NAME;

public class NotificationReceive extends FirebaseMessagingService {

    SharedPreferences sharedPreferences;
    String pic;
    String title;
    String message;
    String senderid;
    String receiverid;
    String action_type;

    Handler handler = new Handler();
    Runnable runnable;

    Snackbar snackbar;

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);
            title = remoteMessage.getData().get("title");
            if (title != null)
                title.replaceAll("@", "");

            message = remoteMessage.getData().get("body");
            pic = remoteMessage.getData().get("icon");
            senderid = remoteMessage.getData().get("senderid");
            receiverid = remoteMessage.getData().get("receiverid");
            action_type = remoteMessage.getData().get("action_type");

            if (!ChatActivity.senderid_for_check_notification.equals(senderid)) {

                SendNotification sendNotification = new SendNotification(this);
                sendNotification.execute(pic);
            }
        }
    }

    // this will store the user firebase token in local storage
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);

        if (s == null) {

        } else if (s.equals("null")) {

        } else if (s.equals("")) {

        } else if (s.length() < 6) {

        } else {
            sharedPreferences.edit().putString(Variables.device_token, s).commit();
        }
    }

    private class SendNotification extends AsyncTask<String, Void, Bitmap> {
        Context ctx;

        public SendNotification(Context context) {
            super();
            this.ctx = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            // in notification first we will get the image of the user and then we will show the notification to user
            // in onPostExecute
            InputStream in;
            try {

                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @SuppressLint("WrongConstant")
        @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);

            showNotification(ctx, title, message, result);

            if (MainMenuActivity.mainMenuActivity != null) {
                if (snackbar != null) {
                    snackbar.getView().setVisibility(View.INVISIBLE);
                    snackbar.dismiss();
                }

                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }


                View layout = MainMenuActivity.mainMenuActivity.getLayoutInflater().inflate(R.layout.item_layout_custom_notification, null);
                TextView titletxt = layout.findViewById(R.id.username);
                TextView messagetxt = layout.findViewById(R.id.message);
                ImageView imageView = layout.findViewById(R.id.user_image);
                titletxt.setText(title);
                messagetxt.setText(message);

                if (result != null)
                    imageView.setImageBitmap(result);


                snackbar = Snackbar.make(MainMenuActivity.mainMenuActivity.findViewById(R.id.container), "", Snackbar.LENGTH_LONG);

                Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
                TextView textView = (TextView) snackbarLayout.findViewById(R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);

                final ViewGroup.LayoutParams params = snackbar.getView().getLayoutParams();
                if (params instanceof CoordinatorLayout.LayoutParams) {
                    ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP;
                } else {
                    ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
                }

                snackbarLayout.setPadding(0, 0, 0, 0);
                snackbarLayout.addView(layout, 0);


                snackbar.getView().setVisibility(View.INVISIBLE);

                snackbar.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                        snackbar.getView().setVisibility(View.VISIBLE);
                    }

                });


                runnable = new Runnable() {
                    @Override
                    public void run() {
                        snackbar.getView().setVisibility(View.INVISIBLE);

                    }
                };

                handler.postDelayed(runnable, 2750);

                snackbar.setDuration(Snackbar.LENGTH_LONG);
                snackbar.show();

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        snackbar.dismiss();
                        snackbar.getView().setVisibility(View.INVISIBLE);

                        if (action_type.equals("message"))
                            chatFragment(senderid, title, pic);
                    }
                });
            }
        }
    }


    private void showNotification(Context context, String title, String message, Bitmap bitmap) {

        // The id of the channel.
        final String CHANNEL_ID = "default";
        final String CHANNEL_NAME = "Default";

        Intent notificationIntent = new Intent(context, MainMenuActivity.class);
        notificationIntent.putExtra("user_id", receiverid);
        notificationIntent.putExtra("user_name", title);
        notificationIntent.putExtra("user_pic", pic);
        notificationIntent.putExtra("message", message);
        notificationIntent.putExtra("type", action_type);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(100, notification);
    }


    private void chatFragment(String receiverid, String name, String picture) {
        if (sharedPreferences.getBoolean(Variables.islogin, false)) {

            if (MainMenuFragment.tabLayout != null) {
                TabLayout.Tab tab3 = MainMenuFragment.tabLayout.getTabAt(3);
                tab3.select();
            }

            ChatActivity chat_activity = new ChatActivity();
            FragmentTransaction transaction = MainMenuActivity.mainMenuActivity.getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);

            Bundle args = new Bundle();
            args.putString("user_id", receiverid);
            args.putString("user_name", name);
            args.putString("user_pic", picture);

            chat_activity.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, chat_activity).commit();

        }
    }

}
