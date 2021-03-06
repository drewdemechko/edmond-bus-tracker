package edu.uco.edmond.bus.tracker;

import edu.uco.edmond.bus.tracker.Dtos.BusStop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.primefaces.model.map.LatLng;

@ManagedBean
@ViewScoped
public class RouteStopManagement implements Serializable {
    
    private ArrayList<BusStop> stops = new ArrayList<>();
    private final String ENV = "https://uco-edmond-bus.herokuapp.com/api/busstopservice/stops";
    
    @PostConstruct
    public void init() {
        try {
            URL obj = new URL(ENV);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            
            // optional default is GET
            con.setRequestMethod("GET");
            
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + ENV);
            System.out.println("Response Code : " + responseCode);
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            //print result
            System.out.println(response.toString());
            JSONArray jsonarray;
            try {
                jsonarray = new JSONArray(response.toString());
                for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject;
                try {
                    jsonobject = jsonarray.getJSONObject(i);
                    int id = jsonobject.getInt("id");
                    String name = jsonobject.getString("name");
                    Double lat = jsonobject.getDouble("latitude");
                    Double lng = jsonobject.getDouble("longitude");
                    System.out.println(name);
                    BusStop temp = new BusStop(id, name, lat, lng);
                    stops.add(temp);
                } catch (JSONException ex) {
                    Logger.getLogger(RouteStopManagement.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            } catch (JSONException ex) {
                Logger.getLogger(RouteStopManagement.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (IOException ex) {
            Logger.getLogger(RouteStopManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<BusStop> getStops() {
        return stops;
    }
    
}
