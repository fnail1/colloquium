package app.laiki.model.entities;

import java.util.Objects;

import app.laiki.model.AppData;
import app.laiki.model.types.Choice;
import app.laiki.toolkit.Flags32;
import app.laiki.toolkit.data.BaseRow;
import app.laiki.toolkit.data.DbColumn;
import app.laiki.toolkit.data.DbForeignKey;
import app.laiki.toolkit.data.DbTable;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Question question = (Question) o;
        return Objects.equals(uniqueId, question.uniqueId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), uniqueId);
    }
}
