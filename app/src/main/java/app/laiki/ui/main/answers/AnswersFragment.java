package app.laiki.ui.main.answers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import app.laiki.R;
import app.laiki.model.PagedDataSource;
import app.laiki.model.entities.Answer;
import app.laiki.service.AppService;
import app.laiki.ui.ContactsActivity;
import app.laiki.ui.base.BaseFragment;
import app.laiki.ui.views.MyFrameLayout;

import static app.laiki.App.appService;
import static app.laiki.App.data;

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

        MyAdapter adapter = (MyAdapter) list.getAdapter();
        adapter.init();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        appService().answerUpdatedEvent.remove(this);
    }

    @Override
    public void onAnswerUpdated(List<Answer> args) {
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

    @OnClick(R.id.contacts)
    public void onViewClicked() {
        startActivity(new Intent(getActivity(), ContactsActivity.class));
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
            return dataSource != null ? dataSource.count() : 0;
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
