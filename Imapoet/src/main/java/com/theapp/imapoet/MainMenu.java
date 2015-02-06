package com.theapp.imapoet;


import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.ArrayList;


public class MainMenu extends ActionBarActivity implements ActionBar.TabListener, InAppPurchaseFragment.InAppPurchaseListener {
    private SectionsPagerAdapter sectionsPagerAdapter; // provides the fragments for the sections
    private ViewPager viewPager; // hosts the section contents
    private MainMenuAsyncQueryHandler mainMenuAsyncQueryHandler;
    private MediaServiceHelper mediaServiceHelper = new MediaServiceHelper(this);


    public void inAppPurchaseClicked(String packName) {
        // update pack value to show it is bought
        (new UpdateAvailabilityToTrueTask()).execute(packName);
    }

    public void resetInAppPurchases(String[] packNames) {
        (new UpdateMultiplePacksAvailabilityToTrueTask()).execute(packNames);
    }

    public void checkInAppPurchases(String[] packNames) {
        (new CheckIfAvailableTask()).execute(packNames);
    }

    private class CheckIfAvailableTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... googleInAppPurchasePacks) {
            Cursor databaseInAppPurchasedPacksCursor = getContentResolver().query(ApplicationContract.getPacks_URI,new String[]{MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME}, MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE + " = 1",null,null);
            ArrayList<String> databaseInAppPurchasedPacks = new ArrayList<String>();
            while(databaseInAppPurchasedPacksCursor.moveToNext()) {
                databaseInAppPurchasedPacks.add(databaseInAppPurchasedPacksCursor.getString(databaseInAppPurchasedPacksCursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME)));
            }
            for(String googleInAppPurchasePack : googleInAppPurchasePacks) {
                if(!databaseInAppPurchasedPacks.contains(googleInAppPurchasePack)) {
                    updateAPackToTrue(googleInAppPurchasePack);
                }
            }
            if(!databaseInAppPurchasedPacksCursor.isClosed()) databaseInAppPurchasedPacksCursor.close();
            return null;
        }
    }

    private class UpdateAvailabilityToTrueTask extends AsyncTask<String, Void, Integer> {
        private String packName;
        protected Integer doInBackground(String... packNames) {
            packName = packNames[0];
            return updateAPackToTrue(packName);
        }
        protected void onPostExecute(Integer result) {
            if(result == 1) {
                displayYourPurchaseHasBeenSuccessfulDialog();
            } else {
               displayProblemDialog(packName);
            }
        }
    }

    // returns the number of rows affected
    private int updateAPackToTrue(String packName) {
        ContentValues updatedPackValues = new ContentValues();
        updatedPackValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE,true);
        return getContentResolver().update(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/pack"),updatedPackValues,MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME + " = " + "'" + packName + "'", null);
    }

    private class UpdateMultiplePacksAvailabilityToTrueTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... packNames) {
            for(String packName : packNames) {
                updateAPackToTrue(packName);
            }
            return null;
        }
        protected void onPostExecute(Void nothing) {
            displaySuccessfulSettingsResetDialog();
        }
    }

    private void displayYourPurchaseHasBeenSuccessfulDialog() {
        String message = "Excellent! Your purchase has gone through. You can now use your new pack!";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }});
        (builder.create()).show();
    }
    private void displaySuccessfulSettingsResetDialog() {
        String message = "Excellent! Your in app purchases have been reset.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }});
        (builder.create()).show();
    }
    /*private void displayProblemWithSettingsUpdateDialog() {
        String message = "Unfortunately, something went wrong loading this settings update. Please try again. If you continue to have a problem with this, please email us using the Contact Us button in the Settings tab of the menu.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // do nothing
                    }
                });
        (builder.create()).show();
    }*/
    private void displayProblemDialog(final String packName) {
        String message = "Unfortunately, something went wrong loading your new purchase into our system. Press 'Try Again' to try loading it again. If you continue to have a problem with this, please email us using the Contact Us button in the Settings tab of the menu.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateAPackToTrue(packName);
                    }})
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // do nothing
                    }
                });
        (builder.create()).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InAppPurchaseFragment inAppPurchaseFragment  = (InAppPurchaseFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
        if(inAppPurchaseFragment != null) {
            inAppPurchaseFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static class MainMenuAsyncQueryHandler extends AsyncQueryHandler {
        //private final WeakReference<MainMenu> mainMenuWeakReference;

        public MainMenuAsyncQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
            //mainMenuWeakReference = new WeakReference<MainMenu>(mainMenu);
        }
        /*@Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            if(token == 0) {
                // move the pack from inAppPurchasePacks to purchasedInAppPurchasePacks
                MainMenu mainMenu = mainMenuWeakReference.get();
                if(result == 1) {
                    if(mainMenu != null) mainMenu.displayYourPurchaseHasBeenSuccessfulDialog();
                } else {
                    if(mainMenu != null) mainMenu.displayProblemDialog((String) cookie);
                }
            }

        }*/

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch (token) {
                case 1:
                    startDelete(2, cookie, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/poem"),
                            MagnetDatabaseContract.MagnetEntry._ID + " = " + cookie, null);
                    break;
                case 2:
                    startDelete(0, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/poem/detail"),
                            MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID + " = " + cookie, null);
                    break;


            }
        }
    }

    public void loadMyWebsite(View view) {
        Uri uri = Uri.parse("http://www.wit00.com");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void loadMyTwitter(View view) {
        Uri uri = Uri.parse("https://twitter.com/swhitneypowell");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void loadMusicWebsite(View view) {
        Uri uri = Uri.parse("http://www.incompetech.com");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void loadMusicTwitter(View view) {
        Uri uri = Uri.parse("https://twitter.com/kmacleod");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setUpActionBarAndStuff();
        //createAsyncQueryHandler();
        mainMenuAsyncQueryHandler = new MainMenuAsyncQueryHandler(getContentResolver());
    }


    public void setUpActionBarAndStuff() {
        final ActionBar actionBar = setUpNavigationModeActionBar();
        setUpViewPager(actionBar);
        // For each of the sections in the app, add a tab to the action bar and give it
        // the tile specified in the adapter.
        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(sectionsPagerAdapter.getPageTitle(i))
                                    // Specify this activity as the callback listener, since it implements the
                                    // TabListener interface.
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        //return id == R.id.action_settings || super.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a SettingsFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return AwardsFragment.newInstance();
                case 1:
                    return SavedPoemsFragment.newInstance();
                case 2:
                    return InAppPurchaseFragment.newInstance();
                case 3:
                    return SettingsFragment.newInstance();
                case 4:
                    return StatisticsFragment.newInstance();
                case 5:
                    return CreditsFragment.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Awards";
                case 1:
                    return "Saved Poems";
                case 2:
                    return "Store";
                case 3:
                    return "Settings";
                case 4:
                    return "Statistics";
                case 5:
                    return "Credits";
            }
            return null;
        }
    }


    // When the given tab is selected, switch to the corresponding page in
    // the ViewPager.
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
    /** Begin Helper Functions **/

    /* Sets up an action bar with a tab navigation mode and
       returns the new action bar.*/
    private ActionBar setUpNavigationModeActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        return actionBar;
    }

    /* Creates the adapter that will return a fragment for each of the three
       primary sections of the activity, and attaches it to the viewPager.
       Also, sets the viewPager's setOnPageChangeListener to select the corresponding
       tab when swiping between the different sections of the viewPager object.
     */
    private ViewPager setUpViewPager(final ActionBar actionBar) {
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        return viewPager;
    }

    /** End Helper Functions **/

    public void loadDeletePoem(View view) {
        final Object poemID = view.findViewById(R.id.delete_poem_button).getTag();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Your Poem?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainMenuAsyncQueryHandler.startDelete(1, poemID, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/currentPoem"),
                                MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID + " = " + poemID, null);
                    }
                })
                .setNegativeButton("no thanks", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        builder.create();
        builder.show();
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