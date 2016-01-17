package com.gkwak.deskclock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.DigitalClock;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkwak.deskclock.coustomdialog.CustomDialog;
import com.gkwak.deskclock.gps.Gps;
import com.gkwak.deskclock.network.OpenWeatherAPITask;
import com.gkwak.deskclock.weather.Weather;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.adapters.BitmapColorAdapter;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {
    private static final int SELECT_IMAGE = 1;
    private static final String TAG = "MainActivity";
    MaterialCalendarView mCal;
    DigitalClock digital_clock;
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
    private static String DAILY_TEXT_PREF = "dailyTextPref";
    private static String DAILY_IMAGE_PREF = "dailyImagePref";
    private static String DAILY_COLOR_PREF = "dailyColorPref";
    private static String DAILY_TEXT = "dailyText";
    private static String DAILY_IMAGE = "dailyImage";
    private static String DAILY_COLOR = "dailyColor";
    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Do not turn off Screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, SplashActivity.class));

        // Google Analytics

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        CalendarView cal = new CalendarView(this);
        dialog = new CustomDialog(this);
        daily_text_view = (TextView)findViewById(R.id.daily_text_view);
        r_layout = (RelativeLayout) findViewById(R.id.r_layout);
        digital_clock = (DigitalClock) findViewById(R.id.digitalClock);

//         Permission Check
//        if (!canAccessLocation() || !canAccessContacts()) {
//            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
//        }
// set Today Date
        mCal = (MaterialCalendarView) findViewById(R.id.calendarView);
        Calendar c = Calendar.getInstance();
        mCal.setDateSelected(c,true);

        // GPS Class
        gps = new Gps();
        // Location
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        LocationListener locationListener = new MyLocationListener();
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        // BackGround
        Resources res = getResources(); //resource handle
        Drawable drawable = res.getDrawable(R.drawable.rain);
        //SharedPreference
        if (!getDailyText().equals("")) daily_text_view.setText(getDailyText());
        if (!getDailyImage().equals("")) {
            Bitmap bitmap = decodeBase64(getDailyImage());
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            r_layout.setBackgroundDrawable(ob);
        }
        if (getDailyColor() != 0) {
            mCal.setSelectionColor(getDailyColor());
            daily_text_view.setTextColor(getDailyColor());
            digital_clock.setTextColor(getDailyColor());
        }


        // TODO : 리팩토링
        daily_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View dialogCustomView = inflater.inflate(R.layout.custom_dialog, null);
                final TextView etName = (EditText) dialogCustomView.findViewById(R.id.dailyText);
                final LobsterPicker lobsterPicker = (LobsterPicker) dialogCustomView.findViewById(R.id.lobsterpicker);
                etName.setText(getDailyText());
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.custom_dialog_title)
                        .setView(dialogCustomView)
                        .setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String daily_text = etName.getText().toString();
                                setDailyText(daily_text);
                                setDailyColor(lobsterPicker.getColor());

                                daily_text_view.setText(daily_text);
                                mCal.setSelectionColor(lobsterPicker.getColor());
                                daily_text_view.setTextColor(lobsterPicker.getColor());
                                digital_clock.setTextColor(lobsterPicker.getColor());
                            }
                        })
                        .setNeutralButton(R.string.backgroun_change, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openGallery();
                            }
                        })
                        .setNegativeButton(R.string.no_btn, null).create();
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

    private String getDailyText() {
        SharedPreferences dailyText = getSharedPreferences(DAILY_TEXT_PREF, MODE_PRIVATE);
        return dailyText.getString(DAILY_TEXT,"");
    }

    private String getDailyImage() {
        SharedPreferences dailyImage = getSharedPreferences(DAILY_IMAGE_PREF, MODE_PRIVATE);
        return dailyImage.getString(DAILY_IMAGE,"");
    }

    private int getDailyColor() {
        SharedPreferences dailyColor = getSharedPreferences(DAILY_COLOR_PREF, MODE_PRIVATE);
        return dailyColor.getInt(DAILY_COLOR, 0);
    }

    private void setDailyText(String daily_text){
        SharedPreferences pref = getSharedPreferences(DAILY_TEXT_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DAILY_TEXT, daily_text);
        editor.commit();
    }

    private void setDailyImage(String daily_image){
        SharedPreferences pref = getSharedPreferences(DAILY_IMAGE_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DAILY_IMAGE, daily_image);
        editor.commit();
    }

    private void setDailyColor(int daily_color){
        SharedPreferences pref = getSharedPreferences(DAILY_COLOR_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(DAILY_COLOR, daily_color);
        editor.commit();
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

            Log.d("onLocationChanged : ","on");
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
        public void onProviderEnabled(String provider) {
            Log.v("onProviderEnabled", "onProviderEnabled");
        }

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
        Log.d("TestAppActivity", "onResume");

        Log.i(TAG, "Setting screen name Main ");
        mTracker.setScreenName("Main");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_change_backgroun) {
            Log.d("select menu " , " ");
            openGallery();
            return true;
        }

        if (id == R.id.menu_change_text_color) {

//            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
//            final View dialogCustomView = inflater.inflate(R.layout.custom_dialog_color, null);
//            final LobsterPicker lobsterPicker = (LobsterPicker) dialogCustomView.findViewById(R.id.lobsterpicker);
//            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("Select Color")
//                    .setView(dialogCustomView)
//                    .setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            Log.d("color : ", lobsterPicker.getColor() + "");
//                            setDailyColor(lobsterPicker.getColor());
//                            mCal.setSelectionColor(lobsterPicker.getColor());
//                            daily_text_view.setTextColor(lobsterPicker.getColor());
//                            digital_clock.setTextColor(lobsterPicker.getColor());
//                        }
//                    })
//                    .setNegativeButton(R.string.no_btn, null).create();
//            dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == MainActivity.this.RESULT_OK) {
                if (data != null) {
                    try {
                        Log.d("onActivityResult", "give a photo");
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), data.getData());
                        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                        r_layout.setBackgroundDrawable(ob);

                        setDailyImage(encodeTobase64(bitmap));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (resultCode == MainActivity.this.RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // method for bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    // method for base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }


}
