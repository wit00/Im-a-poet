package com.theapp.imapoet;

import android.graphics.PointF;

/**
 * Created by whitney on 10/15/14.
 */
public class MagnetSide implements Comparable<MagnetSide>{
    public int id;
    public Magnet referenceToMagnet;
    public PointF xAndyDistances = new PointF(0,0);
    public Side fromSide;
    public Side toSide;

    @Override
    public int compareTo(MagnetSide anotherSide) {
        return Math.abs((int)(xAndyDistances.x + xAndyDistances.y)) - Math.abs((int)(anotherSide.xAndyDistances.x + anotherSide.xAndyDistances.y));
    }


    public MagnetSide(int id, Magnet referenceToMagnet, float xDistance, float yDistance, Side fromSide, Side toSide) {
        this.id = id;
        this.referenceToMagnet = referenceToMagnet;
        this.xAndyDistances.set(xDistance,yDistance);
        this.fromSide = fromSide;
        this.toSide = toSide;
    }

    /* This function returns the closest side as a magnetSide object depending on which of four quadrants the moving tile is in.*/
    protected static MagnetSide closestSide(Magnet movingTile, Magnet stationaryTile) {
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

   protected static MagnetSide getQuadrant1Side(Magnet movingTile, Magnet stationaryTile) {
       float xDifference = stationaryTile.leftTopCorner().x - movingTile.rightBottomCorner().x;
       float yDifference = stationaryTile.leftTopCorner().y - movingTile.rightBottomCorner().y;

       if (xDifference >= 0 && yDifference < 0) { // move right, moving tile is to the left and in line
           return new MagnetSide(movingTile.id(), stationaryTile, xDifference, 0, Side.RIGHT, Side.LEFT);
       } else if (xDifference >= 0 && yDifference >= 0) { // move diagonal, moving tile is to the left and above
           //return new MagnetSide(movingTile.id(), stationaryTile, xDifference, yDifference);
           return null;
       } else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
           return new MagnetSide(movingTile.id(), stationaryTile, 0, yDifference, Side.BOTTOM, Side.TOP);
       }
   }


    protected static MagnetSide getQuadrant2Side(Magnet movingTile, Magnet stationaryTile) {
        float xDifference = stationaryTile.rightTopCorner().x - movingTile.leftBottomCorner().x;
        float yDifference = stationaryTile.rightTopCorner().y - movingTile.leftBottomCorner().y;

        if(xDifference < 0 && yDifference >= 0) { // move right, moving tile is to the left and in line
            //return new MagnetSide(movingTile.id(),stationaryTile,xDifference,yDifference);
            return null;
        }
        if(xDifference >= 0 && yDifference >= 0) { // move diagonal, moving tile is to the left and above
            return new MagnetSide(movingTile.id(),stationaryTile,0,yDifference, Side.BOTTOM, Side.TOP);
        }
        else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,0, Side.LEFT, Side.RIGHT);
        }
    }

    protected static MagnetSide getQuadrant3Side(Magnet movingTile, Magnet stationaryTile) {
        float xDifference = stationaryTile.leftBottomCorner().x - movingTile.rightTopCorner().x;
        float yDifference =  stationaryTile.leftBottomCorner().y - movingTile.rightTopCorner().y;

        if(xDifference >= 0 && yDifference <= 0) { // move right, moving tile is to the left and in line
            //return new MagnetSide(movingTile.id(),stationaryTile,xDifference,yDifference);
            return null;
        }
        if(xDifference >= 0 && yDifference >= 0) { // move diagonal, moving tile is to the left and above
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,0, Side.RIGHT, Side.LEFT);
        }
        else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
            return new MagnetSide(movingTile.id(),stationaryTile,0,yDifference, Side.TOP, Side.BOTTOM);
        }
    }

    protected static MagnetSide getQuadrant4Side(Magnet movingTile, Magnet stationaryTile) {
        float xDifference =  stationaryTile.rightBottomCorner().x - movingTile.leftTopCorner().x;
        float yDifference = stationaryTile.rightBottomCorner().y - movingTile.leftTopCorner().y;

        if(xDifference >= 0 && yDifference < 0) { // move right, moving tile is to the left and in line
            return new MagnetSide(movingTile.id(), stationaryTile, 0, yDifference, Side.TOP, Side.BOTTOM);
        }
        if(xDifference < 0 && yDifference < 0) { // move diagonal, moving tile is to the left and above
            //return new MagnetSide(movingTile.id(),stationaryTile,xDifference,yDifference);
            return null;
        } else { //(xDifference < 0 && yDifference >=0) { // move down, moving tile is above and in line
            return new MagnetSide(movingTile.id(),stationaryTile,xDifference,0, Side.LEFT, Side.RIGHT);
        }
    }

}
