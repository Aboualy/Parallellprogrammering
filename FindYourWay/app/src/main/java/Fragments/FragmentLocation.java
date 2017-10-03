package Fragments;



import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;

import com.startwork.streetguide.AutoSearchView;
import com.startwork.streetguide.R;

import java.util.HashMap;

import com.startwork.streetguide.MarkingPlace;


public class FragmentLocation extends Fragment {

    private int index;
    private Context context;
    private String locationName = null;
    private String mStrLocationFragmentTag;
    private RemoveLocationEventListener mListener;

    private AutoSearchView custSearchLocationView;

    public FragmentLocation() {}

    public FragmentLocation(int index, Context context) {

        this.index = index;
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_location, container, false);

        custSearchLocationView = (AutoSearchView) fragmentView.findViewById(R.id.atvLocationX);
        custSearchLocationView.setThreshold(1);
        setTextChangeListener();
        setItemClickListener();


        ImageButton btnRemoveLocation = (ImageButton) fragmentView.findViewById(R.id.btnRemoveLocation);
        btnRemoveLocation.setOnClickListener(new View.OnClickListener() {

            // Event that invokes the method on the parent activity to remove the fragment
            @Override
            public void onClick(View view) {
                mListener.onRemoveLocationEvent(mStrLocationFragmentTag);
            }
        });

        return fragmentView;
    }

    private void setTextChangeListener() {

        custSearchLocationView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MarkingPlace markingPlace = new MarkingPlace(index, custSearchLocationView);
                markingPlace.execute(s.toString(), context);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setItemClickListener() {

        custSearchLocationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HashMap<String, String> map = (HashMap<String, String>) parent.getItemAtPosition(position);
                locationName = map.get("description");
                locationName = locationName.replaceAll(" ", "+");
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(custSearchLocationView.getWindowToken(), 0);
            }
        });
    }

    public void setFragmentTag(String strFragmentId) {

        mStrLocationFragmentTag = strFragmentId;
    }

    public String getLocationName() {

        return locationName;
    }

    public interface RemoveLocationEventListener {
        void onRemoveLocationEvent(String strLocationFragmentId);
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        mListener = (RemoveLocationEventListener) activity;
    }
}

