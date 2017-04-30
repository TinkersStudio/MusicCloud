package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.authentication.AuthUiActivity;

/**
 * Created by Owner on 2/19/2017.
 */

public class FragmentUserInfo extends Fragment {

    View rootView;
    Button signInButton;
    public FragmentUserInfo(){
        //require an empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_user_info, container, false);
        initLayout();
        initListener();
        return rootView;
        //initialize button in here
    }

    public void initLayout()
    {
        signInButton = (Button)rootView.findViewById(R.id.user_info_sign_in);
    }

    public void initListener()
    {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getBaseContext(), AuthUiActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
