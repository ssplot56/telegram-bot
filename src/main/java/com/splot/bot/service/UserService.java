package com.splot.bot.service;

import com.splot.bot.model.User;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserService {
    User registerNewUser(Message message);

    Boolean checkIfUserExist(Message message);

    User findUserById(Long id);
}
