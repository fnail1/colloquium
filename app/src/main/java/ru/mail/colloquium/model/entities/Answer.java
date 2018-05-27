package ru.mail.colloquium.model.entities;

import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.Gender;
import ru.mail.colloquium.toolkit.data.DbTable;

@DbTable(name = AppData.TABLE_ANSWERS)
public class Answer {
    public Gender gender;
    public long created;
}
