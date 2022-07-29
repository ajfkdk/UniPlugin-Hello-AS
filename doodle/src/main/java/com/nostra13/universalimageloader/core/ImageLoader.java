/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.core;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.FlushedInputStream;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

public class ImageLoader {

    public static final String TAG = ImageLoader.class.getSimpleName();

    static final String LOG_INIT_CONFIG = "Initialize ImageLoader with configuration";
    static final String LOG_DESTROY = "Destroy ImageLoader";
    static final String LOG_LOAD_IMAGE_FROM_MEMORY_CACHE = "Load image from memory cache [%s]";

    private static final String WARNING_RE_INIT_CONFIG = "Try to initialize ImageLoader which had already been initialized before. " + "To re-init ImageLoader with new configuration call ImageLoader.destroy() at first.";
    private static final String ERROR_WRONG_ARGUMENTS = "Wrong arguments were passed to displayImage() method (ImageView reference must not be null)";
    private static final String ERROR_NOT_INIT = "ImageLoader must be init with configuration before using";
    private static final String ERROR_INIT_CONFIG_WITH_NULL = "ImageLoader configuration can not be initialized with null";

    private ImageLoaderConfiguration configuration;
    private ImageLoaderEngine engine;

    private ImageLoadingListener defaultListener = new SimpleImageLoadingListener();

    private volatile static ImageLoader instance;

    /**
     * Returns singleton class instance
     */
    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    protected ImageLoader() {
    }


    public synchronized void init(ImageLoaderConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
        }
        if (this.configuration == null) {
            L.d(LOG_INIT_CONFIG);
            engine = new ImageLoaderEngine(configuration);
            this.configuration = configuration;
        } else {
            L.w(WARNING_RE_INIT_CONFIG);
        }
    }


    public boolean isInited() {
        return configuration != null;
    }


    public void displayImage(String uri, ImageAware imageAware) {
        displayImage(uri, imageAware, null, null, null);
    }


    public void displayImage(String uri, ImageAware imageAware, ImageLoadingListener listener) {
        displayImage(uri, imageAware, null, listener, null);
    }


    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options) {
        displayImage(uri, imageAware, options, null, null);
    }


    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options,
                             ImageLoadingListener listener) {
        displayImage(uri, imageAware, options, listener, null);
    }


    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options,
                             ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        displayImage(uri, imageAware, options, null, listener, progressListener);
    }


    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options,
                             ImageSize targetSize, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        checkConfiguration();
        if (imageAware == null) {
            throw new IllegalArgumentException(ERROR_WRONG_ARGUMENTS);
        }
        if (listener == null) {
            listener = defaultListener;
        }
        if (options == null) {
            options = configuration.defaultDisplayImageOptions;
        }

        if (TextUtils.isEmpty(uri)) {
            engine.cancelDisplayTaskFor(imageAware);
            listener.onLoadingStarted(uri, imageAware.getWrappedView());
            if (options.shouldShowImageForEmptyUri()) {
                imageAware.setImageDrawable(options.getImageForEmptyUri(configuration.resources));
            } else {
                imageAware.setImageDrawable(null);
            }
            listener.onLoadingComplete(uri, imageAware.getWrappedView(), null);
            return;
        }

        if (targetSize == null) {
            targetSize = ImageSizeUtils.defineTargetSizeForView(imageAware, configuration.getMaxImageSize());
        }
        String memoryCacheKey = MemoryCacheUtils.generateKey(uri, targetSize);
        engine.prepareDisplayTaskFor(imageAware, memoryCacheKey);

        listener.onLoadingStarted(uri, imageAware.getWrappedView());

        Bitmap bmp = configuration.memoryCache.get(memoryCacheKey);
        if (bmp != null && !bmp.isRecycled()) {
            L.d(LOG_LOAD_IMAGE_FROM_MEMORY_CACHE, memoryCacheKey);

            if (options.shouldPostProcess()) {
                ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey,
                        options, listener, progressListener, engine.getLockForUri(uri));
                ProcessAndDisplayImageTask displayTask = new ProcessAndDisplayImageTask(engine, bmp, imageLoadingInfo,
                        defineHandler(options));
                if (options.isSyncLoading()) {
                    displayTask.run();
                } else {
                    engine.submit(displayTask);
                }
            } else {
                options.getDisplayer().display(bmp, imageAware, LoadedFrom.MEMORY_CACHE);
                listener.onLoadingComplete(uri, imageAware.getWrappedView(), bmp);
            }
        } else {
            if (options.shouldShowImageOnLoading()) {
                imageAware.setImageDrawable(options.getImageOnLoading(configuration.resources));
            } else if (options.isResetViewBeforeLoading()) {
                imageAware.setImageDrawable(null);
            }

            ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey,
                    options, listener, progressListener, engine.getLockForUri(uri));
            LoadAndDisplayImageTask displayTask = new LoadAndDisplayImageTask(engine, imageLoadingInfo,
                    defineHandler(options));
            if (options.isSyncLoading()) {
                displayTask.run();
            } else {
                engine.submit(displayTask);
            }
        }
    }


    public void displayImage(String uri, ImageView imageView) {
        displayImage(uri, new ImageViewAware(imageView), null, null, null);
    }

    public void displayImage(String uri, ImageView imageView, ImageSize targetImageSize) {
        displayImage(uri, new ImageViewAware(imageView), null, targetImageSize, null, null);
    }


    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        displayImage(uri, new ImageViewAware(imageView), options, null, null);
    }


    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
        displayImage(uri, new ImageViewAware(imageView), null, listener, null);
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options,
                             ImageLoadingListener listener) {
        displayImage(uri, imageView, options, listener, null);
    }


    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options,
                             ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        displayImage(uri, new ImageViewAware(imageView), options, listener, progressListener);
    }

    public void loadImage(String uri, ImageLoadingListener listener) {
        loadImage(uri, null, null, listener, null);
    }

    public void loadImage(String uri, ImageSize targetImageSize, ImageLoadingListener listener) {
        loadImage(uri, targetImageSize, null, listener, null);
    }


    public void loadImage(String uri, DisplayImageOptions options, ImageLoadingListener listener) {
        loadImage(uri, null, options, listener, null);
    }


    public void loadImage(String uri, ImageSize targetImageSize, DisplayImageOptions options,
                          ImageLoadingListener listener) {
        loadImage(uri, targetImageSize, options, listener, null);
    }

    public void loadImage(String uri, ImageSize targetImageSize, DisplayImageOptions options,
                          ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        checkConfiguration();
        if (targetImageSize == null) {
            targetImageSize = configuration.getMaxImageSize();
        }
        if (options == null) {
            options = configuration.defaultDisplayImageOptions;
        }

        NonViewAware imageAware = new NonViewAware(uri, targetImageSize, ViewScaleType.CROP);
        displayImage(uri, imageAware, options, listener, progressListener);
    }


    public Bitmap loadImageSync(String uri) {
        return loadImageSync(uri, null, null);
    }


    public Bitmap loadImageSync(String uri, DisplayImageOptions options) {
        return loadImageSync(uri, null, options);
    }


    public Bitmap loadImageSync(String uri, ImageSize targetImageSize) {
        return loadImageSync(uri, targetImageSize, null);
    }


    public Bitmap loadImageSync(String uri, ImageSize targetImageSize, DisplayImageOptions options) {
        if (options == null) {
            options = configuration.defaultDisplayImageOptions;
        }
        options = new DisplayImageOptions.Builder().cloneFrom(options).syncLoading(true).build();

        SyncImageLoadingListener listener = new SyncImageLoadingListener();
        loadImage(uri, targetImageSize, options, listener);
        return listener.getLoadedBitmap();
    }


    private void checkConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException(ERROR_NOT_INIT);
        }
    }


    public void setDefaultLoadingListener(ImageLoadingListener listener) {
        defaultListener = listener == null ? new SimpleImageLoadingListener() : listener;
    }

    /**
     * Returns memory cache
     *
     * @throws IllegalStateException if {@link #init(ImageLoaderConfiguration)} method wasn't called before
     */
    public MemoryCache getMemoryCache() {
        checkConfiguration();
        return configuration.memoryCache;
    }

    /**
     * Clears memory cache
     *
     * @throws IllegalStateException if {@link #init(ImageLoaderConfiguration)} method wasn't called before
     */
    public void clearMemoryCache() {
        checkConfiguration();
        configuration.memoryCache.clear();
    }

    /**
     * Returns disk cache
     *
     * @throws IllegalStateException if {@link #init(ImageLoaderConfiguration)} method wasn't called before
     * @deprecated Use {@link #getDiskCache()} instead
     */
    @Deprecated
    public DiskCache getDiscCache() {
        return getDiskCache();
    }

    /**
     * Returns disk cache
     *
     * @throws IllegalStateException if {@link #init(ImageLoaderConfiguration)} method wasn't called before
     */
    public DiskCache getDiskCache() {
        checkConfiguration();
        return configuration.diskCache;
    }

    /**
     * Clears disk cache.
     *
     * @throws IllegalStateException if {@link #init(ImageLoaderConfiguration)} method wasn't called before
     * @deprecated Use {@link #clearDiskCache()} instead
     */
    @Deprecated
    public void clearDiscCache() {
        clearDiskCache();
    }

    /**
     * Clears disk cache.
     *
     * @throws IllegalStateException if {@link #init(ImageLoaderConfiguration)} method wasn't called before
     */
    public void clearDiskCache() {
        checkConfiguration();
        configuration.diskCache.clear();
    }

    /**
     * Returns URI of image which is loading at this moment into passed
     * {@link com.nostra13.universalimageloader.core.imageaware.ImageAware ImageAware}
     */
    public String getLoadingUriForView(ImageAware imageAware) {
        return engine.getLoadingUriForView(imageAware);
    }

    /**
     * Returns URI of image which is loading at this moment into passed
     * {@link android.widget.ImageView ImageView}
     */
    public String getLoadingUriForView(ImageView imageView) {
        return engine.getLoadingUriForView(new ImageViewAware(imageView));
    }

    /**
     * Cancel the task of loading and displaying image for passed
     * {@link com.nostra13.universalimageloader.core.imageaware.ImageAware ImageAware}.
     *
     * @param imageAware {@link com.nostra13.universalimageloader.core.imageaware.ImageAware ImageAware} for
     *                   which display task will be cancelled
     */
    public void cancelDisplayTask(ImageAware imageAware) {
        engine.cancelDisplayTaskFor(imageAware);
    }

    /**
     * Cancel the task of loading and displaying image for passed
     * {@link android.widget.ImageView ImageView}.
     *
     * @param imageView {@link android.widget.ImageView ImageView} for which display task will be cancelled
     */
    public void cancelDisplayTask(ImageView imageView) {
        engine.cancelDisplayTaskFor(new ImageViewAware(imageView));
    }

    /**
     * Denies or allows ImageLoader to download images from the network.<br />
     * <br />
     * If downloads are denied and if image isn't cached then
     * {@link ImageLoadingListener#onLoadingFailed(String, View, FailReason)} callback will be fired with
     * {@link FailReason.FailType#NETWORK_DENIED}
     *
     * @param denyNetworkDownloads pass <b>true</b> - to deny engine to download images from the network; <b>false</b> -
     *                             to allow engine to download images from network.
     */
    public void denyNetworkDownloads(boolean denyNetworkDownloads) {
        engine.denyNetworkDownloads(denyNetworkDownloads);
    }

    /**
     * Sets option whether ImageLoader will use {@link FlushedInputStream} for network downloads to handle <a
     * href="http://code.google.com/p/android/issues/detail?id=6066">this known problem</a> or not.
     *
     * @param handleSlowNetwork pass <b>true</b> - to use {@link FlushedInputStream} for network downloads; <b>false</b>
     *                          - otherwise.
     */
    public void handleSlowNetwork(boolean handleSlowNetwork) {
        engine.handleSlowNetwork(handleSlowNetwork);
    }

    /**
     * Pause ImageLoader. All new "load&display" tasks won't be executed until ImageLoader is {@link #resume() resumed}.
     * <br />
     * Already running tasks are not paused.
     */
    public void pause() {
        engine.pause();
    }

    /**
     * Resumes waiting "load&display" tasks
     */
    public void resume() {
        engine.resume();
    }

    /**
     * Cancels all running and scheduled display image tasks.<br />
     * <b>NOTE:</b> This method doesn't shutdown
     * {@linkplain com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder#taskExecutor(java.util.concurrent.Executor)
     * custom task executors} if you set them.<br />
     * ImageLoader still can be used after calling this method.
     */
    public void stop() {
        engine.stop();
    }

    /**
     * {@linkplain #stop() Stops ImageLoader} and clears current configuration. <br />
     * You can {@linkplain #init(ImageLoaderConfiguration) init} ImageLoader with new configuration after calling this
     * method.
     */
    public void destroy() {
        if (configuration != null) L.d(LOG_DESTROY);
        stop();
        configuration.diskCache.close();
        engine = null;
        configuration = null;
    }

    private static Handler defineHandler(DisplayImageOptions options) {
        Handler handler = options.getHandler();
        if (options.isSyncLoading()) {
            handler = null;
        } else if (handler == null && Looper.myLooper() == Looper.getMainLooper()) {
            handler = new Handler();
        }
        return handler;
    }

    /**
     * Listener which is designed for synchronous image loading.
     *
     * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
     * @since 1.9.0
     */
    private static class SyncImageLoadingListener extends SimpleImageLoadingListener {

        private Bitmap loadedImage;

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            this.loadedImage = loadedImage;
        }

        public Bitmap getLoadedBitmap() {
            return loadedImage;
        }
    }
}
