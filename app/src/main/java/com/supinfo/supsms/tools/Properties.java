package com.supinfo.supsms.tools;

import com.supinfo.supsms.entity.User;

public class Properties {
    private static Properties instance = null;

    private User user;

    public static synchronized Properties getInstance() {
        if (null == instance) {
            instance = new Properties();
        }

        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
