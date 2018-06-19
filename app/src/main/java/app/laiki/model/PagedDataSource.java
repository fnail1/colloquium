package app.laiki.model;

import java.util.Collections;
import java.util.List;

import app.laiki.toolkit.concurrent.ThreadPool;

public abstract class PagedDataSource<T> implements AsyncDataSource<T> {

    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int DEFAULT_PREFETCH_THRESHOLD = 5;
    public final T EMPTY;

    private final int pageSize;
    private final int prefetchThreshold;
    private List<T> activePage = Collections.emptyList();
    private int activePageIndex = -1;
    private List<T> shadowPage = Collections.emptyList();
    private int shadowPageIndex = -1;
    private volatile int requestedPage = -1;

    public PagedDataSource(T empty) {
        this(DEFAULT_PAGE_SIZE, DEFAULT_PREFETCH_THRESHOLD, empty);
    }

    public PagedDataSource(int pageSize, int prefetchThreshold, T empty) {
        this.pageSize = pageSize;
        this.prefetchThreshold = prefetchThreshold;
        EMPTY = empty;
    }

    @Override
    public void requestData() {
        prefetch(0);
    }

    @Override
    public T get(int index) {
        final int requestedPageIndex = index / pageSize;

        if (requestedPageIndex == activePageIndex) {
            int offset = index % pageSize;
            if (offset < prefetchThreshold && shadowPageIndex != (requestedPageIndex - 1)) {
                prefetch(requestedPageIndex - 1);
            } else if (offset > (pageSize - prefetchThreshold) && shadowPageIndex != (requestedPageIndex + 1)) {
                prefetch(requestedPageIndex + 1);
            }
            try {
                return activePage.get(offset);
            } catch (IndexOutOfBoundsException e) {
//                e.printStackTrace();
                return EMPTY;
            }
        }

        if (requestedPageIndex == shadowPageIndex) {
            swapPages();
            return get(index);
        }

        prefetchSync(requestedPageIndex);
        return get(index);
    }

    private synchronized void swapPages() {
        int ti = shadowPageIndex;
        shadowPageIndex = activePageIndex;
        activePageIndex = ti;

        List<T> tl = shadowPage;
        shadowPage = activePage;
        activePage = tl;
    }

    private void prefetch(final int pageIndex) {
        if (requestedPage == pageIndex)
            return;
        requestedPage = pageIndex;

        ThreadPool.DB.execute(() -> prefetchSync(pageIndex));
    }

    private synchronized void prefetchSync(int pageIndex) {
        if (shadowPageIndex != pageIndex) {
            List<T> list = prepareDataSync(pageIndex * pageSize, pageSize);

            shadowPageIndex = pageIndex;
            shadowPage = list;
        }
        requestedPage = -1;
    }

    protected abstract List<T> prepareDataSync(int skip, int limit);
}
