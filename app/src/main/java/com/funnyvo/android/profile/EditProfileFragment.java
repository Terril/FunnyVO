package com.funnyvo.android.profile;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ApiCallBack;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.funnyvo.android.main_menu.MainMenuFragment.hasPermissions;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends RootFragment implements View.OnClickListener {

    private View view;
    private Context context;
    FragmentCallback fragment_callback;

    public EditProfileFragment() { }

    public EditProfileFragment(FragmentCallback fragment_callback) {
        this.fragment_callback = fragment_callback;
    }

    private ImageView profile_image;
    private EditText username_edit, firstname_edit, lastname_edit, user_bio_edit;

    private RadioButton male_btn, female_btn;

    private String imageFilePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        context = getContext();

        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.save_btn).setOnClickListener(this);
        view.findViewById(R.id.upload_pic_btn).setOnClickListener(this);


        username_edit = view.findViewById(R.id.username_edit);
        profile_image = view.findViewById(R.id.profile_image);
        firstname_edit = view.findViewById(R.id.firstname_edit);
        lastname_edit = view.findViewById(R.id.lastname_edit);
        user_bio_edit = view.findViewById(R.id.user_bio_edit);


        username_edit.setText(Variables.sharedPreferences.getString(Variables.u_name, ""));
        firstname_edit.setText(Variables.sharedPreferences.getString(Variables.f_name, ""));
        lastname_edit.setText(Variables.sharedPreferences.getString(Variables.l_name, ""));

        Picasso.with(context)
                .load(Variables.sharedPreferences.getString(Variables.u_pic, ""))
                .placeholder(R.drawable.profile_image_placeholder)
                .resize(200, 200)
                .centerCrop()
                .into(profile_image);


        male_btn = view.findViewById(R.id.male_btn);
        female_btn = view.findViewById(R.id.female_btn);


        callApiForUserDetails();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.save_btn:
                if (checkValidation()) {
                    callApiForEditProfile();
                }
                break;
            case R.id.upload_pic_btn:
                selectImage();
                break;
        }
    }


    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    if (checkPermissions())
                        openCameraIntent();

                } else if (options[item].equals("Choose from Gallery")) {
                    if (checkPermissions()) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                    }
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }

            }

        });

        builder.show();

    }


    public boolean checkPermissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 2);
        } else {

            return true;
        }
        return false;
    }


    // below three method is related with taking the picture from camera
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context.getApplicationContext(), getActivity().getPackageName() + ".fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, 1);
            }
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public String getPath(Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        byte[] imageByteArray = new byte[0];
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Matrix matrix = new Matrix();
                try {
                    ExifInterface exif = new ExifInterface(imageFilePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri selectedImage = (Uri.fromFile(new File(imageFilePath)));

                InputStream imageStream = null;
                try {
                    imageStream = Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);
                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);

                Bitmap resized = Bitmap.createScaledBitmap(rotatedBitmap, (int) (rotatedBitmap.getWidth() * 0.7), (int) (rotatedBitmap.getHeight() * 0.7), true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                imageByteArray = baos.toByteArray();
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                InputStream imageStream = null;
                try {
                    imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

                String path = getPath(selectedImage);
                Matrix matrix = new Matrix();
                ExifInterface exif = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    try {
                        exif = new ExifInterface(path);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                matrix.postRotate(90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                matrix.postRotate(180);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                matrix.postRotate(270);
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
                Bitmap resized = Bitmap.createScaledBitmap(rotatedBitmap, (int) (rotatedBitmap.getWidth() * 0.5), (int) (rotatedBitmap.getHeight() * 0.5), true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                imageByteArray = baos.toByteArray();
            }
            saveImage(imageByteArray);

        }

    }

    // this will check the validations like none of the field can be the empty
    public boolean checkValidation() {

        String uname = username_edit.getText().toString();
        String firstname = firstname_edit.getText().toString();
        String lastname = lastname_edit.getText().toString();

        if (TextUtils.isEmpty(uname) || uname.length() < 2) {
            Toast.makeText(context, "Please enter correct username", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(firstname)) {
            Toast.makeText(context, "Please enter first name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(lastname)) {
            Toast.makeText(context, "Please enter last name", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveImage(byte[] imageByteArray) {
        showProgressDialog();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String key = reference.push().getKey();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference filelocation = storageReference.child("User_image")
                .child(key + ".jpg");

        filelocation.putBytes(imageByteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    filelocation.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            callApiForImage(uri.toString());
                        }
                    });
                } else {
                    dismissProgressDialog();
                }
            }
        });
    }


    public void callApiForImage(final String image_link) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("image_link", image_link);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.uploadImage, parameters, new Callback() {
            @Override
            public void response(String resp) {
                dismissProgressDialog();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {

                        Variables.sharedPreferences.edit().putString(Variables.u_pic, image_link).commit();
                        ProfileFragment.pic_url = image_link;
                        Variables.user_pic = image_link;

                        Picasso.with(context)
                                .load(ProfileFragment.pic_url)
                                .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                                .resize(200, 200).centerCrop().into(profile_image);


                        Toast.makeText(context, "Image Update Successfully", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    // this will update the latest info of user in database
    public void callApiForEditProfile() {
        showProgressDialog();
        String uname = username_edit.getText().toString().toLowerCase().replaceAll("\\s", "");
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("username", uname.replaceAll("@", ""));
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("first_name", firstname_edit.getText().toString());
            parameters.put("last_name", lastname_edit.getText().toString());

            if (male_btn.isChecked()) {
                parameters.put("gender", "Male");

            } else if (female_btn.isChecked()) {
                parameters.put("gender", "Female");
            }

            parameters.put("bio", user_bio_edit.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.edit_profile, parameters, new Callback() {
            @Override
            public void response(String resp) {
                dismissProgressDialog();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    JSONArray msg = response.optJSONArray("msg");
                    if (code.equals("200")) {

                        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();

                        String u_name = username_edit.getText().toString();
                        if (!u_name.contains("@"))
                            u_name = "@" + u_name;

                        editor.putString(Variables.u_name, u_name);
                        editor.putString(Variables.f_name, firstname_edit.getText().toString());
                        editor.putString(Variables.l_name, lastname_edit.getText().toString());
                        editor.commit();

                        Variables.user_name = u_name;

                        getActivity().onBackPressed();
                    } else {

                        if (msg != null) {
                            JSONObject jsonObject = msg.optJSONObject(0);
                            Toast.makeText(context, jsonObject.optString("response"), Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    // this will get the user data and parse the data and show the data into views
    public void callApiForUserDetails() {
        showProgressDialog();
        Functions.callApiForGetUserData(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id, ""),
                new ApiCallBack() {
                    @Override
                    public void arrayData(ArrayList arrayList) {
                    }

                    @Override
                    public void onSuccess(String responce) {
                        dismissProgressDialog();
                        parseUserData(responce);
                    }

                    @Override
                    public void onFailure(String responce) {

                    }
                });
    }

    public void parseUserData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            String code = jsonObject.optString("code");

            if (code.equals("200")) {
                JSONArray msg = jsonObject.optJSONArray("msg");
                JSONObject data = msg.getJSONObject(0);

                firstname_edit.setText(data.optString("first_name"));
                lastname_edit.setText(data.optString("last_name"));

                String picture = data.optString("profile_pic");

                Picasso.with(context)
                        .load(picture)
                        .placeholder(R.drawable.profile_image_placeholder)
                        .into(profile_image);

                String gender = data.optString("gender");
                if (gender.equals("Male")) {
                    male_btn.setChecked(true);
                } else {
                    female_btn.setChecked(true);
                }

                user_bio_edit.setText(data.optString("bio"));
            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();

        if (fragment_callback != null)
            fragment_callback.responseCallBackFromFragment(new Bundle());
    }
}
