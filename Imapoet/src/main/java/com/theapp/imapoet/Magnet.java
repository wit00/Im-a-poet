package com.theapp.imapoet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Magnet {
    private int id;
    private String word;
    private int packID;
    private float x = 0;
    private float y = 0;
    private float width;
    private float height;
    private Paint magnetPaint = makePaint(Color.parseColor("#E7E0DB"));
    private Paint borderPaint = makePaint(Color.parseColor("#3C332A"));
    private Paint textPaint = makePaint(Color.parseColor("#3C332A"));
    float textPaintSize = 60.0f;
    private float borderWidth = 5;
    private boolean highlight = false;
    private Paint highlightPaint = makePaint(Color.parseColor("#EAE857"));
    private float textYShift;
    private PointF leftTopCorner = new PointF(0,0);
    private PointF leftBottomCorner = new PointF(0,0);
    private PointF rightTopCorner = new PointF(0,0);
    private PointF rightBottomCorner = new PointF(0,0);
    private ArrayList<Magnet> topSideConnectedMagnets = new ArrayList<Magnet>();
    private ArrayList<Magnet>  bottomSideConnectedMagnets = new ArrayList<Magnet>();
    private ArrayList<Magnet>  leftSideConnectedMagnets = new ArrayList<Magnet>();
    private ArrayList<Magnet>  rightSideConnectedMagnets = new ArrayList<Magnet>();
    private ArrayList<Integer> topSideConnectedIds = new ArrayList<Integer>();
    private ArrayList<Integer> bottomSideConnectedIds = new ArrayList<Integer>();
    private ArrayList<Integer> leftSideConnectedIds = new ArrayList<Integer>();
    private ArrayList<Integer> rightSideConnectedIds = new ArrayList<Integer>();
    private Context context;


    public void setTopSideConnectedMagnet(Magnet magnet) {
        topSideConnectedMagnets.add(magnet);
    }
    public void setBottomSideConnectedMagnet(Magnet magnet) { bottomSideConnectedMagnets.add(magnet); }
    public void setLeftSideConnectedMagnet(Magnet magnet) {
        leftSideConnectedMagnets.add(magnet);
    }
    public void setRightSideConnectedMagnet(Magnet magnet) { rightSideConnectedMagnets.add(magnet); }



    public Magnet clone() {
        return new Magnet(context,this.word,this.id,0,this.packID,this.topSideConnectedMagnets,this.bottomSideConnectedMagnets,this.rightSideConnectedMagnets,this.leftSideConnectedMagnets,this.x,this.y,textPaintSize);
    }

    // a convenience function used by setUpConnectedSides to get a reference to each poemMagnet in the connected magnets arrays and use that reference to replace the temporary one in the list, used when the user loads the manual save or the program loads the auto-saved poem
    private void setUpSide(ArrayList<Magnet> poemMagnets, ArrayList<Integer> connectedMagnetIntegers, ArrayList<Magnet> connectedMagnets) {
        for(Integer ID : connectedMagnetIntegers) {
            if(ID != null) {
                for(Magnet poemMagnet : poemMagnets) {
                    if(ID == poemMagnet.id()) {
                        connectedMagnets.add(poemMagnet);
                    }
                }
            }
        }
    }

    // replace the temporary magnets in the connected magnets list with the real reference to the magnet (used when magnets are loaded from the manual save or the auto save0
    public void setUpConnectedSides(ArrayList<Magnet> poemMagnets) {
        setUpSide(poemMagnets, topSideConnectedIds,topSideConnectedMagnets);
        setUpSide(poemMagnets,bottomSideConnectedIds,bottomSideConnectedMagnets);
        setUpSide(poemMagnets,leftSideConnectedIds,leftSideConnectedMagnets);
        setUpSide(poemMagnets,rightSideConnectedIds,rightSideConnectedMagnets);
    }

    // turn a connected magnet array inlist into a string, separated by commas
    public String getConnectedMagnetsString(ArrayList<Magnet> connectedMagnets) {
        String connectedMagnetsString = "";
        for(Magnet magnet : connectedMagnets) {
            connectedMagnetsString = connectedMagnetsString + Integer.toString(magnet.id()) + ",";
        }
        return connectedMagnetsString;
    }

    public void clearAllConnectedMagnets() {
        topSideConnectedMagnets.clear();
        bottomSideConnectedMagnets.clear();
        leftSideConnectedMagnets.clear();
        rightSideConnectedMagnets.clear();
    }

    public ArrayList<Magnet>  topSideConnectedMagnet() { return topSideConnectedMagnets; }
    public ArrayList<Magnet>  bottomSideConnectedMagnet() { return bottomSideConnectedMagnets; }
    public ArrayList<Magnet>  leftSideConnectedMagnet() { return leftSideConnectedMagnets; }
    public ArrayList<Magnet>  rightSideConnectedMagnet() { return rightSideConnectedMagnets; }


    public String magnetColor() {
        return "#ffffff";
    }

    private Paint makePaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }


    private List<String> separateStringIntoStringIds(String connectedMagnets){
        return Arrays.asList(connectedMagnets.split(","));
    }


    public Magnet(Context context, String word, int id, float scaleFactor, int packID,String top,String bottom, String left, String right,float density){
        clearAllConnectedMagnets();
        this.word = word;
        this.context = context;
        this.id = id;
        this.packID = packID;
        //setTextSizeBasedOnDensity(density);
        textPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.magnet_starting_text_size));
        textPaint.setTextAlign(Paint.Align.CENTER);
        Rect bounds = new Rect();
        textPaint.getTextBounds(word,0,word.length(),bounds);
        float padding = 15;
        width = bounds.width() + padding + borderWidth;
        height = bounds.height() + padding + borderWidth;
        textYShift = height/4;
        setTemporaryConnectedMagnets(top,bottom,left,right);
    }

    public Magnet(Context context, String word, int id, float scaleFactor, int packID,ArrayList<Magnet> topSideConnectedMagnets,ArrayList<Magnet> bottomSideConnectedMagnets, ArrayList<Magnet> leftSideConnectedMagnets, ArrayList<Magnet> rightSideConnectedMagnets, float x, float y, float textPaintSize){
        clearAllConnectedMagnets();
        textPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.magnet_starting_text_size));
        this.word = word;
        this.context = context;
        Rect bounds = new Rect();
        textPaint.getTextBounds(word,0,word.length(),bounds);
        float padding = 15;
        width = bounds.width() + padding + borderWidth;
        height = bounds.height() + padding + borderWidth;
        this.id = id;
        this.packID = packID;
        textPaint.setTextAlign(Paint.Align.CENTER);
        textYShift = height/4;
        this.topSideConnectedMagnets = topSideConnectedMagnets;
        this.bottomSideConnectedMagnets = bottomSideConnectedMagnets;
        this.leftSideConnectedMagnets = leftSideConnectedMagnets;
        this.rightSideConnectedMagnets = rightSideConnectedMagnets;
        setX(x);
        setY(y);
    }



    // creates temporary magnets with the ids of the connected magnets loaded from the database, the full magnets will be filled in later with setUpConnectedSides
    private void setTemporaryConnectedMagnets(String top, String bottom, String left, String right) {
        topSideConnectedIds.clear();
        bottomSideConnectedIds.clear();
        leftSideConnectedIds.clear();
        rightSideConnectedIds.clear();
        if(top != null) {
            for(String connectedMagnetStringID : separateStringIntoStringIds(top)) {
                if(!connectedMagnetStringID.equals("")) topSideConnectedIds.add(Integer.valueOf(connectedMagnetStringID));
            }
        }
        if(bottom != null) {
            for(String connectedMagnetStringID : separateStringIntoStringIds(bottom)) {
                if(!connectedMagnetStringID.equals("")) bottomSideConnectedIds.add(Integer.valueOf(connectedMagnetStringID));
            }
        }
        if(left != null) {
            for(String connectedMagnetStringID : separateStringIntoStringIds(left)) {
                if(!connectedMagnetStringID.equals("")) leftSideConnectedIds.add(Integer.valueOf(connectedMagnetStringID));
            }
        }
        if(right != null) {
            for(String connectedMagnetStringID : separateStringIntoStringIds(right)) {
                if(!connectedMagnetStringID.equals("")) rightSideConnectedIds.add(Integer.valueOf(connectedMagnetStringID));
            }
        }
    }


    public boolean areConnected(Magnet otherMagnet) {
        return (topSideConnectedMagnets.contains(otherMagnet) || bottomSideConnectedMagnets.contains(otherMagnet) || leftSideConnectedMagnets.contains(otherMagnet) || rightSideConnectedMagnets.contains(otherMagnet));
    }

    public void setHighlight(Boolean highlight) {
        this.highlight = highlight;
    }

    private float convertCenterToCorner(float centerCoordinate, float length) {
        return centerCoordinate - length/2;
    }


    private void drawMagnet(Canvas canvas, Paint currentPaint) {
        borderPaint.clearShadowLayer();
        canvas.drawRect(convertCenterToCorner(x,width), convertCenterToCorner(y,height),x + ((width)/2),y + ((height)/2),borderPaint);
        canvas.drawRect(convertCenterToCorner(x,width-borderWidth), convertCenterToCorner(y,height-borderWidth),x + ((width-borderWidth)/2),y + ((height-borderWidth)/2),currentPaint);
    }

    private void drawShadowedMagnet(Canvas canvas, Paint currentPaint,View view) {
        borderPaint.setShadowLayer(10.0f, 3.0f, 5.0f, Color.parseColor("#3C332A"));
        //borderPaint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
        view.setLayerType(View.LAYER_TYPE_SOFTWARE,currentPaint);
        canvas.drawRect(convertCenterToCorner(x,width), convertCenterToCorner(y,height),x + ((width)/2),y + ((height)/2),borderPaint);
        canvas.drawRect(convertCenterToCorner(x,width-borderWidth), convertCenterToCorner(y,height-borderWidth),x + ((width-borderWidth)/2),y + ((height-borderWidth)/2),currentPaint);
    }

    public void draw(Canvas canvas, float canvasWidth, float canvasHeight, View view) {
        Paint currentPaint = magnetPaint;
        if(highlight) currentPaint = highlightPaint;
        if(topSideConnectedMagnets.size() > 0 || bottomSideConnectedMagnets.size() > 0 || leftSideConnectedMagnets.size() > 0 || rightSideConnectedMagnets.size() > 0) {
            drawMagnet(canvas, currentPaint);
        } else {
            drawShadowedMagnet(canvas, currentPaint,view);
        }

        canvas.drawText(word,x,y + textYShift, textPaint);
    }

    public String word() { return word; }
    public float x() { return x; }
    public float y() { return y; }
    public float width() { return width; }
    public float height() { return height; }
    public PointF leftTopCorner() { return leftTopCorner; }
    public PointF leftBottomCorner() { return leftBottomCorner; }
    public PointF rightTopCorner() { return rightTopCorner; }
    public PointF rightBottomCorner() { return rightBottomCorner; }
    public int id() { return id; }
    public int packID() { return packID; }
    public void setX(float x) {
        this.x = x;
        float halfWidth = width/2;
        leftTopCorner.set(x-halfWidth,leftTopCorner.y);
        leftBottomCorner.set(x-halfWidth,leftBottomCorner.y);
        rightTopCorner.set(x+halfWidth,rightTopCorner.y);
        rightBottomCorner.set(x+halfWidth,rightBottomCorner.y);
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

}
