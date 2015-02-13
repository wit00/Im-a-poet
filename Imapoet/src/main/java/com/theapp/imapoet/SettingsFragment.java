package com.theapp.imapoet;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 *
 */
/**
 */
public class SettingsFragment extends android.support.v4.app.Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SettingsListViewAdapter settingsListViewAdapter;
    private ListView listView;
    private SettingsFragmentAsyncQueryHandler settingsFragmentAsyncQueryHandler;
    private boolean music = true;
    private boolean soundEffect = true;
    private IabHelper iabHelper;
    private ArrayList<String> skuList = new ArrayList<String>();
    private InAppPurchaseFragment.InAppPurchaseListener inAppPurchaseListener;


    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {}

    private void initializeSkuList() {
        try {
            Collections.addAll(skuList, getActivity().getAssets().list("inAppPurchasePacks"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        inAppPurchaseListener = null;
        if(iabHelper != null) iabHelper.dispose();
        iabHelper = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            inAppPurchaseListener = (InAppPurchaseFragment.InAppPurchaseListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    private void displayInAppPurchaseSetupFailureMessage() {
        String message = "In app purchasing is not working at this time. Sorry for the inconvenience.";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        (builder.create()).show();
    }

    private void getProductsPurchased() {
        initializeSkuList();
        String base64EncodedPublicKey = ApplicationContract.base64 + ApplicationContract.encoded + ApplicationContract.Public + ApplicationContract.key;
        // compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(getActivity(), base64EncodedPublicKey);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isFailure()) {
                    displayInAppPurchaseSetupFailureMessage();
                } else {
                    iabHelper.queryInventoryAsync(productsPurchasedInventoryListener);
                }
            }
        });

    }

    private IabHelper.QueryInventoryFinishedListener productsPurchasedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (result.isFailure()) {
                displayInAppPurchaseSetupFailureMessage();
            }
            else {
                ArrayList<String> purchases = new ArrayList<String>();
                for(String sku : skuList) {
                    if(inventory.hasPurchase(sku)) {
                        purchases.add(sku);
                    }
                }
                inAppPurchaseListener.resetInAppPurchases(purchases.toArray(new String[purchases.size()]));
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu_settings, container, false);
        listView = (ListView) rootView.findViewById(R.id.settingsListView);
        getLoaderManager().initLoader(0,null,this);
        settingsFragmentAsyncQueryHandler = new SettingsFragmentAsyncQueryHandler(getActivity().getContentResolver(),this);
        return rootView;
    }

    private static class SettingsFragmentAsyncQueryHandler extends AsyncQueryHandler {
        private final WeakReference<SettingsFragment> settingsFragmentWeakReference;

        public SettingsFragmentAsyncQueryHandler(ContentResolver cr, SettingsFragment settingsFragment) {
            super(cr);
            settingsFragmentWeakReference = new WeakReference<SettingsFragment>(settingsFragment);
        }
        @Override
        protected  void onInsertComplete(int token, Object cookie, Uri uri) {
            SettingsFragment settingsFragment = settingsFragmentWeakReference.get();
            if(settingsFragment != null) {
                settingsFragment.settingsListViewAdapter = new SettingsListViewAdapter(settingsFragment.getActivity(),true,true);
                settingsFragment.restartLoader();
            }
        }
    }

    private void insertInitialValues() {
        ContentValues initialSettingsValues = new ContentValues();
        initialSettingsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS,1);
        initialSettingsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC,1);
        settingsFragmentAsyncQueryHandler.startInsert(0,null,ApplicationContract.insertSettings_URI,initialSettingsValues);
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0,null,this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC,
                MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS
        };
        return new CursorLoader(getActivity(),ApplicationContract.getSettings_URI, projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC + MagnetDatabaseContract.MagnetEntry.ASC);
    }



    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() == 0) {
            insertInitialValues();
        } else {
            cursor.moveToFirst();
            music = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC)) > 0;
            soundEffect = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS)) > 0;
            if(settingsListViewAdapter != null) {
                settingsListViewAdapter.setSounds(music, soundEffect);
                settingsListViewAdapter.notifyDataSetChanged();
            } else {
                settingsListViewAdapter = new SettingsListViewAdapter(getActivity(),music,soundEffect);
            }
        }
        listView.setAdapter(settingsListViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        soundEffect = !soundEffect;
                        updateSound(soundEffect, MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS);
                        break;
                    case 3:
                        music = !music;
                        updateSound(music,MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC);

                        break;
                    case 5:
                        String packageName = getActivity().getApplicationContext().getPackageName();
                        Uri googleMarketApplicationUri = Uri.parse(ApplicationContract.googleMarketApplication + packageName);
                        Intent goToGoogleMarketApplication = new Intent(Intent.ACTION_VIEW, googleMarketApplicationUri);
                        try {
                            startActivity(goToGoogleMarketApplication);
                        } catch (ActivityNotFoundException googleMarketApplicationNotFound) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ApplicationContract.googleMarketWebAddress + packageName)));
                        }
                    case 7:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ApplicationContract.supportEmailAddress});
                        intent.putExtra(Intent.EXTRA_TEXT, "send us an email at " + ApplicationContract.supportEmailAddress);
                        startActivity(Intent.createChooser(intent, "Send Us an Email"));
                        break;
                    case 9:
                        getProductsPurchased();
                        break;
                    case 11:
                        Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);
                        mainActivityIntent.putExtra("updatePacks",true);
                        startActivity(mainActivityIntent);
                        break;
                    default:
                        break;
                }

            }
        });
    }

    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.
    public void onLoaderReset(Loader<Cursor> loader) {
        listView.setAdapter(null);
    }

    private void updateSound(Boolean sound, String column) {
        ContentValues settingsValues = new ContentValues();
        settingsValues.put(column, (sound) ? 1 : 0);
        settingsFragmentAsyncQueryHandler.startUpdate((sound) ? 1 : 0, column, ApplicationContract.updateSettings_URI, settingsValues, "", new String[]{});
    }
}
