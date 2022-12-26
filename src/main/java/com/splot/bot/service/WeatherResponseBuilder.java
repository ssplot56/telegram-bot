package com.splot.bot.service;

import com.splot.bot.model.Weather;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WeatherResponseBuilder {
    private final WeatherService weatherService;
    private final SmileParser smileParser;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EE, d LLLL");

    public WeatherResponseBuilder(WeatherService weatherService, SmileParser smileParser) {
        this.weatherService = weatherService;
        this.smileParser = smileParser;
    }

    @SneakyThrows
    public String buildForecastMessage() {
        List<Weather> weatherList = weatherService.timelineRequestHttpClient();
        StringBuilder forecastString = new StringBuilder("Forecast in <b>"
                + weatherList.get(0).getLocationName() + ":</b>\n\n");

        for (Weather weather : weatherList) {
            forecastString.append("<b>").append(weather.getDate().format(FORMATTER)).append(":</b>")
                    .append(" from ").append(weather.getMinTemp()).append("℃")
                    .append(" to ").append(weather.getMaxTemp()).append("℃ ")
                    .append(smileParser.parseSmile(weather.getIcon()))
                    .append("\n\n");
        }
        return forecastString.toString();
    }

    @SneakyThrows
    public String buildTodayWeatherMessage() {
        Weather weather = weatherService.timelineRequestHttpClient().get(0);
        return "Currently in <b>" +
                weather.getLocationName() + "</b>:\n\n" +
                "<b>•Conditions</b>: " + weather.getCondition() + " " +
                smileParser.parseSmile(weather.getIcon()) + "\n" +
                "<b>•Temperature</b>: " + weather.getCurrentTemp() + "℃\n" +
                "<b>•Feels like</b>: " + weather.getFeelsLikeTemp() + "℃\n" +
                "<b>•Max temperature</b>: " + weather.getMaxTemp() + "℃\n" +
                "<b>•Min temperature</b>: " + weather.getMinTemp() + "℃\n" +
                "<b>•Description</b>: " + weather.getDescription();
    }
}
