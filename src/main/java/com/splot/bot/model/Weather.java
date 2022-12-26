package com.splot.bot.model;

import java.time.ZonedDateTime;
import lombok.Data;

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
