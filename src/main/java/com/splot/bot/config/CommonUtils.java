package com.splot.bot.config;

import com.vdurmont.emoji.EmojiParser;

import static com.splot.bot.config.Constants.StringPattern.CITY_REGEX;

public class CommonUtils {

    public static final class Strings {
        public static String normalizeString(String string) {
            return string.trim().toLowerCase();
        }

        public static boolean isCorrectCityPattern(String messageText) {
            return messageText.matches(CITY_REGEX);
        }
    }

    public static final class Smiles {
        public static String parseSmile(String condition) {
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

}
