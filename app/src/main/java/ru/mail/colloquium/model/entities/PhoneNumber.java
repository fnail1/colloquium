package ru.mail.colloquium.model.entities;

import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.Gender;
import ru.mail.colloquium.toolkit.Flags32;
import ru.mail.colloquium.toolkit.data.BaseRow;
import ru.mail.colloquium.toolkit.data.ConflictAction;
import ru.mail.colloquium.toolkit.data.DbColumn;
import ru.mail.colloquium.toolkit.data.DbTable;

@DbTable(name = AppData.TABLE_PHONE_NUMBERS)
public class PhoneNumber extends BaseRow {
    @DbColumn(unique = true, onUniqueConflict = ConflictAction.IGNORE)
    public String normalized;

    @DbColumn(unique = true, onUniqueConflict = ConflictAction.IGNORE)
    public String serverId;

    public Gender gender = Gender.CAMEL;
    public Flags32 flags = new Flags32(0);
    public PhoneRelevance relevance = PhoneRelevance.UNKNOWN;


    @Override
    public String toString() {
        return "PhoneNumber{" +
                "_id=" + _id +
                ", normalized='" + normalized + '\'' +
                ", serverId='" + serverId + '\'' +
                ", flags=" + flags +
                '}';
    }


    public enum PhoneRelevance {
        UNKNOWN,
        MOBILE,
        CONFIRMED
    }
}
