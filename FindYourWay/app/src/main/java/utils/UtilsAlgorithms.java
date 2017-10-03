package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;


public class UtilsAlgorithms {
    private int nof, z, f1 = 0;

    private long arr[][] = new long[10][10];
    private long a[][] = new long[10][10];
    private int traceroute[];

    private long sum = 0;
    private int fin[][] = new int[10][2];
    private int ret[][] = new int[10][2];

    private long[][] time = null;


    private String url = "https://maps.googleapis.com/maps/api/directions/json?";
    private String mainUrl = url;

    private String origin = null;
    private String destination = null;

    private String key = "AIzaSyAAHn2GZ2EufUkY5GysfTrVWlMTJVRSPk4";
    private String keyMatrix = "AIzaSyAAHn2GZ2EufUkY5GysfTrVWlMTJVRSPk4";

    private String urlMatrix = "https://maps.googleapis.com/maps/api/distancematrix/json?";
    private String urlMatrixCopy = urlMatrix;

    private List<String> list = null;
    private long[][] times = null;
    private String mode;

    private LinkedList<String[]> html_instructions_list = new LinkedList<>();

    public void setOrigin(String origin){
        this.origin = origin;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }

    public void setListMatrix(List<String> list){
        this.list = list;
    }

    public void setMode(String mode){
        this.mode = mode;
    }

    public long[][] fetchMatrixResult() {

        urlMatrix = urlMatrixCopy;
        urlMatrix += "origins=";

        int length = list.size();
        times = new long[length][length];
        for(int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                times[i][j] = 0;
            }
        }

        if(length == 1) {
            urlMatrix += list.get(0);
        } else {
            for(int i = 0; i < length - 1; i++) {
                urlMatrix += list.get(i) + "|";
            }

            urlMatrix += list.get(length - 1);
        }
        urlMatrix += "&destinations=";

        if(length == 1) {
            urlMatrix += list.get(0);
        }
        else {
            for(int i = 0; i < length - 1; i++){
                urlMatrix += list.get(i) + "|";
            }
            urlMatrix += list.get(length - 1);
        }

        urlMatrix += "&mode=" + mode;
        urlMatrix += "&key=" + keyMatrix;
        System.out.println(urlMatrix);






        return times;
    }





    public String[] getHtmlInstructions(int index){

        return html_instructions_list.get(index);
    }

    public String fetchResult() {

        url = mainUrl;
        url += "origin=" + origin;
        url += "&destination=" + destination;
        url += "&mode=" + mode;
        url += "&key=" + key;

        try{


            int read;
            URL urlPath = new URL(url);
            char[] chars = new char[1024];
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlPath.openStream()));
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }

            JSONObject json = new JSONObject(buffer.toString());

            JSONArray array = (JSONArray) json.get("routes");
            if(array.length() == 0){
                return "NO ROUTE";
            }

            JSONObject obj = (JSONObject) array.get(0);

            JSONArray array1 = (JSONArray) obj.get("legs");
            JSONObject obj2 = (JSONObject) array1.get(0);
            String origin = obj2.getString("start_address");
            String destination = obj2.getString("end_address");
            String header = origin +" --> "+destination+"\n";
            JSONArray array2 = (JSONArray) obj2.get("steps");
            String[] html_instructions = new String[array2.length()+1];
            html_instructions[0] = header;
            for(int i = 0; i < array2.length(); i++){
                JSONObject obj2a = (JSONObject) array2.get(i);
                String html = obj2a.getString("html_instructions");
                html_instructions[i+1] = html;
            }
            html_instructions_list.addLast(html_instructions);

            JSONObject obj3 = (JSONObject) obj.get("overview_polyline");
            return obj3.getString("points");

        }  catch(IOException | JSONException ex){

            ex.printStackTrace();
        }

        return null;
    }
    public void dist()
    {
        int i, j;
        z = nof;

        traceroute = new int[nof];
        for(int nIndex = 0; nIndex < nof; nIndex++) {
            traceroute[nIndex] = -1;
        }
        f1 = 0;

        for(i = 0; i < nof; i++)
        {
            arr[z][i] = i;
            arr[i][z] = i;
        }

        for(i = 0; i < nof; i++)
        {
            for(j = 0; j < nof; j++)
            {
                if(i != j)
                {
                    long m = time[i][j];
                    arr[i][j] = m;
                    a[i][j] = m;
                }

                else
                {
                    arr[i][j] = -1;
                }
            }
        }




    }

    public void minimization()
    {
        for(int i = 0; i < nof; i++)
        {
            long min = min(i, 0);
            for(int j = 0; j < nof; j++)
            {
                if(arr[i][j] != -1)
                {
                    arr[i][j] = arr[i][j] - min;
                }
            }
        }

        for(int i = 0; i < nof; i++) {
            long min = min(i, 1);
            for (int j = 0; j < nof; j++) {
                if (arr[j][i] != -1) {
                    arr[j][i] = arr[j][i] - min;
                }
            }
        }}


    public long min(int k,int z) {

        long min = 9999999;
        if(z == 0) {

            for(int i = 0; i < nof; i++) {

                if( ( arr[k][i] < min) && (arr[k][i] != -1) ) {
                    min = arr[k][i];
                }
            }
        }

        if(z == 1) {

            for(int i = 0; i < nof; i++) {

                if((arr[i][k] < min) && (arr[i][k] != -1)) {
                    min = arr[i][k];
                }
            }
        }

        return min;
    }

    public long minn(int k,int eli, int z)
    {
        int cost = 0;
        long min = 9999999;
        if(z == 0) {
            for(int d = 0; d < nof; d++) {
                if( (arr[k][d] != -1) && (d != eli)) {
                    cost = 1;
                }
            }

            if(cost == 0) {
                min = 0;
            } else {

                for(int i = 0; i < nof; i++) {
                    if( (arr[k][i] < min) && (arr[k][i] != -1) && (i != eli) ) {
                        min = arr[k][i];
                    }
                }
            }
        } else if(z == 1) {

            for(int d = 0; d < nof; d++) {
                if( (arr[d][k] != -1) && (d != eli)) {
                    cost = 1;
                }
            }

            if( cost == 0) {
                min = 0;
            } else {

                for(int i = 0; i < nof; i++) {

                    if( (arr[i][k] < min) && (arr[i][k] != -1) && (i != eli) ) {
                        min = arr[i][k];
                    }
                }
            }
        }

        return min;
    }

    public void penalty() {

        int i, j, r = 0, c = 0, ak = 0, flag, trigger;
        long mint;
        int rt[] = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int ct[] = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};

        do
        {
            mint = -1;

            for(i = 0; i < nof; i++)
            {
                for(j = 0; j < nof; j++)
                {
                    if(arr[i][j] == 0)
                    {
                        flag = 0;
                        for(int p = 0; p < ak; p++) {
                            if( (rt[p] == i) && (ct[p] == j) ) {
                                flag = 1;
                            }
                        }

                        if(flag == 0) {
                            long minr = minn(i,j,0);
                            long minc = minn(j,i,1);
                            long temp = minr + minc;
                            if(mint < temp) {
                                mint = temp;
                                r = i;
                                c = j;
                            }
                        }
                    }
                }
            }

            int rcheck = 0;
            int ccheck = 0;
            int rowrec = -1;
            int colrec = -1;
            int rw = (int)arr[r][nof];
            int cl = (int)arr[nof][c];
            if(checkloop(rw,cl)) {
                trigger = 0;
                rt[ak] = r;
                ct[ak] = c;
                ++ak;
            } else {
                trigger = 1;
                fin[f1][0] = rw;
                fin[f1][1] = cl;
                f1++;

                sum = sum + a[rw][cl];
                for(int ch = 0;ch < nof; ch++) {
                    if(arr[nof][ch] == rw) {
                        rcheck = 1;
                        rowrec = ch;
                    }

                    if(arr[ch][nof] == cl) {
                        ccheck = 1;
                        colrec = ch;
                    }
                }

                if((rcheck==1) && (ccheck==1))
                {
                    arr[colrec][rowrec] = -1;
                }
                for(i = r; i < nof; i++) {
                    for(j = 0; j <= nof; j++) {
                        arr[i][j] = arr[i+1][j];
                    }
                }

                for(i = 0; i <= nof; i++) {
                    for(j = c; j < nof; j++) {
                        arr[i][j] = arr[i][j+1];
                    }
                }

                nof--;
            }
        }while(trigger == 0);
    }

    private boolean checkloop(int r, int c)
    {
        int i = c,t = 0;
        traceroute[r] = c;
        do {
            i = traceroute[i];
            t++;
            if(i == -1) {
                return false;
            }
        } while(i != c);

        if(t != z) {
            traceroute[r] = -1;
            return true;
        } else {
            return false;
        }
    }

    public void path() {

        int i, k = 0;
        for(i = 0; i < f1; i++) {
            if(fin[i][0] == 0) {
                break;
            }
        }

        while( z > 0) {
            ret[k][0] = fin[i][0];
            ret[k][1] = fin[i][1];
            --z;

            for(int j = 0; j < f1; j++) {
                if(fin[i][1] == fin[j][0]) {
                    i = j;
                    break;
                }
            }
            k++;
        }
    }

    public void setNof(int nof){

        this.nof = nof;
    }


    public int[][] startMinimization(long[][] time) {

        this.time = time;
        dist();
        while(nof > 0) {
            minimization();
            penalty();
        }

        path();
        return ret;
    }

}
