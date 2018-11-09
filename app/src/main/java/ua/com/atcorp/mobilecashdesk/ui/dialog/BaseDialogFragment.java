package ua.com.atcorp.mobilecashdesk.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import ua.com.atcorp.mobilecashdesk.R;

/**

 */
public abstract class BaseDialogFragment extends DialogFragment {
    protected abstract String getDialogTag();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        //DialogFragment dismissed on orientation change when setRetainInstance(true) is set (compatibility library)
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);

        super.onDestroyView();
    }

    public void show(FragmentActivity fragmentActivity) {
        try {
            FragmentManager fm = fragmentActivity.getSupportFragmentManager();

            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag(getDialogTag());
            if (prev != null) {
                ft.remove(prev);
            }
            // show the dialog.
            show(ft, getDialogTag());
        } catch (Exception err) {
            showToastLong(err.getMessage());
            String m = "";
            for(StackTraceElement line : err.getStackTrace())
                m += line.getClassName() + "." + line.getMethodName() + "." + line.getLineNumber();
            showToastLong(m);
        }
    }

    public int show(FragmentTransaction transaction, String tag) {
        return show(transaction, tag, true);
    }


    public int show(FragmentTransaction transaction, String tag, boolean allowStateLoss) {
        transaction.add(this, tag);
        return allowStateLoss ? transaction.commitAllowingStateLoss() : transaction.commit();
    }

    public void hide(FragmentActivity fragmentActivity) {
        if (fragmentActivity != null) {
            FragmentManager fm = fragmentActivity.getSupportFragmentManager();

            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag(getDialogTag());
            if (prev != null) {
                ft.remove(prev);
            }
            ft.commitAllowingStateLoss();
        }
    }

    public void showToastLong(final String message) {
        final Context context = getActivity();
        if (context != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
        }
    }
}
