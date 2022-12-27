package com.splot.bot.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;

@Service
public class SmileParser {
    public String parseSmile(String condition) {
        switch (condition) {
            case "snow":
                return EmojiParser.parseToUnicode(":cloud_snow:");
            case "rain":
                return EmojiParser.parseToUnicode(":cloud_rain:");
            case "fog":
                return EmojiParser.parseToUnicode(":fog:");
            case "wind":
                return EmojiParser.parseToUnicode(":wind_face:");
            case "cloudy":
                return EmojiParser.parseToUnicode(":cloud:");
            case "partly-cloudy-day":
                return EmojiParser.parseToUnicode(":partly_sunny:");
            case "partly-cloudy-night":
                return EmojiParser.parseToUnicode(":full_moon_with_face:");
            default:
                return EmojiParser.parseToUnicode(":sunny:");
        }
    }
}
