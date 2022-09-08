package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CctvActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener {

    private int apiRequestCount;
    public static GoogleMap mMap;
    MapFragment mapFragment;
    private ArrayList<Marker> markerList = new ArrayList();
    public static ArrayList<CctvPoint> Cctv_list = new ArrayList();
    public static boolean startFlagForCctvApi;

    private FusedLocationProviderClient mFusedLocationClient;

    Geocoder geocoder;
    Address addressPoint;

    EditText findStreet;
    Button submitBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findcctv);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.cctvMap);
        mapFragment.getMapAsync(this);
        findStreet = (EditText) findViewById(R.id.findstreet);
        submitBtn = (Button) findViewById(R.id.submitBtn);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = findStreet.getText().toString().trim();

                List<Address> addressResult = null;
                try {
                    addressResult = geocoder.getFromLocationName(address,1);
                }catch (IOException ioException){
                    Toast.makeText(getApplicationContext(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
                }catch (IllegalArgumentException illegalArgumentException){
                    Toast.makeText(getApplicationContext(), "잘못돈 주소입니다", Toast.LENGTH_LONG).show();
                }

                if (addressResult == null || addressResult.size() == 0){
                    Toast.makeText(getApplicationContext(),"주소 미발견",Toast.LENGTH_LONG).show();
                }else {
                    addressPoint = addressResult.get(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(addressPoint.getLatitude(), addressPoint.getLongitude()), 15));
                }
            }
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //리스너 달아주기
        mMap.setOnCameraIdleListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.510759, 126.977943), 15));

    }
    @Override
    public void onCameraIdle() {
        removeMarkerAll();

        String lat = String.valueOf(mMap.getCameraPosition().target.latitude);
        String lon = String.valueOf(mMap.getCameraPosition().target.longitude);
        startFlagForCctvApi = true;
        CctvActivity.CctvApi coronaApi = new CctvActivity.CctvApi();
        coronaApi.execute(lat,lon,"");

        apiRequestCount = 0;
        final Handler temp_handler = new Handler();
        temp_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (apiRequestCount < 100) {
                    if (startFlagForCctvApi) {
                        apiRequestCount++;
                        temp_handler.postDelayed(this, 100);
                    } else {
                        //api 호출이 완료 되었을 떄
                        drawMarker();
                    }
                } else {
                    //api 호출이 10초 이상 경괴했을 때
                    Toast.makeText(getApplicationContext(), "호출에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                }

            }


        }, 100);

    }
    public class CctvApi extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d("Task3", "POST");
            String temp = "Not Gained";
            try {
                temp = GET(strings[0], strings[1]);
                Log.d("REST", temp);
                return temp;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return temp;
        }
    }

    private String GET(String x, String y) throws IOException {
        String corona_API = "https://api.odcloud.kr/api/15077586/v1/centers?page=1&perPage=150&serviceKey=VaU%2Fh4AmMAsiSfCcZw66RZgvvjp9vReO6Qp02uosyBqMCbzJxGoTh6bJ7v4B5M%2FTMIab1jr%2B1StG4g4c2ZIhLA%3D%3D";

        String data = "";

        try {
            URL url = new URL(corona_API);
            Log.d("CctvApi", "The response is :" + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            String line;
            String result = "";
            BufferedReader bf;
            bf = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = bf.readLine()) != null) {
                result = result.concat(line);
            }
            Log.d("CoronaApi", "The response is :" + result);
            JSONObject root = new JSONObject(result);

            JSONArray coronaArray = root.getJSONArray("data");
            for (int i = 0; i < coronaArray.length(); i++) {
                JSONObject item = coronaArray.getJSONObject(i);
                Log.d("Cctv", item.getString("address"));
                CctvPoint cctv_item = new CctvPoint(
                        item.getString("lat"),
                        item.getString("lng"),
                        item.getString("address")
                );
                CctvActivity.Cctv_list.add(cctv_item);
            }
            startFlagForCctvApi = false;
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    private void removeMarkerAll() {
        for (Marker marker : markerList) {
            marker.remove();
        }
    }

    public class CctvPoint {

        private String latitude;
        private String longitude;
        private String address;

        public CctvPoint(String address, String latitude, String longitude){
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        public String getAddr(){return address;}
        public void setAddress(String address){this.address = address;}
        public String getLatitude(){return latitude;}
        public void setLatitude(String latitude){this.latitude = latitude;}
        public String getLongitude(){return longitude;}
        public void setLongitude(String longitude){this.longitude = longitude;}
    }

    private void drawMarker() {
        for (int i =0 ; i< Cctv_list.size(); i++){
            CctvPoint item = Cctv_list.get(i);
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(Double.parseDouble(item.getAddr()), Double.parseDouble(item.getLatitude()) ));
            marker.title(item.getLongitude());
            //marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(marker);
        }
        return;
    }
}
