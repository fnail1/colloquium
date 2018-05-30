package ru.mail.colloquium.model.entities;

import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.toolkit.Flags32;
import ru.mail.colloquium.toolkit.data.BaseRow;
import ru.mail.colloquium.toolkit.data.DbTable;

@DbTable(name = AppData.TABLE_QUESTIONS)
public class Question extends BaseRow {
    public static final int FLAG_ANSWERED = 1;
    public static final int FLAG_SENT = 2;

    public String serverId;
    public String emoji;
    public String question;
    public long createdAt;
    public long updatedAt;
    public String allPhones;
    public String selectedPhone;
    public final Flags32 flags = new Flags32();

}
