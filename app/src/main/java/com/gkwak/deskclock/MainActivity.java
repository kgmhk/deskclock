package com.gkwak.deskclock;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkwak.deskclock.coustomdialog.CustomDialog;
import com.gkwak.deskclock.gps.Gps;
import com.gkwak.deskclock.network.OpenWeatherAPITask;
import com.gkwak.deskclock.weather.Weather;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

//요것은 테스트임당 요것도
public class MainActivity extends Activity {
    CustomDialog dialog;
    TextView daily_text_view;
    RelativeLayout r_layout;
    LocationManager locationManager;
    Gps gps;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };

    private static final int INITIAL_REQUEST=1337;
    private static final int CAMERA_REQUEST=INITIAL_REQUEST+1;
    private static final int CONTACTS_REQUEST=INITIAL_REQUEST+2;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!canAccessLocation() || !canAccessContacts()) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }

        dialog = new CustomDialog(this);
        daily_text_view = (TextView)findViewById(R.id.daily_text_view);
        r_layout = (RelativeLayout) findViewById(R.id.r_layout);
        // GPS Class
        gps = new Gps();
        // Location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        // BackGround
        Resources res = getResources(); //resource handle
        Drawable drawable = res.getDrawable(R.drawable.rain);
        // TODO :  다이얼로그로부터 문자 받아오기.
        daily_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canAccessContacts() {
        return(hasPermission(Manifest.permission.READ_CONTACTS));
    }


    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }

    private void getWeatehr (Double Long, Double Lati) {

        Log.v("getWeatehr GPS",Long.toString() + Lati.toString());

        int gpsLong = Long.intValue();
        int gpsLati = Lati.intValue();

        Log.v("getWeatehr GPS", String.valueOf(gpsLong) + '/' + String.valueOf(gpsLati) );
        // 날씨를 읽어오는 API 호출
        OpenWeatherAPITask t= new OpenWeatherAPITask();
        try {

            Weather w = t.execute(gpsLati, gpsLong).get();

            System.out.println("Temp :"+w.getTemprature());
            System.out.println("현재 날씨 :"+w.getWeather_desc());

            String temperature = String.valueOf(w.getTemprature());

//            r_layout.setBackground(drawable);
            //여기에 날씨를 비교하여 뒷 배경을 바꿔볼까?

//            daily_text_view.setText(temperature);
            //w.getTemprature());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            System.out.println("long" + longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            System.out.println("lat" + latitude);

            gps.setLong(loc.getLongitude());
            gps.setLati(loc.getLatitude());

        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
            catch (IOException e) {
                Log.e("err", e.toString());
                e.printStackTrace();
            }
            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                    + cityName;


            getWeatehr(gps.getLong(), gps.getLati());
            Log.v("test", s);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TestAppActivity", "onResume" + dialog.get());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
