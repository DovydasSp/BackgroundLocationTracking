package speckauskas.dovydas.backgroundlocationtracking.map;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import speckauskas.dovydas.backgroundlocationtracking.DBClass;
import speckauskas.dovydas.backgroundlocationtracking.DBHandler;
import speckauskas.dovydas.backgroundlocationtracking.R;

public class MapsActivity extends AppCompatActivity {

    private MapView mapView;
    private GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapView = (MapView) findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                addPoints();
                mapView.onResume();
            }
        });
    }

    private void addPoints() {
        List<DBClass> lastTrip = findLastTrip();
        if(lastTrip == null || lastTrip.isEmpty())
            noLastTrip();
        else {
            DBClass markerCoords;
            PolylineOptions routeLine;
            int tripLength = lastTrip.size() - 1;

            markerCoords = lastTrip.get(0);
            LatLng firstCoordinate = new LatLng(markerCoords.getLatitude(), markerCoords.getLongitude());
            map.addMarker(new MarkerOptions().position(firstCoordinate).alpha(0.5f).title("START"));
            routeLine = new PolylineOptions().add(firstCoordinate);

            for (DBClass dbClass : lastTrip) {
                routeLine.add(new LatLng(dbClass.getLatitude(), dbClass.getLongitude()));
            }

            markerCoords = lastTrip.get(tripLength);
            LatLng lastCoordinate = new LatLng(markerCoords.getLatitude(), markerCoords.getLongitude());
            map.addMarker(new MarkerOptions().position(lastCoordinate).title("FINISH"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCoordinate, 14));

            map.addPolyline(routeLine);
        }
    }

    private List<DBClass> findLastTrip() {
        DBHandler dbHandler = new DBHandler(this, null, null, 1);
        int lastTrip = dbHandler.findLastTripHandler();
        List<DBClass> trip = dbHandler.findHandler(lastTrip);
        return trip;
    }

    private void noLastTrip(){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), 15));
        if(this.getResources().getInteger(R.integer.notificationIntrusivenessLevel) == 2) {
            Toast.makeText(getApplicationContext(),
                    "NO TRIPS FOUND",
                    Toast.LENGTH_SHORT).show();
        }
    }
}


