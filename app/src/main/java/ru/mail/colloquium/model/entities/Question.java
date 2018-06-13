package ru.mail.colloquium.model.entities;

import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.toolkit.Flags32;
import ru.mail.colloquium.toolkit.data.BaseRow;
import ru.mail.colloquium.toolkit.data.DbColumn;
import ru.mail.colloquium.toolkit.data.DbForeignKey;
import ru.mail.colloquium.toolkit.data.DbTable;

@DbTable(name = AppData.TABLE_QUESTIONS)
public class Question extends BaseRow {
    public static final int FLAG_ANSWERED = 1;
    public static final int FLAG_SENT = 2;

    public String serverId;
    @DbColumn(unique = true)
    public String uniqueId;
    public String emojiUrl;
    public String emojiText;
    public String question;
    public long createdAt;
    public long updatedAt;
    public final Flags32 flags = new Flags32();

    @DbForeignKey(table = AppData.TABLE_CONTACTS)
    public long variant1;

    @DbForeignKey(table = AppData.TABLE_CONTACTS)
    public long variant2;

    @DbForeignKey(table = AppData.TABLE_CONTACTS)
    public long variant3;

    @DbForeignKey(table = AppData.TABLE_CONTACTS)
    public long variant4;

    public Choice answer;

}
