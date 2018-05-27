package ru.mail.colloquium.model.entities;

import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.toolkit.data.BaseRow;
import ru.mail.colloquium.toolkit.data.DbTable;

@DbTable(name = AppData.TABLE_QUESTIONS)
public class Question extends BaseRow {
}
