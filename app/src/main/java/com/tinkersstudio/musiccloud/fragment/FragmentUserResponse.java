package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.tinkersstudio.musiccloud.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;

public class FragmentUserResponse extends Fragment {

    private boolean FLAG = true;
    //Binding layout
    private Unbinder unbinder;

    @BindView(R.id.send_response)
    Button responseButton;


    @BindView(R.id.user_response)
    EditText userResponse;

    @BindView(R.id.user_response_root)
    View view;

    public FragmentUserResponse() {
        // Required empty public constructor
    }

    public static FragmentUserResponse newInstance(String param1, String param2) {
        FragmentUserResponse fragment = new FragmentUserResponse();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //binding in fragment
        view = inflater.inflate(R.layout.fragment_fragment_user_response, container, false);
        unbinder = ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }



    /**Button listener*/
    @OnClick(R.id.send_response)
    public void send(View view) {
        try
        {
            Intent feedbackEmail = new Intent(Intent.ACTION_SEND);
            feedbackEmail.setType("text/email");
            feedbackEmail.putExtra(Intent.EXTRA_EMAIL, new String[] {"dungtrinh1993@gmail.com"});
            feedbackEmail.putExtra(Intent.EXTRA_SUBJECT, "User Feedback");
            feedbackEmail.putExtra(Intent.EXTRA_TEXT, "Testing user feedback");
            startActivity(Intent.createChooser(feedbackEmail, "Send Feedback:"));
            FLAG = true;
        }
        catch (Exception e)
        {
            Toasty.warning(getActivity(), "Can't access email service", Toast.LENGTH_SHORT, true).show();
            FLAG = false;

        }
        finally {
            /**
            if (FLAG != false)
            {
                Snackbar.make(view, R.string.email_sent_confirm, Snackbar.LENGTH_SHORT)
                        .show();
            }
             */

        }
        //Make snack bar in here
    }

}
