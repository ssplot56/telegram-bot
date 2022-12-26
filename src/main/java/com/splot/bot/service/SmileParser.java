package com.splot.bot.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;

@Service
public class SmileParser {
    public String parseSmile(String condition) {
        return EmojiParser.parseToUnicode(switch (condition) {
            case "snow" -> ":cloud_snow:";
            case "rain" -> ":cloud_rain:";
            case "fog" -> ":fog:";
            case "wind" -> ":wind_face:";
            case "cloudy" -> ":cloud:";
            case "partly-cloudy-day" -> ":partly_sunny:";
            case "partly-cloudy-night" -> ":full_moon_with_face:";
            default -> ":sunny:";
        });
    }
}
