package com.splot.bot.service;

import com.splot.bot.config.BotConfig;
import com.splot.bot.model.User;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
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

@Log4j2
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String HELP_TEXT =
            "Type /start to receive greetings \n"
            + "Type /weather to receive weather in your city \n"
            + "Type /forecast to receive 7-day forecast \n"
            + "Type /change to change your current city \n";

    private final UserService userService;
    private final BotConfig config;
    private final WeatherResponseBuilder weatherResponseBuilder;
    private final List<Long> usersChangeCity;

    public TelegramBot(UserService userService, BotConfig config,
                       WeatherResponseBuilder weatherResponseBuilder, List<Long> usersChangeCity) {
        this.userService = userService;
        this.config = config;
        this.weatherResponseBuilder = weatherResponseBuilder;
        this.usersChangeCity = usersChangeCity;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start",
                "welcome message"));
        listOfCommands.add(new BotCommand("/weather",
                "receive weather"));
        listOfCommands.add(new BotCommand("/forecast",
                "receive 7-day forecast"));
        listOfCommands.add(new BotCommand("/change",
                "change current city"));
        listOfCommands.add(new BotCommand("/help",
                "list of commands"));

        try {
            this.execute(new SetMyCommands(listOfCommands,
                    new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
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
            String messageText = message.getText();
            long chatId = message.getChatId();

            if (usersChangeCity.contains(chatId)) {
                if (isCorrectCity(messageText)
                        && weatherResponseBuilder.isAvailableCity(messageText)) {
                    changeUserCity(message);
                    usersChangeCity.remove(chatId);
                } else {
                    sendMessage(chatId, "Incorrect city format "
                            + "or non-existent city, "
                            + "do not use digits and symbols! Try again");
                }
                return;
            }

            switch (messageText) {
                case "/start":
                    startCommandReceived(message);
                    if (userService.checkIfUserExist(chatId)) {
                        registerUser(message);
                    }
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "Current weather":
                case "/weather":
                    showWeather(chatId);
                    break;
                case "Weather forecast":
                case "/forecast":
                    showForecast(chatId);
                    break;
                case "Change city":
                case "/change":
                    usersChangeCity.add(chatId);
                    sendMessage(message.getChatId(), "Type your city:\n");
                    break;
                default: sendMessage(chatId,
                        "Sorry, command was not recognized");
            }
        }
    }

    private void changeUserCity(Message message) {
        User user = userService.getUserById(message.getChatId());
        String messageText = message.getText();
        user.setCity(messageText.toUpperCase());
        user = userService.updateUser(user);
        log.info("User - " + user.getId() + " was changed city to - " + user.getCity());
        sendMessage(user.getId(), "Data was updated, current city - "
                + user.getCity());
    }

    private void showWeather(long chatId) {
        String city = userService.getUserById(chatId).getCity();
        String todayWeatherString = weatherResponseBuilder.buildTodayWeatherMessage(city);
        sendMessage(chatId, todayWeatherString);
    }

    private void showForecast(long chatId) {
        String city = userService.getUserById(chatId).getCity();
        String forecastString = weatherResponseBuilder.buildForecastMessage(city);
        sendMessage(chatId, forecastString);
    }

    private void startCommandReceived(Message message) {
        String name = message.getChat().getFirstName();
        long chatId = message.getChatId();

        String answer = EmojiParser.parseToUnicode("Hi, " + name
                + ", nice to meet you!" + " :wave:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void registerUser(Message message) {
        Long chatId = message.getChatId();
        User user = userService.saveUser(message);
        sendMessage(chatId, "You city by default - Kyiv, "
                + "to change city type /change");
        log.info("User saved " + user);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode("HTML");
        buildKeyboard();
        message.setReplyMarkup(buildKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup buildKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("Current weather");
        row.add("Weather forecast");
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Change city");
        keyboardRows.add(row);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private boolean isCorrectCity(String messageText) {
        return messageText.matches("^[a-zA-Zа-яА-ЯіїІЇ]+"
                + "(?:[\\s-][a-zA-Zа-яА-ЯіїІЇ]+)*$");
    }
}
