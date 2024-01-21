package com.splot.bot.config;

import com.vdurmont.emoji.EmojiParser;

public class Constants {

    public static final class WeatherApi {
        public static final String WEATHER_API_ENDPOINT = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
        public static final String UNIT_GROUP_TYPE = "metric";
        public static final String KEY = "key";
        public static final String UNIT_GROUP = "unitGroup";
        public static final String TIMEZONE = "timezone";
        public static final String RESOLVED_ADDRESS = "resolvedAddress";
        public static final String DAYS = "days";
        public static final String DATETIME_EPOCH = "datetimeEpoch";
        public static final String DESCRIPTION = "description";
        public static final String TEMP = "temp";
        public static final String FEELS_LIKE = "feelslike";
        public static final String TEMP_MAX = "tempmax";
        public static final String TEMP_MIN = "tempmin";
        public static final String ICON = "icon";
        public static final String CONDITIONS = "conditions";
        public static final String BASIC_CITY = "Kyiv";
        public static final int NUMBER_OF_DAYS = 7;
    }

    public static final class TelegramApi {
        public static final String HTML = "HTML";
        public static final String CURRENT_WEATHER = "current weather";
        public static final String WEATHER_FORECAST = "weather forecast";
        public static final String CHANGE_CITY = "change city";
        public static final String ENABLE_REMINDER = "enable reminder";
        public static final String DISABLE_REMINDER = "disable reminder";
    }

    public static final class BotCommand {
        public static final String START_COMMAND = "/start";
        public static final String WEATHER_COMMAND = "/weather";
        public static final String FORECAST_COMMAND = "/forecast";
        public static final String CHANGE_CITY_COMMAND = "/change";
        public static final String HELP_COMMAND = "/help";
        public static final String SCHEDULE_COMMAND = "/schedule";
        public static final String DISABLE_SCHEDULE_COMMAND = "/schedule_disable";
    }

    public static final class CommandDescription {
        public static final String START_DESCRIPTION = "try it :)";
        public static final String WEATHER_DESCRIPTION = "receive actual weather";
        public static final String FORECAST_DESCRIPTION = "receive 7-day forecast";
        public static final String CHANGE_CITY_DESCRIPTION = "change current city";
        public static final String HELP_DESCRIPTION = "list of actual commands";
        public static final String SCHEDULE_DESCRIPTION = "make a daily reminder about the weather";
        public static final String DISABLE_SCHEDULE_DESCRIPTION = "to disable everyday weather reminder";
    }

    public static final class BotMessages {
        public static final String HELP_MESSAGE =
                """
                        Type /start to receive greetings\s
                        Type /weather to receive weather in your city\s
                        Type /forecast to receive 7-day forecast\s
                        Type /schedule to set everyday reminder\s
                        Type /schedule_disable to disable everyday reminder\s
                        """;
        public static final String INCORRECT_CITY_MESSAGE =
                "Incorrect city format or non-existent city, do not use digits and symbols! Try again";
        public static final String TYPE_CITY_MESSAGE = "Type your city:\n";
        public static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, command was not recognized.";
        public static final String CITY_WAS_UPDATED_MESSAGE = "Data was updated, current city - %s";
        public static final String DEFAULT_CITY_MESSAGE = "You city by default - Kyiv, to change city type /change";
        public static final String GREETINGS_MESSAGE =
                EmojiParser.parseToUnicode("Hi, %s, nice to meet you!" + " :wave:");
        public static final String SET_SCHEDULE_MESSAGE =
                "Every day at 8 am you will receive the actual weather for the current day. To disable the reminder, " +
                        "enter : /schedule_disable";
        public static final String REMINDER_MESSAGE = "Have a good day!";
        public static final String DISABLE_SCHEDULE_MESSAGE =
                "Every day reminder successfully disabled. To restore it type /schedule";
        public static final String REMINDER_ALREADY_SET_MESSAGE =
                EmojiParser.parseToUnicode("Everyday reminder already set! :wink:");
        public static final String REMINDER_ALREADY_DISABLED_MESSAGE =
                EmojiParser.parseToUnicode("Everyday reminder already disabled! :dizzy_face:");
    }

    public static final class StringPattern {
        public static final String CITY_REGEX = "^[a-zA-Zа-яА-ЯіїІЇ]+(?:[\\s-][a-zA-Zа-яА-ЯіїІЇ]+)*$";
        public static final String DATE_TIME_PATTERN = "EE, d LLLL";
    }

    public static final class Logger {
        public static final String BAD_RESPONSE = "Bad response status code '%s'";
        public static final String NO_RAW_DATA = "No raw data.";
        public static final String ERROR_SETTINGS_BOT_COMMANDS = "Error setting bot command list: '%s'";
        public static final String USER_CHANGE_CITY = "User with id - '%s' was changed city to - '%s'";
        public static final String USER_SAVED = "User with id - '%s' saved.";
        public static final String REPLIED_TO_USER = "Replied to user with id - '%s'";
        public static final String BUILD_FORECAST = "Build forecast for user with id - '%s'";
        public static final String BUILD_WEATHER = "Build weather for user with id - '%s'";
        public static final String ERROR_OCCURRED = "Error occurred: %s";
        public static final String SET_REMINDER = "User with id - '%s' set everyday reminder.";
        public static final String SEND_EVERYDAY_REMINDER = "Daily reminder successfully sent.";
        public static final String DISABLED_REMINDER = "Reminder for user with id - '%s' disabled.";
    }

    public static final class Exception {
        public static final String USER_NOT_FOUND = "User with id - %s not exist.";
    }

}
