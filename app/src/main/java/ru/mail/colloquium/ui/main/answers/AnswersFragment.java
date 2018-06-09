package ru.mail.colloquium.ui.main.answers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.PagedDataSource;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.service.AppService;
import ru.mail.colloquium.ui.base.BaseFragment;
import ru.mail.colloquium.ui.views.MyFrameLayout;

import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;

public class AnswersFragment extends BaseFragment implements AppService.AnswerUpdatedEventHandler {

    Unbinder unbinder;
    @BindView(R.id.list) RecyclerView list;
    @BindView(R.id.root) MyFrameLayout root;
    @BindView(R.id.placeholder) View placeholder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_answers, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyAdapter adapter = new MyAdapter();
        adapter.init();
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        appService().answerUpdatedEvent.add(this);
        appService().requestAnswers();
    }

    @Override
    public void onPause() {
        super.onPause();
        appService().answerUpdatedEvent.remove(this);
    }

    @Override
    public void onAnswerUpdated() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        activity.runOnUiThread(() -> {
            MyAdapter adapter = (MyAdapter) list.getAdapter();
            adapter.init();
            adapter.notifyDataSetChanged();

            placeholder.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);

        });
    }

    private static class MyAdapter extends RecyclerView.Adapter {

        private RecyclerView list;
        private Context context;
        private LayoutInflater inflater;
        private AnswersDataSource dataSource;

        private MyAdapter() {
        }

        public void init() {
            dataSource = new AnswersDataSource();
        }


        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            list = recyclerView;
            context = list.getContext();
            inflater = LayoutInflater.from(context);
            super.onAttachedToRecyclerView(recyclerView);

        }

        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            inflater = null;
            context = null;
            list = null;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AnswerViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((AnswerViewHolder) holder).bind(dataSource.get(position));
        }

        @Override
        public int getItemCount() {
            return dataSource.count();
        }
    }

    private static class AnswersDataSource extends PagedDataSource<Answer> {

        private final int count;

        public AnswersDataSource() {
            super(new Answer());
            count = (int) data().answers.count();
        }

        @Override
        protected List<Answer> prepareDataSync(int skip, int limit) {
            return data().answers.select(skip, limit).toList();
        }

        @Override
        public int count() {
            return count;
        }
    }
}
