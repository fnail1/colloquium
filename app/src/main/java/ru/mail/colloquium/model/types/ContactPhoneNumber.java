package ru.mail.colloquium.model.types;


import ru.mail.colloquium.model.entities.ContactPhoneLink;
import ru.mail.colloquium.model.entities.PhoneNumber;

public class ContactPhoneNumber extends PhoneNumber implements Cloneable {
    public static final ContactPhoneNumber EMPTY = new ContactPhoneNumber();

    public transient ContactPhoneLink link;

    @Override
    public ContactPhoneNumber clone() {
        try {
            return (ContactPhoneNumber) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }


    @Override
    public String toString() {
        return "ContactPhoneNumber {" +
                "_id=" + _id +
                ", normalized='" + normalized + '\'' +
                ", serverId='" + serverId + '\'' +
                ", flags=" + flags +
                ", link='" + link + '\'' +
                '}';
    }
}
