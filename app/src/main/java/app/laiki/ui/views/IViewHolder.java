package app.laiki.ui.views;

import android.support.v7.widget.RecyclerView;

public interface IViewHolder<TData> {
    default void bind(TData data) {
    }

    default void onAttachView(RecyclerView.Adapter adapter) {
    }

    default void onDetachView() {
    }
}
