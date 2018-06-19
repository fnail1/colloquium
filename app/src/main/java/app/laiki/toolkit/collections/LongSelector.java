package app.laiki.toolkit.collections;

public interface LongSelector<Item> {
    long invoke(Item item);
}
