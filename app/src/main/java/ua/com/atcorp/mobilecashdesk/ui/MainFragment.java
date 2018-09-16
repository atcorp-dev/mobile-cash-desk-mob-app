package ua.com.atcorp.mobilecashdesk.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.com.atcorp.mobilecashdesk.R;

public class MainFragment extends Fragment {

    public interface MainFragmentEventListener {
        void onCartMenuItemClick();
        void onCatalogueMenuItemClick();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        (view.findViewById(R.id.nav_cart)).setOnClickListener(v -> onCartMenuItemClick());
        (view.findViewById(R.id.nav_catalogue)).setOnClickListener(v -> onCatalogueMenuItemClick());
        return view;
    }

    private void onCartMenuItemClick() {
        MainFragmentEventListener listener = (MainFragmentEventListener) getContext();
        if (listener != null)
            listener.onCartMenuItemClick();
    }

    private void onCatalogueMenuItemClick() {
        MainFragmentEventListener listener = (MainFragmentEventListener) getContext();
        if (listener != null)
            listener.onCatalogueMenuItemClick();
    }
}
