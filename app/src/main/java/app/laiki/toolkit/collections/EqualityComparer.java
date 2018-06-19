package app.laiki.toolkit.collections;

public interface EqualityComparer<Item> {
    boolean invoke(Item a, Item b);
}
