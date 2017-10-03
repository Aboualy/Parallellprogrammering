package com.startwork.streetguide;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.content.Intent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import Fragments.FragmentLocation;
import utils.Utils;


public class HomeActivity extends AppCompatActivity
        implements FragmentLocation.RemoveLocationEventListener, View.OnClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private static int m_nFragmentsCounter = 0;
    private static ArrayList<String> m_addenLocationFragmentsList = new ArrayList<>();

    private Spinner m_commuteModeDropdownList;
    Utils objUtils = new Utils();

    AutoSearchView autoCompleteTextView_start;
    AutoSearchView autoCompleteTextView_end;

    private boolean m_bRoundTrip;
    private String m_startLocation = "";
    private String m_requiredLocation = "";

    private MarkingPlace markingPlace;
    private Utils.CommuteMode m_commuteMode;
    private Button goButton, resetButton;
    private ImageButton addPlace;
    List<AutoCompleteTextView> autoCompleteTextViewList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home_screen);
        initView();

        setCommuteModes();
        setModeChangeListener();

    }

    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(false);
        goButton = (Button) findViewById(R.id.btn_go);
        resetButton = (Button) findViewById(R.id.btn_clear);
        addPlace = (ImageButton) findViewById(R.id.btnAddPlaceToVisit);
        autoCompleteTextView_start = (AutoSearchView) findViewById(R.id.atvStartingPoint);
        autoCompleteTextView_start.setThreshold(1);

        autoCompleteTextView_end = (AutoSearchView) findViewById(R.id.atvRequiredDestination);
        autoCompleteTextView_end.setThreshold(1);

        setTextListenerForAutoCompleteSearchLocationControls();
        setItemClickListenerForAutoCompleteSearchLocationControls();

        autoCompleteTextViewList.add(autoCompleteTextView_start);
        autoCompleteTextViewList.add(autoCompleteTextView_end);
        goButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        addPlace.setOnClickListener(this);

        m_commuteModeDropdownList = (Spinner) findViewById(R.id.commuteModeDropdownList);
    }

    private void addNewPlace() {

        FragmentLocation newFragmentLocation = new FragmentLocation((m_nFragmentsCounter + 1), getBaseContext());

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        String strLocFragmentTag = getLocationFragmentTag();
        ft.add(R.id.addedLocationsContainerLayout, newFragmentLocation, strLocFragmentTag);
        newFragmentLocation.setFragmentTag(strLocFragmentTag);

        ft.addToBackStack(null);
        ft.commit();

        m_nFragmentsCounter++;
        m_addenLocationFragmentsList.add(strLocFragmentTag);


    }
    private void goMap() {

        ArrayList<String> arrDestinations = new ArrayList<>();
        if (!m_startLocation.equals("")) {
            arrDestinations.add(m_startLocation);
        }

        FragmentManager fm = getFragmentManager();
        int nFragments = m_addenLocationFragmentsList.size();
        for (int nIndex = 0; nIndex < nFragments; nIndex++) {

            FragmentLocation frag = (FragmentLocation) fm.findFragmentByTag(m_addenLocationFragmentsList.get(nIndex));
            if (frag != null) {
                String strLocation = frag.getLocationName();
                System.out.println(strLocation);
                if (!strLocation.equals("")) {
                    arrDestinations.add(strLocation);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Fragment object is null", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!m_requiredLocation.equals("")) {
            arrDestinations.add(m_requiredLocation);
        }

        boolean bEmptyChecksSuccess = false;
        if (arrDestinations.size() == (nFragments + 2)) {
            bEmptyChecksSuccess = true;
        }

        if (bEmptyChecksSuccess) {

            objUtils.m_bRoundTrip = m_bRoundTrip;
            objUtils.m_commuteMode = m_commuteMode;
            objUtils.m_strStartingPoint = m_startLocation;
            objUtils.m_strFinishPoint = m_requiredLocation;
            objUtils.m_DestinationList.addAll(arrDestinations);

            swipeRefreshLayout.setRefreshing(true);
            swipeRefreshLayout.setEnabled(true);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                    Intent intent = new Intent(HomeActivity.this, DetailedMap.class);
                    intent.putExtra("user_search_inputs", objUtils);

                    HomeActivity.this.startActivity(intent);
                }
            }, 3000);


            swipeRefreshLayout.setColorSchemeColors(R.color.home_color, R.color.secondry_color, R.color.secondry_color);

        } else {

            Toast.makeText(getApplicationContext(), "There Are Missing Data", Toast.LENGTH_LONG).show();
        }


    }

    public void clearAll() {


        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (int nIndex = 0; nIndex < fm.getBackStackEntryCount(); nIndex++) {
            int nFragId = fm.getBackStackEntryAt(nIndex).getId();
            fm.popBackStack(nFragId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        ft.commit();

        m_nFragmentsCounter = 0;
        m_addenLocationFragmentsList.clear();

        RadioButton rbRndTrip = (RadioButton) findViewById(R.id.rbRoundTrip);
        rbRndTrip.setChecked(true);

        autoCompleteTextView_start.setText("");
        autoCompleteTextView_end.setText("");

    }

    private void setTextListenerForAutoCompleteSearchLocationControls() {

        autoCompleteTextView_start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                markingPlace = new MarkingPlace(0, autoCompleteTextView_start);
                markingPlace.execute(s.toString(), getBaseContext());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        autoCompleteTextView_end.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                markingPlace = new MarkingPlace(1, autoCompleteTextView_end);
                markingPlace.execute(s.toString(), getBaseContext());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setItemClickListenerForAutoCompleteSearchLocationControls() {

        autoCompleteTextView_start.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) parent.getItemAtPosition(position);
                m_startLocation = map.get("description");
                m_startLocation = m_startLocation.replaceAll(" ", "+");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(autoCompleteTextView_start.getWindowToken(), 0);
            }
        });

        autoCompleteTextView_end.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) parent.getItemAtPosition(position);
                m_requiredLocation = map.get("description");
                m_requiredLocation = m_requiredLocation.replaceAll(" ", "+");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(autoCompleteTextView_end.getWindowToken(), 0);
            }
        });
    }

    private String getLocationFragmentTag() {

        String strLocationFragmentTag = "Location";
        strLocationFragmentTag = strLocationFragmentTag + (m_nFragmentsCounter + 1);

        return strLocationFragmentTag;
    }

    public void onTripTypeChanged(View view) {

        boolean bIsSelected = ((RadioButton) view).isChecked();
        switch (view.getId()) {

            case R.id.rbRoundTrip:
                if (bIsSelected) {
                    m_bRoundTrip = true;
                }
                break;

            case R.id.rbAtoBTrip:
                if (bIsSelected) {
                    m_bRoundTrip = false;
                }
                break;
        }
    }

    private void setCommuteModes() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.arrCommuteOptions,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        m_commuteModeDropdownList.setAdapter(adapter);
    }

    private void setModeChangeListener() {

        m_commuteModeDropdownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int nItemPosition, long id) {

                switch (nItemPosition) {

                    case 0:
                        m_commuteMode = Utils.CommuteMode.WALK;
                        break;

                    case 1:
                        m_commuteMode = Utils.CommuteMode.BIKE;
                        break;

                    case 2:
                        m_commuteMode = Utils.CommuteMode.CAR;
                        break;

                    case 3:
                        m_commuteMode = Utils.CommuteMode.PUBLIC_TRANSIT;
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    public void onRemoveLocationEvent(String strLocationFragmentTag) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment frag = fm.findFragmentByTag(strLocationFragmentTag);
        if (frag != null) {
            ft.remove(frag);
        }
        ft.commit();

        for (int i = 0; i < m_addenLocationFragmentsList.size(); i++) {

            String strTempTag = m_addenLocationFragmentsList.get(i);
            if (Objects.equals(strTempTag, strLocationFragmentTag)) {
                m_addenLocationFragmentsList.remove(i);
                break;
            }
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go:
                if (Utils.isNetworkAvailable(this)) {
                    goMap();
                } else {
                    Toast.makeText(HomeActivity.this, "please check your connection", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_clear:
                clearAll();
                break;
            case R.id.btnAddPlaceToVisit:
                addNewPlace();


        }

    }


}
