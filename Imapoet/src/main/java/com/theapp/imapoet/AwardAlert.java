package com.theapp.imapoet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import java.util.ArrayList;

/**
 * Controls the radial gradient used in the award animation
 * Created by whitney on 8/11/14.
 */
public class AwardAlert {
    public Bitmap awardAlertBitmap;
    protected float awardAlertWidth;
    protected float awardAlertHeight;
    private boolean awardAlert = false;
    private ArrayList<Bitmap> newAwardGlow = new ArrayList<Bitmap>(10);
    private float[] sizes = new float[]{1,5,10,15,20,25,30,35,40,45,40,35,30,25,20,15,10,5,1,5,10,15,20,25,30,35,40,45,40,35,30,25,20,15,10,5};
    protected float padding = 15;
    private ArrayList<award> awards = new ArrayList<award>();
    private long timeTaken = 0;
    private long lastTime = 0;


    public void addNewAward(String title, String description) {
        awards.add(new award(title,description,0));
    }

    public AwardAlert(Context context) {
        awardAlertBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.new_award_alert);
        awardAlertWidth = awardAlertBitmap.getWidth();
        awardAlertHeight = awardAlertBitmap.getHeight();
        makeRadialGradient();
    }

    private void makeRadialGradient() {
        for(float size : sizes) {
            RadialGradient gradient = new RadialGradient(size, size, size, Color.parseColor("#EAE857"),
                    Color.parseColor("#ffffff"), android.graphics.Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setDither(true);
            paint.setShader(gradient);
            Bitmap bitmap = Bitmap.createBitmap((int)size*2 + 1, (int)size*2+1, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            c.drawCircle(size, size, size, paint);
            newAwardGlow.add(bitmap);
        }
    }

    private int getNextIndex(long elapsedTime, int numberOfImages, int millisecondsBetweenImages) {
        return (int) (((elapsedTime % (millisecondsBetweenImages * 100)) % (numberOfImages * millisecondsBetweenImages * 10)) / millisecondsBetweenImages);
    }

    public boolean drawAwardAlert(Canvas canvas, float width) {
        boolean invalidate = false;
        if(awardAlert) {
            int currentTime = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            int elapsed = (int) currentTime - (int) lastTime;
            boolean firstRun;
            if (timeTaken == 0) firstRun = true; else firstRun = false;
            timeTaken = (timeTaken + elapsed);
            lastTime = currentTime;
            //int index = (int)timeTaken/300;
            int index = getNextIndex(timeTaken,newAwardGlow.size(),300);
            invalidate = true;
            if(index >= newAwardGlow.size() - 1) {
                index = 0;
                timeTaken = 0;
                if(!firstRun) {
                    awardAlert = false;
                    invalidate = false;
                }
            }
            canvas.drawBitmap(newAwardGlow.get(index), width - awardAlertWidth / 2 - sizes[index] - padding, awardAlertHeight / 2 - sizes[index] + padding, null);
        }
        canvas.drawBitmap(awardAlertBitmap, width - awardAlertWidth - padding, 0 + padding, null);
        //canvas.drawBitmap(awardAlertBitmap, width - awardAlertWidth - padding, 0 + padding, null);
        return invalidate;
    }

    public void setAlert(boolean award) {
        awardAlert = award;
    }

    public void removeLastAward() {
        awards.remove(awards.size()-1);
    }

    public int getAwardsSize() {
        return awards.size();
    }

    public award getNextAward() {
        return awards.get(awards.size()-1);
    }


}
