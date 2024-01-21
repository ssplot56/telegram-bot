package com.splot.bot.service;

import com.splot.bot.config.BotConfig;
import com.splot.bot.model.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.splot.bot.config.CommonUtils.Strings.isCorrectCityPattern;
import static com.splot.bot.config.CommonUtils.Strings.normalizeString;
import static com.splot.bot.config.Constants.BotCommand.CHANGE_CITY_COMMAND;
import static com.splot.bot.config.Constants.BotCommand.DISABLE_SCHEDULE_COMMAND;
import static com.splot.bot.config.Constants.BotCommand.FORECAST_COMMAND;
import static com.splot.bot.config.Constants.BotCommand.HELP_COMMAND;
import static com.splot.bot.config.Constants.BotCommand.SCHEDULE_COMMAND;
import static com.splot.bot.config.Constants.BotCommand.START_COMMAND;
import static com.splot.bot.config.Constants.BotCommand.WEATHER_COMMAND;
import static com.splot.bot.config.Constants.BotMessages.CITY_WAS_UPDATED_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.COMMAND_NOT_RECOGNIZED_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.DEFAULT_CITY_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.DISABLE_SCHEDULE_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.GREETINGS_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.HELP_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.INCORRECT_CITY_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.REMINDER_ALREADY_DISABLED_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.REMINDER_ALREADY_SET_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.REMINDER_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.SET_SCHEDULE_MESSAGE;
import static com.splot.bot.config.Constants.BotMessages.TYPE_CITY_MESSAGE;
import static com.splot.bot.config.Constants.CommandDescription.CHANGE_CITY_DESCRIPTION;
import static com.splot.bot.config.Constants.CommandDescription.DISABLE_SCHEDULE_DESCRIPTION;
import static com.splot.bot.config.Constants.CommandDescription.FORECAST_DESCRIPTION;
import static com.splot.bot.config.Constants.CommandDescription.HELP_DESCRIPTION;
import static com.splot.bot.config.Constants.CommandDescription.SCHEDULE_DESCRIPTION;
import static com.splot.bot.config.Constants.CommandDescription.START_DESCRIPTION;
import static com.splot.bot.config.Constants.CommandDescription.WEATHER_DESCRIPTION;
import static com.splot.bot.config.Constants.Logger.BUILD_FORECAST;
import static com.splot.bot.config.Constants.Logger.BUILD_WEATHER;
import static com.splot.bot.config.Constants.Logger.DISABLED_REMINDER;
import static com.splot.bot.config.Constants.Logger.ERROR_OCCURRED;
import static com.splot.bot.config.Constants.Logger.ERROR_SETTINGS_BOT_COMMANDS;
import static com.splot.bot.config.Constants.Logger.REPLIED_TO_USER;
import static com.splot.bot.config.Constants.Logger.SEND_EVERYDAY_REMINDER;
import static com.splot.bot.config.Constants.Logger.SET_REMINDER;
import static com.splot.bot.config.Constants.Logger.USER_CHANGE_CITY;
import static com.splot.bot.config.Constants.Logger.USER_SAVED;
import static com.splot.bot.config.Constants.TelegramApi.CHANGE_CITY;
import static com.splot.bot.config.Constants.TelegramApi.CURRENT_WEATHER;
import static com.splot.bot.config.Constants.TelegramApi.DISABLE_REMINDER;
import static com.splot.bot.config.Constants.TelegramApi.ENABLE_REMINDER;
import static com.splot.bot.config.Constants.TelegramApi.HTML;
import static com.splot.bot.config.Constants.TelegramApi.WEATHER_FORECAST;

@Log4j2
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;

    @Autowired
    private UserService userService;

    @Autowired
    private WeatherResponseService weatherResponseService;

    public TelegramBot(BotConfig config) {
        this.config = config;

        try {
            this.execute(new SetMyCommands(getListOfCommands(), new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(ERROR_SETTINGS_BOT_COMMANDS.formatted(e.getMessage()));
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = normalizeString(message.getText());
            long chatId = message.getChatId();

            if (checkIfNeedUpdateCity(message, chatId, messageText)) {
                return;
            }

            switch (messageText) {
                case START_COMMAND -> startCommandReceived(message);
                case HELP_COMMAND -> sendMessage(chatId, HELP_MESSAGE);
                case WEATHER_COMMAND, CURRENT_WEATHER -> showWeather(chatId);
                case FORECAST_COMMAND, WEATHER_FORECAST -> showForecast(chatId);
                case CHANGE_CITY_COMMAND, CHANGE_CITY -> changeCity(chatId);
                case SCHEDULE_COMMAND, ENABLE_REMINDER -> setSchedule(chatId);
                case DISABLE_SCHEDULE_COMMAND, DISABLE_REMINDER -> disableSchedule(chatId);
                default -> sendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            }
        }
    }

    private void startCommandReceived(Message message) {
        long chatId = message.getChatId();
        sendMessage(chatId, GREETINGS_MESSAGE.formatted(message.getChat().getFirstName()));

        if (!userService.isUserExist(chatId)) {
            registerUser(message);
        }

        log.info(REPLIED_TO_USER.formatted(chatId));
    }

    private void showWeather(long chatId) {
        String city = userService.getCityByUserId(chatId);
        String todayWeatherString = weatherResponseService.buildTodayWeatherMessage(city);
        sendMessage(chatId, todayWeatherString);

        log.info(BUILD_WEATHER.formatted(chatId));
    }

    private void showForecast(long chatId) {
        String city = userService.getCityByUserId(chatId);
        String forecastString = weatherResponseService.buildForecastMessage(city);
        sendMessage(chatId, forecastString);

        log.info(BUILD_FORECAST.formatted(chatId));
    }

    private void changeCity(long chatId) {
        userService.removeCityByUserId(chatId);
        sendMessage(chatId, TYPE_CITY_MESSAGE);
    }

    private void setSchedule(long chatId) {
        if (userService.isReminderSet(chatId)) {
            sendMessage(chatId, REMINDER_ALREADY_SET_MESSAGE);
            return;
        }

        userService.setReminderForUser(chatId, true);
        sendMessage(chatId, SET_SCHEDULE_MESSAGE);

        log.info(SET_REMINDER.formatted(chatId));
    }

    private void disableSchedule(long chatId) {
        if (!userService.isReminderSet(chatId)) {
            sendMessage(chatId, REMINDER_ALREADY_DISABLED_MESSAGE);
            return;
        }

        userService.setReminderForUser(chatId, false);
        sendMessage(chatId, DISABLE_SCHEDULE_MESSAGE);

        log.info(DISABLED_REMINDER.formatted(chatId));
    }

    private boolean checkIfNeedUpdateCity(Message message, Long chatId, String messageText) {
        if (!userService.isUserCityExist(chatId) && userService.isUserExist(chatId)) {
            if (isCorrectCityPattern(messageText) && weatherResponseService.isAvailableCity(messageText)) {
                updateUserCity(message);
            } else {
                sendMessage(chatId, INCORRECT_CITY_MESSAGE);
            }
            return true;
        }

        return false;
    }

    private void updateUserCity(Message message) {
        User user = userService.getById(message.getChatId());
        user.setCity(weatherResponseService.getCorrectCityName(message.getText()));
        user = userService.updateUser(user);
        sendMessage(user.getId(), CITY_WAS_UPDATED_MESSAGE.formatted(user.getCity()));

        log.info(USER_CHANGE_CITY.formatted(user.getId(), user.getCity()));
    }

    private void registerUser(Message message) {
        Long chatId = message.getChatId();
        userService.saveUser(message);
        sendMessage(chatId, DEFAULT_CITY_MESSAGE);

        log.info(USER_SAVED.formatted(chatId));
    }

    @Scheduled(cron = "0 0 8 * * ?")
    private void sendEveryDayReminders() {
        userService.getUsersIdsWithReminders().forEach(this::sendReminder);

        log.info(SEND_EVERYDAY_REMINDER);
    }

    private void sendReminder(long chatId) {
        sendMessage(chatId, REMINDER_MESSAGE);
        showWeather(chatId);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = buildSendMessage(chatId, textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_OCCURRED.formatted(e.getMessage()));
        }
    }

    private SendMessage buildSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode(HTML);
        message.setReplyMarkup(buildKeyboard());

        return message;
    }

    private ReplyKeyboardMarkup buildKeyboard() {
        KeyboardRow upperRow = new KeyboardRow();
        upperRow.add(CURRENT_WEATHER);
        upperRow.add(WEATHER_FORECAST);

        KeyboardRow lowerRow = new KeyboardRow();
        lowerRow.add(CHANGE_CITY);
        lowerRow.add(ENABLE_REMINDER);
        lowerRow.add(DISABLE_REMINDER);

        return new ReplyKeyboardMarkup(List.of(upperRow, lowerRow));
    }

    private List<BotCommand> getListOfCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand(START_COMMAND, START_DESCRIPTION));
        listOfCommands.add(new BotCommand(WEATHER_COMMAND, WEATHER_DESCRIPTION));
        listOfCommands.add(new BotCommand(FORECAST_COMMAND, FORECAST_DESCRIPTION));
        listOfCommands.add(new BotCommand(CHANGE_CITY_COMMAND, CHANGE_CITY_DESCRIPTION));
        listOfCommands.add(new BotCommand(HELP_COMMAND, HELP_DESCRIPTION));
        listOfCommands.add(new BotCommand(SCHEDULE_COMMAND, SCHEDULE_DESCRIPTION));
        listOfCommands.add(new BotCommand(DISABLE_SCHEDULE_COMMAND, DISABLE_SCHEDULE_DESCRIPTION));

        return listOfCommands;
    }

}
