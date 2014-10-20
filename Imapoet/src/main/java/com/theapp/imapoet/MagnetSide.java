package com.theapp.imapoet;

import android.graphics.PointF;

/**
 * Created by whitney on 10/15/14.
 */
public class MagnetSide implements Comparable<MagnetSide>{
    public int id;
    public MagnetTile referenceToMagnetTile;
    public PointF xAndyDistances = new PointF(0,0);

    @Override
    public int compareTo(MagnetSide anotherSide) {
        return Math.abs((int)(xAndyDistances.x*xAndyDistances.y) - (int)(anotherSide.xAndyDistances.x*anotherSide.xAndyDistances.y));
    }
    public MagnetSide(int id, MagnetTile referenceToMagnetTile, float xDistance, float yDistance) {
        this.id = id;
        this.referenceToMagnetTile = referenceToMagnetTile;
        this.xAndyDistances.set(xDistance,yDistance);
    }
    /* This function returns the closest side as a magnetSide object depending on which of four quadrants the moving tile is in.*/
    protected static MagnetSide closestSide(MagnetTile movingTile, MagnetTile stationaryTile) {
        if(movingTile.x() <= stationaryTile.x()) {
            if(movingTile.y() <= stationaryTile.y()) {
                return MagnetSide.getQuadrant1Side(movingTile, stationaryTile);
            } else {
                return MagnetSide.getQuadrant3Side(movingTile,stationaryTile);
            }
        } else {
            if(movingTile.y() <= stationaryTile.y()) {
                return MagnetSide.getQuadrant2Side(movingTile,stationaryTile);
            } else {
                return MagnetSide.getQuadrant4Side(movingTile,stationaryTile);
            }
        }
    }

    /* Returns true if the possible connecting sides are parallel. This case happens when the top two sides are next to each other. If true, getLockingSides will pick the closest one */
    protected static boolean areParallel(PointF side1Distances, PointF side2Distances) {
        return (side1Distances.x == 0 && side2Distances.x == 0) || (side1Distances.y == 0 && side2Distances.y == 0);
    }

    protected static MagnetSide getQuadrant1Side(MagnetTile movingTile, MagnetTile stationaryTile) {
        float xDifference = stationaryTile.leftTopCornerX() - movingTile.rightBottomCornerX();
        float yDifference = stationaryTile.leftTopCornerY() - movingTile.rightBottomCornerY();
        if(xDifference >= 0 && yDifference < 0) { // move right, moving tile is to the left and in line
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,0);
        } else if(xDifference >= 0 && yDifference >= 0) { // move diagonal, moving tile is to the left and above
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,yDifference);
        } else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
            return new MagnetSide(movingTile.id(),stationaryTile,0,yDifference);
        }
    }
    protected static MagnetSide getQuadrant2Side(MagnetTile movingTile, MagnetTile stationaryTile) {
        float xDifference = stationaryTile.rightTopCornerX() - movingTile.leftBottomCornerX();
        float yDifference = stationaryTile.rightTopCornerY() - movingTile.leftBottomCornerY();
        if(xDifference < 0 && yDifference >= 0) { // move right, moving tile is to the left and in line
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,yDifference);
        }
        if(xDifference >= 0 && yDifference >= 0) { // move diagonal, moving tile is to the left and above
            return new MagnetSide(movingTile.id(),stationaryTile,0,yDifference);
        }
        else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,0);
        }
    }

    protected static MagnetSide getQuadrant3Side(MagnetTile movingTile, MagnetTile stationaryTile) {
        float xDifference = stationaryTile.leftBottomCornerX() - movingTile.rightTopCornerX();
        float yDifference =  stationaryTile.leftBottomCornerY() - movingTile.rightTopCornerY();
        if(xDifference >= 0 && yDifference <= 0) { // move right, moving tile is to the left and in line
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,yDifference);
        }
        if(xDifference >= 0 && yDifference >= 0) { // move diagonal, moving tile is to the left and above
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,0);
        }
        else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
            return new MagnetSide(movingTile.id(),stationaryTile,0,yDifference);
        }
    }

    protected static MagnetSide getQuadrant4Side(MagnetTile movingTile, MagnetTile stationaryTile) {
        float xDifference =  stationaryTile.rightBottomCornerX() - movingTile.leftTopCornerX();
        float yDifference = stationaryTile.rightBottomCornerY() - movingTile.leftTopCornerY();

        System.out.println("testing "+Float.toString(stationaryTile.rightBottomCornerY())+","+Float.toString(movingTile.leftTopCornerY())+", "+Float.toString(yDifference));
        if(xDifference >= 0 && yDifference < 0) { // move right, moving tile is to the left and in line
            return new MagnetSide(movingTile.id(),stationaryTile,0,yDifference);
        }
        if(xDifference < 0 && yDifference < 0) { // move diagonal, moving tile is to the left and above
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,yDifference);
        }
        else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,0);
        }
    }

}
