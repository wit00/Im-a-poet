package com.theapp.imapoet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

/**
 * Created by whitney on 8/6/14.
 */
public class TrashCan {
    public Bitmap trashCan;
    public Bitmap trashCanLeft;
    public Bitmap trashCanRight;
    public float trashCanWidth;
    public float trashCanHeight;
    private float trashCanPadding = 5;
    private long lastTime = 0;
    private long timeTaken = 0;


    public TrashCan(Context context) {
        trashCan = BitmapFactory.decodeResource(context.getResources(), R.drawable.trash);
        trashCanWidth = trashCan.getWidth();
        trashCanHeight = trashCan.getHeight();
        trashCanLeft = BitmapFactory.decodeResource(context.getResources(),R.drawable.trash_left);
        trashCanRight = BitmapFactory.decodeResource(context.getResources(),R.drawable.trash_right);
    }

    private int getNextIndex(long elapsedTime, int numberOfImages, int millisecondsBetweenImages) {
        return (int) (((elapsedTime % (millisecondsBetweenImages * 10)) % (numberOfImages * millisecondsBetweenImages)) / millisecondsBetweenImages);
    }

    private void runNewAnimation(Canvas canvas, float width, float height) {
        int currentTime = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        timeTaken = (timeTaken + (currentTime - (int) lastTime));
        lastTime = currentTime;
        switch (getNextIndex(timeTaken,5,100)) {
            case 0:
                canvas.drawBitmap(trashCan, width - trashCanWidth - trashCanPadding, height - trashCanHeight - trashCanPadding, null);
                break;
            case 1:
                canvas.drawBitmap(trashCanLeft, width - trashCanWidth - trashCanPadding, height - trashCanHeight - trashCanPadding, null);
                break;
            case 2:
                canvas.drawBitmap(trashCan, width - trashCanWidth - trashCanPadding, height - trashCanHeight - trashCanPadding, null);
                break;
            case 3:
                canvas.drawBitmap(trashCanRight, width - trashCanWidth - trashCanPadding, height - trashCanHeight - trashCanPadding, null);
                break;
            case 4:
                canvas.drawBitmap(trashCan, width - trashCanWidth - trashCanPadding, height - trashCanHeight - trashCanPadding, null);
                timeTaken = 0;
                break;
        }
    }

    public void drawTrashCan(Canvas canvas, Boolean toDelete, float width, float height) {
        if(toDelete) {
            runNewAnimation(canvas,width,height);
        } else {
            drawTrashCan(canvas,width,height);
        }
    }

    private void drawTrashCan(Canvas canvas, float width, float height) {
        canvas.drawBitmap(trashCan, width - trashCanWidth - trashCanPadding, height - trashCanHeight - trashCanPadding, null);
    }

    public boolean collidesWithTrashCan(Magnet movingTile, float width, float height) {
        float halfPaddingWidth = (movingTile.width() )/2;
        float halfPaddingHeight = (movingTile.height())/2;
        return !(width - trashCanWidth > movingTile.x() + halfPaddingWidth ||
                width - trashCanWidth + trashCan.getWidth() < movingTile.x() - halfPaddingWidth ||
                height-trashCanHeight > movingTile.y() + halfPaddingHeight ||
                height-trashCanHeight + trashCan.getHeight() < movingTile.y() - halfPaddingHeight);
    }
}
