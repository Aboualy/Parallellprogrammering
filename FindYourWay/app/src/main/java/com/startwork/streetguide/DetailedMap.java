package com.startwork.streetguide;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener;
import com.google.android.gms.maps.StreetViewPanorama.OnStreetViewPanoramaChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import Fragments.FragmentRoute;

import utils.Utils;


public class DetailedMap extends AppCompatActivity implements FragmentRoute.saveRouteDialogListener,
        OnMapReadyCallback, OnCameraChangeListener, OnStreetViewPanoramaReadyCallback, OnStreetViewPanoramaChangeListener,
        ConnectionCallbacks, OnStreetViewPanoramaCameraChangeListener, StreetViewPanorama.OnStreetViewPanoramaClickListener,
        OnMapClickListener, OnPolylineClickListener, LocationListener, OnConnectionFailedListener, GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMarkerClickListener, RadioGroup.OnCheckedChangeListener {




    private int currentPositionIndex = 0,bearingToPoint,pointIndex = 0,polylineIndex = 0,distanceToPoint,index = 0
            , polyLineID = -1 ,SV_current_index = 0,markerIndex = 0,nof = 0;
    private float bearingSV,currentBearing,bearing = 0;
    private boolean bearingFinished = false,polyLinesDrawn = false,following = false,waypoint = false,playRoute = false
            ,reset = false, playSubroute = false,viewPolylineInfo = false,clearRB = false;
    TextView currentLL,currentB,distanceTo,bearingTo,textView;
    private HashMap<Integer, Integer> markerIndexToPolylineIndex = new HashMap<>();
    boolean markerClicked = false;
    private HashMap<Integer, Boolean> subrouteMarked = new HashMap<>();
    private RadioGroup rgroup;
    List<String> routeInfoTexts = new ArrayList<>();
    private LinearLayout navigationButtonBar;
    private GoogleMap m_routeMap;
    private LocationRequest locationRequest;
    private StreetViewPanorama streetViewPanorama;
    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;
    private List<String> list;
    private Marker[] markers;
    private String mode = "driving";
    private MapController objController = new MapController();
    private LinkedList<List<LatLng>> listPolylines = new LinkedList<>();
    private LinkedList<Polyline> polyLines = new LinkedList<>();
    private Utils m_Utils;
    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
    Button beginRoute;
    LatLng currentLatLng;
    CheckBox checkBox;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_map);
            init();
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        streetViewPanorama = panorama;
                        streetViewPanorama.setOnStreetViewPanoramaChangeListener(DetailedMap.this);
                        streetViewPanorama.setOnStreetViewPanoramaCameraChangeListener(DetailedMap.this);
                        streetViewPanorama.setOnStreetViewPanoramaClickListener(DetailedMap.this);
                        if (savedInstanceState == null) {
                            streetViewPanorama.setPosition(SYDNEY);
                        }
                    }
                });

    }



    private void init() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
        m_Utils = extras.getParcelable("user_search_inputs");
        list = m_Utils.m_DestinationList;
        markers = new Marker[list.size()];
        String commute = m_Utils.m_commuteMode.toString();
        ImageButton button = null;
        if(commute.equals("WALK")) {
            button = (ImageButton)findViewById(R.id.btnByWalk);
            mode = "walking";
        }
        else if(commute.equals("PUBLIC_TRANSPORT")) {
            button = (ImageButton)findViewById(R.id.btnByPublicTransport);
            mode = "transit";
        }
        else if(commute.equals("CAR")) {
            button = (ImageButton)findViewById(R.id.btnByCar);
            mode = "driving";
        }
        else if(commute.equals("BIKE")) {
            button = (ImageButton)findViewById(R.id.btnByBike);
            mode = "bicycling";
        }
        objController.setMode(mode);
        if(button != null) {
            button.setBackgroundColor(Color.YELLOW);
        }
    }

        setInputsForSearch();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.routeMapFragment);
        mapFragment.getMapAsync(this);

        rgroup = (RadioGroup)findViewById(R.id.rgroup);
        rgroup.setOnCheckedChangeListener(this);

        navigationButtonBar = (LinearLayout) findViewById(R.id.navigationButtonBar);

        beginRoute = (Button) findViewById(R.id.btnStartNavigation);
        beginRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int size = polyLines.get(polyLineID).getPoints().size();
                LatLng latLng = polyLines.get(polyLineID).getPoints().get(size - 1);

                double lat = latLng.latitude;
                double lon = latLng.longitude;
                String m = null;
                if (mode.equals("transit")) {

                    String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f&dirflg=r", lat, lon);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                } else {
                    if (mode.equals("driving")) m = "d";
                    if (mode.equals("bicycling")) m = "b";
                    if (mode.equals("walking")) m = "w";
                    String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=%s", lat, lon, m);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }

            }
        });

        textView = (TextView) findViewById(R.id.polylineInfo);
        textView.setMovementMethod(new ScrollingMovementMethod());
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(200);
        streetViewPanoramaFragment = (SupportStreetViewPanoramaFragment) getSupportFragmentManager().findFragmentById(R.id.streetviewFragment);
        hideSV();

    }


    @Override
    public void onFinishInputDialog(String strRouteName) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        m_routeMap = googleMap;

        m_routeMap.setBuildingsEnabled(true);
        m_routeMap.setOnMapClickListener(this);
        m_routeMap.setOnPolylineClickListener(this);
        m_routeMap.setOnCameraChangeListener(this);
        m_routeMap.setOnMapLoadedCallback(this);
        m_routeMap.setOnMarkerClickListener(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {


            m_routeMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);




            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        }

        int i = 0;
        for(String location: list) {

            List<Address> addresses = null;


            if(addresses != null) {

                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                markers[i] = m_routeMap.addMarker(new MarkerOptions().position(latLng).title(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_red)));
                markers[++i] = m_routeMap.addMarker(new MarkerOptions().position(latLng).title(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_green)));

            }
        }


        startCalculateThread();
    }

    @Override
    public void onMapLoaded() {

        onProceed();
        showAllPolyLines();
        loadRadioButtons();
    }

    private void loadRadioButtons() {

        for(String routeInfo: routeInfoTexts) {
            RadioButton rb = new RadioButton(this);
            rb.setText(routeInfo);
            rgroup.addView(rb);
        }
    }


    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {

        long duration = 20;
        float tilt = 30;
        float bearing = 90;

        StreetViewPanoramaCamera camera = new StreetViewPanoramaCamera.Builder()
                .zoom(panorama.getPanoramaCamera().zoom)
                .bearing(bearing)
                .tilt(tilt)
                .build();

        panorama.setPosition(new LatLng(52.208818, 0.090587));
        panorama.setStreetNamesEnabled(false);
        panorama.setZoomGesturesEnabled(false);
        panorama.animateTo(camera, duration);
    }


    @Override
    public void onMapClick(LatLng point) {

        if(viewPolylineInfo) {

            viewPolylineInfo = false;
        }

        for(Polyline p: polyLines) {

            p.setColor(Color.BLACK);
            p.setVisible(true);
        }

        if(markerClicked) markerClicked = false;
        clearRB = true;
        navigationButtonBar.setVisibility(View.INVISIBLE);
        rgroup.clearCheck();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {


        int id = Integer.parseInt(polyline.getId().substring(2,3));
        try {
            String[] html_instructions = objController.getHTML(id);
            String message = "";
            for(String html_route: html_instructions){
                html_route = html_route.replaceAll( "<[^>]*>", "" );
                message += "-" + html_route + "\n";
            }

            if(viewPolylineInfo) {

                if(id != polyLineID) {

                    for(Polyline p: polyLines) {
                        p.setColor(Color.BLACK);
                    }
                    polyline.setColor(Color.GREEN);
                    textView.setText(message);
                } else {

                    for(Polyline p: polyLines) {
                        p.setColor(Color.BLACK);
                    }

                    viewPolylineInfo = false;
                    textView.setVisibility(View.INVISIBLE);
                    beginRoute.setVisibility(View.INVISIBLE);
                    checkBox.setVisibility(View.INVISIBLE);
                }
            } else {

                textView.setVisibility(View.VISIBLE);
                textView.setText(message);
                polyline.setColor(Color.CYAN);
                viewPolylineInfo = true;
                if(id == 0 || (id > 0 && subrouteMarked.get(id-1))) {
                    beginRoute.setVisibility(View.VISIBLE);
                    checkBox.performClick();
                    checkBox.setVisibility(View.VISIBLE);
                }
            }

            polyLineID = id;

        }catch (Exception e){
            onBackPressed();
        }
       }

    public void viewPolylineInfo(View view){

        Button b = (Button) view;


        if(viewPolylineInfo) {

            viewPolylineInfo = false;
            b.setTextColor(Color.BLACK);
            viewPolylineInfo = false;
            textView.setVisibility(View.INVISIBLE);
        } else {

            viewPolylineInfo = true;
            String[] html_instructions = objController.getHTML(polyLineID);

            String message = "";
            for(String html_route: html_instructions){
                html_route = html_route.replaceAll( "<[^>]*>", "" );
                message+="-"+html_route+"\n";
            }

            b.setTextColor(Color.YELLOW);
            textView.setVisibility(View.VISIBLE);
            textView.setText(message);


        }

    }






    private void hideSV() {

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .hide(streetViewPanoramaFragment)
                .commit();
    }

    private void animateRoute() {

        Polyline polyline = polyLines.get(polyLineID);
        List<LatLng> points = polyline.getPoints();

        if(currentPositionIndex+1 < points.size()) {

            LatLng current = points.get(currentPositionIndex);
            currentPositionIndex++;
            LatLng next = points.get(currentPositionIndex);
            bearing = (float) Utils.computeHeading(current, next);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(current)
                    .zoom(19)
                    .bearing(bearing)
                    .tilt(67)
                    .build();

            m_routeMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            if(playRoute) {
                waypoint = true;
                polyline.setColor(Color.GREEN);
                if(polyLineID < polyLines.size() - 1) {
                    polyLines.get(polyLineID + 1).setColor(Color.BLUE);
                }

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(points.get(currentPositionIndex))
                        .zoom(19)
                        .bearing(bearing)
                        .tilt(67)
                        .build();

                currentPositionIndex = 0;
                m_routeMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else {


                playSubroute = false;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(points.get(currentPositionIndex))
                        .zoom(17)
                        .bearing(90)
                        .tilt(30)
                        .build();

                currentPositionIndex = 0;
                m_routeMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private void animateStreetView() {

        long duration = 500;
        float tilt = 0;

        LatLng from = polyLines.get(0).getPoints().get(SV_current_index);
        LatLng to = polyLines.get(0).getPoints().get(SV_current_index+1);
        bearingSV = (float) Utils.computeHeading(from, to);

        StreetViewPanoramaCamera streetViewPanoramaCamera = new StreetViewPanoramaCamera.Builder()
                .zoom(streetViewPanorama.getPanoramaCamera().zoom)
                .bearing(bearingSV)
                .tilt(tilt)
                .build();

        streetViewPanorama.animateTo(streetViewPanoramaCamera, duration);
        streetViewPanorama.setPosition(to);


    }

    private void zoomOut(LatLng target) {

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(17)
                .bearing(90)
                .tilt(30)
                .build();
        currentPositionIndex = 0;
        m_routeMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void showAllMarkers() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Marker marker: markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);

        m_routeMap.animateCamera(cu);
    }


    private void showAllPolyLines() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Polyline polyline: polyLines) {

            List<LatLng> latLngs = polyline.getPoints();
            for(LatLng latLng: latLngs) {

                builder.include(latLng);
            }
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 170);
        m_routeMap.animateCamera(cu);
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        if(playSubroute || playRoute) {
            if(waypoint) {
                waypoint = false;
                polyLineID++;

                if(polyLineID == polyLines.size()) {
                    playRoute = false;


                    polyLineID = 0;
                    showAllMarkers();
                }
                else
                    animateRoute();
            }
            else
                animateRoute();
        }
        else{
            if(reset) {
                reset = false;
                zoomOut(cameraPosition.target);
            }
        }
    }


    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {


        if(SV_current_index < 10 && playRoute) {

            SV_current_index++;
            try{
                Thread.sleep(1000);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }

            animateStreetView();
        }
    }


    @Override
    public void onStreetViewPanoramaClick(StreetViewPanoramaOrientation orientation) {


        streetViewPanorama.animateTo(
                new StreetViewPanoramaCamera.Builder()
                        .orientation(orientation)
                        .zoom(streetViewPanorama.getPanoramaCamera().zoom)
                        .build(), 1000);
    }

    @Override
    public void onStreetViewPanoramaCameraChange(StreetViewPanoramaCamera streetViewPanoramaCamera) {

        if((int)(360 + bearingSV) == (int)streetViewPanoramaCamera.bearing && !bearingFinished) {
            bearingFinished = true;
            animateStreetView();
        }

    }


    @Override
    public void onLocationChanged(Location location) {

        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentBearing = location.getBearing();
        currentLL.setText("Current LatLng: "+currentLatLng.latitude+","+currentLatLng.longitude);
        currentB.setText("Current Bearing: "+(int)currentBearing);



        if(polyLinesDrawn) {
            LatLng latLng = polyLines.get(polylineIndex).getPoints().get(pointIndex);
            float[] result = new float[3];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), latLng.latitude, latLng.longitude, result);
            if(result.length > 0){
                distanceToPoint = (int)result[0];
                if(result.length > 1){
                    bearingToPoint = (result[1] < 0) ? (int)(360 + result[1]) : (int)result[1];
                    bearingTo.setText("Bearing: "+bearingToPoint);
                }
                distanceTo.setText("Distance: "+distanceToPoint+" m");
            }
        }

        if(following) {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLatLng)
                    .zoom(19)
                    .bearing(bearing)
                    .tilt(67)
                    .build();

            m_routeMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if(distanceToPoint < 5) {
                pointIndex++;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(Bundle bundle) {

        if ( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(!markerClicked) {

            int id = 0;
            for (int i = 0; i < markers.length; i++) {

                if (markers[i].getId().equals(marker.getId())) {
                    id = i;
                }
            }

            int polyLineIndex = markerIndexToPolylineIndex.get(id);
            for (int i = 0; i < polyLines.size(); i++) {

                if (i != polyLineIndex) {
                    polyLines.get(i).setVisible(false);
                    polyLines.get(i).setClickable(false);
                }
            }

            markerClicked = true;
        }

        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {


        if(clearRB){
            if(checkedId == -1){
                clearRB = false;
            }
        }
        else{
            navigationButtonBar.setVisibility(View.VISIBLE);
            if(checkedId != -1){
                polyLineID = checkedId - 1;
                for (int i = 0; i < polyLines.size(); i++) {
                    if (i != polyLineID) {
                        polyLines.get(i).setVisible(false);
                        polyLines.get(i).setColor(Color.BLACK);
                    } else {
                        polyLines.get(i).setColor(Color.BLUE);
                        polyLines.get(i).setVisible(true);
                    }
                }
            }
            showAllMarkers();
        }
    }






    private void setInputsForSearch() {

        objController.setListMatrix(list);
    }

    private void onProceed() {

        index = 0;
        markerIndex = 0;
        int colorValue;
        int width;

        colorValue = Color.GREEN;
        width = 20;

        for(int i = 0; i < listPolylines.size(); i++) {
            Polyline p = m_routeMap.addPolyline(new PolylineOptions().addAll(listPolylines.get(i)).width(22).color(Color.parseColor("#aab1ff")).geodesic(true).clickable(true));
            polyLines.add(p);
        }

        polyLinesDrawn = true;


    }

    private void clearPolyLines() {

        for(Polyline p: polyLines){
            p.remove();
        }

        polyLines.clear();
        listPolylines.clear();
    }

    private void startCalculateThread() {

        Thread thread = new calculateThread();
        thread.start();

        try{
            thread.join();
        } catch (InterruptedException ie){
            ie.printStackTrace();
        }
    }

    public void traceRouteByMode(View v) {

        clearPolyLines();

        ImageButton b1 = (ImageButton) findViewById(R.id.btnByWalk);
        ImageButton b2 = (ImageButton) findViewById(R.id.btnByPublicTransport);
        ImageButton b3 = (ImageButton) findViewById(R.id.btnByCar);
        ImageButton b4 = (ImageButton) findViewById(R.id.btnByBike);
        b1.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
        b2.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
        b3.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
        b4.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.primary));

        ImageButton button = (ImageButton) v;
        button.setBackgroundColor(Color.YELLOW);

        if(v.getId() == R.id.btnByWalk) {
            mode = "walking";

        } else if(v.getId() == R.id.btnByPublicTransport) {
            mode = "transit";

        } else if(v.getId() == R.id.btnByCar) {
            mode = "driving";

        } else if(v.getId() == R.id.btnByBike) {
            mode = "bicycling";
        }

        objController.setMode(mode);
        markerIndexToPolylineIndex.clear();

        startCalculateThread();
        for(int i = 0 ; i < routeInfoTexts.size(); i++){
            RadioButton radioButton = (RadioButton)rgroup.getChildAt(i);
            radioButton.setText(routeInfoTexts.get(i));
        }
        rgroup.clearCheck();

        onProceed();
    }
    private class calculateThread extends Thread {

        public calculateThread(){}

        @Override
        public void run(){

            int count = 0;
            routeInfoTexts.clear();
            int[][] optimizedRoute = objController.calculateTimeResult();
            for(String s: list){

            }

            for(int[] partRoute: optimizedRoute) {

                int fromIndex = partRoute[0];
                int toIndex = partRoute[1];
                if(fromIndex == toIndex) {
                    break;
                }

                markerIndexToPolylineIndex.put(toIndex, count);
                subrouteMarked.put(count, false);

                String origin = list.get(fromIndex);
                String destination = list.get(toIndex);

                objController.setDirectionOrigin(origin);
                objController.setDirectionDestination(destination);

                String polyLine = objController.fetchPolyLine();
                if(polyLine.equals("NO ROUTE")){
                    Toast.makeText(getApplicationContext(), "Mode not supported", Toast.LENGTH_LONG).show();

                    count++;
                    if(count == nof){
                        break;
                    }

                    continue;
                }

                List<LatLng> listPolyLine = objController.decodePoly(polyLine);
                listPolylines.addLast(listPolyLine);

                count++;
                if(count == nof){
                    break;
                }
            }
        }
    }
}

