package com.splot.bot.service.impl;

import com.splot.bot.model.User;
import com.splot.bot.repository.UserRepository;
import com.splot.bot.service.UserService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User registerNewUser(Message message) {
        long chatId = message.getChatId();
        Chat chat = message.getChat();

        User user = new User();
        user.setId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        return repository.save(user);
    }

    @Override
    public Boolean checkIfUserExist(Message message) {
        return repository.findById(message.getChatId()).isEmpty();
    }

    @Override
    public User findUserById(Long id) {
        return repository.findById(id).get();
    }
}
