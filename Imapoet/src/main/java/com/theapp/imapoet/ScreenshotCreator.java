package com.theapp.imapoet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.View;

/**
 * Created by whitney on 9/8/14.
 * Screenshot Creator is a static class for creating a screenshot of a particular view using the public createScreenshot(View targetView) method.
 * It has another public method, createScreenshotWithWatermark(View targetView, Context context, int watermarkDrawable) that takes a screenshot
 * of a view and combines it with a watermark in the drawable directory. Both methods return bitmaps. In this application (I'm a poet), this class
 * is used to create a bitmap for saving to the sd card and to create a bitmap to send on social media.
 */
public class ScreenshotCreator {
    public ScreenshotCreator(){}

    private static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap;
        view.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private static PointF getBottomCenterPointWithPadding(float padding,Bitmap screenshot, Bitmap watermark) {
        PointF point = new PointF();
        point.set(screenshot.getWidth()/2 - watermark.getWidth()/2, screenshot.getHeight() - watermark.getHeight() - padding);
        return point;
    }

    private static Bitmap combineBitmapWithWatermark(Bitmap watermark, Bitmap screenshot) {
        Bitmap combinedBitmaps = Bitmap.createBitmap(screenshot.getWidth(), screenshot.getHeight(), screenshot.getConfig());
        Canvas canvas = new Canvas(combinedBitmaps);
        canvas.drawBitmap(screenshot, new Matrix(), null);
        PointF watermarkPlacement = getBottomCenterPointWithPadding(10,screenshot,watermark);
        canvas.drawBitmap(watermark, watermarkPlacement.x, watermarkPlacement.y, null);
        return combinedBitmaps;
    }

    public static Bitmap createScreenshotWithWatermark(View targetView, Context context, int watermarkDrawable) {
        Bitmap bitmap = getBitmapFromView(targetView);
        return combineBitmapWithWatermark(BitmapFactory.decodeResource(context.getResources(), watermarkDrawable), bitmap);
    }

    public static Bitmap createScreenshot(View targetView) {
        return getBitmapFromView(targetView);
    }


}
