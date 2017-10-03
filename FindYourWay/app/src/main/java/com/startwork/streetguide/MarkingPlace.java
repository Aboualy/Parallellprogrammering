package com.startwork.streetguide;


import android.content.Context;
import android.os.AsyncTask;
import android.widget.SimpleAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MarkingPlace extends AsyncTask<Object, Void, String> {

    private int index;
    Context context;
    ParserTask parserTask;
    AutoSearchView autoCompleteTextView;


    public MarkingPlace(int index, AutoSearchView autoCompleteTextView) {

        this.index = index;
        this.autoCompleteTextView = autoCompleteTextView;
    }
    public List<HashMap<String, String>> parse(JSONObject jsonObject) {

        JSONArray jPlaces = null;

        try{
            jPlaces = jsonObject.getJSONArray("predictions");
        } catch (JSONException jsone){
            jsone.printStackTrace();
        }

        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {

        int placesCount = jPlaces.length();
        HashMap<String, String> place;
        List<HashMap<String, String>> placesList = new ArrayList<>();

        for(int i = 0; i < placesCount; i++) {

            try{
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException jsone){

                jsone.printStackTrace();
            }
        }

        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject jPlace){

        String id, reference, description;
        HashMap<String, String> place = new HashMap<>();

        try {

            description = jPlace.getString("description");
            id = jPlace.getString("id");
            reference = jPlace.getString("reference");

            place.put("description", description);
            place.put("_id", id);
            place.put("reference", reference);

        } catch (JSONException jsone) {

            jsone.printStackTrace();
        }

        return place;
    }

    @Override
    protected String doInBackground(Object... param) {

        String data = "", input = "";
        String key = "key=AIzaSyACPXzuDPBTErvujf8B45MmrfF0qbzHlPA";

        try {
            input = "input=" + URLEncoder.encode((String)param[0], "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        context = (Context)param[1];

        String types = "types=geocode";
        String sensor = "sensor=false";
        String parameters = input + "&" + types + "&" + sensor + "&" + key;

        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        try{
            data = downloadUrl(url);
        } catch (Exception e) {
        }

        return data;
    }

    @Override
    protected void onPostExecute(String result) {

        super.onPostExecute(result);

        parserTask = new ParserTask(index, context, autoCompleteTextView);
        parserTask.execute(result);
    }

    private String downloadUrl(String strUrl) throws IOException {

        String data = "";

        try {

            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null) {

                sb.append(line);
            }

            data = sb.toString();

            br.close();
            inputStream.close();
            urlConnection.disconnect();

        } catch (Exception ex) {

        }

        return data;
    }

    private class ParserTask extends AsyncTask< String, Integer, List< HashMap<String, String> > > {

        private int index;
        private Context context;
        JSONObject jsonObject;
        AutoSearchView autoCompleteTextView;

        public ParserTask(int index, Context context, AutoSearchView autoCompleteTextView) {

            this.index = index;
            this.context = context;
            this.autoCompleteTextView = autoCompleteTextView;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
//            PlaceJSONParser placeJSONParser = new PlaceJSONParser();

            try{

                jsonObject = new JSONObject(jsonData[0]);
                places = parse(jsonObject);
            } catch (Exception e) {

            }

            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[]{ "description" };
            int[] to = new int[]{ android.R.id.text1 };

            SimpleAdapter adapter = new SimpleAdapter(context, result, android.R.layout.simple_list_item_1, from, to);
            autoCompleteTextView.setAdapter(adapter);
        }
    }
}
