package com.funnyvo.android.videorecording;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.funnyvo.android.customview.ActivityIndicator;
import com.funnyvo.android.simpleclasses.Variables;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

// this is the class which will add the selected soung to the created video
public class MergeVideoAudio extends AsyncTask<String, String, String> {

    Context context;

    private String audio, video, output, draft_file;
    ActivityIndicator indicator;

    public MergeVideoAudio(Context context) {
        this.context = context;
        indicator = new ActivityIndicator(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    public String doInBackground(String... strings) {
        try {
            indicator.show();
        } catch (Exception e) {

        }
        audio = strings[0];
        video = strings[1];
        output = strings[2];
        if (strings.length == 4) {
            draft_file = strings[3];
        }

        Thread thread = new Thread(runnable);
        thread.start();

        return null;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }


    public void goToPreviewActivity() {
        Intent intent = new Intent(context, PreviewVideoActivity.class);
        intent.putExtra("path", Variables.outputfile2);
        intent.putExtra("draft_file", draft_file);
        context.startActivity(intent);
    }


    public Track CropAudio(String videopath, Track fullAudio) {
        try {

            IsoFile isoFile = new IsoFile(videopath);

            double lengthInSeconds = (double)
                    isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                    isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            Track audioTrack = (Track) fullAudio;
            double startTime1 = 0;
            double endTime1 = lengthInSeconds;

            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;


            for (int i = 0; i < audioTrack.getSampleDurations().length; i++) {
                long delta = audioTrack.getSampleDurations()[i];

                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }

                lastTime = currentTime;
                currentTime += (double) delta / (double) audioTrack.getTrackMetaData().getTimescale();
                currentSample++;
            }

            CroppedTrack cropperAacTrack = new CroppedTrack(fullAudio, startSample1, endSample1);

            return cropperAacTrack;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fullAudio;
    }


    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Movie m = MovieCreator.build(video);

                List nuTracks = new ArrayList<>();

                for (Track t : m.getTracks()) {
                    if (!"soun".equals(t.getHandler())) {
                        nuTracks.add(t);
                    }
                }

                Track nuAudio = new AACTrackImpl(new FileDataSourceImpl(audio));
                Track crop_track = CropAudio(video, nuAudio);
                nuTracks.add(crop_track);
                m.setTracks(nuTracks);
                Container mp4file = new DefaultMp4Builder().build(m);
                FileChannel fc = new FileOutputStream(new File(output)).getChannel();
                mp4file.writeContainer(fc);
                fc.close();
                try {
                    indicator.hide();
                } catch (Exception e) {
                    Log.d(Variables.tag, e.toString());
                } finally {
                    goToPreviewActivity();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Variables.tag, e.toString());
            }
        }
    };
}
