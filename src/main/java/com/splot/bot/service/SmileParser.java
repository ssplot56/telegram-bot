package com.splot.bot.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;

@Service
public class SmileParser {
    public String parseSmile(String condition) {
        return switch (condition) {
            case "snow" -> EmojiParser.parseToUnicode(":cloud_snow:");
            case "rain" -> EmojiParser.parseToUnicode(":cloud_rain:");
            case "fog" -> EmojiParser.parseToUnicode(":fog:");
            case "wind" -> EmojiParser.parseToUnicode(":wind_face:");
            case "cloudy" -> EmojiParser.parseToUnicode(":cloud:");
            case "partly-cloudy-day" -> EmojiParser.parseToUnicode(":partly_sunny:");
            case "partly-cloudy-night" -> EmojiParser.parseToUnicode(":full_moon_with_face:");
            default -> EmojiParser.parseToUnicode(":sunny:");
        };
    }
}
