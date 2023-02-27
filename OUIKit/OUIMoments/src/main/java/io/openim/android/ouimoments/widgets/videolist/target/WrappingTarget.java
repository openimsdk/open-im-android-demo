package io.openim.android.ouimoments.widgets.videolist.target;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

/**
 * @author Wayne
 */
public class WrappingTarget<Z> implements Target<Z> {
    protected final Target<Z> target;

    public WrappingTarget(Target<Z> target) {
        this.target = target;
    }

    @Override
    public void getSize(SizeReadyCallback cb) {
        target.getSize(cb);
    }

    @Override
    public void removeCallback(@NonNull SizeReadyCallback cb) {

    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        target.onLoadStarted(placeholder);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        target.onLoadFailed( errorDrawable);
    }

    @Override
    public void onResourceReady(@NonNull Z resource, @Nullable Transition<? super Z> transition) {
        target.onResourceReady(resource, transition);
    }


    @Override
    public void onLoadCleared(Drawable placeholder) {
        target.onLoadCleared(placeholder);
    }

    @Override
    public Request getRequest() {
        return target.getRequest();
    }

    @Override
    public void setRequest(Request request) {
        target.setRequest(request);
    }

    @Override
    public void onStart() {
        target.onStart();
    }

    @Override
    public void onStop() {
        target.onStop();
    }

    @Override
    public void onDestroy() {
        target.onDestroy();
    }
}
