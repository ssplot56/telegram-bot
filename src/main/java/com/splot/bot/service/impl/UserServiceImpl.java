package com.splot.bot.service.impl;

import com.splot.bot.model.User;
import com.splot.bot.repository.UserRepository;
import com.splot.bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;

import static com.splot.bot.config.Constants.Exception.USER_NOT_FOUND;
import static com.splot.bot.config.Constants.WeatherApi.BASIC_CITY;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    public void saveUser(Message message) {
        Chat chat = message.getChat();

        User user = new User(
                message.getChatId(),
                chat.getFirstName(),
                chat.getLastName(),
                chat.getUserName(),
                new Timestamp(System.currentTimeMillis()),
                BASIC_CITY
        );

        repository.save(user);
    }

    @Override
    public User updateUser(User user) {
        return repository.save(user);
    }

    @Override
    public Boolean isUserExist(Long id) {
        return repository.findById(id).isPresent();
    }

    @Override
    public User getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    @Override
    public boolean isUserCityExist(Long id) {
        return repository.isUserCityExist(id);
    }

    @Override
    public String getCityByUserId(Long id) {
        return repository.findCityByUserId(id);
    }

    @Override
    public void removeCityByUserId(Long id) {
        repository.removeCityByUserId(id);
    }

}
