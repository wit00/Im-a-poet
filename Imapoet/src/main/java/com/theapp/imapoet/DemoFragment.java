package com.theapp.imapoet;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 */
public class DemoFragment extends Fragment {
    private DemoListener demoListener;
    private static final String ARG_INITIAL_DEMO_TEXT = "initial demo text";
    private static final String ARG_INITIAL_PLACEMENT = "intial placement";
    private String demoText;
    private DemoPart currentDemoPart;


    // This listener is implemented in the MainActivity
    // This listener tells the MainActivity that it is time to animate the screen in response to a demo event
    public interface DemoListener {
        public void highlightDrawer(boolean highlightDrawer);
        public void highlightPacks(boolean highlightPacks);
        public void highlightTrashCan(boolean highlightTrashCan);
        public void highlightAward(boolean highlightAward);
        public void highlightButtons(boolean highlightButtons);
        public void demoComplete();
        public void changeTextView(String text);
    }

    // Begin the demo by telling the MainActivity to highlight the drawer. Set the demoDrawerCompleted boolean to true so this event cannot be run again during this demo run.
    private void runDemoIntro() {
        demoListener.highlightDrawer(true);
        currentDemoPart = DemoPart.DRAWER_OPENED;
    }

    public DemoPart getCurrentDemoPart() {
        return currentDemoPart;
    }

    public void runDemo(DemoPart demoPart) {
        switch (demoPart) {
            case START:
                if(currentDemoPart.equals(DemoPart.START)) runDemoIntro();
                break;
            case DRAWER_OPENED:
                if(currentDemoPart.equals(DemoPart.DRAWER_OPENED)) drawerOpened();
                break;
            case PACKS_SELECTED:
                if(currentDemoPart.equals(DemoPart.PACKS_SELECTED)) packsSelected();
                break;
            case MAGNET_ADDED:
                if(currentDemoPart.equals(DemoPart.MAGNET_ADDED)) magnetTilesChanged();
                break;
            case AWARD_CLICKED:
                if(currentDemoPart.equals(DemoPart.AWARD_CLICKED)) awardClicked();
                break;
            case MAGNET_DELETED:
                if(currentDemoPart.equals(DemoPart.MAGNET_DELETED)) magnetDeleted();
                break;
            case BUTTONS_CLICKED:
                if(currentDemoPart.equals(DemoPart.BUTTONS_CLICKED)) buttonsClicked();
                break;
        }
    }

    // The user has opened the drawer, so remove the highlight on the drawer and add a highlight to the packs. Also, change the text in the demo text bubble.
    public void drawerOpened() {
            demoListener.highlightDrawer(false);
            demoListener.highlightPacks(true);
            demoListener.changeTextView(getString(R.string.demo_drawer_opened));
            currentDemoPart = DemoPart.PACKS_SELECTED;
        //}
    }

    // The user has selected a pack, so remove the highlight from the packs, and change the demo text bubble. There is no highlight for the individual word magnets because I didn't like the way it looked and am not sure it is necessary, could add later.
    public void packsSelected() {
            demoListener.changeTextView(getString(R.string.demo_packs_selected));
            demoListener.highlightPacks(false);
            currentDemoPart = DemoPart.MAGNET_ADDED;
    }

    // A magnet tile has been dragged onto the canvas space, so highlight the award and change the demo text button.
    public void magnetTilesChanged() {
        demoListener.changeTextView(getString(R.string.demo_magnet_tiles_changed));
        demoListener.highlightAward(true);
        currentDemoPart = DemoPart.AWARD_CLICKED;
    }

    // The user has clicked the award (after the award has been highlighted and before the delete action), so change the demo text and highlight the trash can. Also remove the award highlight.
    public void awardClicked() {
            demoListener.changeTextView(getString(R.string.demo_award_clicked));
            demoListener.highlightTrashCan(true);
            demoListener.highlightAward(false);
            currentDemoPart = DemoPart.MAGNET_DELETED;
    }

    // The user has deleted the magnet (after the trash can has been highlighted), so highlight the buttons and change the demo text.
    public void magnetDeleted() {
            demoListener.changeTextView(getString(R.string.demo_magnet_deleted));
            demoListener.highlightButtons(true);
            demoListener.highlightTrashCan(false);
            currentDemoPart = DemoPart.BUTTONS_CLICKED;
    }

    // The user has clicked the buttons (after the magnet has been deleted), so end the demo.
    public void buttonsClicked() {
           demoListener.highlightButtons(false);
           demoListener.demoComplete();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param demoText Parameter 1.
     * @return A new instance of fragment demoDisplayFragment.
     */
    public static DemoFragment newInstance(String demoText,String placement) {
        DemoFragment fragment = new DemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_DEMO_TEXT, demoText);
        args.putString(ARG_INITIAL_PLACEMENT,placement);
        fragment.setArguments(args);
        return fragment;
    }
    
    // Required empty public constructor
    public DemoFragment() {}

    // Override the onCreate function to add the demoText argument to the demoText member variable.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            demoText = getArguments().getString(ARG_INITIAL_DEMO_TEXT);
            switch (DemoPart.valueOf(getArguments().getString(ARG_INITIAL_PLACEMENT))) {
               case START:
                   currentDemoPart = DemoPart.START;
                   break;
                case DRAWER_OPENED:
                    currentDemoPart = DemoPart.START;

                    break;
                case PACKS_SELECTED:
                    currentDemoPart = DemoPart.DRAWER_OPENED;
                    break;
                case MAGNET_ADDED:
                    currentDemoPart = DemoPart.PACKS_SELECTED;
                    break;
                case AWARD_CLICKED:
                    currentDemoPart = DemoPart.MAGNET_ADDED;
                    break;
                case MAGNET_DELETED:
                    currentDemoPart = DemoPart.AWARD_CLICKED;
                    break;
                case BUTTONS_CLICKED:
                    currentDemoPart = DemoPart.MAGNET_DELETED;
                    break;
            }
        }
    }

    // Change the demo text bubble
    private void changeTextView() {
        TextView textView = ((TextView)getView().findViewById(R.id.demoText1));
        if(textView != null) textView.setText(demoText);
    }

    // change the demoText member variable
    public void changeText(String text) {
        this.demoText = text;
        changeTextView();
    }

    // Override onCreateView to set fragment_demo_display.xml as the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo_display, container, false);
    }


    // Change the text that the demo will show and run the demo
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        changeTextView();
        runDemo(currentDemoPart);
    }

    // The fragment is being attached to the activity, so attach the demoListener to the activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            demoListener = (DemoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " demo listener not attached");
        }
    }

    // The fragment has been removed from the activity, so deactivate the demoListener
    @Override
    public void onDetach() {
        super.onDetach();
        demoListener = null;
    }

    public enum DemoPart {
        START,DRAWER_OPENED,PACKS_SELECTED,MAGNET_ADDED,AWARD_CLICKED,MAGNET_DELETED,BUTTONS_CLICKED
    }

}
