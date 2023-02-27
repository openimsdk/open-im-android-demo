package io.openim.android.ouimoments.widgets.videolist.model;

import android.media.MediaPlayer;

import io.openim.android.ouimoments.widgets.videolist.widget.TextureVideoView;


/**
 * @author Wayne
 */
public interface VideoLoadMvpView {

    TextureVideoView getVideoView();

    void videoBeginning();

    void videoStopped();

    void videoPrepared(MediaPlayer player);

    void videoResourceReady(String videoPath);
}
