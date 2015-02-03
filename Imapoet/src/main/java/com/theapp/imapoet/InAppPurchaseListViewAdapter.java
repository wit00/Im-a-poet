package com.theapp.imapoet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter for listing the in app purchase items.
 * Created by whitney on 10/7/14.
 */
public class InAppPurchaseListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<InAppPurchase> inAppPurchases;

    public InAppPurchaseListViewAdapter(Context context, ArrayList<InAppPurchase> inAppPurchases) {
        this.context = context;
        this.inAppPurchases = inAppPurchases;
    }


    public int getCount() {
        return inAppPurchases.size();
    }

    public InAppPurchase getItem(int position) {
        return inAppPurchases.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    private void setRegularText(InAppPurchase inAppPurchase, View rowView) {
        TextView title = ((TextView)rowView.findViewById(R.id.inAppPurchaseTitle));
        title.setTextColor(Color.BLACK);
        title.setText(inAppPurchase.title());
        TextView description = ((TextView)rowView.findViewById(R.id.inAppPurchaseDetail));
        description.setTextColor(Color.BLACK);
        description.setText(inAppPurchase.description());
        TextView price = ((TextView)rowView.findViewById(R.id.inAppPrice));
        price.setTextColor(Color.BLACK);
        price.setText(inAppPurchase.price());



        /*((TextView)rowView.findViewById(R.id.inAppPurchaseTitle)).setText(inAppPurchase.title());
        ((TextView)rowView.findViewById(R.id.inAppPurchaseDetail)).setText(inAppPurchase.description());
        ((TextView)rowView.findViewById(R.id.inAppPrice)).setText(inAppPurchase.price());*/
        String productId = inAppPurchase.productId().substring(0, inAppPurchase.productId().length()-4);
        int newDeckImageResourceID = context.getResources().getIdentifier(productId, "drawable", "com.theapp.imapoet");
        System.out.println("product id " + productId);
        if(newDeckImageResourceID != 0) ((ImageView)rowView.findViewById(R.id.new_deck_icon)).setImageResource(newDeckImageResourceID);
        else ((ImageView)rowView.findViewById(R.id.new_deck_icon)).setImageResource(R.drawable.default_new_deck);
    }

    private void setGrayedOutText(InAppPurchase inAppPurchase, View rowView) {
        TextView title = ((TextView)rowView.findViewById(R.id.inAppPurchaseTitle));
        title.setTextColor(Color.GRAY);
        title.setText(inAppPurchase.title());
        TextView description = ((TextView)rowView.findViewById(R.id.inAppPurchaseDetail));
        description.setTextColor(Color.GRAY);
        description.setText(inAppPurchase.description());
        TextView price = ((TextView)rowView.findViewById(R.id.inAppPrice));
        price.setTextColor(Color.GRAY);
        price.setText("You have purchased this item.");
    }


    private void setText(InAppPurchase inAppPurchase, View rowView) {
        if(inAppPurchase.hasBeenPurchased()) {
            setGrayedOutText(inAppPurchase,rowView);
        } else {
            setRegularText(inAppPurchase,rowView);
        }
    }
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view= inflater.inflate(R.layout.fragment_inapppurchase_row,parent, false);
        }
        setText(inAppPurchases.get(position),view);
        return view;
    }


}
