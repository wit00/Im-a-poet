package com.theapp.imapoet;



import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.example.android.trivialdrivesample.util.SkuDetails;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements AsyncTaskRetainDataFragment.AsyncTaskListener,
        NewPoemSaveAlertDialog.NewPoemSaveAlertDialogListener, ExistingPoemSaveAlertDialog.ExistingPoemDialogListener, DrawingPanel.CanvasListener, DemoFragment.DemoListener, AdapterView.OnItemSelectedListener,LoaderManager.LoaderCallbacks<Cursor>  {
    private DemoFragment demoFragment = null;
    private Context helperContext = this;
    private DrawingPanelFragment drawingPanelFragment;
    private GameState gameState;
    private MediaServiceHelper mediaServiceHelper = new MediaServiceHelper(this);
    private DrawingPanelRetainDataFragment drawingPanelRetainDataFragment;
    protected DrawerLayout drawerLayout;
    private GridView gridView;
    private DrawerMagnetsAdapter drawerMagnetsAdapter;
    //private BasicDrawerMagnetsAdapter drawerMagnetsAdapter;
    private DrawerSpinnerAdapter drawerSpinnerAdapter;
    private int spinnerPosition = 0;
    private boolean spinnerListenerSetUp = false;
    private IabHelper iabHelper;
    private ArrayList<String> skuList = new ArrayList<String>();
    private AsyncQueryHandler asyncQueryHandler;
    private boolean applicationIsRunning = false;


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
        //fragmentTransaction.commitAllowingStateLoss();
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




    private void loadMagnetsOntoCanvas(FragmentManager fragmentManager) {
        Bundle extras = getIntent().getExtras();
        if(extras != null) { // we are loading from save
            gameState.loadSavedMagnets(extras);
        } else { // we are not loading from save
            // it's an orientation change, so just load the magnets from data retaining fragment
//System.out.println("magnet: loading from drawing panel retain data fragment");
            if(drawingPanelRetainDataFragment == null) { // this is not an orientation change because drawingPanelRetainFragment is null
                if(drawingPanelFragment.isAdded()) drawingPanelFragment.loadMagnets();
                else {
                    drawingPanelFragment = (DrawingPanelFragment) getSupportFragmentManager().findFragmentById(R.id.the_canvas);
                    //drawingPanelFragment.loadMagnets();
                }
                drawingPanelRetainDataFragment = drawingPanelRetainDataFragment.newInstance();
                fragmentManager.beginTransaction().add(drawingPanelRetainDataFragment,"drawingPanelData").commit();
            } else
                drawingPanelFragment.loadMagnets(drawingPanelRetainDataFragment.getMagnets(), drawingPanelRetainDataFragment.getPreviouslySavedPoem(), drawingPanelRetainDataFragment.getPreviouslySavedPoemID(), drawingPanelRetainDataFragment.getPreviouslySavedPoemName(), drawingPanelRetainDataFragment.getScaleFactor(), drawingPanelRetainDataFragment.getScalePivotX(), drawingPanelRetainDataFragment.getScalePivotY(), drawingPanelRetainDataFragment.getScrollXOffset(), drawingPanelRetainDataFragment.getScrollYOffset());
        }
    }

    private void loadDemoIfItIsRunning(SharedPreferences sharedPreferences) {
        String demoRunning = sharedPreferences.getString(ApplicationContract.DEMO,"");
        if(!demoRunning.equals("")) {
            addDemoFragment(demoRunning);
        }
    }





    public void onDrawerMagnetClicked(String clickedMagnetText, int packID) {
        //if(demoFragment == null) gameState.updatePackSize(drawingPanelFragment.setWord(clickedMagnetText,packID),drawerFragment.getCurrentPack(),drawerFragment.getCurrentPackName());
        if(demoFragment == null) gameState.updatePackSize(drawingPanelFragment.setWord(clickedMagnetText,packID),getCurrentPack(),getCurrentPackName());
        else drawingPanelFragment.setWord(clickedMagnetText,packID);
    }

    public void toggleDrag() {
        if(demoFragment != null) demoFragment.runDemo(DemoFragment.DemoPart.SCALE_AND_DRAG);
    }

    public void onSpinnerClicked() {
        if(demoFragment != null) demoFragment.runDemo(DemoFragment.DemoPart.PACKS_SELECTED);
    }

    private void setFirstLaunchToFalse(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(ApplicationContract.FIRST_LAUNCH, false);
        sharedPreferencesEditor.apply();
    }

    private void updateSharedPreferencesVersionNumber(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putInt("version",ApplicationContract.VERSION);
        sharedPreferencesEditor.apply();
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
                            case R.id.action_slide:
                                drawingPanelFragment.drawingPanel().toggleDoubleTap();
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

    private void initializeIabHelper() {
        String base64EncodedPublicKey = ApplicationContract.base64 + ApplicationContract.encoded + ApplicationContract.Public + ApplicationContract.key;
        initializeSkuList();
        // compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(this, base64EncodedPublicKey);

        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isFailure()) {
                    displayInAppPurchaseSetupFailureMessage();
                }
            }
        });
    }

    private void setupDrawerSpinnerAndDrawerMagnetGrid() {
        drawerSpinnerAdapter = new DrawerSpinnerAdapter(this, R.layout.fragment_drawer_spinner_layout, null, new String[]{}, new int[]{});
        drawerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drawerMagnetsAdapter = new DrawerMagnetsAdapter(this, null, new String[]{}, new int[]{});

        setupMagnetDeckSpinner();
        setupGridView();
        setGridViewListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getReferencesToFragmentsAndHandlePackUpdates();
        loadDemoIfItIsRunning(getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.hide();
        setSystemVisibility();
        setContentView(R.layout.activity_main);
        setupOverflowButton();
        createAsyncQueryHandler();
        initializeIabHelper();
        setupDrawerSpinnerAndDrawerMagnetGrid();
    }


    private void getReferencesToFragmentsAndHandlePackUpdates() {
        FragmentManager fragmentManager = getFragmentManager();
        SharedPreferences sharedPreferences = getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0);
        drawingPanelRetainDataFragment = (DrawingPanelRetainDataFragment)fragmentManager.findFragmentByTag("drawingPanelData");
        drawingPanelFragment = (DrawingPanelFragment) getSupportFragmentManager().findFragmentById(R.id.the_canvas);
        drawerLayout = ((DrawerLayout) findViewById(R.id.pager));
        applicationIsRunning = true;
        getSupportLoaderManager().initLoader(0,null,this);
        ((DrawerLayout)findViewById(R.id.pager)).setDrawerListener(new MagnetDrawerListener(this));

        if(sharedPreferences.getBoolean(ApplicationContract.FIRST_LAUNCH,true)) {
            findViewById(R.id.big_loading_spinner).setVisibility(View.VISIBLE);
            findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
            if(fragmentManager.findFragmentByTag("asyncData") == null) {
                displayFirstLoadDialog();
                AsyncTaskRetainDataFragment asyncTaskRetainDataFragment = AsyncTaskRetainDataFragment.newInstance();
                fragmentManager.beginTransaction().add(asyncTaskRetainDataFragment,"asyncData").commit();
                asyncTaskRetainDataFragment.performFirstRunAsyncTask();
            } else {
                ((AsyncTaskRetainDataFragment)(fragmentManager.findFragmentByTag("asyncData"))).signUpForFirstRunCompletionNotification(this);
            }
            //new loadTextFilesIntoPacksAsyncTask().execute();
            gameState = new GameState(drawingPanelFragment.drawingPanel(),true,this);
            updateSharedPreferencesVersionNumber(sharedPreferences);
        } else {
            Bundle extras = getIntent().getExtras();
            boolean loadFromSettings = false;
            if(extras != null) {
                loadFromSettings = getIntent().getExtras().getBoolean("updatePacks");
            }
            if(sharedPreferences.getInt("version",-1) != ApplicationContract.VERSION  || loadFromSettings) {
                if(extras != null) getIntent().removeExtra("updatePacks");
                findViewById(R.id.big_loading_spinner).setVisibility(View.VISIBLE);
                findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
                if(fragmentManager.findFragmentByTag("asyncData") == null) {
                    displayUpdatedSystemDialog();
                    AsyncTaskRetainDataFragment asyncTaskRetainDataFragment = AsyncTaskRetainDataFragment.newInstance();
                    fragmentManager.beginTransaction().add(asyncTaskRetainDataFragment,"asyncData").commit();
                    asyncTaskRetainDataFragment.performUpdateAsyncTask();
                } else {
                    ((AsyncTaskRetainDataFragment)(fragmentManager.findFragmentByTag("asyncData"))).signUpForUpdateCompletionNotification(this);
                }
            }

            gameState = new GameState(drawingPanelFragment.drawingPanel(),false,this);
        }
        loadMagnetsOntoCanvas(fragmentManager);
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
        applicationIsRunning = false;
        if(demoFragment != null) { // if the demo is going on, set current demo fragment
            SharedPreferences sharedPreferences = getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(ApplicationContract.DEMO, demoFragment.getCurrentDemoPart().toString());
            sharedPreferencesEditor.apply();
            demoComplete();
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(ApplicationContract.DEMO, "");
            sharedPreferencesEditor.apply();
        }
        gameState.setCurrentPoemForAutoSave(drawingPanelFragment.getPoem());
        getIntent().removeExtra("poem_name");
        getIntent().removeExtra("poem_id");
        if (drawingPanelRetainDataFragment != null) {drawingPanelRetainDataFragment.setMagnetData(drawingPanelFragment.getPoem(),drawingPanelFragment.drawingPanel().getSavedPoemState(),drawingPanelFragment.drawingPanel().getSavedPoemId(),drawingPanelFragment.drawingPanel().getSavedPoemName(),drawingPanelFragment.drawingPanel().scaleFactor(),drawingPanelFragment.drawingPanel().scalePivotX(),drawingPanelFragment.drawingPanel().scalePivotY(),drawingPanelFragment.drawingPanel().getScrollXOffset(), drawingPanelFragment.drawingPanel().getScrollYOffset());}

    }

    /* If the user shares a poem, a temporary bitmap is created in the cache directory. Delete that bitmap when the activity completes.*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new File(this.getCacheDir(), "tempShareBitmap.jpg").delete();
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

    private void fadeOutView(final View view) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setRepeatCount(0);
        view.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    public void firstRunAsyncLoadingComplete() {
        fadeOutView(findViewById(R.id.loading_spinner));
        fadeOutView(findViewById(R.id.big_loading_spinner));
        setFirstLaunchToFalse(getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0));
        if(isFinishing() || !applicationIsRunning && demoFragment == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(ApplicationContract.DEMO, DemoFragment.DemoPart.START.toString());
            sharedPreferencesEditor.apply();
        } else {
            addDemoFragment(DemoFragment.DemoPart.START.toString());
        }
    }

    public void updateAsyncLoadingComplete() {
        fadeOutView(findViewById(R.id.loading_spinner));
        fadeOutView(findViewById(R.id.big_loading_spinner));
        updateSharedPreferencesVersionNumber(getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0));
    }

    private void initLoader() {
        getSupportLoaderManager().initLoader(0,null,this);
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


    /* Drawer fragment stuff*/

    private void initializeSkuList() {
        try {
            Collections.addAll(skuList, this.getAssets().list("inAppPurchasePacks"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void updatePackAvailability(String packName, boolean isAvailable) {
        ContentValues updatedPackValues = new ContentValues();
        updatedPackValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE,isAvailable);
        asyncQueryHandler.startUpdate(0, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/pack"), updatedPackValues, MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME + " = " + "'" + packName + "'", null);
    }

    private void displayYourPurchaseHasBeenSuccessfulDialog() {
        String message = "Excellent! Your purchase has gone through. You can now use your new pack!";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadSpinner();

                    }});
        (builder.create()).show();
    }

    private void displayProblemDialog(final String packName) {
        String message = "Unfortunately, something went wrong loading your new purchase into our system. Press 'Try Again' to try loading it again. If you continue to have a problem with this, please email us using the Contact Us button in the Settings tab of the menu.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updatePackAvailability(packName,true);
                    }})
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // do nothing
                    }
                });
        (builder.create()).show();

    }

    private void createAsyncQueryHandler() {
        asyncQueryHandler = new AsyncQueryHandler(this.getContentResolver()) {
            @Override
            protected void onUpdateComplete(int token, Object cookie, int result) {
                if (token == 0) {
                    // move the pack from inAppPurchasePacks to purchasedInAppPurchasePacks
                    if (result == 1) {
                        // if(DataLoaderHelper.copyFile((String) cookie, "inAppPurchasePacks", "purchasedInAppPurchasePacks", getApplicationContext())) {
                        // if has succeeded
                        displayYourPurchaseHasBeenSuccessfulDialog();
                        //displayYourPurchaseHasBeenSuccessfulDialog((String) cookie);
                    } else {
                        // io exception
                        // displayProblemDialog("love.txt");
                        displayProblemDialog((String) cookie);
                    }
                }

            }
        };
    }


    private void setGridViewListener() {
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                view.startDrag(null, new View.DragShadowBuilder(view), null, 0);
                if(drawerLayout != null) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout = (DrawerLayout) findViewById(R.id.pager);
                    drawerLayout.closeDrawers();
                }
                Cursor cursor = (Cursor) drawerMagnetsAdapter.getItem(position);
                onDrawerMagnetClicked(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT)),cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID)));
            return true;

            }

        });

    }


    private void displayInAppPurchaseSetupFailureMessage() {
        String message = "In app purchasing is not working at this time. Sorry for the inconvenience.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        (builder.create()).show();
    }

    private void setupMagnetDeckSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.sets_spinner);
        spinner.setAdapter(drawerSpinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void setupGridView() {
        gridView = (GridView) findViewById(R.id.gridview);
        //drawerMagnetsAdapter = new DrawerMagnetsAdapter(getActivity(), null, new String[]{}, new int[]{});
        gridView.setAdapter(drawerMagnetsAdapter);
    }


    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure()) {
                displayUnsuccessfulPurchaseDialog();
            }
            else {
                updatePackAvailability(purchase.getSku(), true);
                //);
            }

        }
    };

    private void displayUnsuccessfulPurchaseDialog() {
        String message = "Unfortunately something is wrong with the in-app purchase system, and you cannot buy this item at this time. We're sorry for the inconvenience.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //purchaseInAppProduct(inAppPurchase.productId());
                        // tempPurchaseInAppProduct();
                    }
                });
        (builder.create()).show();
    }

    private void displaySomethingIsWrongWithInAppPurchaseDialog() {
        String message = "This pack requires an in-app purchase. Unfortunately something is wrong with the in-app purchase system at this time. We're sorry for the inconvenience.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //purchaseInAppProduct(inAppPurchase.productId());
                        // tempPurchaseInAppProduct();
                    }
                });
        (builder.create()).show();
    }
    private void purchaseInAppProduct(String productSKU) {
        iabHelper.launchPurchaseFlow(this, productSKU, 1 ,purchaseFinishedListener,null);
    }

    private void displayUpdatedSystemDialog() {
        String message = "Your system is updating. It might take a couple of minutes until all of your words and packs are available.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        (builder.create()).show();
    }

    private void displayFirstLoadDialog () {
        String message = "Welcome to Be a poet! We're loading your initial system data. This could take a couple of minutes.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        (builder.create()).show();
    }

    private void displayWouldYouLikeToBuyDialog(final InAppPurchase inAppPurchase) {
        String message = "This pack requires an in-app purchase. Would you like to buy " + inAppPurchase.title() + " for " + inAppPurchase.price() + " ?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        purchaseInAppProduct(inAppPurchase.productId());
                    }
                })
                .setNegativeButton("Not right now.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        ((Spinner) findViewById(R.id.sets_spinner)).setSelection(spinnerPosition);
                        loadSpinner();
                    }
                });
        AlertDialog alertDialog = (builder.create());
        (alertDialog).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ((Spinner) findViewById(R.id.sets_spinner)).setSelection(spinnerPosition);
                loadSpinner();
            }
        });
        alertDialog.show();
    }


    private void loadSpinner() {
        Cursor cursor = (Cursor) drawerSpinnerAdapter.getItem(spinnerPosition);
        Bundle bundle = new Bundle();
        bundle.putInt("id",cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
        if(spinnerListenerSetUp) {
            getSupportLoaderManager().restartLoader(1,bundle,this);
        }
    }
    private IabHelper.QueryInventoryFinishedListener
            queryForNotPurchasedPacksFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                displaySomethingIsWrongWithInAppPurchaseDialog();
            } else {
                SkuDetails skuDetails = inventory.getSkuDetails(clickedSku);
                displayWouldYouLikeToBuyDialog(new InAppPurchase(
                        skuDetails.getTitle(),
                        skuDetails.getDescription(),
                        skuDetails.getType(),
                        skuDetails.getPrice(),
                        skuDetails.getSku(),false));
            }
        }
    };


    private void resetSpinner(int position, Cursor cursor) {
        Bundle bundle = new Bundle();
        spinnerPosition = position;
        bundle.putInt("id", cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));

        if (spinnerListenerSetUp) {
            getSupportLoaderManager().destroyLoader(1);
            getSupportLoaderManager().initLoader(1, bundle, this);
            onSpinnerClicked();
        }
    }

    private String clickedSku = null;
    /** onItemSelected and onNothingSelected are parts of the interface for the magnet deck spinner **/
    public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
        Cursor cursor = (Cursor) drawerSpinnerAdapter.getItem(position);
        if(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE))==0) {
            clickedSku = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME));
            resetSpinner(position,cursor);
            iabHelper.queryInventoryAsync(true, skuList,queryForNotPurchasedPacksFinishedListener);
        } else {
            resetSpinner(position,cursor);
        }
        spinnerListenerSetUp = true;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    /* Get the current pack by returning the pack id of the first magnet in the pack. */
    public int getCurrentPack() {
        Cursor cursor = (Cursor) drawerMagnetsAdapter.getItem(0);
        return cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID));
    }

    /* Get the current pack name by returning the name of the current item in the spinner */
    public String getCurrentPackName() {
        Cursor cursor = (Cursor) drawerSpinnerAdapter.getItem(spinnerPosition);
        return cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME));
    }

    /* Implements the loader callback methods. Has two loaders with ids 0 and 1. Loader 0 loads the packs from the database and puts them into the drawerSpinnerAdapter. Loader 1 gets the magnets for a particular pack from the database and loads them into the drawerMagnetsAdapter.*/
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id==0) {
            String [] projection = {
                    MagnetDatabaseContract.MagnetEntry._ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE
            };
            return new CursorLoader(this,ApplicationContract.getPacks_URI,projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE + MagnetDatabaseContract.MagnetEntry.DESC);
        }
        else {
            String [] projection = {
                    MagnetDatabaseContract.MagnetEntry._ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT
            };
            String[] whereArguments = {Integer.toString(args.getInt("id"))};

            //return new CursorLoader(this,ApplicationContract.getMagnets_URI,projection, null, null , MagnetDatabaseContract.MagnetEntry._ID + MagnetDatabaseContract.MagnetEntry.ASC);
            return new CursorLoader(this,ApplicationContract.getMagnets_URI,projection, MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID + " =? ", whereArguments , MagnetDatabaseContract.MagnetEntry._ID + MagnetDatabaseContract.MagnetEntry.ASC);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0 && !cursor.isClosed()) {
            if(loader.getId() == 0) {
                // get all packs
                // if any packs not included here, add to db?
                cursor.moveToFirst();
                Bundle bundle = new Bundle();
                bundle.putInt("id",cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
                //if(getSupportLoaderManager().getLoader(1) != null) getSupportLoaderManager().destroyLoader(1);
                getSupportLoaderManager().initLoader(1,bundle,this);
                drawerSpinnerAdapter.swapCursor(cursor);
            }
            if(loader.getId() == 1) {
                drawerMagnetsAdapter.swapCursor(cursor);
            }
        }
    }

    // This is called when the last Cursor provided to onLoadFinished() above is about to be closed.  We need to make sure we are no longer using any of the cursors
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == 0) {
            drawerSpinnerAdapter.swapCursor(null);
        } else {
            drawerMagnetsAdapter.swapCursor(null);
        }
    }


}





