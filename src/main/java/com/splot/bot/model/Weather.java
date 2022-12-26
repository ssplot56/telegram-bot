package com.splot.bot.model;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Weather {
    private String locationName;
    private ZonedDateTime date;
    private Double currentTemp;
    private Double feelsLikeTemp;
    private Double maxTemp;
    private Double minTemp;
    private String description;
    private String icon;
    private String condition;
}
