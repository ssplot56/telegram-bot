package com.splot.bot.service;

import com.splot.bot.model.Weather;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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

import static com.splot.bot.config.Constants.Logger.BAD_RESPONSE;
import static com.splot.bot.config.Constants.Logger.NO_RAW_DATA;
import static com.splot.bot.config.Constants.WeatherApi.API_KEY;
import static com.splot.bot.config.Constants.WeatherApi.CONDITIONS;
import static com.splot.bot.config.Constants.WeatherApi.DATETIME_EPOCH;
import static com.splot.bot.config.Constants.WeatherApi.DAYS;
import static com.splot.bot.config.Constants.WeatherApi.DESCRIPTION;
import static com.splot.bot.config.Constants.WeatherApi.FEELS_LIKE;
import static com.splot.bot.config.Constants.WeatherApi.ICON;
import static com.splot.bot.config.Constants.WeatherApi.KEY;
import static com.splot.bot.config.Constants.WeatherApi.NUMBER_OF_DAYS;
import static com.splot.bot.config.Constants.WeatherApi.RESOLVED_ADDRESS;
import static com.splot.bot.config.Constants.WeatherApi.TEMP;
import static com.splot.bot.config.Constants.WeatherApi.TEMP_MAX;
import static com.splot.bot.config.Constants.WeatherApi.TEMP_MIN;
import static com.splot.bot.config.Constants.WeatherApi.TIMEZONE;
import static com.splot.bot.config.Constants.WeatherApi.UNIT_GROUP;
import static com.splot.bot.config.Constants.WeatherApi.UNIT_GROUP_TYPE;
import static com.splot.bot.config.Constants.WeatherApi.WEATHER_API_ENDPOINT;

@Service
@Log4j2
public class WeatherService {

    public List<Weather> timelineRequestHttpClient(String city) throws Exception {
        URIBuilder builder = new URIBuilder(WEATHER_API_ENDPOINT
                + URLEncoder.encode(city, StandardCharsets.UTF_8));

        builder.setParameter(UNIT_GROUP, UNIT_GROUP_TYPE)
                .setParameter(KEY, API_KEY);

        HttpGet get = new HttpGet(builder.build());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(get);

        String rawResult = null;

        try {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error(BAD_RESPONSE.formatted(response.getStatusLine().toString()));
                return null;
            }

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                rawResult = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
        } finally {
            response.close();
        }

        return parseTimelineJson(rawResult);
    }

    private List<Weather> parseTimelineJson(String rawResult) {
        if (rawResult == null || rawResult.isEmpty()) {
            log.error(NO_RAW_DATA);
            return null;
        }

        JSONObject timelineResponse = new JSONObject(rawResult);
        ZoneId zoneId = ZoneId.of(timelineResponse.getString(TIMEZONE));
        String location = timelineResponse.getString(RESOLVED_ADDRESS);
        JSONArray values = timelineResponse.getJSONArray(DAYS);

        List<Weather> weatherList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_DAYS; i++) {
            JSONObject dayValue = values.getJSONObject(i);
            Weather weatherForDay = createWeatherForDay(dayValue, location, zoneId);
            weatherList.add(weatherForDay);
        }

        return weatherList;
    }

    private Weather createWeatherForDay(JSONObject dayValue, String location, ZoneId zoneId) {
        Weather weather = new Weather();

        ZonedDateTime datetime = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(dayValue.getLong(DATETIME_EPOCH)), zoneId
        );

        weather.setDate(datetime);
        weather.setLocationName(location);
        weather.setDescription(dayValue.getString(DESCRIPTION));
        weather.setCurrentTemp(dayValue.getDouble(TEMP));
        weather.setFeelsLikeTemp(dayValue.getDouble(FEELS_LIKE));
        weather.setMaxTemp(dayValue.getDouble(TEMP_MAX));
        weather.setMinTemp(dayValue.getDouble(TEMP_MIN));
        weather.setIcon(dayValue.getString(ICON));
        weather.setCondition(dayValue.getString(CONDITIONS));

        return weather;
    }

}
