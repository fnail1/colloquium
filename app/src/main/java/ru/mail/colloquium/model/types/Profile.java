package ru.mail.colloquium.model.types;

import java.io.Serializable;

public class Profile implements Serializable {
    public Gender gender;
    public Age age;
    public String name;
    public long createdAt;
    public long updatedAt;
    public String phone;
}
