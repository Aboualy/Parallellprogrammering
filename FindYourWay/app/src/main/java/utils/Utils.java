package utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;


public class Utils implements Parcelable {
    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public static double computeHeading(LatLng from, LatLng to) {

        double fromLat = toRadians(from.latitude);
        double fromLng = toRadians(from.longitude);
        double toLat = toRadians(to.latitude);
        double toLng = toRadians(to.longitude);
        double dLng = toLng - fromLng;
        double heading = atan2( sin(dLng) * cos(toLat),
                cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng));

        return wrap(toDegrees(heading), -180, 180);
    }

    static double wrap(double n, double min, double max) {

        return (n >= min && n < max) ? n : (mod(n - min, max - min) + min);
    }

    static double mod(double x, double m) {

        return ((x % m) + m) % m;
    }
    public enum CommuteMode {
        WALK,
        BIKE,
        CAR,
        PUBLIC_TRANSIT
    }

    public boolean m_bRoundTrip;
    public CommuteMode m_commuteMode;
    public String m_strStartingPoint;
    public String m_strFinishPoint;
    public List<String> m_DestinationList;

    public Utils() {

        m_bRoundTrip = false;
        m_strStartingPoint = m_strFinishPoint = "";
        m_DestinationList = new ArrayList<>();
    }

    public Utils(Parcel parcel) {

        m_bRoundTrip = (parcel.readInt() == 1);
        m_commuteMode = (CommuteMode) (parcel.readValue(CommuteMode.class.getClassLoader()));
        m_strStartingPoint = parcel.readString();
        m_strFinishPoint = parcel.readString();
        if(m_DestinationList == null) {

            m_DestinationList = new ArrayList<>();
        }
        parcel.readStringList(m_DestinationList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outObject, int flags) {
        outObject.writeInt(m_bRoundTrip ? 1 : 0);
        outObject.writeValue(m_commuteMode);
        outObject.writeString(m_strStartingPoint);
        outObject.writeString(m_strFinishPoint);
        outObject.writeStringList(m_DestinationList);
    }


    public static Creator<Utils> CREATOR = new Creator<Utils>() {

        @Override
        public Utils createFromParcel(Parcel inObject) {
            return new Utils(inObject);
        }

        @Override
        public Utils[] newArray(int size) {
            return new Utils[size];
        }
    };

}
