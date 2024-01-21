package com.splot.bot.service;

import com.splot.bot.model.Weather;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.splot.bot.config.CommonUtils.Smiles.parseSmile;
import static com.splot.bot.config.Constants.StringPattern.DATE_TIME_PATTERN;

@Service
public class WeatherResponseService {

    @Autowired
    private WeatherService weatherService;

    @SneakyThrows
    public String buildForecastMessage(String city) {
        List<Weather> weatherList = weatherService.timelineRequestHttpClient(city);
        StringBuilder forecastString = new StringBuilder("Forecast in <b>"
                + weatherList.getFirst().getLocationName() + ":</b>\n\n");

        for (Weather weather : weatherList) {
            forecastString.append("<b>")
                    .append(weather.getDate().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                    .append(":</b>")
                    .append(" from ").append(weather.getMinTemp()).append("℃")
                    .append(" to ").append(weather.getMaxTemp()).append("℃ ")
                    .append(parseSmile(weather.getIcon()))
                    .append("\n\n");
        }
        return forecastString.toString();
    }

    @SneakyThrows
    public String buildTodayWeatherMessage(String city) {
        Weather weather = weatherService.timelineRequestHttpClient(city).getFirst();
        return "Currently in <b>" +
                weather.getLocationName() + "</b>:\n\n" +
                "<b>•Conditions</b>: " + weather.getCondition() + " " +
                parseSmile(weather.getIcon()) + "\n" +
                "<b>•Temperature</b>: " + weather.getCurrentTemp() + "℃\n" +
                "<b>•Feels like</b>: " + weather.getFeelsLikeTemp() + "℃\n" +
                "<b>•Max temperature</b>: " + weather.getMaxTemp() + "℃\n" +
                "<b>•Min temperature</b>: " + weather.getMinTemp() + "℃\n" +
                "<b>•Description</b>: " + weather.getDescription();

    }

    @SneakyThrows
    public String getCorrectCityName(String city) {
        List<Weather> weathers = weatherService.timelineRequestHttpClient(city);
        return weathers.getFirst().getLocationName();
    }

    @SneakyThrows
    public boolean isAvailableCity(String city) {
        return weatherService.timelineRequestHttpClient(city) != null;
    }

}
