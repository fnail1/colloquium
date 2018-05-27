package ru.mail.colloquium.toolkit.collections;

public interface LongSelector<Item> {
    long invoke(Item item);
}
