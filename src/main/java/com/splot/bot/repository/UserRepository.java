package com.splot.bot.repository;

import com.splot.bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.city " +
            "FROM users u " +
            "WHERE u.id = ?1")
    String findCityByUserId(Long id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM users u " +
            "WHERE u.id = ?1 AND u.city IS NOT NULL")
    boolean isUserCityExist(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE users u " +
            "SET u.city = NULL " +
            "WHERE u.id = ?1")
    void removeCityByUserId(Long id);

}
