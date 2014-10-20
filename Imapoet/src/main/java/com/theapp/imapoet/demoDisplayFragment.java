package com.theapp.imapoet;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link demoDisplayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link demoDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class demoDisplayFragment extends Fragment {

    private DemoListener demoListener;
    private boolean demoDrawerCompleted = false;
    private boolean demoPacksCompleted = false;
    private boolean demoWordCompleted = false;
    private boolean demoAwardsCompleted = false;
    private boolean demoDeleteCompleted = false;
    private boolean demoFinished = false;

    public interface DemoListener {
        public void highlightDrawer(boolean highlightDrawer);
        public void highlightPacks(boolean highlightPacks);
        public void highlightTrashCan(boolean highlightTrashCan);
        public void highlightAward(boolean highlightAward);
        public void highlightMagnets(boolean highlightMagnets);
        public void highlightButtons(boolean highlightButtons);
        public void demoComplete();
        public void changeTextView(String text);
    }

    private void runDemoIntro() {
        demoListener.highlightDrawer(true);
        demoDrawerCompleted = true;
    }

    public void drawerOpened() {
        if(demoDrawerCompleted) {
            demoListener.highlightDrawer(false);
            demoListener.highlightPacks(true);
            demoListener.changeTextView("Excellent! Your words are separated into packs. To use a new pack, select it from the drop down list above. Select a new pack to continue.");
            demoPacksCompleted = true;
        }
    }

    public void packsSelected() {
        if(demoPacksCompleted) {
            demoListener.changeTextView("You can use a word by dragging it to the right. Drag a word to the right to continue. ");
            demoWordCompleted = true;
            demoListener.highlightPacks(false);
            //demoListener.highlightMagnets(true);
        }
    }


    public void magnetTilesChanged() {
        //if(demoFinished) demoListener.demoComplete();
         if(demoWordCompleted && !demoAwardsCompleted) {
            demoListener.changeTextView("You can win awards by completing certain tasks. When you win an award, the award icon will light up. Click on the award icon to continue.");
             demoListener.highlightAward(true);
            demoAwardsCompleted = true;
        }
    }


    public void awardClicked() {
        if(demoAwardsCompleted && !demoDeleteCompleted) {
            demoListener.changeTextView("You can delete magnets by dragging them into the trash can. Delete a magnet to continue.");
            demoListener.highlightTrashCan(true);
            demoListener.highlightAward(false);
            demoDeleteCompleted = true;

        }
    }


    public void magnetDeleted() {
        if(demoDeleteCompleted) {
            demoListener.changeTextView("That's it! The buttons above take you to the menu, let you save your poems, help you share them with the world, and more! Start exploring. Click one of the buttons above to end this demo");
            demoFinished = true;
            demoListener.highlightButtons(true);
            demoListener.highlightTrashCan(false);
        }
    }

    public void buttonsClicked() {
        demoFinished = true;
        demoListener.highlightButtons(false);
        demoListener.demoComplete();

    }



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment demoDisplayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static demoDisplayFragment newInstance(String param1, String param2) {
        demoDisplayFragment fragment = new demoDisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public demoDisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void changeTextView() {
        ((TextView)getView().findViewById(R.id.demoText1)).setText(mParam1);
    }
    public void changeText(String text) {
        this.mParam1 = text;
        changeTextView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // ((TextView)getView().findViewById(R.id.demoText1)).setText(mParam1);
        return inflater.inflate(R.layout.fragment_demo_display, container, false);
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        changeTextView();
        runDemoIntro();

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            demoListener = (DemoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
