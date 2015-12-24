package com.gkwak.deskclock.network;

import android.os.AsyncTask;

import com.gkwak.deskclock.weather.Weather;

/**
 * Created by 기현 on 2015-12-13.
 */
public class OpenWeatherAPITask extends AsyncTask<Integer, Void, Weather> {
    @Override
    public Weather doInBackground(Integer... params) {
        OpenWeatherAPIClient client = new OpenWeatherAPIClient();

        int lat = params[0];
        int lon = params[1];
        // API 호출
        Weather w = client.getWeather(lat,lon);

        //System.out.println("Weather : "+w.getTemprature());

        // 작업 후 리
        return w;
    }
}
