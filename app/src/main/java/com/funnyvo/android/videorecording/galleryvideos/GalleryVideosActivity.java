package com.funnyvo.android.videorecording.galleryvideos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.videorecording.galleryselectedvideo.GallerySelectedVideoActivity;
import com.funnyvo.android.videorecording.galleryvideos.datamodel.GalleryVideo;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GalleryVideosActivity extends BaseActivity {

    ArrayList<GalleryVideo> data_list;
    public RecyclerView recyclerView;
    GalleryVideosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_videos);

        data_list = new ArrayList();

        recyclerView = findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        data_list = new ArrayList<>();
        adapter = new GalleryVideosAdapter(this, data_list, new GalleryVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, GalleryVideo item, View view) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                Bitmap bmp = null;
                try {
                    retriever.setDataSource(item.video_path);
                    bmp = retriever.getFrameAtTime();
                    int videoHeight = bmp.getHeight();
                    int videoWidth = bmp.getWidth();

                    Log.d("resp", "" + videoWidth + "---" + videoHeight);

                } catch (Exception e) {

                }

                if (item.video_duration_ms < 19500) {
                    changeVideoSize(item.video_path, Variables.gallery_resize_video);

                } else {
                    try {
                        startTrim(new File(item.video_path), new File(Variables.gallery_trimed_video), 1000, 18000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        recyclerView.setAdapter(adapter);
        getAllVideoPathDraft();

        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

            }
        });
    }


    public void getAllVideoPathDraft() {

        String path = Variables.draft_app_folder;
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            GalleryVideo item = new GalleryVideo();
            item.video_path = file.getAbsolutePath();
            item.video_duration_ms = getFileDuration(Uri.parse(file.getAbsolutePath()));

            Log.d("resp", "" + item.video_duration_ms);

            if (item.video_duration_ms > 5000) {
                item.video_time = changeSecToTime(item.video_duration_ms);
                data_list.add(item);
            }
        }

    }


    public void getAllVideoPath(Context context) {

        new AsyncTask<Object, Object, Object>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Object doInBackground(Object[] objects) {
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Video.VideoColumns.DATA};
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    try {

                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                GalleryVideo item = new GalleryVideo();
                                item.video_path = cursor.getString(0);
                                item.video_duration_ms = getFileDuration(Uri.parse(cursor.getString(0)));

                                Log.d("resp", "" + item.video_duration_ms);

                                if (item.video_duration_ms > 5000) {
                                    item.video_time = changeSecToTime(item.video_duration_ms);
                                    data_list.add(item);
                                }

                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                adapter.notifyDataSetChanged();
            }
        }.execute();


    }


    // get the audio file duration that is store in our directory
    public long getFileDuration(Uri uri) {
        try {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int file_duration = Integer.parseInt(durationStr);

            return file_duration;
        } catch (Exception e) {

        }
        return 0;
    }


    public String changeSecToTime(long file_duration) {
        long second = (file_duration / 1000) % 60;
        long minute = (file_duration / (1000 * 60)) % 60;

        return String.format("%02d:%02d", minute, second);

    }


    public void changeVideoSize(String src_path, String destination_path) {

      /*  Functions.Show_determinent_loader(this,false,false);
        new GPUMp4Composer(src_path, destination_path)
                .size(720, 1280)
                .videoBitrate((int) (0.25 * 16 * 540 * 960))
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp",""+(int) (progress*100));
                        Functions.Show_loading_progress((int)(progress*100));

                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancel_determinent_loader();

                                Intent intent=new Intent(GalleryVideos_A.this, GallerySelectedVideo_A.class);
                                intent.putExtra("video_path",Variables.gallery_resize_video);
                                startActivity(intent);

                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp",exception.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Functions.cancel_determinent_loader();

                                    Toast.makeText(GalleryVideos_A.this, "Try Again", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){

                                }
                            }
                        });

                    }
                })
                .start();
*/

        File source = new File(src_path);
        File destination = new File(destination_path);
        try {
            if (source.exists()) {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Intent intent = new Intent(GalleryVideosActivity.this, GallerySelectedVideoActivity.class);
                intent.putExtra("video_path", Variables.gallery_resize_video);
                intent.putExtra("draft_file", src_path);
                startActivity(intent);

            } else {
                Toast.makeText(GalleryVideosActivity.this, "Failed to get video from Draft", Toast.LENGTH_SHORT).show();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startTrim(final File src, final File dst, final int startMs, final int endMs) throws IOException {

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {

                    FileDataSourceImpl file = new FileDataSourceImpl(src);
                    Movie movie = MovieCreator.build(file);
                    List<Track> tracks = movie.getTracks();
                    movie.setTracks(new LinkedList<Track>());
                    double startTime = startMs / 1000;
                    double endTime = endMs / 1000;
                    boolean timeCorrected = false;

                    for (Track track : tracks) {
                        if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                            if (timeCorrected) {
                                throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                            }
                            startTime = Functions.correctTimeToSyncSample(track, startTime, false);
                            endTime = Functions.correctTimeToSyncSample(track, endTime, true);
                            timeCorrected = true;
                        }
                    }
                    for (Track track : tracks) {
                        long currentSample = 0;
                        double currentTime = 0;
                        long startSample = -1;
                        long endSample = -1;

                        for (int i = 0; i < track.getSampleDurations().length; i++) {
                            if (currentTime <= startTime) {
                                startSample = currentSample;
                            }
                            if (currentTime <= endTime) {
                                endSample = currentSample;
                            } else {
                                break;
                            }
                            currentTime += (double) track.getSampleDurations()[i] / (double) track.getTrackMetaData().getTimescale();
                            currentSample++;
                        }
                        movie.addTrack(new CroppedTrack(track, startSample, endSample));
                    }

                    Container out = new DefaultMp4Builder().build(movie);
                    MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
                    mvhd.setMatrix(Matrix.ROTATE_180);
                    if (!dst.exists()) {
                        dst.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(dst);
                    WritableByteChannel fc = fos.getChannel();
                    try {
                        out.writeContainer(fc);
                    } finally {
                        fc.close();
                        fos.close();
                        file.close();
                    }

                    file.close();
                    return "Ok";
                } catch (IOException e) {
                    return "error";
                }

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("error")) {
                    Toast.makeText(GalleryVideosActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                } else {
                    dismissProgressDialog();
                    changeVideoSize(Variables.gallery_trimed_video, Variables.gallery_resize_video);
                }
            }


        }.execute();

    }


    @Override
    protected void onStart() {
        super.onStart();
        deleteFile();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        deleteFile();
    }

    public void deleteFile() {
        File output = new File(Variables.outputfile);
        File output2 = new File(Variables.outputfile2);
        File outputFilterFile = new File(Variables.OUTPUT_FILTER_FILE);
        File gallery_trim_video = new File(Variables.gallery_trimed_video);
        File gallery_resize_video = new File(Variables.gallery_resize_video);

        if (output.exists()) {
            output.delete();
        }
        if (output2.exists()) {

            output2.delete();
        }
        if (outputFilterFile.exists()) {
            outputFilterFile.delete();
        }

        if (gallery_trim_video.exists()) {
            gallery_trim_video.delete();
        }

        if (gallery_resize_video.exists()) {
            gallery_resize_video.delete();
        }

    }
}
