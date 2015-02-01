package com.andreas.example.rxglide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observers.TestSubscriber;

/**
 * Created by andreas on 2/1/15.
 */
public class GlideCacheWarmerTest extends AndroidTestCase {

    private GlideCacheWarmer cacheWarmer;

    public void setUp() {
        cacheWarmer = new GlideCacheWarmer(getContext());
    }

    public void testDownloadAllImageObservable() {
        Observable observable = cacheWarmer.createDownloadAllImage(SplashScreenActivity.URLS);

        TestSubscriber testSubscriber = new TestSubscriber();
        observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List list = testSubscriber.getOnNextEvents();
        assertEquals(SplashScreenActivity.URLS.length, list.size());
    }

    public void testDownloadAllImageObservableWithMockWebServer() throws IOException {
        MockWebServer server = new MockWebServer();

        server.enqueue(new MockResponse().setBody("binaryimage1"));
        server.enqueue(new MockResponse().setBody("binaryimage2"));

        server.play();

        URL url1 = server.getUrl("/image1");
        URL url2 = server.getUrl("/image2");

        Observable observable = cacheWarmer.createDownloadAllImage(
                new String[] {url1.toString(), url2.toString()});

        TestSubscriber testSubscriber = new TestSubscriber();
        observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List list = testSubscriber.getOnNextEvents();
        assertEquals(2, list.size());

        server.shutdown();
    }

    public void testDownloadAllImageObservableWithOneError() throws IOException {
        MockWebServer server = new MockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        server.enqueue(new MockResponse().setBody("binaryimage1"));

        server.play();

        URL url1 = server.getUrl("/image1");
        URL url2 = server.getUrl("/image2");

        Observable observable = cacheWarmer.createDownloadAllImage(
                new String[] {url1.toString(), url2.toString()});

        TestSubscriber testSubscriber = new TestSubscriber();
        observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        assertEquals(1, testSubscriber.getOnNextEvents().size());
        assertEquals(1, testSubscriber.getOnErrorEvents().size());

        server.shutdown();
    }
}
