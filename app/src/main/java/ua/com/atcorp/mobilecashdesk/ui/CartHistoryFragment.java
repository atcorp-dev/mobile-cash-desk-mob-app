package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.CartHistoryRecyclerViewAdapter;
import ua.com.atcorp.mobilecashdesk.dummy.DummyContent;
import ua.com.atcorp.mobilecashdesk.dummy.DummyContent.DummyItem;
import ua.com.atcorp.mobilecashdesk.repositories.CartRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.CartDto;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CartHistoryFragment extends Fragment {

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private CartRepository mCartRepository;

    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CartHistoryFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CartHistoryFragment newInstance() {
        CartHistoryFragment fragment = new CartHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_carthistory_list, container, false);
        mCartRepository = new CartRepository(getContext());
        View historyListVie = layoutView.findViewById(R.id.list);
        // Set the adapter
        if (historyListVie instanceof RecyclerView) {
            Context context = layoutView.getContext();
            RecyclerView recyclerView = (RecyclerView) historyListVie;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mCartRepository.getCarts((carts, err) -> {
                if (err != null) {
                    err.printStackTrace();
                    Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<DummyItem> items = new ArrayList<>();
                int i = 0;
                for(CartDto cart : carts) {
                    items.add(new DummyItem(++i + "", cart.id, cart.clientInfo));
                }
                recyclerView.setAdapter(new CartHistoryRecyclerViewAdapter(items, mListener));
            });
        }
        return layoutView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } /*else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
