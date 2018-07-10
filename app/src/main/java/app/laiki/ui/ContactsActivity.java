package app.laiki.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import app.laiki.model.entities.Contact;
import app.laiki.service.AppService;
import butterknife.BindView;
import butterknife.ButterKnife;
import app.laiki.R;
import app.laiki.ui.base.BaseActivity;
import app.laiki.ui.main.contacts.ContactsAdapter;

import static app.laiki.App.appService;

public class ContactsActivity extends BaseActivity implements AppService.ContactUpdatedEventHandler {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appbar) AppBarLayout appbar;
    @BindView(R.id.list) RecyclerView list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new ContactsAdapter());

    }

    @Override
    protected void onResume() {
        super.onResume();
        appService().contactUpdated.add(this);
        list.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        appService().contactUpdated.remove(this);
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contacts, menu);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((ContactsAdapter) list.getAdapter()).setFilter(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContactUpdated(Contact args) {
        runOnUiThread(() -> list.getAdapter().notifyDataSetChanged());
    }
}
