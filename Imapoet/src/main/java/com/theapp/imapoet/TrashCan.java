package com.theapp.imapoet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;

/**
 * Created by whitney on 8/6/14.
 */
public class TrashCan {
    public Bitmap trashCan;
    public float trashCanWidth;
    public float trashCanHeight;
    private float trashCanPadding = 5;
    private int nextDirection = 1;
    private long lastTime = System.currentTimeMillis();
    private long totalTime = 500;
    private long timeTaken = 0;


    public TrashCan(Context context) {
        trashCan = BitmapFactory.decodeResource(context.getResources(), R.drawable.trash);
        trashCanWidth = trashCan.getWidth();
        trashCanHeight = trashCan.getHeight();
    }

    private void runAnimation(Canvas canvas, float width, float height) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastTime;
        timeTaken = timeTaken + elapsed;
        if(timeTaken/totalTime > 1) {
            nextDirection = nextDirection*-1;
            timeTaken = 0;
        }
        canvas.save();
        float xPointOfRotation = width - trashCanPadding- (trashCanWidth/2);
        float yPointOfRotation = height - trashCanPadding - (trashCanHeight/2);
        canvas.rotate(45*nextDirection,xPointOfRotation,yPointOfRotation);
        drawTrashCan(canvas,width,height);
        canvas.restore();
        lastTime = currentTime;
    }

    public void drawTrashCan(Canvas canvas, Boolean toDelete, float width, float height) {
        if(toDelete) {
            runAnimation(canvas,width,height);
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
