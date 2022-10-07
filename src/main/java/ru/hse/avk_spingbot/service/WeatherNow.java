package ru.hse.avk_spingbot.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("application.properties")
public class WeatherNow {
    private static HttpURLConnection conn;

    public static String getWeatherNow(String city, String weatherToken, String weatherURL) {

        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        try{
            URL url = new URL(String.format(weatherURL, city, weatherToken));
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);

            // Test if the response from the server is successful
            int status = conn.getResponseCode();

            if (status >= 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
                return null;
            }
            else {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }
            List<String> result = parse(responseContent.toString());
            return String.format("В городе %s %s \nТемпература: %s℃, ощущается как: %s℃", city, result.get(0), result.get(1), result.get(2));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

        return null;
    }
    private static List<String> parse(String responseBody) {
        JSONObject jobj = new JSONObject(responseBody);
        JSONObject jWeather = jobj.getJSONArray("weather").getJSONObject(0);
        JSONObject jMain = jobj.getJSONObject("main");

        List<String> arr = new ArrayList<>();

        arr.add(jWeather.getString("description"));
        arr.add(String.valueOf(jMain.getDouble("temp")));
        arr.add(String.valueOf(jMain.getDouble("feels_like")));

        return arr;
    }
}
