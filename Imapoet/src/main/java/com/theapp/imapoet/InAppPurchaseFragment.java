package com.theapp.imapoet;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.example.android.trivialdrivesample.util.SkuDetails;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link //Callbacks}
 * interface.
 */
public class InAppPurchaseFragment extends android.support.v4.app.Fragment implements AbsListView.OnItemClickListener {
    private InAppPurchaseListener inAppPurchaseListener;
    private ArrayList<InAppPurchase> productsAvailableForPurchase = new ArrayList<InAppPurchase>();
    private ArrayList<String> skuList = new ArrayList<String>();
    private AbsListView inAppPurchaseListView;
    private ListAdapter inAppPurchaseAdapter;
    private IabHelper iabHelper;
    public static InAppPurchaseFragment newInstance() {
        return new InAppPurchaseFragment();
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InAppPurchaseFragment() {}
    private IabHelper.QueryInventoryFinishedListener
            queryForNotPurchasedPacksFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isSuccess()){
                for(String sku : skuList) {
                    SkuDetails skuDetails = inventory.getSkuDetails(sku);
                    if(skuDetails != null){
                        productsAvailableForPurchase.add(new InAppPurchase(
                                skuDetails.getTitle(),
                                skuDetails.getDescription(),
                                skuDetails.getType(),
                                skuDetails.getPrice(),
                                skuDetails.getSku(), // product id?
                                false
                        ));
                    } else {
                        displayInAppPurchaseSetupFailureMessage();
                    }
                }
                getPurchasedProducts();
            }
            else {
                displayInAppPurchaseSetupFailureMessage();
            }
        }
    };
    private void getProductDetailsForSkuList() {
        iabHelper.queryInventoryAsync(true, skuList,queryForNotPurchasedPacksFinishedListener);
    }

    private void initializeSkuList() {
        try {
            Collections.addAll(skuList, getActivity().getAssets().list("inAppPurchasePacks"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void initializeIabHelper() {
        initializeSkuList();
        String base64EncodedPublicKey = ApplicationContract.base64 + ApplicationContract.encoded + ApplicationContract.Public + ApplicationContract.key;
        // compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(getActivity(), base64EncodedPublicKey);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // put an alert message here.
                    displayInAppPurchaseSetupFailureMessage();
                } else {
                    getProductDetailsForSkuList();
                }
            }
        });
    }

    private void getPurchasedProducts() {
        iabHelper.queryInventoryAsync(productsPurchasedInventoryListener);
    }
    private void setUpAdapter() {
       // Set the adapter
        inAppPurchaseListView = (AbsListView) getView().findViewById(android.R.id.list);
        (inAppPurchaseListView).setAdapter(inAppPurchaseAdapter); // what if adapter hasn't been initialized yet?
        // Set OnItemClickListener so we can be notified on item clicks
        inAppPurchaseListView.setOnItemClickListener(this);
        System.out.println("sku adapter count:" + Integer.toString(inAppPurchaseAdapter.getCount()));
    }
    private IabHelper.QueryInventoryFinishedListener productsPurchasedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,Inventory inventory) {
            if (result.isFailure()) {
                // handle error here
                displayInAppPurchaseSetupFailureMessage();
            }
            else {
                for(String sku : skuList) {
                    if (inventory.hasPurchase(sku)) {
                        SkuDetails skuDetails = inventory.getSkuDetails(sku);
                        productsAvailableForPurchase.add(new InAppPurchase(
                                skuDetails.getTitle(),
                                skuDetails.getDescription(),
                                skuDetails.getType(),
                                skuDetails.getPrice(),
                                skuDetails.getSku(),
                                true
                        ));
                    }
                }
                inAppPurchaseAdapter = new InAppPurchaseListViewAdapter(getActivity(),productsAvailableForPurchase);
                setUpAdapter();
            }
        }
    };
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeIabHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inapppurchase, container, false);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            inAppPurchaseListener = (InAppPurchaseListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        inAppPurchaseListener = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(iabHelper != null) iabHelper.dispose();
        iabHelper = null;
    }
    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isSuccess()) {
                inAppPurchaseListener.inAppPurchaseClicked(purchase.getSku());
            }
            else {
                displayUnsuccessfulPurchaseDialog();
            }
        }
    };
    private void purchaseInAppProduct(String productSKU) {
        iabHelper.launchPurchaseFlow(getActivity(), productSKU, 1 ,purchaseFinishedListener,null);
    }
    private void displayWouldYouLikeToBuyDialog(final InAppPurchase inAppPurchase) {
        String message = "Would you like to buy " + inAppPurchase.title() + " for " + inAppPurchase.price() + " ?";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        purchaseInAppProduct(inAppPurchase.productId());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                    }
                });
        (builder.create()).show();
    }
    private void displayUnsuccessfulPurchaseDialog() {
        String message = "Unfortunately something is wrong with the in-app purchase system, and you cannot buy this item at this time. We're sorry for the inconvenience.";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        (builder.create()).show();
    }
    private void displayYouHaveBoughtThisDialog(final InAppPurchase inAppPurchase) {
        String message = "You have purchased " + inAppPurchase.title() + " for " + inAppPurchase.price() + " .";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        (builder.create()).show();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != inAppPurchaseListener) {
    // Notify the active callbacks interface (the activity, if the fragment is attached to one) that an item has been selected.
            InAppPurchase clickedInAppPurchase = (InAppPurchase) inAppPurchaseAdapter.getItem(position);
            if(clickedInAppPurchase.hasBeenPurchased()) displayYouHaveBoughtThisDialog(clickedInAppPurchase);
            else displayWouldYouLikeToBuyDialog(clickedInAppPurchase);
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface InAppPurchaseListener {
        public void inAppPurchaseClicked(String packName);
    }
}


