package com.startwork.streetguide;



import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import utils.UtilsAlgorithms;
public class MapController {

    private int nof = 0;
    private UtilsAlgorithms ua = new UtilsAlgorithms();

    public List<LatLng> decodePLine(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {

            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)), (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    public void setListMatrix(List<String> list) {

        nof = list.size();
        ua.setListMatrix(list);
    }

    public void setMode(String mode) {

        ua.setMode(mode);
    }

    public int[][] calculateTimeResult() {

        long[][] times = ua.fetchMatrixResult();
        ua.setNof(nof);
        return ua.startMinimization(times);
    }

    public List<LatLng> decodePoly(String encodedString) {

        return decodePLine(encodedString);
    }

    public String[] getHTML(int index) {

        return ua.getHtmlInstructions(index);
    }

    public void setDirectionOrigin(String origin) {

        ua.setOrigin(origin);
    }

    public void setDirectionDestination(String destination) {

        ua.setDestination(destination);
    }

    public String fetchPolyLine() {

        return ua.fetchResult();
    }
}