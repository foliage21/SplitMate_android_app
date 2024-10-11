package com.example.splitmate_delta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoUtils {

    /**
     * Extracts a specified number of frames from the video.
     *
     * @param context    Context
     * @param videoUri   URI of the video
     * @param frameCount Number of frames to extract
     * @return List of extracted frames
     */
    public static List<Bitmap> extractFramesFromVideo(Context context, Uri videoUri, int frameCount) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        List<Bitmap> frames = new ArrayList<>();

        try {
            retriever.setDataSource(context, videoUri);

            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr == null) {
                Toast.makeText(context, "Unable to retrieve video duration", Toast.LENGTH_SHORT).show();
                return frames;
            }
            long videoDuration = Long.parseLong(durationStr); // Get video duration in milliseconds

            // Extract one frame at every 1/frameCount of the total video duration
            for (int i = 0; i < frameCount; i++) {
                long frameTime = videoDuration * i * 1000L / frameCount; // Convert to microseconds
                Bitmap frame = retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
                if (frame != null) {
                    frames.add(frame);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to extract video frames: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error releasing resources: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return frames;
    }
}