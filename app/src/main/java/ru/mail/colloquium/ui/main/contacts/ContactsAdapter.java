package ru.mail.colloquium.ui.main.contacts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.toolkit.concurrent.ExclusiveExecutor;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;

import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.toolkit.collections.Query.query;

public class ContactsAdapter extends RecyclerView.Adapter {

    private RecyclerView list;
    private Context context;
    private LayoutInflater inflater;
    private final List<Contact> contacts;
    private List<Contact> filteredContacts;
    private final ExclusiveExecutor executor;
    private String filter;


    public ContactsAdapter() {
        contacts = data().contacts.selectAb().toList();
        filteredContacts = contacts;
        executor = contacts.size() > 50 ? new ExclusiveExecutor(0, ThreadPool.SCHEDULER, this::applyFilter) : null;
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
                ((ContactViewHolder) holder).bind(filteredContacts.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return filteredContacts.size();
    }

    public void setFilter(final String filter) {
        this.filter = filter;

        if (executor != null) {
            executor.execute(false);
        } else {
            applyFilter();
        }
    }

    private void applyFilter() {
        String[] words = filter.toLowerCase().split("\\W");

        List<Contact> l = query(contacts).where(c -> {
            String[] names = c.displayName.toLowerCase().split("\\W");

            nextWord:
            for (String word : words) {
                for (String name : names) {
                    if(name.startsWith(word))
                        continue nextWord;
                }
                return false;
            }
            return true;

        }).toList();

        ThreadPool.UI.post(() -> onFilterComplete(l));
    }

    private void onFilterComplete(List<Contact> l) {
        filteredContacts = l;
        notifyDataSetChanged();
    }
}
