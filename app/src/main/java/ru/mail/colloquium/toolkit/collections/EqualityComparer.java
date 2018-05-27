package ru.mail.colloquium.toolkit.collections;

public interface EqualityComparer<Item> {
    boolean invoke(Item a, Item b);
}
