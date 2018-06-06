package ru.mail.colloquium.ui.main.contacts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.ui.base.BaseFragment;
import ru.mail.colloquium.ui.views.MyFrameLayout;

import static ru.mail.colloquium.App.data;

public class ContactsFragment extends BaseFragment {
    @BindView(R.id.list) RecyclerView list;
    @BindView(R.id.root) MyFrameLayout root;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_contacts, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list.setAdapter(new MyAdapter());
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        private final List<Contact> contacts;


        private MyAdapter() {
            contacts = data().contacts.selectAb().toList();
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

        @Override
        public int getItemViewType(int position) {
            return R.layout.item_contact;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case R.layout.item_contact:
                    return new ContactViewHolder(inflater, parent);
            }
            throw new IllegalArgumentException(String.valueOf(viewType));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case R.layout.item_contact:
                    ((ContactViewHolder) holder).bind(contacts.get(position));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }
    }
}
