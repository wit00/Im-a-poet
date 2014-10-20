package com.theapp.imapoet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import java.util.ArrayList;

public class MagnetTile {
    private float scaleFactor;
    private int id;
    private String word;
    private int packID;
    private float x = 0;
    private float y = 0;
    private float unscaledWidth;
    private float unscaledHeight = 30;
    private float width;
    private float height;
    private Paint magnetPaint = makePaint(Color.WHITE);
    private Paint borderPaint = makePaint(Color.parseColor("#3C332A"));
    private Paint textPaint = makePaint(Color.parseColor("#3C332A"));
    float textPaintSize = 12.0f;
    private float borderWidth = 5;
    private boolean scaled = false;
    private boolean highlight = false;
    private Paint highlightPaint = makePaint(Color.parseColor("#EAE857"));
    private float textYShift;
    private PointF leftTopCorner = new PointF(0,0);
    private PointF leftBottomCorner = new PointF(0,0);
    private PointF rightTopCorner = new PointF(0,0);
    private PointF rightBottomCorner = new PointF(0,0);

    public String magnetColor() {
        return "#ffffff";
    }

    private Paint makePaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    public MagnetTile(String word,int id, float scaleFactor, int packID){
        textPaint.setTextSize(textPaintSize);
        this.word = word;
        this.scaleFactor = scaleFactor;
        float padding = 5;
        this.unscaledWidth = textPaint.measureText(word) + padding + borderWidth;
        width = unscaledWidth * scaleFactor;
        height = unscaledHeight * scaleFactor;
        this.id = id + 1;
        this.packID = packID;
        textPaint.setTextAlign(Paint.Align.CENTER);
        setNewTextSize();
    }

    public void setHighlight(Boolean highlight) {
        this.highlight = highlight;
    }

    private float convertCenterToCorner(float centerCoordinate, float length) {
        return centerCoordinate - length/2;
    }

    public void setNewTextSize() {
        textPaint.setTextSize(textPaintSize*(scaleFactor));
        textYShift = textPaint.getTextSize()/3;
    }

    public void draw(Canvas canvas, float canvasWidth, float canvasHeight) {
        Paint currentPaint = new Paint();
        currentPaint = magnetPaint;
        if(highlight) currentPaint = highlightPaint;
        canvas.drawRect(convertCenterToCorner(x,width), convertCenterToCorner(y,height),x + ((width)/2),y + ((height)/2),borderPaint);
        canvas.drawRect(convertCenterToCorner(x,width-borderWidth), convertCenterToCorner(y,height-borderWidth),x + ((width-borderWidth)/2),y + ((height-borderWidth)/2),currentPaint);
        if(scaled) {
            setNewTextSize();
            scaled = false;
        }
        canvas.drawText(word,x,y + textYShift, textPaint);
    }

    public String word() { return word; }
    public float x() { return x; }
    public float y() { return y; }
    public float width() { return width; }
    public float height() { return height; }
    public void setX(float x) {
        this.x = x;
        float halfWidth = width/2;
        leftTopCorner.set(x-halfWidth,leftTopCornerY());
        leftBottomCorner.set(x-halfWidth,leftBottomCornerY());
        rightTopCorner.set(x+halfWidth,rightTopCornerY());
        rightBottomCorner.set(x+halfWidth,rightBottomCornerY());

    }
    public void setY(float y) {
        this.y = y;
        float halfWidth = width/2;
        float halfHeight = height/2;
        leftTopCorner.set(x-halfWidth,y-halfHeight);
        leftBottomCorner.set(x-halfWidth,y+halfHeight);
        rightTopCorner.set(x+halfWidth,y-halfHeight);
        rightBottomCorner.set(x+halfWidth,y+halfHeight);
    }
    public float leftTopCornerX() { return leftTopCorner.x; }
    public float leftTopCornerY() { return leftTopCorner.y; }
    public float leftBottomCornerX() { return leftBottomCorner.x; }
    public float leftBottomCornerY() { return leftBottomCorner.y; }
    public float rightTopCornerX() { return rightTopCorner.x; }
    public float rightTopCornerY() { return rightTopCorner.y; }
    public float rightBottomCornerX() { return rightBottomCorner.x; }
    public float rightBottomCornerY() { return rightBottomCorner.y; }




    public void setXAndY(float x, float y) {
        this.x = x;
        this.y = y;
        float halfWidth = width/2;
        float halfHeight = height/2;
        leftTopCorner.set(x-halfWidth,y-halfHeight);
        leftBottomCorner.set(x-halfWidth,y+halfHeight);
        rightTopCorner.set(x+halfWidth,y-halfHeight);
        rightBottomCorner.set(x+halfWidth,y+halfHeight);
    }
    public int id() { return id; }
    public int packID() { return packID; }
    public void updateScaleFactor(float scaleFactor, ArrayList<MagnetTile> magnetTiles) {
        this.scaleFactor = scaleFactor;
        //borderWidth = borderWidth * scaleFactor;
        width = unscaledWidth * scaleFactor;
        height = unscaledHeight * scaleFactor;
        scaled = true;
        //isTouchingOtherMagnetTile(magnetTiles);
    }
}
