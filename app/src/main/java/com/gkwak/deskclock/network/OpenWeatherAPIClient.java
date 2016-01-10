package com.gkwak.deskclock.network;



import android.util.Log;

import com.gkwak.deskclock.weather.Weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by terry on 2015. 8. 27..
 * 목표
 * 1. AsyncTask와 HTTPURLConnection을 이용한 간단한 HTTP 호출 만들기
 * 2. 리턴된 JSON을 파싱하는 방법을 통하여, JSON 객체 다루는 법 습득하기
 * 3. Phone Location (GPS) API 사용 방법 파악하기
 *
 * 참고 자료 : http://developer.android.com/training/basics/network-ops/connecting.html
 * */
public class OpenWeatherAPIClient {
    final static String openWeatherURL = "http://api.openweathermap.org/data/2.5/weather";
    public Weather getWeather(int lat,int lon){
        Weather w = new Weather();
//        String urlString = openWeatherURL + "?lat=35&lon=139&appid=2de143494c0b295cca9337e1e96b00e0";
        String urlString = openWeatherURL + "?lat="+lat+"&lon="+lon + "&appid=2de143494c0b295cca9337e1e96b00e0";

        Log.v("getOpenURL : ", urlString);
        try {
            // call API by using HTTPURLConnection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
//            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(in));

            // parse JSON
            w = parseJSON(json);
            w.setIon(lon);
            w.setLat(lat);


        }catch(MalformedURLException e){
            System.err.println("Malformed URL");
            e.printStackTrace();
            return null;

        }catch(JSONException e) {
            System.err.println("JSON parsing error");
            e.printStackTrace();
            return null;
        }catch(IOException e){
            System.err.println("URL Connection failed");
            e.printStackTrace();
            return null;
        }

        // set Weather Object

        return w;
    }

    private Weather parseJSON(JSONObject json) throws JSONException {
        Weather w = new Weather();
        w.setTemprature(json.getJSONObject("main").getInt("temp"));
        w.setCity(json.getString("name"));
        w.setWeather_desc(json.getJSONArray("weather").getJSONObject(0).getString("main"));
        //w.setCloudy();

        return w;
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
}
