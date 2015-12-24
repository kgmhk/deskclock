package com.gkwak.deskclock;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gkwak.deskclock.coustomdialog.CustomDialog;
import com.gkwak.deskclock.network.OpenWeatherAPITask;
import com.gkwak.deskclock.weather.Weather;

import java.util.concurrent.ExecutionException;

//요것은 테스트임당
public class MainActivity extends AppCompatActivity {
    CustomDialog dialog;
    TextView daily_text_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new CustomDialog(this);
        daily_text_view = (TextView)findViewById(R.id.daily_text_view);

        daily_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        // 날씨를 읽어오는 API 호출
        OpenWeatherAPITask t= new OpenWeatherAPITask();
        try {
            Weather w = t.execute(35,139).get();

            System.out.println("Temp :"+w.getTemprature());
            System.out.println("현재 날씨 :"+w.getWeather_desc());

            String temperature = String.valueOf(w.getTemprature());

            //여기에 날씨를 비교하여 뒷 배경을 바꿔볼까?

//            daily_text_view.setText(temperature);
            //w.getTemprature());


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
