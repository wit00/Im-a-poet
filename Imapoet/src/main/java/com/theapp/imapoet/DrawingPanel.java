package com.theapp.imapoet;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.support.v4.view.ViewCompat;
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
import java.util.Iterator;



//toDo, can't delete with scaled, boundary with scale
/**
 * Drawing canvas
 * Created by whitney on 6/5/14.
 */
public class DrawingPanel extends SurfaceView implements SurfaceHolder.Callback, View.OnDragListener, GameState.DrawingPanelListener, AwardHandler.AwardManagerListener {
    private CanvasListener canvasListener;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private String word = "";
    private ArrayList<Magnet> magnets = new ArrayList<Magnet>();
    private boolean notAddedTile = false;
    private float scaleFactor = 1.0f;
    public Magnet clickedMagnet = null;
    private boolean soundEffects = false;
    private TrashCan trashCan;
    private AwardAlert awardAlert;
    private boolean magnetIsAboveTrashCan = false; // a boolean that tells if a magnet is to be deleted
    private ArrayList<Magnet> toHighlightMagnets = new ArrayList<Magnet>();
    private MediaPlayer mediaPlayerForSoundEffect = MediaPlayer.create(getContext(),R.raw.finger_snapping);
    private ArrayList<Integer> packsUsedIds = new ArrayList<Integer>(5);
    private int packID;
    private ArrayList<MagnetSide> sidesToLockToNext = new ArrayList<MagnetSide>(2); // will never be more than 2
    private boolean continuouslyAnimateTrashCan = false;
    private boolean continuouslyAnimateAward = false;
    private boolean previouslySavedPoem = false;
    private String previouslySavedPoemID = null;
    private String previouslySavedPoemName = null;
    private ArrayList<ArrayList<Magnet>> connectedMagnets = new ArrayList<ArrayList<Magnet>>(1);


    public float scaleFactor() { return scaleFactor; }
    public float scalePivotX() { return scalePivotX; }
    public float scalePivotY() { return scalePivotY; }


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
        String top = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_TOP));
        String bottom = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_BOTTOM));
        String left = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_LEFT));
        String right = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_RIGHT));
        magnets.add(new Magnet(cursor.getString(cursor.getColumnIndex(textColumn)), magnets.size(),scaleFactor,thisPackID,top,bottom,left,right,getDensity()));
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

    // I think everything but the invalidate call is not needed because the references should not be deleted during garbage collection, but just in case I'm wrong...
    public void loadMagnets(ArrayList<Magnet> newMagnets, boolean saved, String id, String title,float scaleFactor, float scalePivotX, float scalePivotY) {
        this.magnets.clear();
        this.magnets = newMagnets;
        this.scaleFactor = scaleFactor;
        this.scalePivotX = scalePivotX;
        this.scalePivotY = scalePivotY;
        setPreviouslySavedPoem(saved,id,title);
        invalidate();
    }


    /* Implements GameState.drawingPanelListener. The listener calls this function during onResume when the magnets need to be loaded back onto the canvas. This call covers magnets restored from the auto-save. */
    public void loadMagnets(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn) {
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
        for(Magnet magnet : magnets) {
            magnet.setUpConnectedSides(magnets);
        }
        invalidate(); // redraw the canvas
    }

    /* Implements GameState.drawingPanelListener. The listener calls this function during onResume when the magnets need to be loaded back onto the canvas. This call covers magnets restored from the manual-save. */
    public String loadSavedMagnets(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn) {
        int id = -1;
        magnets.clear();
        while(cursor.moveToNext()) {
            id = getMagnetsAndPacksFromCursor(cursor,packIDColumn,textColumn,xColumn,yColumn,idColumn);
        }
        for(Magnet magnet : magnets) {
            magnet.setUpConnectedSides(magnets);
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


    /* Implements the GameState.drawingPanel Listener. It sets the sound effects and game music settings. */
    public void setSettings(boolean soundEffects) {
        this.soundEffects = soundEffects;
    }

    /* Implements the AwardManager.AwardManager Listener. Sets a new award to be displayed and makes a call to redraw the canvas. */
    public void loadAward(String name, String description, int id) {
        awardAlert.setAlert(true);
        awardAlert.addNewAward(name,description);
        invalidate();
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


    private float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    /* addTileToCanvas is used only by the onDrag event listener to add a new magnet tile */
    private int addTileToCanvas() {
        magnets.add(new Magnet(word, magnets.size(),1.0f,packID,null,null,null,null,getDensity()));
        notAddedTile = false;
        canvasListener.magnetTilesChanged(magnets.size());
        return magnets.size()-1;
    }


    /* adjustTheClickedTile is used by the onTouch and onDrag event listeners to adjust a magnet tile after the drop/up action. It also clears the clickedMagnetTile and sidesToLockToNext variables*/
    private void adjustTheClickedTile() {
        if(!sidesToLockToNext.isEmpty()) {
            for(MagnetSide magnetSide : sidesToLockToNext) {
                clickedMagnet.setXAndY(clickedMagnet.x() + magnetSide.xAndyDistances.x, clickedMagnet.y() + magnetSide.xAndyDistances.y);
                if(soundEffects) mediaPlayerForSoundEffect.start();
                setSides(magnetSide);
            }

        }
        clickedMagnet = null;
        sidesToLockToNext.clear();
    }

    // used in MotionEvent.ACTION_MOVE, clear you are moving the magnet, clear it of any connections it has to other magnets and clear those magnets of connections to it
    private void clearConnections() {
        for(Magnet magnet : clickedMagnet.topSideConnectedMagnet()) {
            magnet.bottomSideConnectedMagnet().remove(clickedMagnet);
        }
        for(Magnet magnet : clickedMagnet.bottomSideConnectedMagnet()) {
            magnet.topSideConnectedMagnet().remove(clickedMagnet);
        }
        for(Magnet magnet : clickedMagnet.leftSideConnectedMagnet()) {
            magnet.rightSideConnectedMagnet().remove(clickedMagnet);
        }
        for(Magnet magnet : clickedMagnet.rightSideConnectedMagnet()) {
            magnet.leftSideConnectedMagnet().remove(clickedMagnet);
        }
        clickedMagnet.clearAllConnectedMagnets();
        for (Iterator<ArrayList<Magnet>> iterator = connectedMagnets.iterator(); iterator.hasNext();) {
            ArrayList<Magnet> magnetBlock = iterator.next();
            if (magnetBlock.contains(clickedMagnet)) {
                // Remove the current element from the iterator and the list.
                iterator.remove();
            }
        }
        for(ArrayList<Magnet> arrayList : connectedMagnets) {
            System.out.println("now the connected tiles are: ");
            for(Magnet magnet : arrayList) {
                System.out.println("connected tiles : " + magnet.word());
            }
        }

    }


    float startX = 0;
    float startY = 0;
    float previousXOffset = 0;
    float previousYOffset = 0;
    float lastWidth = getWidth();
    float lastHeight = getHeight();

    float lastScaleFactor = 1.0f;
    private float scalePivotX= 0;
    private float scalePivotY = 0;

    private float getScaledCoordinate(float coordinate) {
        return (-1*scalePivotX/scaleFactor) + (coordinate / scaleFactor) + (scalePivotX) ;
    }

    /* Overrides the View method of the same name. onTouchEvent is the center of user interaction for the drawing area. The action, Action_Down occurs when the user touches the drawing area. Action_Move happens when the user moves their finger across the drawing area. Action_Up occurs when the user moves their finger off of the screen. */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean retVal = scaleGestureDetector.onTouchEvent(motionEvent);
        retVal = gestureDetector.onTouchEvent(motionEvent) || retVal;
        float oldXTouch, oldYTouch, xTouch,yTouch;
        oldXTouch = motionEvent.getX();
        oldYTouch = motionEvent.getY();
        xTouch = (-1*scalePivotX/scaleFactor) + (oldXTouch / scaleFactor) + (scalePivotX) ;
        yTouch = (-1*scalePivotY/scaleFactor) + (oldYTouch / scaleFactor) + (scalePivotY) ;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // has the user clicked a magnet, if so, set clickedMagnetTile to this magnet
                checkForTouchCollisions(xTouch,yTouch,oldXTouch,oldYTouch);
                if(clickedMagnet == null) {
                    startX = motionEvent.getX() - previousXOffset;
                    startY = motionEvent.getY() - previousYOffset;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // does the user have a magnet? If so, deal with magnet-magnet collisions, etc.
                if(clickedMagnet != null) {
                    clearConnections();
                    sidesToLockToNext.clear();
                    handleMovingClickedTile(clickedMagnet,xTouch,yTouch);
                } else {
                    xOffset = motionEvent.getX() - startX;
                    yOffset = motionEvent.getY() - startY;
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
            packsUsedIds.remove((Integer) clickedMagnet.packID());
            canvasListener.magnetDeleted();
            canvasListener.magnetTilesChanged(magnets.size());
        }
    }

    /* onTouchEvent calls this method when the user presses down on the drawable area to check if the user has touched a magnet tile or the award image. If so, clickedMagnetTile is set or the award dialog is set. */
    private void checkForTouchCollisions(float transformedXTouch, float transformedYTouch, float oldXTouch, float oldYTouch) {
        for(Magnet magnet : magnets) {
            if(theUserHasTouchedAMagnet(magnet, transformedXTouch, transformedYTouch)) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                clickedMagnet = magnet;
            }
        }
        if(clickedMagnet == null && theUserHasTouchedTheAward(oldXTouch,oldYTouch)) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            loadAwardDialog();
        }
    }

    /* This function checks if a user has touched the award image. It is called by checkForTouchCollisions */
    private boolean theUserHasTouchedTheAward(float x, float y) {
        float touchPadding = 5;
        return isTouchInBox(x,y,awardAlert.padding, awardAlert.awardAlertHeight + awardAlert.padding,
                getWidth() - awardAlert.awardAlertWidth - awardAlert.padding,
                getWidth() - awardAlert.padding,touchPadding);
    }

    /* This function checks if a user has touched a particular magnet tile. It is called by checkForTouchCollisions and collidesWith */
    private boolean theUserHasTouchedAMagnet(Magnet magnet, float touchedX, float touchedY) {
        float smallestY = magnet.leftTopCornerY() ;//* scaleFactor ;
        //float smallestY = (-1*scalePivotY*scaleFactor) + (magnet.leftTopCornerY() * scaleFactor) + (scalePivotY) ;//* scaleFactor ;
        //float biggestY = (-1*scalePivotY*scaleFactor) + (magnet.leftBottomCornerY() * scaleFactor) + scalePivotY ;//* scaleFactor ;
        float biggestY = magnet.leftBottomCornerY() ;//* scaleFactor ;
        //float smallestX = (-1*scaleCenterX*scaleFactor) + (magnet.leftBottomCornerX()*scaleFactor) + scaleCenterX ;//* scaleFactor ;
        float smallestX = magnet.leftBottomCornerX() ;//* scaleFactor ;
        float biggestX = magnet.rightBottomCornerX() ;//* scaleFactor ;
        //float biggestX = (-1*scaleCenterX*scaleFactor) + (magnet.rightBottomCornerX()*scaleFactor) + scaleCenterX ;//* scaleFactor ;
        return isTouchInBox(touchedX,touchedY, smallestY, biggestY,
                smallestX, biggestX,0);
    }

    /* isTouchInBox runs the touch collision detection for when the user touches a magnet or the award image. The box can be expanded by adding boxPadding > 0*/
    private boolean isTouchInBox(float xTouch, float yTouch, float boxSmallestY, float boxBiggestY, float boxSmallestX, float boxBiggestX, float boxPadding) {
        return (xTouch > (boxSmallestX - boxPadding)) && (xTouch < (boxBiggestX + boxPadding)) && (yTouch > (boxSmallestY - boxPadding)) && (yTouch < (boxBiggestY + boxPadding));
    }

    private boolean magnetsAreAttached(Magnet magnet1, Magnet magnet2) {
        return magnet1.topSideConnectedMagnet().contains(magnet2) || magnet1.bottomSideConnectedMagnet().contains(magnet2) || magnet1.leftSideConnectedMagnet().contains(magnet2) || magnet1.rightSideConnectedMagnet().contains(magnet2);
    }


    private void handleCollision(MagnetSide closestMagnet) {
        if(soundEffects) mediaPlayerForSoundEffect.start();
        clickedMagnet.setXAndY(clickedMagnet.x() + closestMagnet.xAndyDistances.x, clickedMagnet.y() + closestMagnet.xAndyDistances.y);
        setSides(closestMagnet);
    }

    private void highlightIfCloseEnough(MagnetSide closestMagnet, Magnet movingMagnet, float xTouchPosition, float yTouchPosition, int closePadding) {
        if (!(getCloseMagnets(movingMagnet,xTouchPosition, yTouchPosition,closePadding)).isEmpty()) { // if there are close magnets
            toHighlightMagnets.clear();
            toHighlightMagnets.add(closestMagnet.referenceToMagnet);
            sidesToLockToNext.add(closestMagnet);
        }
    }

    private MagnetSide checkForCollisionsInQuadrants(Magnet movingMagnet, Magnet magnet, side connectedSide, MagnetSide cc, float xTouchPosition, float yTouchPosition, float shiftX
    , float shiftY) {
        MagnetSide closestMagnet = null;

        return closestMagnet;
    }

    private MagnetSide getSideForQuadrant1(side connectedSide, PointF movingMagnetTopRightNewPoint,PointF movingMagnetTopLeftNewPoint, Magnet movingMagnet, Magnet magnet) {
        MagnetSide closestMagnet = null;
        if(connectedSide == side.BOTTOM) {
            if(movingMagnetTopRightNewPoint.y <= magnet.leftBottomCornerY() && movingMagnet.leftBottomCorner().x <= magnet.rightBottomCorner().x) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftBottomCorner().y-movingMagnet.leftTopCorner().y);
                handleCollision(closestMagnet);
            }
        } else if(connectedSide == side.RIGHT) {
            if(movingMagnetTopLeftNewPoint.x <= magnet.rightBottomCornerX() && movingMagnet.leftTopCorner().y <= magnet.rightBottomCorner().y) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet, magnet.rightTopCorner().x-movingMagnet.leftTopCorner().x,0);
                handleCollision(closestMagnet);
            }
        }
        return closestMagnet;
    }

    private MagnetSide getSideForQuadrant3(side connectedSide, PointF movingMagnetBottomLeftNewPoint, PointF movingMagnetTopLeftNewPoint, Magnet movingMagnet, Magnet magnet) {
        MagnetSide closestMagnet = null;
        if(connectedSide == side.TOP) {
            if(movingMagnetBottomLeftNewPoint.y >= magnet.leftTopCornerY() && movingMagnet.leftBottomCorner().x <= magnet.rightBottomCorner().x) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftTopCorner().y-movingMagnet.leftBottomCorner().y);
                handleCollision(closestMagnet);
            }
        } else if(connectedSide == side.RIGHT) {
            if(movingMagnetTopLeftNewPoint.x <= magnet.rightBottomCorner().x && movingMagnet.leftBottomCorner().y >= magnet.leftTopCorner().y) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet, magnet.rightTopCorner().x-movingMagnet.leftTopCorner().x,0);
                handleCollision(closestMagnet);
            }
        }
        return closestMagnet;
    }

    private MagnetSide getSideForQuadrant2(side connectedSide, PointF movingMagnetTopRightNewPoint, Magnet movingMagnet, Magnet magnet) {
        MagnetSide closestMagnet = null;
        if(connectedSide == side.BOTTOM) {
            if(movingMagnetTopRightNewPoint.y <= magnet.leftBottomCornerY() && movingMagnet.rightBottomCorner().x >= magnet.leftBottomCorner().x) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftBottomCorner().y-movingMagnet.leftTopCorner().y);
                handleCollision(closestMagnet);
            }
        } else if(connectedSide == side.LEFT) {
            if(movingMagnetTopRightNewPoint.x >= magnet.leftBottomCornerX() && movingMagnet.rightTopCorner().y <= magnet.leftBottomCorner().y) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,magnet.leftTopCorner().x-movingMagnet.rightTopCorner().x,0);
                handleCollision(closestMagnet);
            }
        }
        return closestMagnet;
    }



    private MagnetSide getSideForQuadrant4(side connectedSide, PointF movingMagnetBottomLeftNewPoint,PointF movingMagnetBottomRightNewPoint, Magnet movingMagnet, Magnet magnet) {
        MagnetSide closestMagnet = null;

        if(connectedSide == side.TOP) {
            if(movingMagnetBottomLeftNewPoint.y >= magnet.leftTopCornerY() && movingMagnet.rightBottomCorner().x >= magnet.leftBottomCorner().x) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftTopCorner().y-movingMagnet.leftBottomCorner().y);
                handleCollision(closestMagnet);
            }
        } else if(connectedSide == side.LEFT) {
            if(movingMagnetBottomRightNewPoint.x >= magnet.leftTopCornerX() && movingMagnet.rightBottomCorner().y >= magnet.rightTopCorner().y) {
                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,magnet.leftTopCorner().x-movingMagnet.rightTopCorner().x,0);
                handleCollision(closestMagnet);
            }
        }
        return closestMagnet;
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
    private void handleMovingClickedTile(Magnet movingMagnet, float xTouchPosition, float yTouchPosition) {
        checkIfTheMagnetIsAboveTheTrashCan(movingMagnet);
        MagnetSide closestMagnet = null;
        boolean atLeastOneCollision = false;
        int closePadding = (int)(45 * scaleFactor);
        float shiftX = xTouchPosition - movingMagnet.x();
        float shiftY = yTouchPosition - movingMagnet.y();
        for(Magnet magnet : magnets) {
            side connectedSide = null;

            if(magnet.id() != movingMagnet.id()) {
                int currentQuadrant = getQuadrant(magnet);
                PointF movingMagnetTopLeftNewPoint = new PointF(movingMagnet.leftTopCornerX() + shiftX, movingMagnet.leftTopCornerY() + shiftY);
                PointF movingMagnetBottomLeftNewPoint = new PointF(movingMagnet.leftBottomCornerX() + shiftX,movingMagnet.leftBottomCornerY() + shiftY);
                PointF movingMagnetTopRightNewPoint = new PointF(movingMagnet.rightTopCornerX() + shiftX,movingMagnet.rightTopCornerY() + shiftY);
                PointF movingMagnetBottomRightNewPoint = new PointF(movingMagnet.rightBottomCornerX() + shiftX,movingMagnet.rightBottomCornerY() + shiftY);

                PointF staticMagnetTopLeftNewPoint = new PointF(magnet.leftTopCornerX() + shiftX, magnet.leftTopCornerY() + shiftY);
                PointF staticMagnetBottomLeftNewPoint = new PointF(magnet.leftBottomCornerX() + shiftX,magnet.leftBottomCornerY() + shiftY);
                PointF staticMagnetTopRightNewPoint = new PointF(magnet.rightTopCornerX() + shiftX,magnet.rightTopCornerY() + shiftY);
                PointF staticMagnetBottomRightNewPoint = new PointF(magnet.rightBottomCornerX() + shiftX,magnet.rightBottomCornerY() + shiftY);
                switch (currentQuadrant) {
                    case 1:
                        //connectedSide = findPossibleCollisionsForFirstQuadrant(magnet,movingMagnet,movingMagnetTopLeftNewPoint,movingMagnetBottomLeftNewPoint,movingMagnetTopRightNewPoint);
                        /*if(connectedSide == side.BOTTOM) {
                            if(movingMagnetTopRightNewPoint.y <= magnet.leftBottomCornerY() && movingMagnet.leftBottomCorner().x <= magnet.rightBottomCorner().x) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftBottomCorner().y-movingMagnet.leftTopCorner().y);
                                handleCollision(closestMagnet);
                            }
                        } else if(connectedSide == side.RIGHT) {
                            if(movingMagnetTopLeftNewPoint.x <= magnet.rightBottomCornerX() && movingMagnet.leftTopCorner().y <= magnet.rightBottomCorner().y) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet, magnet.rightTopCorner().x-movingMagnet.leftTopCorner().x,0);
                                handleCollision(closestMagnet);
                            }
                        }*/
                        //closestMagnet = getSideForQuadrant1(connectedSide,movingMagnetTopRightNewPoint,movingMagnetTopLeftNewPoint,movingMagnet,magnet);

                       // if(movingMagnet.width() <= magnet.width()) {
                        connectedSide = findPossibleCollisionsForFirstQuadrant(magnet,movingMagnet,movingMagnetTopLeftNewPoint,movingMagnetBottomLeftNewPoint,movingMagnetTopRightNewPoint, movingMagnetBottomRightNewPoint, staticMagnetBottomLeftNewPoint,  staticMagnetBottomRightNewPoint,  staticMagnetTopRightNewPoint);
                        closestMagnet = getSideForQuadrant1(connectedSide,movingMagnetTopRightNewPoint,movingMagnetTopLeftNewPoint,movingMagnet,magnet);

                        if(closestMagnet != null) atLeastOneCollision = true;
                        break;
                    case 2:
                        /*connectedSide = findPossibleCollisionsForSecondQuadrant(magnet,movingMagnet,movingMagnetTopLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnetTopRightNewPoint);
                        if(connectedSide == side.BOTTOM) {
                            if(movingMagnetTopRightNewPoint.y <= magnet.leftBottomCornerY() && movingMagnet.rightBottomCorner().x >= magnet.leftBottomCorner().x) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftBottomCorner().y-movingMagnet.leftTopCorner().y);
                                handleCollision(closestMagnet);
                            }
                        } else if(connectedSide == side.LEFT) {
                            if(movingMagnetTopRightNewPoint.x >= magnet.leftBottomCornerX() && movingMagnet.rightTopCorner().y <= magnet.leftBottomCorner().y) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,magnet.leftTopCorner().x-movingMagnet.rightTopCorner().x,0);
                                handleCollision(closestMagnet);
                            }
                        }*/
                        //if(movingMagnet.width() <= magnet.width()) {
                            connectedSide = findPossibleCollisionsForSecondQuadrant(magnet,movingMagnet,movingMagnetTopLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnetTopRightNewPoint,staticMagnetBottomLeftNewPoint,staticMagnetBottomRightNewPoint,staticMagnetTopLeftNewPoint);
                            closestMagnet = getSideForQuadrant2(connectedSide,movingMagnetTopRightNewPoint,movingMagnet,magnet);
                       // } else {
                         //   connectedSide = findPossibleCollisionsForThirdQuadrant(movingMagnet, magnet, movingMagnetTopLeftNewPoint, movingMagnetBottomLeftNewPoint, movingMagnetBottomRightNewPoint,staticMagnetTopRightNewPoint,staticMagnetBottomRightNewPoint,staticMagnetTopLeftNewPoint);
                         //  / if(connectedSide == side.TOP) closestMagnet = getSideForQuadrant2(side.BOTTOM,movingMagnetTopRightNewPoint,movingMagnet,magnet);//
                          //  else if(connectedSide == side.RIGHT) closestMagnet = getSideForQuadrant2(side.LEFT,movingMagnetTopRightNewPoint,movingMagnet,magnet);

                        //}
                        if(closestMagnet != null) atLeastOneCollision = true;


                        break;
                    case 3:
                        /*connectedSide = findPossibleCollisionsForThirdQuadrant(magnet, movingMagnet, movingMagnetTopLeftNewPoint, movingMagnetBottomLeftNewPoint, movingMagnetBottomRightNewPoint);
                        if(connectedSide == side.TOP) {
                            if(movingMagnetBottomLeftNewPoint.y >= magnet.leftTopCornerY() && movingMagnet.leftBottomCorner().x <= magnet.rightBottomCorner().x) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftTopCorner().y-movingMagnet.leftBottomCorner().y);
                                handleCollision(closestMagnet);
                            }
                        } else if(connectedSide == side.RIGHT) {
                            if(movingMagnetTopLeftNewPoint.x <= magnet.rightBottomCorner().x && movingMagnet.leftBottomCorner().y >= magnet.leftTopCorner().y) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet, magnet.rightTopCorner().x-movingMagnet.leftTopCorner().x,0);
                                handleCollision(closestMagnet);
                            }
                        }*/
                       // if(movingMagnet.width() <= magnet.width()) {
                            connectedSide = findPossibleCollisionsForThirdQuadrant(magnet, movingMagnet, movingMagnetTopLeftNewPoint, movingMagnetBottomLeftNewPoint, movingMagnetBottomRightNewPoint,staticMagnetTopRightNewPoint,staticMagnetBottomRightNewPoint,staticMagnetTopLeftNewPoint);
                            closestMagnet = getSideForQuadrant3(connectedSide,movingMagnetBottomLeftNewPoint,movingMagnetTopLeftNewPoint,movingMagnet,magnet);
                        //} else {
                         //   connectedSide = findPossibleCollisionsForSecondQuadrant(movingMagnet,magnet,movingMagnetTopLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnetTopRightNewPoint);
                          //  if(connectedSide == side.BOTTOM) closestMagnet = getSideForQuadrant3(side.TOP,movingMagnetBottomLeftNewPoint,movingMagnetTopLeftNewPoint,movingMagnet,magnet);
                          //  else if(connectedSide == side.LEFT) closestMagnet = getSideForQuadrant3(side.RIGHT,movingMagnetBottomLeftNewPoint,movingMagnetTopLeftNewPoint,movingMagnet,magnet);

                       // }
                       if(closestMagnet != null) atLeastOneCollision = true;//

                        break;
                    case 4:
                        /*connectedSide = findPossibleCollisionsForFourthQuadrant(magnet,movingMagnet,movingMagnetBottomLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnetTopRightNewPoint);
                        if(connectedSide == side.TOP) {
                            if(movingMagnetBottomLeftNewPoint.y >= magnet.leftTopCornerY() && movingMagnet.rightBottomCorner().x >= magnet.leftBottomCorner().x) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,0,magnet.leftTopCorner().y-movingMagnet.leftBottomCorner().y);
                                handleCollision(closestMagnet);
                            }
                        } else if(connectedSide == side.LEFT) {
                            if(movingMagnetBottomRightNewPoint.x >= magnet.leftTopCornerX() && movingMagnet.rightBottomCorner().y >= magnet.rightTopCorner().y) {
                                closestMagnet = new MagnetSide(movingMagnet.id(),magnet,magnet.leftTopCorner().x-movingMagnet.rightTopCorner().x,0);
                                handleCollision(closestMagnet);
                            }
                        }*/
                        //connectedSide = findPossibleCollisionsForFourthQuadrant(magnet,movingMagnet,movingMagnetBottomLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnetTopRightNewPoint);
                        //closestMagnet = getSideForQuadrant4(connectedSide,movingMagnetBottomLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnet,magnet);
                        //if(movingMagnet.width() <= magnet.width()) {
                            connectedSide = findPossibleCollisionsForFourthQuadrant(magnet,movingMagnet,movingMagnetBottomLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnetTopRightNewPoint,movingMagnetTopLeftNewPoint,staticMagnetBottomLeftNewPoint,staticMagnetBottomRightNewPoint,staticMagnetTopRightNewPoint);
                            closestMagnet = getSideForQuadrant4(connectedSide,movingMagnetBottomLeftNewPoint,movingMagnetBottomRightNewPoint,movingMagnet,magnet);
                        if(closestMagnet != null) atLeastOneCollision = true;
                        break;
                }
            }

    }
        if(!atLeastOneCollision) {
            toHighlightMagnets.clear();
            ArrayList<Magnet> closeMagnets = getCloseMagnets(movingMagnet,xTouchPosition, yTouchPosition,closePadding);
            if (!closeMagnets.isEmpty()) { // if there are close magnets
               // setUpHighlightingForCloseButNotTouchingTiles(movingMagnet, closeMagnets);
                ArrayList<MagnetSide> lockingSides = getLockingSides(movingMagnet,closeMagnets);
                if(lockingSides != null) {
                    for(MagnetSide side : lockingSides) {
                        toHighlightMagnets.add(side.referenceToMagnet);
                        sidesToLockToNext.add(side);
                    }
                }
            }
            movingMagnet.setXAndY(xTouchPosition,yTouchPosition);
        }
    }


    private boolean thereIsABottomCollisionInQuadrant1(Magnet movingMagnet, Magnet magnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomLeftNewPoint, PointF movingMagnetTopRightNewPoint, PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopRightNewPoint){
        if(movingMagnet.width() > magnet.width()) {
            if((thereIsACollision(magnet.leftBottomCorner(),staticMagnetBottomLeftNewPoint,movingMagnet.leftTopCorner(),movingMagnet.rightTopCorner())) ||
                    (thereIsACollision(magnet.rightBottomCorner(),staticMagnetBottomRightNewPoint,movingMagnet.leftTopCorner(),movingMagnet.rightTopCorner())) ||
                    (thereIsACollision(magnet.rightTopCorner(),staticMagnetTopRightNewPoint,movingMagnet.leftTopCorner(),movingMagnet.rightTopCorner())))     {
                return true;
            }
        } else {
            if ((thereIsACollision(movingMagnet.leftTopCorner(), movingMagnetTopLeftNewPoint, magnet.leftBottomCorner(), magnet.rightBottomCorner())) ||
                    (thereIsACollision(movingMagnet.leftBottomCorner(), movingMagnetBottomLeftNewPoint, magnet.leftBottomCorner(), magnet.rightBottomCorner())) ||
                    (thereIsACollision(movingMagnet.rightTopCorner(), movingMagnetTopRightNewPoint, magnet.leftBottomCorner(), magnet.rightBottomCorner()))) {
                //collidedSide = side.BOTTOM;
                return true;
            }
        }
        return false;
    }

    private boolean thereIsARightCollisionInQuadrant1(Magnet movingMagnet, Magnet magnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomLeftNewPoint, PointF movingMagnetTopRightNewPoint,PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopRightNewPoint) {
        if(movingMagnet.height() > magnet.height()) {
            if((thereIsACollision(magnet.leftBottomCorner(),staticMagnetBottomLeftNewPoint,movingMagnet.leftTopCorner(),movingMagnet.leftBottomCorner())) ||
                    (thereIsACollision(magnet.rightBottomCorner(),staticMagnetBottomRightNewPoint,movingMagnet.leftTopCorner(),movingMagnet.leftBottomCorner())) ||
                    (thereIsACollision(magnet.rightTopCorner(),staticMagnetTopRightNewPoint,movingMagnet.leftTopCorner(),movingMagnet.leftBottomCorner()))) {
                return true;
            }
        } else {
            if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner())) ||
                    (thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner())) ||
                    (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))) {
                return true;
            }
        }
        return false;
    }

    private boolean thereIsABottomCollisionInQuadrant2(Magnet movingMagnet, Magnet magnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomRightNewPoint, PointF movingMagnetTopRightNewPoint, PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopLeftNewPoint){
        if(movingMagnet.width() > magnet.width()) {
            if((thereIsACollision(magnet.leftTopCorner(),staticMagnetTopLeftNewPoint,movingMagnet.leftTopCorner(),movingMagnet.rightTopCorner()))
                    || (thereIsACollision(magnet.rightBottomCorner(),staticMagnetBottomRightNewPoint,movingMagnet.leftTopCorner(),movingMagnet.rightTopCorner()))
                    || (thereIsACollision(magnet.leftBottomCorner(),staticMagnetBottomLeftNewPoint,movingMagnet.leftTopCorner(),movingMagnet.rightTopCorner()))) {
                return true;
            }
        } else {
            if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner()) )
                    || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner()))
                    || (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner()))) {
                //collidedSide = side.BOTTOM;
                return true;
            }
        }
        return false;
    }

    private boolean thereIsALeftCollisionInQuadrant2(Magnet movingMagnet, Magnet magnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomRightNewPoint, PointF movingMagnetTopRightNewPoint,PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopLeftNewPoint) {
        if(movingMagnet.height() > magnet.height()) {
            if((thereIsACollision(magnet.leftTopCorner(),staticMagnetTopLeftNewPoint,movingMagnet.rightBottomCorner(),movingMagnet.rightTopCorner()))
                    || (thereIsACollision(magnet.rightBottomCorner(),staticMagnetBottomRightNewPoint,movingMagnet.rightBottomCorner(),movingMagnet.rightTopCorner()))
                    || (thereIsACollision(magnet.leftBottomCorner(),staticMagnetBottomLeftNewPoint,movingMagnet.rightBottomCorner(),movingMagnet.rightTopCorner()))) {
                return true;
            }
        } else {
            if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.leftBottomCorner(),magnet.leftTopCorner()))
                    || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftBottomCorner(),magnet.leftTopCorner()))
                    || (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftBottomCorner(),magnet.leftTopCorner()))) {
                return true;
            }
        }
        return false;
    }

    private boolean thereIsATopCollisionInQuadrant3(Magnet movingMagnet, Magnet magnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomRightNewPoint, PointF movingMagnetBottomLeftNewPoint, PointF staticMagnetTopRightNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopLeftNewPoint){
        if(movingMagnet.width() > magnet.width()) {
            if((thereIsACollision(magnet.leftTopCorner(),staticMagnetTopLeftNewPoint,movingMagnet.leftBottomCorner(),movingMagnet.rightBottomCorner()) )
                    || (thereIsACollision(magnet.rightBottomCorner(),staticMagnetBottomRightNewPoint,movingMagnet.leftBottomCorner(),movingMagnet.rightBottomCorner()))
                    || (thereIsACollision(magnet.rightTopCorner(),staticMagnetTopRightNewPoint,movingMagnet.leftBottomCorner(),movingMagnet.rightBottomCorner()))) {
                return true;
            }
        } else {
            if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))
                    || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))
                    || (thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))) {
                //collidedSide = side.BOTTOM;
                return true;
            }
        }
        return false;
    }

    private boolean thereIsARightCollisionInQuadrant3(Magnet movingMagnet, Magnet magnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomRightNewPoint, PointF movingMagnetBottomLeftNewPoint,PointF staticMagnetTopRightNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopLeftNewPoint) {
        if(movingMagnet.height() > magnet.height()) {
            if((thereIsACollision(magnet.leftTopCorner(),staticMagnetTopLeftNewPoint,movingMagnet.leftBottomCorner(),movingMagnet.leftTopCorner()))
                    || (thereIsACollision(magnet.rightBottomCorner(),staticMagnetBottomRightNewPoint,movingMagnet.leftBottomCorner(),movingMagnet.leftTopCorner()))
                    || (thereIsACollision(magnet.rightTopCorner(),staticMagnetTopRightNewPoint,movingMagnet.leftBottomCorner(),movingMagnet.leftTopCorner()))) {
                return true;
            }
        } else {
            if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))
                    || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))
                    || (thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))) {
                return true;
            }
        }
        return false;
    }


    private boolean thereIsTopCollisionInQuadrant4(Magnet movingMagnet, Magnet magnet, PointF movingMagnetBottomRightNewPoint, PointF movingMagnetBottomLeftNewPoint, PointF movingMagnetTopRightNewPoint, PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetTopLeftNewPoint, PointF staticMagnetTopRightNewPoint){
        if(movingMagnet.width() > magnet.width()) {
            if((thereIsACollision(magnet.leftTopCorner(), staticMagnetTopLeftNewPoint, movingMagnet.leftBottomCorner(), movingMagnet.rightBottomCorner())) ||
                    (thereIsACollision(magnet.leftBottomCorner(), staticMagnetBottomLeftNewPoint, movingMagnet.leftBottomCorner(), movingMagnet.rightBottomCorner())) ||
                    (thereIsACollision(magnet.rightTopCorner(), staticMagnetTopRightNewPoint, movingMagnet.leftBottomCorner(), movingMagnet.rightBottomCorner()))) {
                return true;
            }
        } else {
            if((thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner())) ||
                    (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner())) ||
                    (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))) {
                return true;
            }
        }
        return false;
    }

    private boolean thereIsLeftCollisionInQuadrant4(Magnet movingMagnet, Magnet magnet, PointF movingMagnetBottomRightNewPoint, PointF movingMagnetBottomLeftNewPoint, PointF movingMagnetTopRightNewPoint,PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetTopLeftNewPoint, PointF staticMagnetTopRightNewPoint) {
        if(movingMagnet.height() > magnet.height()) {
            if((thereIsACollision(magnet.leftTopCorner(),staticMagnetTopLeftNewPoint,movingMagnet.rightBottomCorner(),movingMagnet.rightTopCorner())) ||
                    (thereIsACollision(magnet.leftBottomCorner(),staticMagnetBottomLeftNewPoint,movingMagnet.rightBottomCorner(),movingMagnet.rightTopCorner())) ||
                    (thereIsACollision(magnet.rightTopCorner(),staticMagnetTopRightNewPoint,movingMagnet.rightBottomCorner(),movingMagnet.rightTopCorner()))) {
                return true;
            }
        } else {
            if((thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.leftTopCorner(),magnet.leftBottomCorner())) ||
                    (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftTopCorner(),magnet.leftBottomCorner())) ||
                    (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftTopCorner(),magnet.leftBottomCorner()))) {
                return true;
            }
        }


        return false;
    }


    private side findPossibleCollisionsForFirstQuadrant(Magnet magnet, Magnet movingMagnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomLeftNewPoint,PointF movingMagnetTopRightNewPoint, PointF movingMagnetBottomRightNewPoint,PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopRightNewPoint) {
        side collidedSide = null;

        //if(thereIsABottomCollisionInQuadrant1(movingMagnet,magnet,movingMagnetTopLeftNewPoint,movingMagnetBottomLeftNewPoint,movingMagnetTopRightNewPoint)) collidedSide = side.BOTTOM;

        if(thereIsABottomCollisionInQuadrant1(movingMagnet,magnet,movingMagnetTopLeftNewPoint,movingMagnetBottomLeftNewPoint,movingMagnetTopRightNewPoint,staticMagnetBottomLeftNewPoint,staticMagnetBottomRightNewPoint,staticMagnetTopRightNewPoint)) collidedSide = side.BOTTOM;
        if(thereIsARightCollisionInQuadrant1(movingMagnet,magnet,movingMagnetTopLeftNewPoint,movingMagnetBottomLeftNewPoint,movingMagnetTopRightNewPoint, staticMagnetBottomLeftNewPoint, staticMagnetBottomRightNewPoint, staticMagnetTopRightNewPoint)) {
            if(collidedSide == null) {
                collidedSide = side.RIGHT;
            } else {
                if((movingMagnet.rightTopCorner().y - magnet.leftBottomCorner().y) >= 0) collidedSide = side.BOTTOM;
                else collidedSide = side.RIGHT;
            }

        }

       /* if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner())) ||
                (thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner())) ||
                (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner()))) {
            collidedSide = side.BOTTOM;
        }

        if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner())) ||
                (thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner())) ||
                (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))) {
            if(collidedSide == null) {
                collidedSide = side.RIGHT;
            } else {
                if((movingMagnet.rightTopCorner().y - magnet.leftBottomCorner().y) >= 0) collidedSide = side.BOTTOM;
                else collidedSide = side.RIGHT;
            }
        }*/

        return collidedSide;
    }
    private side findPossibleCollisionsForSecondQuadrant(Magnet magnet, Magnet movingMagnet, PointF movingMagnetTopLeftNewPoint,PointF movingMagnetBottomRightNewPoint,PointF movingMagnetTopRightNewPoint, PointF staticMagnetBottomLeftNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopLeftNewPoint) {
        side collidedSide = null;
        /*if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner()) )
                || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner()))
                || (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftBottomCorner(),magnet.rightBottomCorner()))) {
            collidedSide = side.BOTTOM;
        }
        if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.leftBottomCorner(),magnet.leftTopCorner()))
                || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftBottomCorner(),magnet.leftTopCorner()))
                || (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftBottomCorner(),magnet.leftTopCorner()))) {
            if(collidedSide == null) {
                collidedSide = side.LEFT;
            } else {
                if((movingMagnet.rightTopCorner().y - magnet.leftBottomCorner().y) >= 0) collidedSide = side.BOTTOM;
                else collidedSide = side.LEFT;
            }
        }*/
        if(thereIsABottomCollisionInQuadrant2(movingMagnet,magnet, movingMagnetTopLeftNewPoint,  movingMagnetBottomRightNewPoint, movingMagnetTopRightNewPoint,  staticMagnetBottomLeftNewPoint,  staticMagnetBottomRightNewPoint,  staticMagnetTopLeftNewPoint)) collidedSide = side.BOTTOM;
        if(thereIsALeftCollisionInQuadrant2( movingMagnet,  magnet,  movingMagnetTopLeftNewPoint,  movingMagnetBottomRightNewPoint,  movingMagnetTopRightNewPoint, staticMagnetBottomLeftNewPoint, staticMagnetBottomRightNewPoint, staticMagnetTopLeftNewPoint)) {
            if(collidedSide == null) {
                collidedSide = side.LEFT;
            } else {
                if((movingMagnet.rightTopCorner().y - magnet.leftBottomCorner().y) >= 0) collidedSide = side.BOTTOM;
                else collidedSide = side.LEFT;
            }

        }
        return collidedSide;
    }

    private side findPossibleCollisionsForThirdQuadrant(Magnet magnet, Magnet movingMagnet, PointF movingMagnetTopLeftNewPoint, PointF movingMagnetBottomLeftNewPoint, PointF movingMagnetBottomRightNewPoint, PointF staticMagnetTopRightNewPoint, PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopLeftNewPoint) {
        side collidedSide = null;

       /* if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))
                || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))
                || (thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))) {
            collidedSide = side.TOP;
        }
        if((thereIsACollision(movingMagnet.leftTopCorner(),movingMagnetTopLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))
                || (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))
                || (thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.rightBottomCorner(),magnet.rightTopCorner()))) {
            if(collidedSide == null) {
                collidedSide = side.RIGHT;
            } else {
                if((magnet.leftTopCorner().y - movingMagnet.leftBottomCorner().y) >= 0) collidedSide = side.TOP;
                else collidedSide = side.RIGHT;
            }
        }*/
        if(thereIsATopCollisionInQuadrant3( movingMagnet, magnet,  movingMagnetTopLeftNewPoint,  movingMagnetBottomRightNewPoint,  movingMagnetBottomLeftNewPoint,  staticMagnetTopRightNewPoint,  staticMagnetBottomRightNewPoint,  staticMagnetTopLeftNewPoint)) collidedSide = side.TOP;
        if(thereIsARightCollisionInQuadrant3(movingMagnet, magnet, movingMagnetTopLeftNewPoint, movingMagnetBottomRightNewPoint, movingMagnetBottomLeftNewPoint,staticMagnetTopRightNewPoint, staticMagnetBottomRightNewPoint, staticMagnetTopLeftNewPoint )) {
            if(collidedSide == null) {
                collidedSide = side.RIGHT;
            } else {
                if((magnet.leftTopCorner().y - movingMagnet.leftBottomCorner().y) >= 0) collidedSide = side.TOP;
                else collidedSide = side.RIGHT;
            }

        }

        return collidedSide;
    }

    private side findPossibleCollisionsForFourthQuadrant(Magnet magnet, Magnet movingMagnet, PointF movingMagnetBottomLeftNewPoint, PointF movingMagnetBottomRightNewPoint, PointF movingMagnetTopRightNewPoint,PointF movingMagnetTopLeftNewPoint,PointF staticMagnetBottomLeftNewPoint,PointF staticMagnetBottomRightNewPoint, PointF staticMagnetTopRightNewPoint) {
        side collidedSide = null;
        /*if((thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner())) ||
                (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner())) ||
                (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftTopCorner(),magnet.rightTopCorner()))) {
            collidedSide = side.TOP;
        }
        if((thereIsACollision(movingMagnet.leftBottomCorner(),movingMagnetBottomLeftNewPoint,magnet.leftTopCorner(),magnet.leftBottomCorner())) ||
                (thereIsACollision(movingMagnet.rightBottomCorner(),movingMagnetBottomRightNewPoint,magnet.leftTopCorner(),magnet.leftBottomCorner())) ||
                (thereIsACollision(movingMagnet.rightTopCorner(),movingMagnetTopRightNewPoint,magnet.leftTopCorner(),magnet.leftBottomCorner()))) {
            if(collidedSide == null) {
                collidedSide = side.LEFT;

            }else {
                if((magnet.leftTopCorner().y - movingMagnet.leftBottomCorner().y) >= 0) collidedSide = side.TOP;
                else collidedSide = side.LEFT;
            }
        }*/
        if(thereIsTopCollisionInQuadrant4(movingMagnet,magnet,movingMagnetTopLeftNewPoint,movingMagnetBottomLeftNewPoint,movingMagnetTopRightNewPoint,staticMagnetBottomLeftNewPoint,staticMagnetBottomRightNewPoint,staticMagnetTopRightNewPoint)) collidedSide = side.TOP;
        if(thereIsLeftCollisionInQuadrant4(movingMagnet,magnet,movingMagnetTopLeftNewPoint,movingMagnetBottomLeftNewPoint,movingMagnetTopRightNewPoint, staticMagnetBottomLeftNewPoint, staticMagnetBottomRightNewPoint, staticMagnetTopRightNewPoint)) {
            if(collidedSide == null) {
                collidedSide = side.LEFT;
            } else {
                if((magnet.leftTopCorner().y - movingMagnet.leftBottomCorner().y) >= 0) collidedSide = side.TOP;
                else collidedSide = side.LEFT;
            }

        }
        return collidedSide;
    }

    private int getQuadrant(Magnet magnet) {
        float centerX = clickedMagnet.x();
        float centerY = clickedMagnet.y();
        if(magnet.x() <= centerX) {
            if(magnet.y() <= centerY) {
                return 1;
            } else {
                return 3;
            }
        }else {
            if(magnet.y() <= centerY ) {
                return 2;
            } else {
                return 4;
            }
        }
    }

    // are the two cross products different signs?, if so, there is a collision
    private boolean thereIsACollision(PointF movingMagnetOldPoint, PointF movingMagnetNewPoint, PointF point1, PointF point2) {
        PointF movingMagnetVector = new PointF((movingMagnetNewPoint.x - movingMagnetOldPoint.x), (movingMagnetNewPoint.y - movingMagnetOldPoint.y));
        PointF vectorFromMovingMagnetToEdge1 = new PointF((point1.x - movingMagnetOldPoint.x), (point1.y - movingMagnetOldPoint.y));
        PointF vectorFromMovingMagnetToEdge2 = new PointF((point2.x - movingMagnetOldPoint.x),( point2.y - movingMagnetOldPoint.y));
        float crossProduct1 = zComponentOfCrossProduct(movingMagnetVector,vectorFromMovingMagnetToEdge1);
        float crossProduct2 = zComponentOfCrossProduct(movingMagnetVector,vectorFromMovingMagnetToEdge2);
        return theCrossProductsHaveADifferentSign(crossProduct1,crossProduct2);
    }

    private boolean theMagnetIsMovingAway(PointF movingMagnetVector,PointF vectorFromMovingMagnetToEdge1) {
        //return ((movingMagnetVector.x * vectorFromMovingMagnetToEdge1.x) < 0) || ((movingMagnetVector.y * vectorFromMovingMagnetToEdge1.y) < 0);
        return false;
    }

    private boolean theCrossProductsHaveADifferentSign(double crossProduct1, double crossProduct2) {
        return (crossProduct1 * crossProduct2) < 0;
    }

    private float zComponentOfCrossProduct(PointF vector1, PointF vector2) {
        return (vector1.x * vector2.y) - (vector1.y * vector2.x);
    }


    /* A convenience method called by handleMovingTile that gets all of the locking sides of the close magnet tiles, adds them to sidesToLockToNext and highlights them. When all is said and done, it moves the clicked tile. */
    private void setUpHighlightingForCloseButNotTouchingTiles(Magnet clickedTile, ArrayList<Magnet> closeMagnets) {
        toHighlightMagnets.clear();
        ArrayList<MagnetSide> lockingSides = getLockingSides(clickedTile,closeMagnets);
        if(lockingSides != null) {
            for(MagnetSide side : lockingSides) {
                toHighlightMagnets.add(side.referenceToMagnet);
                sidesToLockToNext.add(side);
            }
        }
    }

    /*private void addToConnectedTilesArrayList(MagnetSide magnetSide, ArrayList<Magnet> side1ConnectedMagnets, ArrayList<Magnet> side2ConnectedMagnets, ArrayList<Magnet> side3ConnectedMagnets) {
        if(side1ConnectedMagnets.size() > 0 || side2ConnectedMagnets.size() > 0 || side3ConnectedMagnets.size() > 0) {
            for(ArrayList<Magnet> connectedMagnetsList : connectedMagnets) {
                if(connectedMagnetsList.contains(magnetSide.referenceToMagnet)) {
                    connectedMagnetsList.add(clickedMagnet);
                }
            }
        } else {
            ArrayList<Magnet> newConnectedTilesArrayList = new ArrayList<Magnet>(2);
            newConnectedTilesArrayList.add(clickedMagnet);
            newConnectedTilesArrayList.add(magnetSide.referenceToMagnet);
            connectedMagnets.add(newConnectedTilesArrayList);
        }

    }*/

    private void setSides(MagnetSide magnetSide) {
        side otherSide = MagnetSide.getOtherMagnetSide(magnetSide.xAndyDistances);
        switch (otherSide) {
            case TOP:
                magnetSide.referenceToMagnet.setTopSideConnectedMagnet(clickedMagnet);
                clickedMagnet.setBottomSideConnectedMagnet(magnetSide.referenceToMagnet);
                //addToConnectedTilesArrayList(magnetSide,magnetSide.referenceToMagnet.bottomSideConnectedMagnet(),magnetSide.referenceToMagnet.leftSideConnectedMagnet(),magnetSide.referenceToMagnet.rightSideConnectedMagnet());
                break;
            case BOTTOM:
                magnetSide.referenceToMagnet.setBottomSideConnectedMagnet(clickedMagnet);
                clickedMagnet.setTopSideConnectedMagnet(magnetSide.referenceToMagnet);
                //addToConnectedTilesArrayList(magnetSide,magnetSide.referenceToMagnet.topSideConnectedMagnet(),magnetSide.referenceToMagnet.leftSideConnectedMagnet(),magnetSide.referenceToMagnet.rightSideConnectedMagnet());
                break;
            case LEFT:
                magnetSide.referenceToMagnet.setLeftSideConnectedMagnet(clickedMagnet);
                clickedMagnet.setRightSideConnectedMagnet(magnetSide.referenceToMagnet);
                //addToConnectedTilesArrayList(magnetSide,magnetSide.referenceToMagnet.bottomSideConnectedMagnet(),magnetSide.referenceToMagnet.topSideConnectedMagnet(),magnetSide.referenceToMagnet.rightSideConnectedMagnet());
                break;
            case RIGHT:
                magnetSide.referenceToMagnet.setRightSideConnectedMagnet(clickedMagnet);
                clickedMagnet.setLeftSideConnectedMagnet(magnetSide.referenceToMagnet);
                //addToConnectedTilesArrayList(magnetSide,magnetSide.referenceToMagnet.bottomSideConnectedMagnet(),magnetSide.referenceToMagnet.leftSideConnectedMagnet(),magnetSide.referenceToMagnet.topSideConnectedMagnet());
                break;
        }
    }

    private float transformToWorldCoordinates(float coordinate) {
        return (-1*scalePivotX / scaleFactor) + (coordinate / scaleFactor) + (scalePivotX);
    }

    /* A convenience method called by handleMovingClickedTile to set the magnetIsAboveTrashCan class variable.*/
    private void checkIfTheMagnetIsAboveTheTrashCan(Magnet clickedTile) {
        //float width = (-1*scalePivotX / scaleFactor) + (getWidth() / scaleFactor) + (scalePivotX);
        //float height = (-1*scalePivotY / scaleFactor) + (getHeight() / scaleFactor) + (scalePivotY);
        //float width = (-1*scalePivotX / scaleFactor) + (getWidth() / scaleFactor) + (scalePivotX);
        //float height = (-1*scalePivotY / scaleFactor) + (getHeight() / scaleFactor) + (scalePivotY);

        magnetIsAboveTrashCan = trashCan.collidesWithTrashCan(clickedTile,getWidth(),getHeight(),scaleFactor,scalePivotX,scalePivotY);
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


    public DrawingPanel(Context context, CanvasListener theCanvasListener) {
        super(context);
        getHolder().addCallback(this);
        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        gestureDetector = new GestureDetector(context, gestureListener);
        canvasListener = theCanvasListener;
        this.setHapticFeedbackEnabled(true);
        trashCan = new TrashCan(context);
        awardAlert = new AwardAlert(context);

    }

    private void resetIfPastCanvasBoundary(Magnet magnet, float rightBoundaryOriginal, float bottomBoundaryOriginal) {
        float halfWidth = (magnet.width()*scaleFactor)/2;
        float halfHeight = (magnet.height()*scaleFactor)/2;
        float movingTileX = (-1*scalePivotX * scaleFactor) + (magnet.x() * scaleFactor) + (scalePivotX);
        float movingTileY = (-1*scalePivotY * scaleFactor) + (magnet.y() * scaleFactor) + (scalePivotY);
        float rightBoundary = (-1*scalePivotX / scaleFactor) + (rightBoundaryOriginal / scaleFactor) + (scalePivotX);
        float leftBoundary = (-1*scalePivotX / scaleFactor) + (0 / scaleFactor) + (scalePivotX);
        float topBoundary = (-1*scalePivotY / scaleFactor) + (0 / scaleFactor) + (scalePivotY);
        float bottomBoundary = (-1*scalePivotY / scaleFactor) + (bottomBoundaryOriginal / scaleFactor) + (scalePivotY);
        if((movingTileX + halfWidth) > rightBoundaryOriginal) magnet.setX(rightBoundary-magnet.width()/2);
        if(movingTileX - halfWidth < 0) {
            magnet.setX(leftBoundary + magnet.width()/2);
        }
        if (movingTileY + halfHeight > bottomBoundaryOriginal) magnet.setY(bottomBoundary - magnet.height()/2);
        if (movingTileY - halfHeight < 0) magnet.setY(topBoundary + magnet.height()/2);
    }


    // load the award dialog when the award icon has been clicked, different dialogs are displayed depending on the number of awards the user has not looked at
    private void loadAwardDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final int numberOfAwards = awardAlert.getAwardsSize();
        if(numberOfAwards == 0) {
            builder.setTitle("You don't have any current awards to view")
                    .setMessage("Check out the awards page to check out which awards you've received and all of the awards you can get!")
                    .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            canvasListener.awardClicked();
                        }
                    })
                    .setPositiveButton("See my awards", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getContext().startActivity(new Intent(getContext(), MainMenu.class));
                        }
                    });
        } else if(numberOfAwards == 1){
            award thisAward = awardAlert.getNextAward();
            builder.setTitle("Your newest award: " + thisAward.name())
                    .setMessage("About this award: " + thisAward.description())
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            canvasListener.awardClicked();
                            awardAlert.removeLastAward();
                        }
                    });
        } else {
            award thisAward = awardAlert.getNextAward();
            builder.setTitle("Your newest award: " + thisAward.name())
                    .setMessage("About this award: " + thisAward.description())
                    .setPositiveButton("See the next award", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            awardAlert.removeLastAward();
                            loadAwardDialog();
                        }
                    });
        }
        builder.create();
        builder.show();
    }

    // used during the demo to animate the trash can until the user pulls the magnet into it
    public void continuouslyAnimateTrashCan(boolean animateTrashCan) {
        if(trashCan != null) continuouslyAnimateTrashCan = animateTrashCan;
        invalidate();
    }

    // used during the demo to animate the award until the user clicks it
    public void continuouslyAnimateAward(boolean continuouslyAnimateAward) {
        this.continuouslyAnimateAward = continuouslyAnimateAward;
        if(continuouslyAnimateAward) {
            if(awardAlert!=null) awardAlert.addNewAward("Demo Award", "You have received your first award for running the demo.");
        } else {
            if(awardAlert != null) awardAlert.setAlert(false);
        }
        invalidate();
    }


    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#EFEBE7"));
        canvas.save();
        canvas.scale(scaleFactor,scaleFactor,scalePivotX,scalePivotY);
        for(Magnet magnet : magnets) {
            magnet.setHighlight(false);
        }
        for(Magnet toHighlightMagnet : toHighlightMagnets) {
            toHighlightMagnet.setHighlight(true);
        }
        for(Magnet magnet : magnets) {
            resetIfPastCanvasBoundary(magnet,getWidth(),getHeight());
            magnet.draw(canvas, getWidth(),  getHeight(),getRootView());
            //magnet.setHighlight(false);
        }
        canvas.restore();
        boolean animateAward;
        if(continuouslyAnimateTrashCan) {
            trashCan.drawTrashCan(canvas,true,getScaledCoordinate(getWidth()),getHeight());
        } else {
            trashCan.drawTrashCan(canvas,magnetIsAboveTrashCan,getWidth(),getHeight());
        }

        //System.out.println("width " + Float.toString(getWidth()));

        if(continuouslyAnimateAward) {
            awardAlert.setAlert(true);
            awardAlert.drawAwardAlert(canvas, getWidth());
            animateAward = true;
        } else {
            animateAward = awardAlert.drawAwardAlert(canvas, getWidth());
        }


        //toHighlightMagnets.clear();
        // if the trash can should be shaking, keep drawing
        if(magnetIsAboveTrashCan || animateAward|| continuouslyAnimateTrashCan) {
            invalidate();
        }
        //canvas.restore();
        //lastWidth = getWidth();
        //lastHeight = getHeight();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()
        setOnDragListener(this);



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
            //lastScaleFactor = scaleFactor;
            scaleFactor *= scaleGestureDetector.getScaleFactor();
            // Don't let the object get too small or too large.
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.5f));

            scalePivotX = scaleGestureDetector.getFocusX();
            scalePivotY = scaleGestureDetector.getFocusY();

            scalePivotX = (-1*scalePivotX/scaleFactor) + (scalePivotX / scaleFactor) + (scalePivotX) ;
            scalePivotY = (-1*scalePivotY/scaleFactor) + (scalePivotY / scaleFactor) + (scalePivotY) ;

            invalidate();
            return true;
        }
    };


    // The current viewport. This rectangle represents the currently visible
// chart domain and range.
    private RectF mCurrentViewport =
            new RectF(0,0, 500, 500);


    private float xOffset = 0;
    private float yOffset = 0;

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
        /*@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }*/
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // Scrolling uses math based on the viewport (as opposed to math using pixels).

            // Pixel offset is the offset in screen pixels, while viewport offset is the


            return true;
        }
    };
    /**
     * Sets the current viewport (defined by mCurrentViewport) to the given
     * X and Y positions. Note that the Y value represents the topmost pixel position,
     * and thus the bottom of the mCurrentViewport rectangle.
     */
    private void setViewportBottomLeft(float x, float y) {
    /*
     * Constrains within the scroll range. The scroll range is simply the viewport
     * extremes (AXIS_X_MAX, etc.) minus the viewport size. For example, if the
     * extremes were 0 and 10, and the viewport size was 2, the scroll range would
     * be 0 to 8.
     */

        float curWidth = mCurrentViewport.width();
        float curHeight = mCurrentViewport.height();
        x = Math.max(0, Math.min(x, 500 - curWidth));
        y = Math.max(0 + curHeight, Math.min(y, 500));

        mCurrentViewport.set(x, y - curHeight, x + curWidth, y);

        // Invalidates the View to update the display.
        ViewCompat.postInvalidateOnAnimation(this);
    }
}





enum side {
    LEFT,RIGHT,TOP,BOTTOM
}

//class MagnetSide
//}

