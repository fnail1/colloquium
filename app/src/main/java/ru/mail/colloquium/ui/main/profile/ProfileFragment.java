package ru.mail.colloquium.ui.main.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.mail.colloquium.R;
import ru.mail.colloquium.diagnostics.DebugUtils;
import ru.mail.colloquium.ui.base.BaseActivity;
import ru.mail.colloquium.ui.base.BaseFragment;

import static ru.mail.colloquium.App.data;

public class ProfileFragment extends BaseFragment {
    @BindView(R.id.import_db) Button importDb;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.import_db)
    public void onViewClicked() {
        DebugUtils.importFile((BaseActivity) getActivity());
    }
}
