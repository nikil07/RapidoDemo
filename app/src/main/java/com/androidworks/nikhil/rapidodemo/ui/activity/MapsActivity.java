package com.androidworks.nikhil.rapidodemo.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidworks.nikhil.rapidodemo.R;
import com.androidworks.nikhil.rapidodemo.interfaces.ApiRapido;
import com.androidworks.nikhil.rapidodemo.model.DirectionResults;
import com.androidworks.nikhil.rapidodemo.model.Legs;
import com.androidworks.nikhil.rapidodemo.model.PolyLineInfo;
import com.androidworks.nikhil.rapidodemo.model.Route;
import com.androidworks.nikhil.rapidodemo.model.Steps;
import com.androidworks.nikhil.rapidodemo.ui.adapter.DirectionsAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final int[] COLORS = {Color.BLUE, Color.RED, Color.GREEN, Color.CYAN, Color.MAGENTA};
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final int POLYLINE_STROKE_WIDTH_PX = 18;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String BASE_URL = "https://maps.googleapis.com";
    @BindView(R.id.bt_find_path)
    Button findPathButton;
    @BindView(R.id.tv_distance_between)
    TextView distanceBWTV;
    @BindView(R.id.listLayout)
    FrameLayout listLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.directionsListView)
    ListView directionsListView;
    private SupportMapFragment mapFragment;
    private List<LatLng> polyLines = new ArrayList<>();
    private List<Polyline> polyLinesList = new ArrayList<>();
    private String[] directions;
    private Map<Polyline, PolyLineInfo> polyLineInfoMap = new HashMap<>();
    private GoogleMap mMap;
    private Place selectedStartingPlace;
    private Place selectedDestPlace;
    private DirectionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        ButterKnife.bind(this);
        askPermissions();
        initAutoCompleteFragments();
    }

    private void initAutoCompleteFragments() {

        PlaceAutocompleteFragment autocompleteFragmentStarting = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_starting);
        autocompleteFragmentStarting.setHint("Search for your destination");
        autocompleteFragmentStarting.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Toast.makeText(MapsActivity.this, "Selected Starting point: " + place.getName(), Toast.LENGTH_SHORT).show();
                selectedStartingPlace = place;
            }

            @Override
            public void onError(Status status) {
                Log.d(TAG, status.toString());
            }
        });

        PlaceAutocompleteFragment autocompleteFragmentDest = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_dest);
        autocompleteFragmentDest.setHint("Search for your destination");
        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Toast.makeText(MapsActivity.this, "Selected Ending point: " + place.getName(), Toast.LENGTH_SHORT).show();
                selectedDestPlace = place;
            }

            @Override
            public void onError(Status status) {
                Log.d(TAG, status.toString());
            }
        });

    }

    @OnClick(R.id.bt_find_path)
    public void findPath() {
        removeAllPolyLines();
        callAPI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                listLayout.setVisibility(View.GONE);
                refreshPolyLineColors();
                distanceBWTV.setText("---");
            }
        });
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.d(TAG, polyline.toString());
        refreshPolyLineColors();
        polyline.setColor(Color.DKGRAY);
        PolyLineInfo info = polyLineInfoMap.get(polyline);
        distanceBWTV.setText(String.format("%.1f", info.getDistance() / 1000) + " km");
        if (adapter == null)
            adapter = new DirectionsAdapter(this, info.getDirections());
        else
            adapter.updateList(info.getDirections());
        directionsListView.setAdapter(adapter);
        listLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapFragment.getMapAsync(this);
            } else {
                askPermissions();
            }
        }
    }

    /**
     * Helper method to call API
     */
    private void callAPI() {

        if (selectedStartingPlace == null || selectedStartingPlace.getLatLng() == null) {
            Toast.makeText(MapsActivity.this, "Please choose a starting point", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDestPlace == null || selectedDestPlace.getLatLng() == null) {
            Toast.makeText(MapsActivity.this, "Please choose an ending point", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL).build();

        ApiRapido service = retrofit.create(ApiRapido.class);
        Call<DirectionResults> call = service.getJson(getLatLng(selectedStartingPlace.getLatLng()), getLatLng(selectedDestPlace.getLatLng()), true);
        call.enqueue(new Callback<DirectionResults>() {
            @Override
            public void onResponse(Call<DirectionResults> call, Response<DirectionResults> response) {
                if (response != null && response.body() != null) {
                    List<Route> routes = response.body().getRoutes();
                    handleResponse(routes);
                    mMap.setOnPolylineClickListener(MapsActivity.this);
                }
            }

            @Override
            public void onFailure(Call<DirectionResults> call, Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
    }

    /**
     * helper method to handle the API response
     *
     * @param routes
     */
    private void handleResponse(List<Route> routes) {
        float distance;
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            polyLines = decodePolyLine(route.getOverviewPolyLine().getPoints());
            distance = 0;
            Legs leg = route.getLegs().get(0);
            int stepsSize = leg.getSteps().size();
            directions = new String[stepsSize];
            for (int l = 0; l < stepsSize; l++) {
                Steps steps = leg.getSteps().get(l);
                distance += steps.getDistance().value;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    directions[l] = Html.fromHtml(steps.getInstructions(), FROM_HTML_MODE_LEGACY).toString().replaceAll("\n\n", "\n");
                    if (directions[l].lastIndexOf("\n") > 0) {
                        directions[l] = directions[l].substring(0, directions[l].lastIndexOf("\n"));
                    }
                } else {
                    directions[l] = Html.fromHtml(steps.getInstructions()).toString().replaceAll("\n\n", "\n");
                    if (directions[l].lastIndexOf("\n") > 0) {
                        directions[l] = directions[l].substring(0, directions[l].lastIndexOf("\n"));
                    }
                }
            }
            PolyLineInfo info = new PolyLineInfo();
            info.setDirections(directions);
            info.setDistance(distance);
            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(polyLines)
                    .width(POLYLINE_STROKE_WIDTH_PX)
                    .color(COLORS[i % COLORS.length]));
            polyLineInfoMap.put(polyline, info);
            polyLinesList.add(polyline);
            drawMarkers(leg, stepsSize);
        }
    }

    /**
     * remove all polylines on the map
     */
    private void removeAllPolyLines() {
        for (Polyline polyline : polyLinesList) {
            polyline.remove();
        }
        polyLinesList.clear();
    }

    /**
     * draw start and end markers
     *
     * @param leg
     * @param stepsSize
     */
    private void drawMarkers(Legs leg, int stepsSize) {
        LatLng startLatLng = new LatLng(leg.getSteps().get(0).getStart_location().getLat(), leg.getSteps().get(0).getStart_location().getLng());
        mMap.addMarker(new MarkerOptions().position(startLatLng).title("Starting location"));
        LatLng endLatLng = new LatLng(leg.getSteps().get(stepsSize - 1).getEnd_location().getLat(), leg.getSteps().get(stepsSize - 1).getEnd_location().getLng());
        mMap.addMarker(new MarkerOptions().position(endLatLng).title("Ending location"));
        progressBar.setVisibility(View.GONE);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(startLatLng);
        boundsBuilder.include(endLatLng);
        final LatLngBounds bounds = boundsBuilder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
    }

    /**
     * helper method to reset route lines to their original colors
     */
    private void refreshPolyLineColors() {
        for (int i = 0; i < polyLinesList.size(); i++) {
            polyLinesList.get(i).setColor(COLORS[i % COLORS.length]);
        }
    }

    /**
     * ask for location permissions
     */
    public void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
    }

    /**
     * helper method to get Latitude and Longitude
     *
     * @param latLng
     * @return
     */
    private String getLatLng(LatLng latLng) {
        return latLng.latitude + "," + latLng.longitude;
    }

    /**
     * helper method to decode a polyline into a list of LatLng's
     *
     * @param poly
     * @return
     */
    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }


}
