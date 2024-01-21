package com.splot.bot.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
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
