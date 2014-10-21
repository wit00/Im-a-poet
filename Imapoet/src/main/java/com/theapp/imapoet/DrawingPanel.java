package com.theapp.imapoet;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Drawing canvas
 * Created by whitney on 6/5/14.
 */
public class DrawingPanel extends SurfaceView implements SurfaceHolder.Callback, View.OnDragListener, GameState.DrawingPanelListener, AwardManager.AwardManagerListener {
    private CanvasListener canvasListener;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private String word = "";
    private ArrayList<Magnet> magnets = new ArrayList<Magnet>();
    private boolean notAddedTile = false;
    private float scaleFactor = 1.5f;
    private ArrayList<Magnet> clickedMagnets = new ArrayList<Magnet>();
    public Magnet clickedMagnet = null;
    public float collisionZonePadding = 10;
    private boolean soundEffects = false;
    private boolean music = false;
    private TrashCan trashCan;
    private AwardAlert awardAlert;
    private boolean magnetIsAboveTrashCan = false; // a boolean that tells if a magnet is to be deleted
    private ArrayList<Magnet> toHighlightMagnets = new ArrayList<Magnet>();
    private MediaPlayer mediaPlayer = MediaPlayer.create(getContext(),R.raw.clonk);
    private ArrayList<Integer> packsUsedIds = new ArrayList<Integer>(5);
    private int packID;
    private ArrayList<MagnetSide> sidesToLockToNext = new ArrayList<MagnetSide>(2); // will never be more than 2
    private boolean continuouslyAnimateTrashCan = false;
    private boolean continuouslyAnimateAward = false;
    private boolean previouslySavedPoem = false;
    private String previouslySavedPoemID = null;
    private String previouslySavedPoemName = null;


    /* The CanvasListener interface is implemented by the MainActivity and lets the MainActivity know when something important has happened in the drawing area. The MainActivity then alertsthe GameState or AwardAlert as needed. */
    public interface CanvasListener {
        public void magnetTilesChanged(int numberMagnetTiles);
        public void magnetDeleted();
        public void awardClicked();
    }

    /* The MainActivity calls this method when a new tile has been moved from the drawerFragment onto the drawing surface. The letter or word on the magnet and the magnet's pack ID are stored in the temporary word and packID variables. It returns the number of different packs used during this session so an alert can be sent if the right number of packs have been used. */
    public int setWord(String new_word, int newPackID) {
        this.word = new_word;
        this.packID = newPackID;
        if(!packsUsedIds.contains(newPackID)) packsUsedIds.add(newPackID);
        return packsUsedIds.size();
    }

    // used by loadMagnets and loadSavedMagnets to put the magnets and pack info into the appropriate member variables from the Cursor
    private int getMagnetsAndPacksFromCursor(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn) {
        int thisPackID = cursor.getInt(cursor.getColumnIndex(packIDColumn));
        magnets.add(new Magnet(cursor.getString(cursor.getColumnIndex(textColumn)), magnets.size(),scaleFactor,thisPackID));
        Magnet currentMagnet = magnets.get(magnets.size()-1);
        currentMagnet.setX(cursor.getInt(cursor.getColumnIndex(xColumn)));
        currentMagnet.setY(cursor.getInt(cursor.getColumnIndex(yColumn)));
        int id = cursor.getInt(cursor.getColumnIndex(idColumn));
        if(!packsUsedIds.contains(thisPackID)) packsUsedIds.add(thisPackID);
        return id;
    }

    // Set the values for the previously saved poem member variables
    private void setPreviouslySavedPoem(Boolean saved, String id, String title) {
        previouslySavedPoem = saved;
        previouslySavedPoemID = id;
        previouslySavedPoemName = title;
    }

    /* Implements GameState.drawingPanelListener. The listener calls this function during onResume when the magnets need to be loaded back onto the canvas. This call covers magnets restored from the auto-save and the manual save. */
    public String loadMagnets(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn) {
        int id = -1;
        magnets.clear();
        String ifSavedPoemTitle = null;
        for(int i = 0; i < cursor.getCount(); i++) {
            id = getMagnetsAndPacksFromCursor(cursor,packIDColumn,textColumn,xColumn,yColumn,idColumn);
            ifSavedPoemTitle = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE));
            cursor.moveToNext();
        }
        if(id != -1) {
            setPreviouslySavedPoem(true,Integer.toString(id),ifSavedPoemTitle);
        } else {
            setPreviouslySavedPoem(false,null,null);
        }
        invalidate(); // redraw the canvas
        return  Integer.toString(id);
    }

    public String loadSavedMagnets(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn) {
        int id = -1;
        magnets.clear();
        while(cursor.moveToNext()) {
            id = getMagnetsAndPacksFromCursor(cursor,packIDColumn,textColumn,xColumn,yColumn,idColumn);
        }
        invalidate(); // redraw the canvas
        return  Integer.toString(id);
    }

    public boolean getSavedPoemState() {
        return previouslySavedPoem;
    }

    public void setSavedPoemState(boolean savedPoem,String title) {
        this.previouslySavedPoem = savedPoem;
        this.previouslySavedPoemName = title;
        System.out.println("loading, changing title to - " + previouslySavedPoemName);
    }
    public void setSavedPoemState(boolean savedPoem) {
        this.previouslySavedPoem = savedPoem;
    }
    public String getSavedPoemId() {
        return previouslySavedPoemID;
    }
    public String getSavedPoemName() {
        return previouslySavedPoemName;
    }
    public void setSavedPoemId(String savedPoemId) {
        this.previouslySavedPoemID = savedPoemId;
    }

    public String load(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn) {
        String id = "";
        magnets.clear();
        //canvasListener.reportSavedId(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID)));
        for(int i = 0; i < cursor.getCount(); i++) {
            int thisPackID = cursor.getInt(cursor.getColumnIndex(packIDColumn));
            magnets.add(new Magnet(cursor.getString(cursor.getColumnIndex(textColumn)), magnets.size(),scaleFactor,thisPackID));
            Magnet currentMagnet = magnets.get(magnets.size()-1);
            currentMagnet.setX(cursor.getInt(cursor.getColumnIndex(xColumn)));
            currentMagnet.setY(cursor.getInt(cursor.getColumnIndex(yColumn)));
            id = cursor.getString(cursor.getColumnIndex(idColumn));
            if(!packsUsedIds.contains(thisPackID)) packsUsedIds.add(thisPackID);
            cursor.moveToNext();
        }
        System.out.println("loading");
        System.out.println("loading "+Integer.toString(magnets.size())+","+cursor.getCount());
        invalidate(); // redraw the canvas
        return  id;
    }

    /* Implements the GameState.drawingPanel Listener. It sets the sound effects and game music settings. */
    public void setSettings(boolean soundEffects, boolean music) {
        this.soundEffects = soundEffects;
        this.music = music;
    }

    /* Implements the AwardManager.AwardManager Listener. Sets a new award to be displayed and makes a call to redraw the canvas. */
    public void loadAward(String name, String description, int id) {
        awardAlert.setAlert(true);
        awardAlert.awardName = name;
        awardAlert.awardDescription = description;
        invalidate();
    }

    /* Implements the AwardManager Listener. Lets the drawing panel know that the awards manager is alive and returns the number of magnets on the screen at this time. */
    public int getInitialNumberMagnets() {
        return magnets.size();
    }

    /* Clears the magnets from the screen and memory.
       Called by the MainActivity when the clear the screen
       button is pressed. */
    public void clearMagnets() {
        magnets.clear();
        packsUsedIds.clear();
        invalidate();
    }

    /* Called by the MainActivity, getPoem() returns the magnetTiles ArrayList */
    public ArrayList<Magnet> getPoem() {
        return magnets;
    }


    // todo ondrag doesn't work the same as the later event stuff
    /* Drag events occur when the magnet tile is dragged from the drawer fragment onto the canvas.
    *  Implements View.OnDragListener */
    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        int currentTile = magnets.size() - 1 ;
        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: // The magnet has been clicked (a long-click event in the drawerFragment)
                notAddedTile = true;
                return true;
            case DragEvent.ACTION_DRAG_ENTERED: // The magnet has entered the drawing area
                currentTile = addTileToCanvas();
                clickedMagnet = magnets.get(currentTile);
                break;
            case DragEvent.ACTION_DRAG_LOCATION: // Called every time the magnet is moved in the drawing area
                if(currentTile != -1) {
                    handleMovingClickedTile(magnets.get(currentTile), dragEvent.getX(), dragEvent.getY());
                } else {
                    handleMovingClickedTile(magnets.get(0), dragEvent.getX(), dragEvent.getY());
                }
                break;
            case DragEvent.ACTION_DROP: // The magnet is dropped when the finger moves up, off the screen
                if(notAddedTile) {
                    currentTile = addTileToCanvas();
                    magnets.get(currentTile).setX(dragEvent.getX());
                    magnets.get(currentTile).setY(dragEvent.getY());
                }
                adjustTheClickedTile();
                break;
        }
        this.invalidate();
        return false;
    }

    /* addTileToCanvas is used only by the onDrag event listener to add a new magnet tile */
    private int addTileToCanvas() {
        magnets.add(new Magnet(word, magnets.size(),scaleFactor,packID));
        notAddedTile = false;
        canvasListener.magnetTilesChanged(magnets.size());
        return magnets.size()-1;
    }

    /* adjustTheClickedTile is used by the onTouch and onDrag event listeners to adjust a magnet tile after the drop/up action. It also clears the clickedMagnetTile and sidesToLockToNext variables*/
    private void adjustTheClickedTile() {
        if(!sidesToLockToNext.isEmpty()) {
            for(MagnetSide magnetSide : sidesToLockToNext) {
                clickedMagnet.setXAndY(clickedMagnet.x() + magnetSide.xAndyDistances.x, clickedMagnet.y() + magnetSide.xAndyDistances.y);
                if(soundEffects) mediaPlayer.start();
            }
        }
        clickedMagnet = null;
        sidesToLockToNext.clear();
    }


    /* Overrides the View method of the same name. onTouchEvent is the center of user interaction for the drawing area. The action, Action_Down occurs when the user touches the drawing area. Action_Move happens when the user moves their finger across the drawing area. Action_Up occurs when the user moves their finger off of the screen. */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean retVal = scaleGestureDetector.onTouchEvent(motionEvent);
        retVal = gestureDetector.onTouchEvent(motionEvent) || retVal;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // has the user clicked a magnet, if so, set clickedMagnetTile to this magnet
                checkForTouchCollisions(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                // does the user have a magnet? If so, deal with magnet-magnet collisions, etc.
                if(clickedMagnet != null) {
                    sidesToLockToNext.clear();
                    handleMovingClickedTile(clickedMagnet,motionEvent.getX(),motionEvent.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                ifThereIsAMagnetOverTheTrashCanDeleteIt();
                adjustTheClickedTile();   // get the tile in the right position (no overlap)
                toHighlightMagnets.clear();
                break;
        }
        this.invalidate();
        return retVal || super.onTouchEvent(motionEvent);
    }

    /* This is a convenience function only used by onTouchEvent. When the user removes their finger from the screen, if a magnet is currently being held over the trash can, delete it. */
    private void ifThereIsAMagnetOverTheTrashCanDeleteIt() {
        if(magnetIsAboveTrashCan) { // the clicked magnet tile is being held over the trash can
            magnetIsAboveTrashCan = false;
            magnets.remove(clickedMagnet);
            canvasListener.magnetDeleted();
            canvasListener.magnetTilesChanged(magnets.size());
        }
    }

    /* onTouchEvent calls this method when the user presses down on the drawable area to check if the user has touched a magnet tile or the award image. If so, clickedMagnetTile is set or the award dialog is set. */
    private void checkForTouchCollisions(MotionEvent motionEvent) {
        for(Magnet magnet : magnets) {
            if(theUserHasTouchedAMagnet(magnet, motionEvent.getX(), motionEvent.getY())) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                clickedMagnet = magnet;
            }
        }
        if(clickedMagnet == null && theUserHasTouchedTheAward(motionEvent.getX(),motionEvent.getY())) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            loadAwardDialog();
        }
    }

    /* This function checks if a user has touched the award image. It is called by checkForTouchCollisions */
    private boolean theUserHasTouchedTheAward(float x, float y) {
        return isTouchInBox(x,y,awardAlert.padding, awardAlert.awardAlertHeight + awardAlert.padding,
                getWidth() - awardAlert.awardAlertWidth - awardAlert.padding,
                getWidth() - awardAlert.padding,0);
    }

    /* This function checks if a user has touched a particular magnet tile. It is called by checkForTouchCollisions and collidesWith */
    private boolean theUserHasTouchedAMagnet(Magnet magnet, float touchedX, float touchedY) {
        return isTouchInBox(touchedX,touchedY, magnet.leftTopCornerY(), magnet.leftBottomCornerY(),
                magnet.leftBottomCornerX(), magnet.rightBottomCornerX(),0);
    }

    /* isTouchInBox runs the touch collision detection for when the user touches a magnet or the award image. The box can be expanded by adding boxPadding > 0*/
    private boolean isTouchInBox(float xTouch, float yTouch, float boxSmallestY, float boxBiggestY, float boxSmallestX, float boxBiggestX, float boxPadding) {
        return (xTouch > (boxSmallestX - boxPadding)) && (xTouch < (boxBiggestX + boxPadding)) && (yTouch > (boxSmallestY - boxPadding)) && (yTouch < (boxBiggestY + boxPadding));
    }


    /* This function handles everything when the user has touched a tile and is moving it around (this happens during an action_move event).
    *  First, we check if the magnet is located in the trash can collision zone and handle all of the stuff that comes with that.
    *  Then, we have to look at any magnets that are close to our clicked tile.
    *  If we have close tiles (the closeMagnets ArrayList is not empty), then we see if any of those
    *  tiles are colliding with our moving magnet tile. If there aren't any collisions (the tilesThatCollideWithClickedTile ArrayList is empty),
    *  then we find the sides the magnet will lock to when released, save them to sidesToLockToNext and highlight them.
    *  If there are collisions, play the collision sound and adjust the tiles so that they don't overlap.
    *  Otherwise, if the moving tile is not near any other tiles, just move the darn thing.
    * */
    private void handleMovingClickedTile(Magnet clickedTile, float xTouchPosition, float yTouchPosition) {
        checkIfTheMagnetIsAboveTheTrashCan(clickedTile);
        ArrayList<Magnet> closeMagnets = getCloseMagnets(clickedTile,xTouchPosition, yTouchPosition,50);
        if (!closeMagnets.isEmpty()) { // if there are close magnets
            ArrayList<Magnet> tilesThatCollideWithClickedTile = getCollidingMagnets(clickedTile,closeMagnets,xTouchPosition,yTouchPosition);
            if(tilesThatCollideWithClickedTile.isEmpty()) { // no collisions
                setUpHighlightingForCloseButNotTouchingTiles(clickedTile,xTouchPosition,yTouchPosition,closeMagnets);
            } else { // if it has already collided
                adjustCollidingMagnetTiles(clickedTile,xTouchPosition,yTouchPosition,tilesThatCollideWithClickedTile);
            }
        } else {
            // magnet is not near any other magnets, so just move it
            clickedTile.setXAndY(xTouchPosition,yTouchPosition);
        }
    }

    /* A convenience method called by handleMovingTile that gets all of the locking sides of the close magnet tiles, adds them to sidesToLockToNext and highlights them. When all is said and done, it moves the clicked tile. */
    private void setUpHighlightingForCloseButNotTouchingTiles(Magnet clickedTile, float xTouchPosition, float yTouchPosition, ArrayList<Magnet> closeMagnets) {
        ArrayList<MagnetSide> lockingSides = getLockingSides(clickedTile,closeMagnets);
        if(lockingSides != null) {
            for(MagnetSide side : lockingSides) {
                toHighlightMagnets.add(side.referenceToMagnet);
                sidesToLockToNext.add(side);
            }
        }
        clickedTile.setXAndY(xTouchPosition,yTouchPosition);
    }

    // todo, look at this one.
    /* A convenience method called by handleMovingTile when there has been a collision between the moving tile and other tiles. This function plays the soundEffect (if the setting is on) and makes the clickedTile back up so it is not overlapping any other tile. */
    private void adjustCollidingMagnetTiles(Magnet clickedTile, float xTouchPosition, float yTouchPosition, ArrayList<Magnet> collidesWithTiles) {
        if(soundEffects) mediaPlayer.start();
        ArrayList<MagnetSide> lockingSides = getLockingSides(clickedTile,collidesWithTiles);
        if(lockingSides != null) {
            makeMagnetBackUp(clickedTile,lockingSides.get(0).referenceToMagnet); // should be closest if > 1 sides bc of sort in get locking sides
        }
    }

    /* A convenience method called by handleMovingClickedTile to set the magnetIsAboveTrashCan class variable.*/
    private void checkIfTheMagnetIsAboveTheTrashCan(Magnet clickedTile) {
        magnetIsAboveTrashCan = trashCan.collidesWithTrashCan(clickedTile,getWidth(),getHeight());
    }

    /* Returns all of the magnets out of the set of close magnets that physically collide with the moving magnet tile. */
    private ArrayList<Magnet> getCollidingMagnets(Magnet clickedTile,ArrayList<Magnet> closeTiles, float xTouchPosition, float yTouchPosition) {
        ArrayList<Magnet> collidesWithTiles = new ArrayList<Magnet>();
        for(Magnet closeTile : closeTiles) {
            if(theseTwoTilesCollide(clickedTile,closeTile,xTouchPosition,yTouchPosition)) collidesWithTiles.add(closeTile);
        }
        return collidesWithTiles;
    }

    //todo this isn't right
    /* Returns true if the user's finger has touched the close tile or the close tile is in the collision zone of the moving clicked tile. Called by getCollidingMagnets */
    private boolean theseTwoTilesCollide(Magnet clickedTile, Magnet closeTile, float xTouchPosition, float yTouchPosition) {
        // add case for point outside of collision zone that must have gone through the collision zone (rectangle, line segment intersection?)
        return theUserHasTouchedAMagnet(closeTile,xTouchPosition,yTouchPosition) || isTouchInBox(xTouchPosition,yTouchPosition,closeTile.leftTopCornerY(),closeTile.leftBottomCornerY(),closeTile.leftBottomCornerX(),closeTile.rightBottomCornerX(),0);
    }

    /* Another convenience function used by handleMovingClickedTile. This function calls isTouchInBox on the clicked magnet tile and all of the other magnet tiles in  the drawing area to find the number of magnet tiles within a set area (within the padding zone). */
    private ArrayList<Magnet> getCloseMagnets(Magnet clickedTile, float xTouchPosition, float yTouchPosition, int padding) {
        ArrayList<Magnet> closeMagnets = new ArrayList<Magnet>();
        for(Magnet magnet : magnets) {
            if(clickedTile.id() != magnet.id()) {
                if(isTouchInBox(xTouchPosition,yTouchPosition, magnet.leftTopCornerY(), magnet.leftBottomCornerY(), magnet.leftBottomCornerX(), magnet.rightBottomCornerX(),padding))
                    closeMagnets.add(magnet);
            }
        }
        return closeMagnets;
    }

    /* getSides is a convenience function for getLockingSides that returns the closest side of each magnet tile in closeMagnets. (The closest side is the side closest to tile the user is moving with their finger. */
    private ArrayList<MagnetSide> getSides(Magnet clickedTile,ArrayList<Magnet> closeMagnets) {
        ArrayList<MagnetSide> possibleMagnetSides = new ArrayList<MagnetSide>();
        for(Magnet closeTile : closeMagnets) {
            MagnetSide magnetSide = MagnetSide.closestSide(clickedTile,closeTile);
            if(magnetSide != null) possibleMagnetSides.add(magnetSide);
        }
        return possibleMagnetSides;
    }

    /* ToDo think about this one a bit */
    /* the special case is when the one or both of the possible sides require an x and y adjustment*/
    private void reworkIfSpecialCase(MagnetSide side1, MagnetSide side2) {
        if(side1.xAndyDistances.x != 0 && side1.xAndyDistances.y != 0) {
            if(side2.xAndyDistances.x == 0) {
                side1.xAndyDistances.set(side1.xAndyDistances.x,0);
            } else if(side2.xAndyDistances.y == 0) {
                side1.xAndyDistances.set(0,side1.xAndyDistances.y);
            } else {
                side1.xAndyDistances.set(side1.xAndyDistances.x,0);
                side2.xAndyDistances.set(0,side2.xAndyDistances.y);
            }
        }
        if(side2.xAndyDistances.x != 0 && side2.xAndyDistances.y != 0) {
            if(side1.xAndyDistances.x == 0) {
                side2.xAndyDistances.set(side2.xAndyDistances.x,0);
            } else if(side1.xAndyDistances.y == 0) {
                side2.xAndyDistances.set(0,side2.xAndyDistances.y);
            } else {
                side1.xAndyDistances.set(side1.xAndyDistances.x,0);
                side2.xAndyDistances.set(0,side2.xAndyDistances.y);
            }
        }
    }

    /* getLockingSides returns the one or two MagnetSide objects that will be used to make the moving magnet 'lock' to one or two close magnets when the user releases their finger from the screen. */
    private ArrayList<MagnetSide> getLockingSides(Magnet clickedTile, ArrayList<Magnet> closeMagnets) {
        // get all the possible sides from all of the possible magnets that are in the collision zone
        ArrayList<MagnetSide> sides = getSides(clickedTile,closeMagnets);
        if(sides.isEmpty()) {
            return null;
        }
        if(sides.size() == 1) {
            ArrayList<MagnetSide> lockingSides = new ArrayList<MagnetSide>(1);
            lockingSides.add(sides.get(0));
            return lockingSides;
        }
        // order those sides from smallest to largest, defined in MagnetSide.java
        Collections.sort(sides);
        MagnetSide side1 = sides.get(0);
        MagnetSide side2 = sides.get(1);
        if(side1.referenceToMagnet.id() == side2.referenceToMagnet.id()) { // if they are the same, just add the one
            ArrayList<MagnetSide> lockingSides = new ArrayList<MagnetSide>(1);
            lockingSides.add(side1);
            return lockingSides;
        } else {
            if(MagnetSide.areParallel(side1.xAndyDistances,side2.xAndyDistances)) { // the two possible sides to lock to are parallel, so just take the closest one.
                ArrayList<MagnetSide> lockingSides = new ArrayList<MagnetSide>(1);
                lockingSides.add(side1);
                return lockingSides;
            } else { // we have a perpendicular situation, the only time we'll return two MagnetSide objects
                ArrayList<MagnetSide> lockingSides = new ArrayList<MagnetSide>(2);
                reworkIfSpecialCase(side1, side2);
                lockingSides.add(side1);
                lockingSides.add(side2);
                return lockingSides;
            }
        }
    }

    /* toDo rewrite! */
    private void makeMagnetBackUp(Magnet clickedTile, Magnet collidedMagnet) {
        float halfWidth = clickedTile.width()/2;
        float halfHeight = clickedTile.height()/2;
        float leftSide = clickedTile.x() - halfWidth;
        float rightSide = clickedTile.x() + halfWidth;
        float topSide = clickedTile.y() - halfHeight;
        float bottomSide = clickedTile.y() + halfHeight;

        float rightBoundary = collidedMagnet.x() + collidedMagnet.width()/2;
        float bottomBoundary = collidedMagnet.y() + collidedMagnet.height()/2;
        float leftBoundary = collidedMagnet.x() - collidedMagnet.width()/2;
        float topBoundary = collidedMagnet.y() - collidedMagnet.height()/2;

        side theSide = returnSide(clickedTile,collidedMagnet);
        if(theSide == side.TOP) {
            clickedTile.setY(clickedTile.y() + (bottomBoundary - (clickedTile.y() - halfHeight)));
        }
        if(theSide == side.LEFT) {
            clickedTile.setX(clickedTile.x()- (rightSide-leftBoundary));
        }
        if(theSide == side.BOTTOM) {
            clickedTile.setY(clickedTile.y() - ((clickedTile.y() + halfHeight)-topBoundary));
        }
        if(theSide == side.RIGHT) {
            clickedTile.setX(clickedTile.x()+ (rightBoundary - leftSide));
        }
        //MagnetSide magnetSide = MagnetSide.closestSide(clickedTile,collidedMagnet);
        //clickedMagnetTile.setXAndY(clickedMagnetTile.x() + magnetSide.xAndyDistances.x, clickedMagnetTile.y() + magnetSide.xAndyDistances.y);
    }

    private side returnSide(Magnet movingTile, Magnet stationaryTile) {
        float xDifference = movingTile.x() - stationaryTile.x();
        float yDifference = movingTile.y() - stationaryTile.y();
        //left or right
        if (Math.abs(xDifference) >= Math.abs(yDifference)) {
            if (xDifference >= 0) return side.RIGHT;
            else return side.LEFT;
        } else {
            if (yDifference >= 0) return side.TOP;
            else return side.BOTTOM;
        }
    }
    
    public DrawingPanel(Context context, CanvasListener theCanvasListener) {
        super(context);
        getHolder().addCallback(this);
        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        gestureDetector = new GestureDetector(context, gestureListener);
        canvasListener = theCanvasListener;
        this.setHapticFeedbackEnabled(true);


    }

    private void resetIfPastCanvasBoundary(Magnet magnet, float rightBoundary, float bottomBoundary) {
        float halfWidth = magnet.width()/2;
        float halfHeight = magnet.height()/2;
        if((magnet.x() + halfWidth) > rightBoundary) magnet.setX(rightBoundary - halfWidth);
        if(magnet.x() - halfWidth < 0) {
            magnet.setX(halfWidth);
        }
        if (magnet.y() + halfHeight > bottomBoundary) magnet.setY(bottomBoundary - halfHeight);
        if (magnet.y() - halfHeight < 0) magnet.setY(halfHeight);
    }




    private void loadAwardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if(awardAlert.awardName == null) {
            builder.setTitle("You haven't received any awards this session")
                    .setMessage("Check out the awards page to check out which awards you've received and all of the awards you can get!")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            canvasListener.awardClicked();
                        }
                    });
        } else {
            builder.setTitle("Your newest award: " + awardAlert.awardName)
                    .setMessage("About this award: " + awardAlert.awardDescription)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            canvasListener.awardClicked();
                        }
                    });
        }
        builder.create();
        builder.show();
    }

    public void continuouslyAnimateTrashCan(boolean animateTrashCan) {
        continuouslyAnimateTrashCan = animateTrashCan;
        invalidate();
    }

    public void continuouslyAnimateAward(boolean continuouslyAnimateAward) {
        this.continuouslyAnimateAward = continuouslyAnimateAward;
        if(continuouslyAnimateAward) {
            awardAlert.awardName = "Demo Award";
            awardAlert.awardDescription = "You have received your first award for running the demo.";
        } else {
            awardAlert.setAlert(false);
        }
        invalidate();

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        boolean animateAward;
        if(continuouslyAnimateTrashCan) {
            trashCan.drawTrashCan(canvas,true,getWidth(),getHeight());
        } else {
            trashCan.drawTrashCan(canvas,magnetIsAboveTrashCan,getWidth(),getHeight());
        }

        if(continuouslyAnimateAward) {
            awardAlert.setAlert(true);
            awardAlert.drawAwardAlert(canvas,getWidth());
            animateAward = true;
        } else {
            animateAward = awardAlert.drawAwardAlert(canvas,getWidth());
        }

        for(Magnet toHighlightMagnet : toHighlightMagnets) {
            toHighlightMagnet.setHighlight(true);
        }
        for(Magnet magnet : magnets) {
            resetIfPastCanvasBoundary(magnet,getWidth(),getHeight());
            magnet.draw(canvas, getWidth(),  getHeight());
            magnet.setHighlight(false);
        }
        toHighlightMagnets.clear();
        // if the trash can should be shaking, keep drawing
        if(magnetIsAboveTrashCan || animateAward|| continuouslyAnimateTrashCan) {
            invalidate();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()
        setOnDragListener(this);
        trashCan = new TrashCan(getContext());
        awardAlert = new AwardAlert(getContext());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    /**
     * The scale listener, used for handling multi-finger scale gestures.
     * todo: handle scaling better
     */
    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        /**
         * This is the active focal point in terms of the viewport. Could be a local
         * variable but kept here to minimize per-frame allocations.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            scaleFactor *= scaleGestureDetector.getScaleFactor();
            // Don't let the object get too small or too large.
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            for(Magnet magnet : magnets) {
                magnet.updateScaleFactor(scaleFactor, magnets);
            }
            invalidate();
            return true;
        }
    };



    /**
     * The gesture listener is used for handling simple gestures such as double touches, scrolls,
     * and flings. I'm not using any of this right now, but I'm leaving it here just in case.
     */
    private final GestureDetector.SimpleOnGestureListener gestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    };
}



enum side {
    LEFT,RIGHT,TOP,BOTTOM
}

//class MagnetSide
//}

