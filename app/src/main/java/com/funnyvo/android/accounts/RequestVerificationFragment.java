package com.funnyvo.android.accounts;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.funnyvo.android.R;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.FileUtils;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.funnyvo.android.main_menu.MainMenuFragment.hasPermissions;

public class RequestVerificationFragment extends RootFragment implements View.OnClickListener {

    private View view;
    private Context context;
    private EditText username_edit, fullname_edit;
    private TextView file_name_txt;
    private String base_64;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_request_verification, container, false);
        context = getContext();

        view.findViewById(R.id.Goback).setOnClickListener(this);

        username_edit = view.findViewById(R.id.username_edit);
        fullname_edit = view.findViewById(R.id.fullname_edit);

        username_edit.setText(Variables.sharedPreferences.getString(Variables.u_name, ""));
        fullname_edit.setText(Variables.sharedPreferences.getString(Variables.f_name, "") + " " + Variables.sharedPreferences.getString(Variables.l_name, ""));

        file_name_txt = view.findViewById(R.id.file_name_txt);

        view.findViewById(R.id.choose_file_btn).setOnClickListener(this);
        view.findViewById(R.id.send_btn).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.choose_file_btn:
                selectImage();
                break;

            case R.id.send_btn:
                if (Check_Validation()) {
                    callApi();
                }
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
                    if (check_permissions())
                        openCameraIntent();

                } else if (options[item].equals("Choose from Gallery")) {

                    if (check_permissions()) {
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


    public boolean check_permissions() {

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

    String imageFilePath;

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


    File image_file;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

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
                image_file = new File(imageFilePath);
                Uri selectedImage = (Uri.fromFile(image_file));

                InputStream imageStream = null;
                try {
                    imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);
                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);

                Bitmap resized = Bitmap.createScaledBitmap(rotatedBitmap, (int) (rotatedBitmap.getWidth() * 0.7), (int) (rotatedBitmap.getHeight() * 0.7), true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                base_64 = Functions.bitmapToBase64(getActivity(), resized);

                if (image_file != null)
                    file_name_txt.setText(image_file.getName());
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                try {
                    image_file = FileUtils.getFileFromUri(context, selectedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

                base_64 = Functions.bitmapToBase64(getActivity(), resized);

                if (image_file != null)
                    file_name_txt.setText(image_file.getName());


            }

        }

    }


    // this will check the validations like none of the field can be the empty
    public boolean Check_Validation() {

        String uname = username_edit.getText().toString();
        String fullname = fullname_edit.getText().toString();

        if (TextUtils.isEmpty(uname) || uname.length() < 2) {
            Toast.makeText(context, "Please enter correct username", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(context, "Please enter full name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (base_64 == null) {
            Toast.makeText(context, "Please select the image", Toast.LENGTH_SHORT).show();
        }

        return true;
    }


    public void callApi() {
        JSONObject params = new JSONObject();
        try {
            params.put("fb_id", Variables.user_id);
            params.put("Full name", fullname_edit.getText().toString());
            params.put("Username", username_edit.getText().toString());
            params.put("attachment", base_64);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Functions.showLoader(context, false, false);
        ApiRequest.callApi(context, Variables.getVerified, params, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equalsIgnoreCase("200")) {
                        Toast.makeText(context, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }


}
