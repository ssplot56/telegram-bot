package com.splot.bot.service;

import com.splot.bot.model.Weather;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class WeatherService {
    private static final String apiEndPoint="https://weather.visualcrossing.com"
            + "/VisualCrossingWebServices/rest/services/timeline/";
    private String city;
    private static final String unitGroup="metric"; //us,metric,uk
    private static final String apiKey="NCDHRXZGEX6NMRMBBKTWVN3KE";


    public List<Weather> timelineRequestHttpClient() throws Exception {

        URIBuilder builder = new URIBuilder(apiEndPoint
                + URLEncoder.encode(city, StandardCharsets.UTF_8));

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
            log.error("No raw data");
            return null;
        }

        JSONObject timelineResponse = new JSONObject(rawResult);
        ZoneId zoneId=ZoneId.of(timelineResponse.getString("timezone"));
        String location = timelineResponse.getString("resolvedAddress");
        JSONArray values = timelineResponse.getJSONArray("days");

        List<Weather> weatherList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            JSONObject dayValue = values.getJSONObject(i);

            ZonedDateTime datetime=ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(dayValue.getLong("datetimeEpoch")), zoneId);
            Weather weather = new Weather();
            weather.setDate(datetime);
            weather.setLocationName(location);
            weather.setDescription(dayValue.getString("description"));
            weather.setCurrentTemp(dayValue.getDouble("temp"));
            weather.setFeelsLikeTemp(dayValue.getDouble("feelslike"));
            weather.setMaxTemp(dayValue.getDouble("tempmax"));
            weather.setMinTemp(dayValue.getDouble("tempmin"));
            weather.setIcon(dayValue.getString("icon"));
            weather.setCondition(dayValue.getString("conditions"));
            weatherList.add(weather);
        }
        return weatherList;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
