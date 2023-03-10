package com.splot.bot.service;

import com.splot.bot.model.User;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserService {
    User saveUser(Message message);

    User updateUser(User user);

    Boolean checkIfUserExist(Long id);

    User getUserById(Long id);
}
