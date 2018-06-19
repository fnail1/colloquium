package app.laiki.toolkit.collections;

import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends collections and arrays with query methods.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Query<Item>
        implements Iterable<Item> {

    /**
     * in the absence of information the size of collection is taken as 16
     */
    @SuppressWarnings("WeakerAccess")
    public static final int PREDEFINED_APPROXIMATED_SIZE = 16;

    @SuppressWarnings("unchecked")
    public static <Item> Query<Item> query(Item source) {
        if (source instanceof Query)
            return (Query<Item>) source;
        else
            return new QueryRootSingle<>(source);
    }

    public static <Item> Query<Item> query(final Collection<Item> source) {
        return new QueryRoot<>(source);
    }

    public static <Item> Query<Item> query(final List<Item> source) {
        return new QueryRootList<>(source);
    }

    @SafeVarargs
    public static <Item> Query<Item> query(Item... source) {
        return new QueryRoot<>(new IterableArray<>(source));
    }

    public static <Item> Query<Item> query(final Iterable<Item> source) {
        if (source instanceof Query)
            return (Query<Item>) source;
        else
            return new QueryRoot<>(source);
    }

    protected static <Item> int approximateCount(Iterable<Item> source) {
        return source instanceof Collection ? ((Collection) source).size() : PREDEFINED_APPROXIMATED_SIZE;
    }

    @NonNull
    @Override
    public abstract Iterator<Item> iterator();


    /**
     * Method is useful to initiate destination collections such for copy of query results
     *
     * @return Approximated count of items of query result. Could by less or more of real one.
     */
    protected abstract int approximateCount();

    public List<Item> toList() {
        ArrayList<Item> list = new ArrayList<>(approximateCount());
        for (Item item : this) {
            list.add(item);
        }
        return list;
    }

    @NonNull
    public <TKey> HashMap<TKey, Item> toMap(Func<Item, TKey> keySelector) {
        HashMap<TKey, Item> list = new HashMap<>(approximateCount());
        for (Item item : this) {
            TKey key = keySelector.invoke(item);
            list.put(key, item);
        }

        return list;
    }

    public SparseArrayCompat<Item> toSparseArray(IntegerSelector<Item> keySelector) {
        SparseArrayCompat<Item> a = new SparseArrayCompat<>(approximateCount());
        for (Item item : this) {
            a.put(keySelector.invoke(item), item);
        }

        return a;
    }

    public LongSparseArray<Item> toLongSparseArray(LongSelector<Item> keySelector) {
        LongSparseArray<Item> a = new LongSparseArray<>(approximateCount());
        for (Item item : this) {
            a.put(keySelector.invoke(item), item);
        }

        return a;
    }

    public LongSparseArray<ArrayList<Item>> groupByLong(LongSelector<Item> keySelector) {
        LongSparseArray<ArrayList<Item>> a = new LongSparseArray<>(approximateCount());
        for (Item item : this) {
            long key = keySelector.invoke(item);
            ArrayList<Item> l = a.get(key);
            if (l == null) {
                l = new ArrayList<>();
                a.put(key, l);
            }
            l.add(item);
        }

        return a;
    }

    public SparseArrayCompat<ArrayList<Item>> groupByInt(IntegerSelector<Item> keySelector) {
        SparseArrayCompat<ArrayList<Item>> a = new SparseArrayCompat<>(approximateCount());
        for (Item item : this) {
            int key = keySelector.invoke(item);
            ArrayList<Item> l = a.get(key);
            if (l == null) {
                l = new ArrayList<>();
                a.put(key, l);
            }
            l.add(item);
        }

        return a;
    }

    public <TKey> HashMap<TKey, ArrayList<Item>> groupByObj(Func<Item, TKey> keySelector) {
        HashMap<TKey, ArrayList<Item>> a = new HashMap<>(approximateCount());
        for (Item item : this) {
            TKey key = keySelector.invoke(item);
            ArrayList<Item> l = a.get(key);
            if (l == null) {
                l = new ArrayList<>();
                a.put(key, l);
            }
            l.add(item);
        }

        return a;
    }

    @NonNull
    public <TKey, TValue> HashMap<TKey, TValue> toMap(Func<Item, TKey> keySelector, Func<Item, TValue> valueSelector) {
        HashMap<TKey, TValue> list = new HashMap<>(approximateCount());
        for (Item item : this) {
            TKey key = keySelector.invoke(item);
            TValue value = valueSelector.invoke(item);
            list.put(key, value);
        }

        return list;
    }

    public Set<Item> toOrderedSet() {
        LinkedHashSet<Item> set = new LinkedHashSet<>(approximateCount());
        for (Item item : this) {
            set.add(item);
        }
        return set;
    }

    @Override
    public String toString() {
        return toString(", ");
    }

    public String toString(final String separator) {
        StringBuilder result;
        Iterator<Item> iterator = iterator();
        if (!iterator.hasNext())
            return "";

        result = new StringBuilder();
        Item item = iterator.next();
        result.append(String.valueOf(item));

        while (iterator.hasNext()) {
            item = iterator.next();
            result.append(separator).append(String.valueOf(item));
        }
        return result.toString();
    }

    public <Result> Query<Result> select(final Func<Item, Result> selector) {
        return new SelectExpr<>(this, selector);
    }

    public Query<Item> where(final Predicate<Item> predicate) {
        return new Query<Item>() {
            @NonNull
            @Override
            public Iterator<Item> iterator() {
                return new WhereExprIterator<Item>(Query.this) {
                    @Override
                    protected boolean filter(Item item) {
                        return predicate.invoke(item);
                    }
                };
            }

            @Override
            protected int approximateCount() {
                return Query.this.approximateCount();
            }
        };
    }

    public Query<Item> notNull() {
        return new Query<Item>() {
            @NonNull
            @Override
            public Iterator<Item> iterator() {
                return new WhereExprIterator<Item>(Query.this) {
                    @Override
                    protected boolean filter(Item item) {
                        return item != null;
                    }
                };
            }

            @Override
            protected int approximateCount() {
                return Query.this.approximateCount();
            }
        };
    }

    public Query<Item> skip(final int count) {
        return new SkipExpr<>(this, count);
    }

    public Query<Item> limit(final int count) {
        return new LimitExpr<>(this, count);
    }

    public int sum(final IntegerSelector<Item> selector) {
        int result = 0;
        for (Item item : this) {
            result += selector.invoke(item);
        }
        return result;
    }

    public long longSum(final LongSelector<Item> selector) {
        long result = 0;
        for (Item item : this) {
            result += selector.invoke(item);
        }
        return result;
    }

    public Item max(IntegerSelector<Item> selector) {
        int key = Integer.MIN_VALUE;
        Item value = null;
        for (Item item : this) {
            int k = selector.invoke(item);
            if (key < k) {
                key = k;
                value = item;
            }
        }
        return value;
    }

    public <Result> Query<Result> extract(final Func<Item, Iterable<Result>> extractor) {
        return new ExtractExpr<>(this, extractor);
    }

    public <Result> Query<Result> cast() {
        return new CastExpr<>(this);
    }

    public Item first() {
        Iterator<Item> iterator = iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public Item first(final Predicate<Item> predicate) {
        return where(predicate).first();
    }

    public Query<Item> concat(final Item second) {
        return concat(query(second));
    }

    public Query<Item> concat(final Iterable<? extends Item> second) {
        return new ConcatExpr<>(this, second);
    }

    public Query<Item> concat(Item[] second) {
        return new ConcatExpr<>(this, new IterableArray<>(second));
    }

    public Query<Item> distinct() {
        return new Query<Item>() {
            @NonNull
            @Override
            public Iterator<Item> iterator() {
                return new WhereExprIterator<Item>(Query.this) {
                    private final HashSet<Item> hash = new HashSet<>();

                    @Override
                    protected boolean filter(Item item) {
                        return hash.add(item);
                    }
                };
            }

            @Override
            protected int approximateCount() {
                return Query.this.approximateCount();
            }
        };
    }

    public <Key> Query<Item> distinct(final Func<Item, Key> keySelector) {
        return new Query<Item>() {
            @NonNull
            @Override
            public Iterator<Item> iterator() {
                return new WhereExprIterator<Item>(Query.this) {
                    private final HashSet<Key> hash = new HashSet<>();

                    @Override
                    protected boolean filter(Item item) {
                        return hash.add(keySelector.invoke(item));
                    }
                };
            }

            @Override
            protected int approximateCount() {
                return Query.this.approximateCount();
            }
        };
    }

    public <Key> Query<Item> distinct(final IntegerSelector<Item> keySelector) {
        return new Query<Item>() {
            @NonNull
            @Override
            public Iterator<Item> iterator() {
                return new WhereExprIterator<Item>(Query.this) {
                    private final SparseArrayCompat<Item> hash = new SparseArrayCompat<>(Query.this.approximateCount());

                    @Override
                    protected boolean filter(Item item) {
                        int key = keySelector.invoke(item);
                        if (hash.indexOfKey(key) >= 0)
                            return false;
                        hash.put(key, item);
                        return true;
                    }
                };
            }

            @Override
            protected int approximateCount() {
                return Query.this.approximateCount();
            }
        };
    }

    public <Key> Query<Item> distinct(final LongSelector<Item> keySelector) {
        return new Query<Item>() {
            @NonNull
            @Override
            public Iterator<Item> iterator() {
                return new WhereExprIterator<Item>(Query.this) {
                    private final LongSparseArray<Item> hash = new LongSparseArray<>(Query.this.approximateCount());

                    @Override
                    protected boolean filter(Item item) {
                        long key = keySelector.invoke(item);
                        if (hash.indexOfKey(key) >= 0)
                            return false;
                        hash.put(key, item);
                        return true;
                    }
                };
            }

            @Override
            protected int approximateCount() {
                return Query.this.approximateCount();
            }
        };
    }


    public Query<Item> orderBy(final Comparator<Item> keyComparator) {
        return new CustomOrderByExp<>(this, keyComparator);
    }

    public int count() {
        int count = 0;
        for (Item ignored : this) {
            count++;
        }
        return count;
    }

    private static class IterableArray<Item> implements Iterable<Item> {
        private final Item[] array;

        public IterableArray(Item[] array) {
            this.array = array;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return new InternalArrayIterator<>(array);
        }

        static class InternalArrayIterator<Item> implements Iterator<Item> {

            private final Item[] array;
            private int index;

            public InternalArrayIterator(Item[] array) {
                this.array = array;
                index = 0;
            }

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            public Item next() {
                return array[index++];
            }

            @Override
            public void remove() {
                throw new RuntimeException();
            }
        }
    }

    private static abstract class QueryChain<Item, TPrevItem> extends Query<Item> {
        protected Query<TPrevItem> source;

        public QueryChain(Query<TPrevItem> source) {
            this.source = source;
        }

        @Override
        protected int approximateCount() {
            return source.approximateCount();
        }
    }

    private static class CastExpr<Item, TPrevItem> extends QueryChain<Item, TPrevItem> {
        public CastExpr(Query<TPrevItem> source) {
            super(source);
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return new CastExprIterator();
        }

        @Override
        public int count() {
            return super.count();
        }

        @Override
        protected int approximateCount() {
            return source.approximateCount();
        }

        private class CastExprIterator implements Iterator<Item> {
            final Iterator<? extends TPrevItem> sourceIterator = source.iterator();

            @Override
            public boolean hasNext() {
                return sourceIterator.hasNext();
            }

            @SuppressWarnings("unchecked")
            @Override
            public Item next() {
                return (Item) sourceIterator.next();
            }

            @Override
            public void remove() {
                sourceIterator.remove();
            }
        }
    }

    private static class ConcatExpr<Item> extends QueryChain<Item, Item> {
        private final Iterable<? extends Item> second;

        public ConcatExpr(Query<Item> first, Iterable<? extends Item> second) {
            super(first);
            this.second = second;
        }


        @Override
        public int count() {
            int count = source.count();
            if (second instanceof Query)
                return count + ((Query) second).count();
            else if (second instanceof Collection)
                return count + ((Collection) second).size();
            for (Item i : second) {
                count++;
            }
            return count;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return new Iterator<Item>() {
                private Iterator<? extends Item> current;
                private boolean isSecond;

                @Override
                public boolean hasNext() {
                    if (current == null) {
                        current = source.iterator();
                    }
                    if (current.hasNext()) {
                        return true;
                    }
                    if (!isSecond) {
                        current = second.iterator();
                        isSecond = true;
                    }
                    return current.hasNext();
                }

                @Override
                public void remove() {
                    if (hasNext()) {
                        current.remove();
                    }
                }

                @Override
                public Item next() {
                    if (hasNext()) {
                        return current.next();
                    }
                    return null;
                }
            };
        }

        @Override
        protected int approximateCount() {
            return source.approximateCount() + approximateCount(second);
        }
    }

    private static class ExtractExpr<Item, TPrevItem> extends QueryChain<Item, TPrevItem> {
        private final Func<TPrevItem, Iterable<Item>> extractor;

        public ExtractExpr(Query<TPrevItem> iterator, Func<TPrevItem, Iterable<Item>> extractor) {
            super(iterator);
            this.extractor = extractor;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return new Iterator<Item>() {
                private final Iterator<? extends TPrevItem> sourceIterator = source.iterator();
                private Iterator<Item> current;

                @Override
                public boolean hasNext() {
                    while (true) {
                        if (current == null) {
                            if (sourceIterator.hasNext()) {
                                TPrevItem prevItem = sourceIterator.next();
                                current = extractor.invoke(prevItem).iterator();
                                continue;
                            } else {
                                return false;
                            }
                        }

                        if (!current.hasNext()) {
                            current = null;
                            continue;
                        }

                        return true;
                    }
                }

                @Override
                public Item next() {
                    hasNext();
                    return current.next();
                }

                @Override
                public void remove() {
                    hasNext();
                    current.remove();
                }
            };
        }

        @Override
        protected int approximateCount() {
            return source.approximateCount() * PREDEFINED_APPROXIMATED_SIZE;
        }
    }

    private static class QueryRoot<Item> extends Query<Item> {
        protected final Iterable<Item> source;

        public QueryRoot(Iterable<Item> source) {
            this.source = source;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return source.iterator();
        }

        @Override
        protected int approximateCount() {
            return approximateCount(source);
        }

    }

    private static class QueryRootSingle<Item> extends Query<Item> {

        private Item item;
        private boolean hasNext;

        private QueryRootSingle(Item item) {
            this.item = item;
            hasNext = true;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return new Iterator<Item>() {
                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public Item next() {
                    hasNext = false;
                    return item;
                }

                @Override
                public void remove() {
                    hasNext = false;
                }
            };
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        protected int approximateCount() {
            return 1;
        }
    }

    private static class QueryRootList<Item> extends QueryRoot<Item> {

        public QueryRootList(List<Item> source) {
            super(source);
        }

        @Override
        public List<Item> toList() {
            return (List<Item>) source;
        }

        @Override
        public int count() {
            return ((List<Item>) source).size();
        }

        @Override
        protected int approximateCount() {
            return ((List<Item>) source).size();
        }
    }

    private static class SelectExpr<Item, Result> extends QueryChain<Result, Item> {
        private final Func<Item, Result> selector;


        public SelectExpr(Query<Item> iterator, Func<Item, Result> selector) {
            super(iterator);
            this.selector = selector;
        }

        @NonNull
        @Override
        public Iterator<Result> iterator() {
            return new Iterator<Result>() {
                Iterator<? extends Item> iterator = source.iterator();

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Result next() {
                    return selector.invoke(iterator.next());
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }

        @Override
        public int count() {
            return super.count();
        }
    }

    private static abstract class WhereExprIterator<Item> implements Iterator<Item> {
        Iterator<? extends Item> iterator;
        private Item next;
        private boolean hasNext;

        protected WhereExprIterator(Iterable<Item> source) {
            iterator = source.iterator();
        }

        @Override
        public boolean hasNext() {
            if (hasNext)
                return true;

            while (iterator.hasNext() && !hasNext) {
                next = iterator.next();
                hasNext = filter(next);
            }
            return hasNext;
        }

        @Override
        public Item next() {
            if (hasNext()) {
                Item r = next;
                next = null;
                hasNext = false;
                return r;
            }
            return null;
        }

        @Override
        public void remove() {
            if (hasNext()) {
                next = null;
                hasNext = false;
                iterator.remove();
            }
        }

        protected abstract boolean filter(Item item);
    }

    private static class SkipExpr<Item> extends QueryChain<Item, Item> {
        private final int count;

        public SkipExpr(Query<Item> iterator, int count) {
            super(iterator);
            this.count = count;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return new Iterator<Item>() {
                Iterator<? extends Item> iterator = source.iterator();
                private int counter = count;
                private boolean hasNext;

                @Override
                public boolean hasNext() {
                    if (hasNext)
                        return true;

                    while ((hasNext = iterator.hasNext()) && --counter >= 0)
                        iterator.next();

                    return hasNext;
                }

                @Override
                public Item next() {
                    if (hasNext()) {
                        hasNext = false;
                        return iterator.next();
                    }
                    return null;
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }

        @Override
        protected int approximateCount() {
            return Math.max(0, super.approximateCount() - count);
        }
    }

    private static class LimitExpr<Item> extends QueryChain<Item, Item> {
        private int count;

        public LimitExpr(Query<Item> iterator, int count) {
            super(iterator);
            this.count = count;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            return new Iterator<Item>() {
                Iterator<? extends Item> iterator = source.iterator();
                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return iterator.hasNext() && currentIndex < count;
                }

                @Override
                public Item next() {
                    if (hasNext()) {
                        currentIndex++;
                        return iterator.next();
                    }
                    return null;
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }

        @Override
        protected int approximateCount() {
            return Math.min(count, super.approximateCount());
        }
    }

    private static class CustomOrderByExp<Item> extends QueryChain<Item, Item> {
        private Comparator<Item> comparator;

        public CustomOrderByExp(Query<Item> iterator, Comparator<Item> comparator) {
            super(iterator);
            this.comparator = comparator;
        }

        @NonNull
        @Override
        public Iterator<Item> iterator() {
            List<Item> lst = source.toList();
            Collections.sort(lst, comparator);
            return lst.iterator();
        }

        @Override
        public int count() {
            return super.count();
        }

    }
}

