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
    public Bitmap trashCan;// = BitmapFactory.decodeResource(getResources(), R.drawable.trash);
    public float trashCanWidth;// = trashCan.getWidth()
    public float trashCanHeight;
    private long elapsedTime = 0;
    private long flipPoint = 50000;
    private long flopPoint = 100000;
    private float rotationDegrees = 25;

    private float totalAnimationTime = 250;
    private float trashCanPadding = 5;

    private int switchTime = 90000;
    private int nextDirection = 1;

    Paint paint = new Paint();

    long lastTime = System.currentTimeMillis();




    public void beginBlink() {

    }

    public boolean animate(Canvas canvas, float width, float height) {
        paint.setFilterBitmap(true);
        long time = 0;
        long startMills = System.currentTimeMillis();
        if (elapsedTime < totalAnimationTime) {
            //canvas.save();
            //canvas.rotate(runAnimation((int)elapsedTime,25f,totalAnimationTime,0f),width - trashCanWidth/2,height-trashCanHeight/2);
            //System.out.println("moo moo "+Integer.toString(255/totalAnimationTime));
            elapsedTime = elapsedTime + System.currentTimeMillis() - startMills;
            //paint.setAlpha(20);
            //canvas.drawBitmap(trashCan, width - trashCanWidth, height - trashCanHeight, paint);
            //canvas.restore();
            return true;
        } else {
            //canvas.drawBitmap(trashCan, width - trashCanWidth, height - trashCanHeight, paint);
            return false;

        }

    }

    /*private float runAnimation(int elapsed, float change, float duration, float from) {
        //elapsed*(change/duration) + from;
        //paint.setAlpha((int)((elapsed*(change/duration)) + from));
        //System.out.println("moo "+Integer.toString((int)(((elapsed*(change/duration))) + from)));
        return (elapsed*(change/duration)) + from;
    }*/


    public TrashCan(Context context) {
        trashCan = BitmapFactory.decodeResource(context.getResources(), R.drawable.trash);
        trashCanWidth = trashCan.getWidth();
        trashCanHeight = trashCan.getHeight();

    }


    private void runAnimation(Canvas canvas, float width, float height) {
        canvas.save();
        float px = width - trashCanPadding- (trashCanWidth/2);
        float py = height - trashCanPadding - (trashCanHeight/2);
        if(elapsedTime < flipPoint) { // new collision
            canvas.rotate(rotationDegrees,px,py);
        } else {
            canvas.rotate(-1*rotationDegrees,px,py);
            if(elapsedTime > flopPoint)  elapsedTime = 0;
        }
        drawTrashCan(canvas,width,height);
        canvas.restore();
        elapsedTime = elapsedTime + SystemClock.currentThreadTimeMillis();
    }

    long totaltime = 500;
    long timeTaken = 0;

    private void runAnimation2(Canvas canvas, float width, float height) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastTime;
        timeTaken = timeTaken + elapsed;
        if(timeTaken/totaltime > 1) {
            nextDirection = nextDirection*-1;
            timeTaken = 0;
        }
        //elapsedTime = elapsedTime + SystemClock.currentThreadTimeMillis();

       /* int numberOfFlips = (int)elapsedTime/switchTime;
        if(numberOfFlips != 0) {
            if(numberOfFlips%2 == 0) { //is even
                //elapsedTime = 0;
                drawTrashCan(canvas,width,height);
            } else {
                canvas.save();
                float xPointOfRotation = width - trashCanPadding- (trashCanWidth/2);
                float yPointOfRotation = height - trashCanPadding - (trashCanHeight/2);
                canvas.rotate(nextDirection*rotationDegrees,xPointOfRotation,yPointOfRotation);
                drawTrashCan(canvas,width,height);

                nextDirection = nextDirection*-1;
                canvas.restore();
                //elapsedTime = 0;

            }
        } else {
            drawTrashCan(canvas,width,height);

        }*/
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
            runAnimation2(canvas,width,height);
        } else {
            drawTrashCan(canvas,width,height);
        }
    }

    private void drawTrashCan(Canvas canvas, float width, float height) {
        canvas.drawBitmap(trashCan, width - trashCanWidth - trashCanPadding, height - trashCanHeight - trashCanPadding, null);
    }

    public boolean collidesWithTrashCan(MagnetTile movingTile, float width, float height) {
        float halfPaddingWidth = (movingTile.width() )/2;
        float halfPaddingHeight = (movingTile.height())/2;
        return !(width - trashCanWidth > movingTile.x() + halfPaddingWidth ||
                width - trashCanWidth + trashCan.getWidth() < movingTile.x() - halfPaddingWidth ||
                height-trashCanHeight > movingTile.y() + halfPaddingHeight ||
                height-trashCanHeight + trashCan.getHeight() < movingTile.y() - halfPaddingHeight);
    }
}
