package com.theapp.imapoet;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
ToDo, breaks on rotate, do i need to fix?
 */
public class DemoFragment extends Fragment {
    private DemoListener demoListener;
    private boolean demoDrawerCompleted = false;
    private boolean demoPacksCompleted = false;
    private boolean demoWordCompleted = false;
    private boolean demoAwardsCompleted = false;
    private boolean demoDeleteCompleted = false;
    private boolean demoCompleted = false;
    private static final String ARG_INITIAL_DEMO_TEXT = "";
    private String demoText;

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
        demoDrawerCompleted = true;
    }

    // The user has opened the drawer, so remove the highlight on the drawer and add a highlight to the packs. Also, change the text in the demo text bubble.
    public void drawerOpened() {
        if(demoDrawerCompleted) {
            demoListener.highlightDrawer(false);
            demoListener.highlightPacks(true);
            demoListener.changeTextView(getString(R.string.demo_drawer_opened));
            demoPacksCompleted = true;
        }
    }

    // The user has selected a pack, so remove the highlight from the packs, and change the demo text bubble. There is no highlight for the individual word magnets because I didn't like the way it looked and am not sure it is necessary, could add later.
    public void packsSelected() {
        if(demoPacksCompleted) {
            demoListener.changeTextView(getString(R.string.demo_packs_selected));
            demoWordCompleted = true;
            demoListener.highlightPacks(false);
        }
    }

    // A magnet tile has been dragged onto the canvas space, so highlight the award and change the demo text button.
    public void magnetTilesChanged() {
         if(demoWordCompleted && !demoAwardsCompleted) {
            demoListener.changeTextView(getString(R.string.demo_magnet_tiles_changed));
             demoListener.highlightAward(true);
            demoAwardsCompleted = true;
        }
    }

    // The user has clicked the award (after the award has been highlighted and before the delete action), so change the demo text and highlight the trash can. Also remove the award highlight.
    public void awardClicked() {
        if(demoAwardsCompleted && !demoDeleteCompleted) {
            demoListener.changeTextView(getString(R.string.demo_award_clicked));
            demoListener.highlightTrashCan(true);
            demoListener.highlightAward(false);
            demoDeleteCompleted = true;
        }
    }

    // The user has deleted the magnet (after the trash can has been highlighted), so highlight the buttons and change the demo text.
    public void magnetDeleted() {
        if(demoDeleteCompleted) {
            demoListener.changeTextView(getString(R.string.demo_magnet_deleted));
            demoListener.highlightButtons(true);
            demoListener.highlightTrashCan(false);
            demoCompleted = true;
        }
    }

    // The user has clicked the buttons (after the magnet has been deleted), so end the demo.
    public void buttonsClicked() {
       if(demoCompleted) {
           demoListener.highlightButtons(false);
           demoListener.demoComplete();
       }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param demoText Parameter 1.
     * @return A new instance of fragment demoDisplayFragment.
     */
    public static DemoFragment newInstance(String demoText) {
        DemoFragment fragment = new DemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_DEMO_TEXT, demoText);
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
        runDemoIntro();
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


}
