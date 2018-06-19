package app.laiki.toolkit.collections;

import java.util.Iterator;

public class CollectionsUtils {
    /**
     * Merges source collection into destination by calling
     * - {@link MergeVisitor#insert(Object, int)} - source collection contains item not existing in destination one;
     * - {@link MergeVisitor#delete(Object)} - destination collection contains item not existing in source;
     * - {@link IntegerMergeVisitor#update(int, int)} - item occers in both collections;
     *
     * @param src     - source collection
     * @param dst     - destination collection
     * @param visitor - the interface implementing update logic
     */
    public static <TSrc, TDst> void merge(Iterable<TSrc> src, Iterable<TDst> dst, MergeVisitor<TSrc, TDst> visitor) {
        merge(src, dst, visitor, 0);
    }

    /**
     * Merges source collection into destination by calling
     * - {@link MergeVisitor#insert(Object, int)} - source collection contains item not existing in destination one;
     * - {@link MergeVisitor#delete(Object)} - destination collection contains item not existing in source;
     * - {@link IntegerMergeVisitor#update(int, int)} - item occers in both collections;
     *
     * @param src        - source collection
     * @param dst        - destination collection
     * @param visitor    - the interface implementing update logic
     * @param startIndex - index of first item within the page
     */
    public static <TSrc, TDst> void merge(Iterable<TSrc> src, Iterable<TDst> dst, MergeVisitor<TSrc, TDst> visitor, int startIndex) {
        Iterator<TSrc> i1 = src.iterator();
        Iterator<TDst> i2 = dst.iterator();
        TSrc srcObj = null;
        TDst dstObj = null;
        boolean srcFlag = false;
        boolean dstFlag = false;
        int srcIndex = startIndex - 1;

        while (true) {
            if (!srcFlag) {
                if (!i1.hasNext())
                    break;
                srcObj = i1.next();
                srcFlag = true;
                srcIndex++;
            }
            if (!dstFlag) {
                if (!i2.hasNext())
                    break;
                dstObj = i2.next();
                dstFlag = true;
            }

            int c = visitor.compare(srcObj, dstObj, srcIndex);
            if (c < 0) {
                visitor.insert(srcObj, srcIndex);
                srcFlag = false;
                continue;
            }

            if (c > 0) {
                visitor.delete(dstObj);
                dstFlag = false;
                continue;
            }

            visitor.update(srcObj, dstObj, srcIndex);
            srcFlag = false;
            dstFlag = false;
        }

        if (srcFlag)
            visitor.insert(srcObj, srcIndex++);

        while (i1.hasNext())
            visitor.insert(i1.next(), srcIndex++);

        if (dstFlag)
            visitor.delete(dstObj);

        while (i2.hasNext())
            visitor.delete(i2.next());
    }

    public static void merge(int[] src, int[] dst, IntegerMergeVisitor visitor) {
        int srcIdx = 0;
        int dstIdx = 0;
        int srcObj = 0;
        int dstObj = 0;
        boolean srcFlag = false;
        boolean dstFlag = false;

        while (true) {
            if (!srcFlag) {
                if (srcIdx == src.length)
                    break;
                srcObj = src[srcIdx++];
                srcFlag = true;
            }
            if (!dstFlag) {
                if (dstIdx == dst.length)
                    break;
                dstObj = dst[dstIdx++];
                dstFlag = true;
            }

            int c = visitor.compare(srcObj, dstObj, srcIdx);
            if (c < 0) {
                visitor.insert(srcObj);
                srcFlag = false;
                continue;
            }

            if (c > 0) {
                visitor.delete(dstObj);
                dstFlag = false;
                continue;
            }

            visitor.update(srcObj, dstObj);
            srcFlag = false;
            dstFlag = false;
        }

        while (srcIdx < src.length)
            visitor.insert(src[srcIdx++]);

        while (dstIdx < dst.length)
            visitor.delete(dst[dstIdx++]);
    }

    public static void merge(long[] src, long[] dst, LongMergeVisitor visitor) {
        int srcIdx = 0;
        int dstIdx = 0;
        long srcObj = 0;
        long dstObj = 0;
        boolean srcFlag = false;
        boolean dstFlag = false;

        while (true) {
            if (!srcFlag) {
                if (srcIdx == src.length)
                    break;
                srcObj = src[srcIdx++];
                srcFlag = true;
            }
            if (!dstFlag) {
                if (dstIdx == dst.length)
                    break;
                dstObj = dst[dstIdx++];
                dstFlag = true;
            }

            int c = visitor.compare(srcObj, dstObj, srcIdx);
            if (c < 0) {
                visitor.insert(srcObj);
                srcFlag = false;
                continue;
            }

            if (c > 0) {
                visitor.delete(dstObj);
                dstFlag = false;
                continue;
            }

            visitor.update(srcObj, dstObj);
            srcFlag = false;
            dstFlag = false;
        }

        while (srcIdx < src.length)
            visitor.insert(src[srcIdx++]);

        while (dstIdx < dst.length)
            visitor.delete(dst[dstIdx++]);
    }

    public interface IntegerMergeVisitor {
        int compare(int srcObj, int dstObj, int srcIdx);

        int insert(int srcObj, int srcIdx);

        default int insert(int srcObj) {
            return insert(srcObj, -1);
        }

        void delete(int dstObj);

        void update(int srcObj, int dstObj, int srcIdx);

        default void update(int srcObj, int dstObj) {
            update(srcObj, dstObj, -1);
        }
    }

    public interface LongMergeVisitor {
        int compare(long srcObj, long dstObj, int srcIdx);

        long insert(long srcObj, int srcIdx);

        default long insert(long srcObj) {
            return insert(srcObj, -1);
        }

        void delete(long dstObj);

        void update(long srcObj, long dstObj, int srcIdx);

        default void update(long srcObj, long dstObj) {
            update(srcObj, dstObj, -1);
        }
    }

    public interface MergeVisitor<TSrc, TDst> {
        int compare(TSrc srcObj, TDst dstObj, int srcIndex);

        TDst insert(TSrc srcObj, int srcIndex);

        default TDst insert(TSrc srcObj) {
            return insert(srcObj, -1);
        }

        void delete(TDst dstObj);

        void update(TSrc srcObj, TDst dstObj, int srcIndex);

        default void update(TSrc srcObj, TDst dstObj) {
            update(srcObj, dstObj, -1);
        }
    }

}
