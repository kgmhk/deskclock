package com.gkwak.deskclock.weather;

/**
 * Created by 기현 on 2015-12-13.
 */
public class Weather {
    int lat;
    int ion;
    int temprature;
    int cloudy;
    String city;
    String weather_desc;

    public void setLat(int lat){ this.lat = lat;}
    public void setIon(int ion){ this.ion = ion;}
    public void setTemprature(int t){ this.temprature = t;}
    public void setCloudy(int cloudy){ this.cloudy = cloudy;}
    public void setCity(String city){ this.city = city;}
    public void setWeather_desc(String weather_desc){ this.weather_desc = weather_desc; }

    public int getLat(){ return lat;}
    public int getIon() { return ion;}
    public int getTemprature() { return temprature;}
    public int getCloudy() { return cloudy; }
    public String getCity() { return city; }
    public String getWeather_desc() { return weather_desc; }
}
