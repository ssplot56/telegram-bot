package com.splot.bot.service;

import com.splot.bot.config.BotConfig;
import com.splot.bot.model.User;
import com.splot.bot.model.Weather;
import com.vdurmont.emoji.EmojiParser;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String HELP_TEXT = """
            This bot can do nothing for now :(\s
            Type /start to receive greetings
            Type /mydata to receive collected information about you
            Type /deletedata to delete collected data about you
            Use context menu for other commands
            """;

    private final UserService userService;
    private final BotConfig config;
    private final WeatherService weatherService;
    private boolean tryToChangeCity = false;

    public TelegramBot(UserService userService, BotConfig config, WeatherService weatherService) {
        this.userService = userService;
        this.config = config;
        this.weatherService = weatherService;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start",
                "welcome message"));
        listOfCommands.add(new BotCommand("/mydata",
                "get your data"));
        listOfCommands.add(new BotCommand("/deletedata",
                "delete your data"));
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

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            long chatId = message.getChatId();

            if (tryToChangeCity) {
                if (isCorrect(messageText)) {
                    changeUserCity(update);
                    tryToChangeCity = false;
                    return;
                } else {
                    sendMessage(message.getChatId(), "Uncorrect format, "
                            + "do not use digits and symbols! Try again");
                }
            }

            switch (messageText) {
                case "/start" -> startCommandReceived(message);
                case "register" -> registerUser(message);
                case "/help" -> sendMessage(chatId, HELP_TEXT);
                case "/mydata", "check my data" -> checkData(message);
                case "/deletedata", "delete my data" -> deleteData(message);
                case "current weather" -> showWeather(chatId);
                case "weather forecast" -> showForecast(chatId);
                case "change city" -> {
                    tryToChangeCity = true;
                    sendMessage(message.getChatId(), "Type your city:\n");
                }
                default -> sendMessage(chatId,
                        "Sorry, command was not recognized");
            }
        }
    }

    @SneakyThrows
    private void changeUserCity(Update update) {
        Message message = update.getMessage();
        User user = userService.getUserById(message.getChatId());
        String messageText = message.getText();
        user.setCity(messageText);
        userService.updateUser(user);
        sendMessage(user.getId(), "City was updated, current city - "
                + user.getCity());
    }

    private boolean isCorrect(String messageText) {
        return messageText.matches("[a-zA-Z]+");
    }

    @SneakyThrows
    private void showForecast(long chatId) {
        List<Weather> weatherList = weatherService.timelineRequestHttpClient();


    }

    @SneakyThrows
    private void showWeather(Long chatId) {
        Weather weather = weatherService.timelineRequestHttpClient().get(0);
        User user = userService.getUserById(chatId);
        String city = user.getCity();

        sendMessage(chatId, "Currently in " + city +":\n"
                + "Temperature - " + weather.getCurrentTemp() + "\n"
                + "Feels like - " + weather.getFeelsLikeTemp() + "\n"
                + "Max temperature - " + weather.getMaxTemp() + "\n"
                + "Min temperature - " + weather.getMinTemp());
    }

    private void deleteData(Message message) {
        Long chatId = message.getChatId();
        if (userService.checkIfUserExist(message)) {
            sendMessage(chatId, "Bot doesn't have any information about you");
        } else {
            userService.deleteUser(chatId);
            sendMessage(chatId, "Your data successful deleted :)");
            log.info("User with chatId: " + chatId + " was deleted");
        }
    }

    private void checkData(Message message) {
        Long chatId = message.getChatId();
        if (userService.checkIfUserExist(message)) {
            sendMessage(chatId, "You not registered, register first");
        } else {
            sendMessage(chatId,userService.findUserById(chatId).toString());
        }
    }

    private void registerUser(Message message) {
        if (userService.checkIfUserExist(message)) {
            User user = userService.saveUser(message);
            sendMessage(message.getChatId(), "You city by default - Kyiv, "
                    + "to change city type 'change city'");
            log.info("User saved " + user);
            sendMessage(message.getChatId(), "Successfully registered");
        } else {
            sendMessage(message.getChatId(), "You already registered :)");
        }
    }

    private void startCommandReceived(Message message) throws InterruptedException {
        String name = message.getChat().getFirstName();
        long chatId = message.getChatId();

        String answer = EmojiParser.parseToUnicode("Hi, " + name
                + ", nice to meet you!" + ":pig:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);

        if (userService.checkIfUserExist(message)) {
            Thread.sleep(1500);
            sendMessage(chatId, "You can register in bot, just type 'register'");
        }

    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("current weather");
        row.add("weather forecast");
        row.add("change city");
        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("register");
        row.add("check my data");
        row.add("delete my data");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
