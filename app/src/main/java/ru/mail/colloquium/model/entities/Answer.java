package ru.mail.colloquium.model.entities;

import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.Age;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.model.types.Gender;
import ru.mail.colloquium.toolkit.Flags32;
import ru.mail.colloquium.toolkit.data.BaseRow;
import ru.mail.colloquium.toolkit.data.DbTable;

@DbTable(name = AppData.TABLE_ANSWERS)
public class Answer extends BaseRow {
    public static final int FLAG_READ = 1;
    public static final int FLAG_SENT = 2;

    public String serverId;
    public long createdAt;
    public Gender gender;
    public Age age = Age.SUPERSTAR;
    public final Flags32 flags = new Flags32();
    public String variantA;
    public String variantB;
    public String variantC;
    public String variantD;
    public Choice answer;
    public String answerName;
    public String questionServerId;
    public String questionEmoji;
    public String questionText;
    public long questionCreatedAt;
    public long questionUpdatedAt;
}
