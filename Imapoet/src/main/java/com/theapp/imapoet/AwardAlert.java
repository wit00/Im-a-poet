package com.theapp.imapoet;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.os.SystemClock;
import java.util.ArrayList;

/**
 * Created by whitney on 8/11/14.
 */
public class AwardAlert {
    private Context context;
    public Bitmap awardAlertBitmap;
    protected float awardAlertWidth;
    protected float awardAlertHeight;
    private boolean awardAlert = false;
    private long elapsedTime = 0;
    private ArrayList<Bitmap> newAwardGlow = new ArrayList<Bitmap>(10);
    private int newAwardGlowIndex = 0;
    private boolean moveUp = true;
    private float[] sizes = new float[]{1,5,10,15,20,25,30,35,40,45};
    protected float padding = 15;
    private boolean countdown = false;
    private ArrayList<award> awards = new ArrayList<award>();
    private AsyncQueryHandler queryHandler;
    private long timeTaken = 0;
    private long lastTime = System.currentTimeMillis();


    public void addNewAward(String title, String description) {
        awards.add(new award(title,description,0,0));
    }

    public AwardAlert(Context context) {
        awardAlertBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.new_award_alert);
        awardAlertWidth = awardAlertBitmap.getWidth();
        awardAlertHeight = awardAlertBitmap.getHeight();
        makeRadialGradient();
        this.context = context;
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

    private void moveNewAwardGlowIndex() {
        if(moveUp) {
            if(newAwardGlowIndex != newAwardGlow.size() - 1) {
                newAwardGlowIndex ++;
            } else {
                moveUp = false;
            }
        } else {
            if(newAwardGlowIndex != 0) {
                newAwardGlowIndex --;
            } else {
                moveUp = true;
            }
        }
    }

    private void runAwardAnimation(Canvas canvas, float width) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastTime;
        timeTaken = timeTaken + elapsed;
        canvas.drawBitmap(newAwardGlow.get(newAwardGlowIndex),width-awardAlertWidth/2-sizes[newAwardGlowIndex] - padding,awardAlertHeight/2-sizes[newAwardGlowIndex] + padding,null);
        long gradientChange = 30000;
        if(timeTaken >= gradientChange) {
            moveNewAwardGlowIndex();
            timeTaken = 0;
        }
    }

    private boolean runCountdownAnimation(Canvas canvas, float width) {
        if(newAwardGlowIndex == 0) {
            awardAlert = false;
            countdown = false;
            return false;
        } else {
            runAwardAnimation(canvas, width);
            return  true;
        }
    }

    public boolean drawAwardAlert(Canvas canvas, float width) {
        boolean invalidate = false;
        if(awardAlert) {
            if (countdown) {
                invalidate = runCountdownAnimation(canvas,width);
            } else {
                long awardTime = 2000000;
                if (elapsedTime < awardTime) {
                    runAwardAnimation(canvas, width);
                    elapsedTime = elapsedTime + SystemClock.currentThreadTimeMillis();
                    invalidate = true;
                } else {
                    elapsedTime = 0;
                    countdown = true;
                    moveUp = false;
                    invalidate = runCountdownAnimation(canvas,width);
                }
            }
        }
        canvas.drawBitmap(awardAlertBitmap, width - awardAlertWidth - padding, 0 + padding, null);
        return invalidate;
    }

    public void setAlert(boolean award) {
        awardAlert = award;
    }

    private void createAsyncQueryHandler() {
        queryHandler = new AsyncQueryHandler(context.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                switch (token) {
                    case 1:
                        if(cursor.getCount() > 0) {
                            cursor.moveToFirst();

                        } else {
                           // setSettings(true,true);
                        }
                        break;
                }
            }
        };
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
