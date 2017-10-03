package Fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.DialogFragment;

import com.startwork.streetguide.R;


public class FragmentRoute extends DialogFragment implements Button.OnClickListener {

    public interface saveRouteDialogListener {

        void onFinishInputDialog(String strRouteName);
    }

    private EditText m_tfRouteName;

    public FragmentRoute() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_save_route, container);

        m_tfRouteName = (EditText) view.findViewById(R.id.tfRouteName);
        Button m_btnSave = (Button) view.findViewById(R.id.btnSave);

        getDialog().setTitle(R.string.strSaveRouteLabel);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        m_tfRouteName.requestFocus();
        m_btnSave.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        saveRouteDialogListener activity = (saveRouteDialogListener) getActivity();
        activity.onFinishInputDialog(m_tfRouteName.getText().toString());
        this.dismiss();
    }
}
