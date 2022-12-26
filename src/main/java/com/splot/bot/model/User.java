package com.splot.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity(name = "users")
public class User {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registeredAt;
    private String city;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "id " + id +
                ", firstName '" + firstName + '\'' +
                ", lastName '" + lastName + '\'' +
                ", userName '" + userName + '\'' +
                ", registeredAt '" + registeredAt + '\'' +
                ", city '" + city + '\'';
    }
}
