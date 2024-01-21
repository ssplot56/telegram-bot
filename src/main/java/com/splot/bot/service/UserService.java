package com.splot.bot.service;

import com.splot.bot.model.User;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserService {

    void saveUser(Message message);

    User updateUser(User user);

    Boolean isUserExist(Long id);

    User getById(Long id);

    boolean isUserCityExist(Long id);

    String getCityByUserId(Long id);

    void removeCityByUserId(Long chatId);

}
