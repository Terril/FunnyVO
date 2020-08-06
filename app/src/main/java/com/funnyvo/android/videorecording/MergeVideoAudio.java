package com.funnyvo.android.videorecording;

import android.os.AsyncTask;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
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

    private MergeVideoAudioCallBack callBack;
    private String audio, video, output, draft_file;
    //  private ActivityIndicator indicator;

    public MergeVideoAudio(MergeVideoAudioCallBack callBack) {
        this.callBack = callBack;
//        indicator = new ActivityIndicator(activity);
    }

    @Override
    public String doInBackground(String... strings) {

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

    private Track cropAudio(String videopath, Track fullAudio) {
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
                Movie movieCreator = MovieCreator.build(video);
                List newCreatedTrack = new ArrayList<>();

                for (Track t : movieCreator.getTracks()) {
                    if (!"soun".equals(t.getHandler())) {
                        newCreatedTrack.add(t);
                    }
                }

                Track newAudio = new AACTrackImpl(new FileDataSourceImpl(audio));
                Track cropTrack = cropAudio(video, newAudio);
                newCreatedTrack.add(cropTrack);
                movieCreator.setTracks(newCreatedTrack);
                Container mp4file = new DefaultMp4Builder().build(movieCreator);
                FileChannel fc = new FileOutputStream(new File(output)).getChannel();
                mp4file.writeContainer(fc);
                fc.close();
//                try {
//                    indicator.hide();
//                } catch (Exception e) {
//                    Log.d(Variables.tag, e.toString());
//                } finally {
                if (callBack != null) {
                    callBack.onCompletion(true, draft_file);
                }
                //             }

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Variables.tag, e.toString());
            }
        }
    };
}

interface MergeVideoAudioCallBack {
    void onCompletion(boolean state, String draftFile);
}
