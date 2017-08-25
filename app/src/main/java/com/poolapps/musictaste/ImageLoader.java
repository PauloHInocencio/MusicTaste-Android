package com.poolapps.musictaste;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

class ImageLoader<T> extends HandlerThread  {

    private static final String TAG = ImageLoader.class.getSimpleName();
    private static final int MESSAGE_GET_IMAGE_PLEASE = 0;

    private Handler mResponseHandler;
    private Handler mRequestHandler;
    private LruCache<String, Bitmap> mCache;
    private ConcurrentMap<T, String> mMapOfTargetsWaitingForBitmap;
    private ImageLoaderListener<T> mListener;

    interface ImageLoaderListener<T> {
        void imageHasBeenDownloaded(T targetWaitingForBitmap, Bitmap bitmapDownloaded);
        void imageWasNotDownloaded(T targetWaitingForBitmap);
    }


    ImageLoader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        setupMemoryCache();
    }

    public void setImageLoaderListener(ImageLoaderListener<T> listener) {
        mListener = listener;
    }


    public void queueRequestToLoadBitmap(T targetWaitingForBitmap, String urlOfTheBitmap) {
        final Bitmap bitmapOnCache = getBitmapFromMemoryCache(urlOfTheBitmap);
        if (bitmapOnCache != null){
            mMapOfTargetsWaitingForBitmap.remove(targetWaitingForBitmap);
            mListener.imageHasBeenDownloaded(targetWaitingForBitmap, bitmapOnCache);
            return;
        }

        mMapOfTargetsWaitingForBitmap.put(targetWaitingForBitmap, urlOfTheBitmap);
        mRequestHandler.obtainMessage(MESSAGE_GET_IMAGE_PLEASE, targetWaitingForBitmap)
                .sendToTarget();
    }




    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MESSAGE_GET_IMAGE_PLEASE:
                        T target = (T) msg.obj;
                        requestDownloadBitmap(target);
                        break;
                }
            }
        };
    }

    private void requestDownloadBitmap(final T targetWaitingForBitmap) {
        try {
            final String urlOfTheImageRequested = mMapOfTargetsWaitingForBitmap.get(targetWaitingForBitmap);
            final Bitmap bitmapDownloaded = downloadBitmap(urlOfTheImageRequested);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    boolean theTargetStillWaitingForTheSameImageRequested =
                            mMapOfTargetsWaitingForBitmap.get(targetWaitingForBitmap)
                                    .equals(urlOfTheImageRequested);

                    if (theTargetStillWaitingForTheSameImageRequested){
                        mMapOfTargetsWaitingForBitmap.remove(targetWaitingForBitmap);
                        addBitmapToMemoryCache(urlOfTheImageRequested, bitmapDownloaded);
                        mListener.imageHasBeenDownloaded(targetWaitingForBitmap, bitmapDownloaded);
                    }
                }
            });
        } catch (IOException ioe) {
            mListener.imageWasNotDownloaded(targetWaitingForBitmap);
        }
    }

    private Bitmap downloadBitmap(String url) throws IOException {
        if (url == null){
            return null;
        }

        byte[] bitmapBytes = new ItunesFetchr().getUrlBytes(url);
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
    }


    private void setupMemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize =  maxMemory / 4;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than number of items.
                return super.sizeOf(key, value) / 1024;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null) {
            mCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return mCache.get(key);
    }



}
