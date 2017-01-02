package io.github.tomszilagyi.svhu1972;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class ImageUtils {
    private int viewHeight;
    private int viewWidth;

    public ImageUtils(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public void setViewSize(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public static int
    calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
               && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap
    decodeAssetImage(Context ctx, String filename, BitmapFactory.Options options) {
        Bitmap bmp = null;
        InputStream is = null;
        try {
            is = ctx.getAssets().open(filename);
            bmp = BitmapFactory.decodeStream(is, null, options);
            is.close();
        } catch (IOException e) {
            Log.e("Szotar", "IO Exception opening "+filename+": "+e);
        }
        return bmp;
    }

    public static Bitmap
    decodeSampledBitmapFromAssets(Context ctx, int index,
                                  int reqWidth, int reqHeight) {

        int main = index / 2;
        int sub = index % 2 + 1;
        String filename = String.format(Locale.UK, "images/%04d_%d.png", main, sub);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeAssetImage(ctx, filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8; /* use 4x less bitmap memory */
        return decodeAssetImage(ctx, filename, options);
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Context ctx, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(ctx.getResources(), bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        private Context context;

        public BitmapWorkerTask(Context context, ImageView imageView) {
            this.context = context;
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            return decodeSampledBitmapFromAssets(context, data, viewWidth, viewHeight);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    onImageLoaded();
                }
            }
        }
    }

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public void loadBitmap(Context context, int index, ImageView imageView) {
        if (cancelPotentialWork(index, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(context, imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context, null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(index);
        }
    }

    /* Override this function to get a callback when the loaded bitmap
     * is set in the imageView */
    public void onImageLoaded() {}
}
