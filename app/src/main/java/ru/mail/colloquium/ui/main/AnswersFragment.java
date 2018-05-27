package ru.mail.colloquium.ui.main;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.types.Gender;
import ru.mail.colloquium.ui.base.BaseFragment;
import ru.mail.colloquium.ui.views.MyFrameLayout;

import static ru.mail.colloquium.App.appState;

public class AnswersFragment extends BaseFragment {
    @BindView(R.id.list) RecyclerView list;
    @BindView(R.id.root) MyFrameLayout root;
    Unbinder unbinder;

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
        list.setAdapter(new MyAdapter());
        list.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        list.addItemDecoration(new AnswerCardDecoration(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private static class MyAdapter extends RecyclerView.Adapter {

        private RecyclerView list;
        private Context context;
        private LayoutInflater inflater;
        private final Answer[] data;

        private MyAdapter() {
            data = new Answer[50];
            Random random = new Random();
            long serverTime = appState().getServerTime();
            for (int i = 0; i < data.length; i++) {
                Answer datum = data[i] = new Answer();
                datum.gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
                datum.created = serverTime - random.nextInt(5 * 24 * 60 * 60) * 1000;
            }

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
            ((AnswerViewHolder) holder).bind(data[position]);
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }
}
