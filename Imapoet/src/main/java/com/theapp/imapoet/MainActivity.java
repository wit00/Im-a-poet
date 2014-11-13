package com.theapp.imapoet;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements DrawerFragment.OnFragmentInteractionListener,
        NewPoemSaveAlertDialog.NewPoemSaveAlertDialogListener, ExistingPoemSaveAlertDialog.ExistingPoemDialogListener, DrawingPanel.CanvasListener, DemoFragment.DemoListener {

    private DemoFragment demoFragment = null;
    private Context helperContext = this;
    private DrawerFragment drawerFragment;
    private DrawingPanelFragment drawingPanelFragment;
    private GameState gameState;
    private MediaServiceHelper mediaServiceHelper = new MediaServiceHelper(this);
    private DrawingPanelRetainDataFragment drawingPanelRetainDataFragment;

    private void highlight(int viewID) {
        View view = findViewById(viewID);
        view.setBackgroundResource(R.drawable.yellow_rounded_corner_background);
        view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink));
    }

    private void clearHighlight(int viewID) {
        View view = findViewById(viewID);
        view.clearAnimation();
        view.setBackgroundColor(Color.TRANSPARENT);
    }


    public void highlightButtons(boolean highlightButtons) {
        if(highlightButtons) {
            highlight(R.id.share_buttons);

        } else {

            clearHighlight(R.id.share_buttons);
        }
    }
    public void highlightDrawer(boolean highlightDrawer) {
        if(highlightDrawer) highlight(R.id.drawer_button);
        else  clearHighlight(R.id.drawer_button);
    }


    public void highlightPacks(boolean highlightPacks) {
        if(highlightPacks) highlight(R.id.sets_spinner);
        else clearHighlight(R.id.sets_spinner);
    }


    public void highlightTrashCan(boolean highlightTrashCan) {
        if(drawingPanelFragment != null && drawingPanelFragment.drawingPanel() != null) {
            drawingPanelFragment.drawingPanel().continuouslyAnimateTrashCan(highlightTrashCan);
        }
    }

    public void highlightAward(boolean highlightAward) {
        if(drawingPanelFragment != null && drawingPanelFragment.drawingPanel() != null) {
            drawingPanelFragment.drawingPanel().continuouslyAnimateAward(highlightAward);
        }
    }

    public void addDemoFragment(String placement) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.demo_slide_in, R.anim.demo_slide_in);//, R.anim.abc_fade_in, animPopExit);
        demoFragment = DemoFragment.newInstance(getString(R.string.demo_opening_string),placement);
        fragmentTransaction.add(android.R.id.content, demoFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void changeTextView(String text) {
        if(demoFragment != null) demoFragment.changeText(text);
    }


    public void drawerOpened() {
        if(demoFragment != null) demoFragment.runDemo(DemoFragment.DemoPart.DRAWER_OPENED);
    }

    public void demoComplete() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(demoFragment);
        clearHighlight(R.id.drawer_button);
        clearHighlight(R.id.sets_spinner);
        clearHighlight(R.id.share_buttons);
        highlightTrashCan(false);
        highlightAward(false);
        fragmentTransaction.commit();
        demoFragment = null;
    }


    public void magnetTilesChanged(int numberMagnetTiles) {
        //magnetListener.magnetAdded();
        gameState.magnetTilesChanged(numberMagnetTiles);
        if(demoFragment != null) {
            demoFragment.runDemo(DemoFragment.DemoPart.MAGNET_ADDED);
        }
    }

    public void magnetDeleted() {
        if(demoFragment != null) {
            demoFragment.runDemo(DemoFragment.DemoPart.MAGNET_DELETED);
        } else {
            gameState.magnetDeleted();
        }
    }

    public void awardClicked() {
        if(demoFragment!=null) {
            demoFragment.runDemo(DemoFragment.DemoPart.AWARD_CLICKED);
            gameState.demoComplete();
        }
    }

    public void newPoemInsertIntoDatabase(String titleInputValue, boolean newPoem) {
        insertPoemIntoDatabase(titleInputValue, drawingPanelFragment.getPoem(),newPoem);
    }

    public void existingPoemInsertIntoDatabase(String titleInputValue, boolean newPoem) {
        insertPoemIntoDatabase(titleInputValue, drawingPanelFragment.getPoem(), newPoem);
    }


    private void handleFirstLaunch(SharedPreferences sharedPreferences) {
        addDemoFragment(DemoFragment.DemoPart.START.toString());
        loadValuesIntoDatabase();
        setFirstLaunchToFalse(sharedPreferences);
        gameState = new GameState(this, drawingPanelFragment.drawingPanel(),true);
    }

    private void setReferencesToFragments(FragmentManager fragmentManager) {
        drawingPanelRetainDataFragment = (DrawingPanelRetainDataFragment)fragmentManager.findFragmentByTag("drawingPanelData");
        drawerFragment = (DrawerFragment)getSupportFragmentManager().findFragmentById(R.id.drawer_layout);
        drawingPanelFragment = (DrawingPanelFragment) getSupportFragmentManager().findFragmentById(R.id.the_canvas);
        ((DrawerLayout)findViewById(R.id.pager)).setDrawerListener(new MagnetDrawerListener(this));
    }

    private void loadMagnetsOntoCanvas(FragmentManager fragmentManager) {
        Bundle extras = getIntent().getExtras();
        if(extras != null) { // we are loading from save
            gameState.loadSavedMagnets(extras);
        } else { // we are not loading from save
            if(drawingPanelRetainDataFragment == null) { // this is not an orientation change because drawingPanelRetainFragment is null
                drawingPanelFragment.loadMagnets();
                drawingPanelRetainDataFragment = drawingPanelRetainDataFragment.newInstance();
                fragmentManager.beginTransaction().add(drawingPanelRetainDataFragment,"drawingPanelData").commit();
            } else { // it's an orientation change, so just load the magnets from data retaining fragment
                drawingPanelFragment.loadMagnets(drawingPanelRetainDataFragment.getMagnets(),drawingPanelRetainDataFragment.getPreviouslySavedPoem(),drawingPanelRetainDataFragment.getPreviouslySavedPoemID(),drawingPanelRetainDataFragment.getPreviouslySavedPoemName());
            }
        }
    }

    private void loadDemoIfItIsRunning(SharedPreferences sharedPreferences) {
        String demoRunning = sharedPreferences.getString(ApplicationContract.DEMO,"");
        if(!demoRunning.equals("")) {
            addDemoFragment(demoRunning);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        FragmentManager fragmentManager = getFragmentManager();
        SharedPreferences sharedPreferences = getPreferences(0);
        setReferencesToFragments(fragmentManager);
        if(sharedPreferences.getBoolean(ApplicationContract.FIRST_LAUNCH,true)) {
            handleFirstLaunch(sharedPreferences);
        } else {
            gameState = new GameState(this, drawingPanelFragment.drawingPanel(),false);
        }
        loadMagnetsOntoCanvas(fragmentManager);
        loadDemoIfItIsRunning(sharedPreferences);
    }


    public void onDrawerMagnetClicked(String clickedMagnetText, int packID) {
        if(demoFragment == null) gameState.updatePackSize(drawingPanelFragment.setWord(clickedMagnetText,packID),drawerFragment.getCurrentPack(),drawerFragment.getCurrentPackName());
        else drawingPanelFragment.setWord(clickedMagnetText,packID);
    }

    public void onSpinnerClicked() {
        if(demoFragment != null) demoFragment.runDemo(DemoFragment.DemoPart.PACKS_SELECTED);
    }

    private void setFirstLaunchToFalse(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(ApplicationContract.FIRST_LAUNCH, false);
        sharedPreferencesEditor.apply();
    }

    private void loadValuesIntoDatabase() {
        new loadTextFilesIntoPacksAsyncTask().execute();
    }

    private void saveBitmapToSDCard(File poemDirectory) {
        File newPoemFile = SD_CardHelper.appendNumberToFilenameIfExists(getDate(),poemDirectory);
        Bitmap screenshot = ScreenshotCreator.createScreenshot(getSupportFragmentManager().findFragmentById(R.id.the_canvas).getView());
        if(SD_CardHelper.saveBitmap(newPoemFile,screenshot)) {
            loadSuccessfulSaveDialog(screenshot,newPoemFile);
        } // else load an unsuccessful dialog?
    }

    private void loadSuccessfulSaveDialog(final Bitmap screenshot, File dateWithSeconds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(helperContext);
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because it's going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_save_to_sd,null);
        ((TextView) view.findViewById(R.id.message)).setText("Your poem has been saved as " + dateWithSeconds.getName() + " in the 'Pictures/poems' directory on your sd card");
        ((ImageView) view.findViewById(R.id.screenshot)).setImageBitmap(screenshot);
        builder.setTitle("Your Screenshot")
            .setView(view)
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        builder.create();
        builder.show();
    }

    private void loadNoSDCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(helperContext);
        builder.setTitle("No sd card")
                .setMessage("Your poem cannot be saved to an sd card because you do not have an sd card mounted on your device.")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //saveBitmapToSDCard(poemDirectory,filenameInput.getText().toString());
                    }
                });
        builder.create();
        builder.show();
    }
    private void setupOverflowButton() {
        final ImageView overflowButton = (ImageView) findViewById(R.id.overflow_button);
        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, overflowButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_clear:
                                AlertDialog.Builder builder = new AlertDialog.Builder(helperContext);
                                builder.setTitle("Clear the board")
                                        .setMessage("Clicking yes will clear all of your words off of the board. Make sure to save any changes before continuing.")
                                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                drawingPanelFragment.clearMagnets();
                                                gameState.setSavedPoemState(false);
                                            }
                                        })
                                        .setNegativeButton("no thanks", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // User cancelled the dialog
                                            }
                                        });
                                builder.create();
                                builder.show();
                                break;
                            case R.id.action_save_to_sd:
                                /* Checks if external storage is available for read and write */
                                if (SD_CardHelper.sdCardIsMounted()) {
                                    File poemDirectory = SD_CardHelper.getAlbumStorageDirectory("poems");
                                    if(poemDirectory.exists() && poemDirectory.isDirectory()) {
                                        saveBitmapToSDCard(poemDirectory);
                                    } else {
                                        loadNoSDCardDialog();
                                    }
                                }
                                break;
                            case R.id.action_demo:
                                //demo = new Demo(MainActivity.this,helperContext);
                                //demo.runDemoIntro();
                                if(demoFragment == null) addDemoFragment(DemoFragment.DemoPart.START.toString());
                                else {
                                    demoComplete();
                                    addDemoFragment(DemoFragment.DemoPart.START.toString());
                                }
                                break;
                            case R.id.action_rate:
                                String packageName = helperContext.getPackageName();
                                Uri googleMarketApplicationUri = Uri.parse(ApplicationContract.googleMarketApplication + packageName);
                                Intent goToGoogleMarketApplication = new Intent(Intent.ACTION_VIEW, googleMarketApplicationUri);
                                try {
                                    startActivity(goToGoogleMarketApplication);
                                } catch (ActivityNotFoundException googleMarketApplicationNotFound) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ApplicationContract.googleMarketWebAddress + packageName)));
                                }
                                break;
                        }

                        return true;
                    }
                });
                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method
    }



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setSystemVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.hide();
        setSystemVisibility();
        setContentView(R.layout.activity_main);
        setupOverflowButton();
    }

    public void loadMenu(View view) {
        if(demoFragment != null ) demoFragment.runDemo(DemoFragment.DemoPart.BUTTONS_CLICKED); // the demo is running and a button has been clicked
        Intent mainMenuIntent = new Intent(this, MainMenu.class);
        startActivity(mainMenuIntent);
    }

    public void loadOverflow(View view) {
        //load overflow dropdown list
    }

    private String getDate() {
        return (new SimpleDateFormat("MM-dd-yyyy")).format(new Date());
    }


    private void insertPoemIntoDatabase(String title, ArrayList<Magnet> magnets, boolean update) {
        if(update) {
            gameState.updateAnExistingPoem(getDate(), magnets);
        } else {
            gameState.insertANewPoem(title,getDate(), magnets);
        }
    }


    /* Update the auto-save poem information during onPause using the gameState.setCurrentPoemForAutoSave method. */
    @Override
    protected void onPause() {
        super.onPause();
        if(demoFragment != null) { // if the demo is going on, set current demo fragment
            SharedPreferences sharedPreferences = getPreferences(0);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(ApplicationContract.DEMO, demoFragment.getCurrentDemoPart().toString());


            //sharedPreferencesEditor.remove(ApplicationContract.DEMO);

            sharedPreferencesEditor.apply();
            demoComplete();
        } else {
            SharedPreferences sharedPreferences = getPreferences(0);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(ApplicationContract.DEMO, "");
            //sharedPreferencesEditor.remove(ApplicationContract.DEMO);
            sharedPreferencesEditor.apply();
        }
        gameState.setCurrentPoemForAutoSave(drawingPanelFragment.getPoem());
        getIntent().removeExtra("poem_name");
        getIntent().removeExtra("poem_id");

    }


    /* If the user shares a poem, a temporary bitmap is created in the cache directory. Delete that bitmap when the activity completes.*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new File(this.getCacheDir(), "tempShareBitmap.jpg").delete();
        drawingPanelRetainDataFragment.setMagnetData(drawingPanelFragment.getPoem(),drawingPanelFragment.drawingPanel().getSavedPoemState(),drawingPanelFragment.drawingPanel().getSavedPoemId(),drawingPanelFragment.drawingPanel().getSavedPoemName());
        //unbindFromMediaMusicService();
    }

    private void createExistingPoemSaveAlertDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("loadedPoemName",drawingPanelFragment.drawingPanel().getSavedPoemName());
        ExistingPoemSaveAlertDialog existingPoemSaveAlertDialog = new ExistingPoemSaveAlertDialog();
        existingPoemSaveAlertDialog.setArguments(bundle);
        existingPoemSaveAlertDialog.show(getFragmentManager(),null);
    }

    private void createNewPoemSaveAlertDialog() {
        (new NewPoemSaveAlertDialog()).show(getFragmentManager(), null);
    }

    public void loadSave(View view) {
        if(demoFragment != null ) demoFragment.runDemo(DemoFragment.DemoPart.BUTTONS_CLICKED); // the demo is running and a button has been clicked
        if(drawingPanelFragment.drawingPanel().getSavedPoemState()) {
            createExistingPoemSaveAlertDialog();
        } else {
            createNewPoemSaveAlertDialog();
        }
    }

    private void createShareIntent(Uri bitmapUri) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_extra_text));
        sendIntent.putExtra("sms_body", getString(R.string.share_extra_sms_body));
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_intent_title)));
        gameState.shareOptionLoaded();
    }

    private class ShareTask extends AsyncTask<Void, Void, File> {
        protected File doInBackground(Void... nothing) {
            Bitmap screenshot = ScreenshotCreator.createScreenshotWithWatermark(getSupportFragmentManager().findFragmentById(R.id.the_canvas).getView(), helperContext, R.drawable.watermark);
            File file = new File(helperContext.getCacheDir(), "tempShareBitmap.jpg");
            try {
                file.createNewFile();
                file.setReadable(true,false);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                screenshot.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (java.io.IOException IOException) {
                IOException.printStackTrace();
            }
            return file;
        }

        protected void onPostExecute(File file) {
            createShareIntent(Uri.fromFile(file));

        }
    }

    public void loadShareDialog(View view) {
        if(demoFragment != null ) demoFragment.runDemo(DemoFragment.DemoPart.BUTTONS_CLICKED); // the demo is running and a button has been clicked
        (new ShareTask()).execute();


    }

    public void openDrawer(View view) {
        ((DrawerLayout) findViewById(R.id.pager)).openDrawer(GravityCompat.START);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public static class CanvasHolderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.canvas_fragment, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
    }

    private class loadTextFilesIntoPacksAsyncTask extends AsyncTask<Void, Void, ArrayList<Pack>> {
        protected ArrayList<Pack> doInBackground(Void... params) {
            return DataLoaderHelper.loadMagnetText(helperContext);
        }
        protected void onPostExecute(ArrayList<Pack> packs) {
            for(Pack pack : packs) {
                gameState.insertPacksFromTextFiles(pack);
            }
        }
    }

    /** new media service stuff **/
    @Override
    protected void onStart() {
        super.onStart();
        mediaServiceHelper.bindToMediaMusicService();
    }


    @Override
    protected void onStop() {
        super.onStop();
       if(!isChangingConfigurations()) {
           mediaServiceHelper.unbindFromMediaMusicService();
       }
    }

}





