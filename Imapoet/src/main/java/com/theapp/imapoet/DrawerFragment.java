package com.theapp.imapoet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;

import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.example.android.trivialdrivesample.util.SkuDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


/* The DrawerFragment is the drawer that sits in the MainActivity and can be opened by swiping the left edge or pressing the top left button on the screen. It holds the Packs and Magnets that can be dragged onto the canvas */
public class DrawerFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener,  LoaderManager.LoaderCallbacks<Cursor>  {
    private OnFragmentInteractionListener drawerFragmentListener;
    protected DrawerLayout drawerLayout;
    private GridView gridView;
    private DrawerMagnetsAdapter drawerMagnetsAdapter;
    private DrawerSpinnerAdapter drawerSpinnerAdapter;
    private int spinnerPosition = 0;
    private boolean spinnerListenerSetUp = false;
    private IabHelper iabHelper;
    private ArrayList<String> skuList = new ArrayList<String>();
    private AsyncQueryHandler queryHandler;


    private void initializeSkuList() {
        try {
            Collections.addAll(skuList, getActivity().getAssets().list("inAppPurchasePacks"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void updatePackAvailability(String packName, boolean isAvailable) {
        ContentValues updatedPackValues = new ContentValues();
        updatedPackValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE,isAvailable);
        queryHandler.startUpdate(0,null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/pack"), updatedPackValues, MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME + " = " + "'" + packName + "'",null);
    }

    private void displayYourPurchaseHasBeenSuccessfulDialog() {
        String message = "Excellent! Your purchase has gone through. You can now use your new pack!";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }});
        (builder.create()).show();
    }

    private void displayProblemDialog(final String packName) {
        String message = "Unfortunately, something went wrong loading your new purchase into our system. Press 'Try Again' to try loading it again. If you continue to have a problem with this, please email us using the Contact Us button in the Settings tab of the menu.";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
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
    // Required empty public constructor
    public DrawerFragment() {}

    private void setGridViewListener() {
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                view.startDrag(null, new View.DragShadowBuilder(view), null, 0);
                if(drawerLayout != null) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.pager);
                    drawerLayout.closeDrawers();
                }
                Cursor cursor = (Cursor) drawerMagnetsAdapter.getItem(position);
                drawerFragmentListener.onDrawerMagnetClicked(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT)),
                        cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID)));
                return true;
            }
        });
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
    // override the onCreate method to instantiate and run the cursor loader
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createAsyncQueryHandler();
        String base64EncodedPublicKey = ApplicationContract.base64 + ApplicationContract.encoded + ApplicationContract.Public + ApplicationContract.key;
        initializeSkuList();
        // compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

        /*iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isFailure()) {
                    displayInAppPurchaseSetupFailureMessage();
                }
            }
        });*/
        getLoaderManager().initLoader(0,null,this);
    }

    private void setupMagnetDeckSpinner(View currentView) {
        Spinner spinner = (Spinner) currentView.findViewById(R.id.sets_spinner);
        drawerSpinnerAdapter = new DrawerSpinnerAdapter(getActivity(), R.layout.fragment_drawer_spinner_layout, null, new String[]{}, new int[]{});
        drawerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(drawerSpinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }
    
    private void setupGridView(View currentView) {
        gridView = (GridView) currentView.findViewById(R.id.gridview);
        drawerMagnetsAdapter = new DrawerMagnetsAdapter(getActivity(), null, new String[]{}, new int[]{});
        gridView.setAdapter(drawerMagnetsAdapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View currentView = inflater.inflate(R.layout.fragment_drawer, container, false);
        setupMagnetDeckSpinner(currentView);
        setupGridView(currentView);
        setGridViewListener();
        return currentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            drawerFragmentListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        drawerFragmentListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onDrawerMagnetClicked(String clickedMagnetText, int packID);
        public void onSpinnerClicked();
    }

    /*public void restartMainLoader() {
        Bundle bundle = new Bundle();
        bundle.putInt("id",getCurrentPack());
        getLoaderManager().restartLoader(1, bundle, this);
    }*/


    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure()) {
                displayUnsuccessfulPurchaseDialog();
            }
            else {
                updatePackAvailability(purchase.getSku(),true);
            }

        }
    };

    private void displayUnsuccessfulPurchaseDialog() {
        String message = "Unfortunately something is wrong with the in-app purchase system, and you cannot buy this item at this time. We're sorry for the inconvenience.";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        iabHelper.launchPurchaseFlow(getActivity(), productSKU, 1 ,purchaseFinishedListener,null);
    }
    private void displayWouldYouLikeToBuyDialog(final InAppPurchase inAppPurchase) {
        String message = "This pack requires an in-app purchase. Would you like to buy " + inAppPurchase.title() + " for " + inAppPurchase.price() + " ?";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        purchaseInAppProduct(inAppPurchase.productId());
                    }
                })
                .setNegativeButton("Not right now.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // do nothing
                    }
                });
        (builder.create()).show();
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

    private String clickedSku = null;
    /** onItemSelected and onNothingSelected are parts of the interface for the magnet deck spinner **/
    public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
        Cursor cursor = (Cursor) drawerSpinnerAdapter.getItem(position);
        if(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE))==0) {
            clickedSku = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME));
            iabHelper.queryInventoryAsync(true, skuList,queryForNotPurchasedPacksFinishedListener);
        } else {
            Bundle bundle = new Bundle();
            spinnerPosition = position;
            bundle.putInt("id",cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
            getLoaderManager().restartLoader(1, bundle, this);
            if(spinnerListenerSetUp) drawerFragmentListener.onSpinnerClicked();
            spinnerListenerSetUp = true;
        }

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
            return new CursorLoader(getActivity(),ApplicationContract.getPacks_URI,projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE + MagnetDatabaseContract.MagnetEntry.DESC);
        }
        else {
            String [] projection = {
                    MagnetDatabaseContract.MagnetEntry._ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT
            };
          String[] whereArguments = {Integer.toString(args.getInt("id"))};
            return new CursorLoader(getActivity(),ApplicationContract.getMagnets_URI,projection, MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID + " =? ", whereArguments , MagnetDatabaseContract.MagnetEntry._ID + MagnetDatabaseContract.MagnetEntry.ASC);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            if(loader.getId() == 0) {
                cursor.moveToFirst();
                Bundle bundle = new Bundle();
                bundle.putInt("id",cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
                getLoaderManager().initLoader(1,bundle,this);
                drawerSpinnerAdapter.swapCursor(cursor);
            }
            if(loader.getId() == 1) {
                drawerMagnetsAdapter.swapCursor(cursor);
            }
        }
    }

    // This is called when the last Cursor provided to onLoadFinished() above is about to be closed.  We need to make sure we are no longer using any of the cursors
    public void onLoaderReset(Loader<Cursor> loader) {
        drawerMagnetsAdapter.swapCursor(null);
        drawerSpinnerAdapter.swapCursor(null);
    }
}
