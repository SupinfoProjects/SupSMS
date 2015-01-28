package com.supinfo.supsms.entity;

import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class Contact {
    private String name;
    private List<String> numbers;
    private List<String> emails;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}
