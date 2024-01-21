package com.splot.bot.service;

import com.splot.bot.model.User;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface UserService {

    void saveUser(Message message);

    User updateUser(User user);

    Boolean isUserExist(Long id);

    User getById(Long id);

    boolean isUserCityExist(Long id);

    String getCityByUserId(Long id);

    void removeCityByUserId(Long id);

    void setReminderForUser(Long id, boolean isNeed);

    List<Long> getUsersIdsWithReminders();

    boolean isReminderSet(Long id);

}
