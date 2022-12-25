package com.splot.bot.service;

import com.splot.bot.config.BotConfig;
import com.splot.bot.model.User;
import com.vdurmont.emoji.EmojiParser;
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
            Type /settings to set own preferences
            """;

    private final UserService userService;
    private final BotConfig config;

    public TelegramBot(UserService userService, BotConfig config) {
        this.userService = userService;
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start",
                "welcome message"));
        listOfCommands.add(new BotCommand("/mydata",
                "get your data"));
        listOfCommands.add(new BotCommand("/deletedata",
                "delete your data"));
        listOfCommands.add(new BotCommand("/help",
                "list of commands"));
        listOfCommands.add(new BotCommand("/settings",
                "set your preferences"));
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
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" :
                case "register":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId,
                            update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/mydata":
                case "check my data":
                    sendMessage(chatId, userService.findUserById(chatId).toString());
                    break;
                case "/deletedata":
                case "delete my data":
                    sendMessage(chatId, "Your data successful deleted :)");
                    break;
                case "/settings":
                    break;
                default: sendMessage(chatId,
                        "Sorry, command was not recognized");
            }
        }
    }

    private void registerUser(Message message) {
        if (userService.checkIfUserExist(message)) {
            User user = userService.registerNewUser(message);
            log.info("User saved " + user);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name
                + ", nice to meet you!" + ":pig:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("weather");
        row.add("get random joke");
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
