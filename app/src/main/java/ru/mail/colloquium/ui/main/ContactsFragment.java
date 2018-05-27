package ru.mail.colloquium.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.model.entities.PhoneNumber;
import ru.mail.colloquium.model.types.ContactPhoneNumber;
import ru.mail.colloquium.ui.base.BaseFragment;
import ru.mail.colloquium.ui.views.MyFrameLayout;

import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.toolkit.collections.Query.query;

public class ContactsFragment extends BaseFragment {
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
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private static class MyAdapter extends RecyclerView.Adapter {

        private final List<Object> data;
        private RecyclerView list;
        private Context context;
        private LayoutInflater inflater;
        private final List<Contact> contacts;
        private final LongSparseArray<ArrayList<ContactPhoneNumber>> phones;


        private MyAdapter() {
            contacts = data().contacts.selectAb().toList();
            phones = data().phoneNumbers.selectToSync().groupByLong(p -> p.link.contact);
            Contact first = query(contacts).first(c -> phones.get(c._id) == null);
            data = query(contacts).extract(c -> query(c).cast().concat(phones.get(c._id))).toList();
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
            if (data.get(position) instanceof Contact)
                return R.layout.item_contact;
            else if (data.get(position) instanceof PhoneNumber)
                return R.layout.item_phone;
            throw new IllegalArgumentException(String.valueOf(data.get(position)));
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case R.layout.item_contact:
                    return new ContactViewHolder(inflater, parent);
                case R.layout.item_phone:
                    return new PhoneViewHolder(inflater, parent);
            }
            throw new IllegalArgumentException(String.valueOf(viewType));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()){
                case R.layout.item_contact:
                    ((ContactViewHolder) holder).bind((Contact) data.get(position));
                    break;
                case R.layout.item_phone:
                    ((PhoneViewHolder) holder).bind((ContactPhoneNumber) data.get(position));
                    break;

            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
