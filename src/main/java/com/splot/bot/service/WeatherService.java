package com.splot.bot.service;

import com.splot.bot.model.Weather;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {
    private static final String apiEndPoint="https://weather.visualcrossing.com"
            + "/VisualCrossingWebServices/rest/services/timeline/";
    private static final String LOCATION="Kyiv";
    private static final String startDate=null; //optional (omit for forecast)
    private static final String endDate=null; //optional (requires a startDate if present)
    private static final String unitGroup="metric"; //us,metric,uk
    private static final String apiKey="NCDHRXZGEX6NMRMBBKTWVN3KE";

    public List<Weather> timelineRequestHttpClient() throws Exception {

        StringBuilder requestBuilder=new StringBuilder(apiEndPoint);
        requestBuilder.append(URLEncoder.encode(LOCATION, StandardCharsets.UTF_8));

        if (startDate!=null && !startDate.isEmpty()) {
            requestBuilder.append("/").append(startDate);
            if (endDate!=null && !endDate.isEmpty()) {
                requestBuilder.append("/").append(endDate);
            }
        }

        URIBuilder builder = new URIBuilder(requestBuilder.toString());

        builder.setParameter("unitGroup", unitGroup)
                .setParameter("key", apiKey);



        HttpGet get = new HttpGet(builder.build());

        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response = httpclient.execute(get);

        String rawResult = null;
        try {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                System.out.printf("Bad response status code:%d%n", response.getStatusLine().getStatusCode());
                return null;
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                rawResult= EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }


        } finally {
            response.close();
        }

        return parseTimelineJson(rawResult);

    }
    private List<Weather> parseTimelineJson(String rawResult) {

        if (rawResult == null || rawResult.isEmpty()) {
            System.out.printf("No raw data%n");
            return null;
        }

        JSONObject timelineResponse = new JSONObject(rawResult);

        ZoneId zoneId=ZoneId.of(timelineResponse.getString("timezone"));

        System.out.printf("Weather data for: %s%n", timelineResponse.getString("resolvedAddress"));

        JSONArray values=timelineResponse.getJSONArray("days");

        System.out.printf("Date\tMaxTemp\tMinTemp\tPrecip\tSource%n");
        List<Weather> weatherList = new ArrayList<>();
        for (int i = 0; i < values.length(); i++) {
            JSONObject dayValue = values.getJSONObject(i);

            ZonedDateTime datetime=ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(dayValue.getLong("datetimeEpoch")), zoneId);
            Weather weather = new Weather();
            weather.setCurrentTemp(dayValue.getDouble("temp"));
            weather.setFeelsLikeTemp(dayValue.getDouble("feelslike"));
            weather.setMaxTemp(dayValue.getDouble("tempmax"));
            weather.setMinTemp(dayValue.getDouble("tempmin"));
            weatherList.add(weather);
        }
        return weatherList;
    }
}
