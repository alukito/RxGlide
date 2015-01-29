package com.andreas.example.rxglide;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by andreas on 1/30/15.
 */
public class GlideCacheWarmer {

    private final Context context;

    public GlideCacheWarmer(Context context) {
        this.context = context;
    }

    public Observable createDownloadAllImage(String[] urls) {
        Observable merged = createDownloadImageObservable(urls[0]);
        for(int i=1 ; i < urls.length ; i++) {
            merged = Observable.mergeDelayError(merged, createDownloadImageObservable(urls[i]));
        }
        return merged;
    }

    private Observable createDownloadImageObservable(final String url) {

        return Observable.create(new Observable.OnSubscribe<File>() {

             @Override
             public void call(final Subscriber<? super File> subscriber) {
                 Glide.with(context).load(url).downloadOnly(new SimpleTarget<File>() {
                     @Override
                     public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                         subscriber.onNext(resource);
                         subscriber.onCompleted();
                     }

                     @Override
                     public void onLoadFailed(Exception e, Drawable errorDrawable) {
                         subscriber.onError(e);
                     }
                 });
             }
         });
    }
}
