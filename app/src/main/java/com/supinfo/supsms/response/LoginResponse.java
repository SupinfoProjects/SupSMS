package com.supinfo.supsms.response;

import com.supinfo.supsms.entity.User;

@SuppressWarnings("UnusedDeclaration")
public class LoginResponse extends Response {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
