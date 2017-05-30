package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tinkersstudio.musiccloud.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jun Trinh on 2/19/2017.
 */

public class FragmentUserInfo extends Fragment {

    //Sign In Default Menu
    private static final String UNCHANGED_CONFIG_VALUE = "CHANGE-ME";
    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final String FIREBASE_TOS_URL = "https://firebase.google.com/terms/";
    private static final int RC_SIGN_IN = 100;
    private static final String TAG = "UserInfo";
    FirebaseUser user;



    View rootView;
    Button signInButton, signOutButton;
    ImageView mUserProfilePicture;
    EditText mUserName, mUserGenre, mUserSinger;
    public FragmentUserInfo(){
        //require an empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_user_info, container, false);
        initLayout();
        readFile();
        initListener();
        setHasOptionsMenu(true);
        return rootView;
        //initialize button in here
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void initLayout()
    {

        mUserName = (EditText)rootView.findViewById(R.id.fragment_user_info_name);
        mUserGenre = (EditText)rootView.findViewById(R.id.fragment_user_info_genre);
        mUserSinger = (EditText)rootView.findViewById(R.id.fragment_user_info_singer);

        signInButton = (Button)rootView.findViewById(R.id.user_info_sign_in);
        signOutButton = (Button)rootView.findViewById(R.id.user_info_sign_out);
        mUserProfilePicture = (ImageView)rootView.findViewById((R.id.fragment_user_profile_picture));


        //user signout
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            if (user.getPhotoUrl() != null) {
                Glide.with(FragmentUserInfo.this)
                        .load(user.getPhotoUrl())
                        .fitCenter()
                        .into(mUserProfilePicture);
            }
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        }
        else
        {
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_save){
            saveFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initListener()
    {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set the signed in function
                //Intent intent = new Intent(getActivity().getBaseContext(), AuthUiActivity.class);
                //startActivity(intent);
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder()
                                .setTheme(getSelectedTheme())
                                .setLogo(getSelectedLogo())
                                .setProviders(getSelectedProviders())
                                .setTosUrl(getSelectedTosUrl())
                                .setIsSmartLockEnabled(false)
                                .setAllowNewEmailAccounts(true)
                                .build(),
                        RC_SIGN_IN);

            }
        });


        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                signInButton.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.GONE);
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        showSnackbar(R.string.unknown_response);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);
        // Successfully signed in
        if (resultCode == ResultCodes.OK) {
            //startActivity(SignedInActivity.createIntent(getActivity(), response));
            //getActivity().finish();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);

            //get the user picture
            if (user.getPhotoUrl() != null) {
                Glide.with(FragmentUserInfo.this)
                        .load(user.getPhotoUrl())
                        .fitCenter()
                        .into(mUserProfilePicture);
            }

            /**
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                            }
                        }
                    });
             */
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    /**Read the file about user profile and update the field*/
    public void readFile()
    {
        //FIXME: Reading name is still wrong
        try {
            FileInputStream in = getActivity().getApplicationContext().openFileInput("musicclouduser.txt");
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            String info = new String(buffer);
            String[] content = info.split(" ");
            mUserName.setText(content[0]);
            mUserGenre.setText(content[1]);
            mUserSinger.setText(content[2]);
        }
        catch (Exception e)
        {
            Log.i("UserInfo", "File is missing");
        }
    }


    public void saveFile()
    {
        try
        {
            String content = mUserName.getText() + " " + mUserGenre.getText() + " " + mUserSinger.getText();
            FileOutputStream outputStream = getActivity().getApplicationContext()
                    .openFileOutput("musicclouduser.txt", Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        }
        catch(IOException e)
        {
            Log.i("UserInfo", "File is missing. Can't write to file");
        }

    }

    @MainThread
    @StyleRes
    private int getSelectedTheme() {
        //set theme
        return R.style.GreenTheme;
        //return AuthUI.getDefaultTheme();
    }

    @MainThread
    @DrawableRes
    private int getSelectedLogo() {
        return R.mipmap.launcher;
    }

    @MainThread
    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();


        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                        .setPermissions(getFacebookPermissions())
                        .build());
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());

        return selectedProviders;
    }

    @MainThread
    private String getSelectedTosUrl() {
        return FIREBASE_TOS_URL;
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(rootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @MainThread
    private List<String> getFacebookPermissions() {
        List<String> result = new ArrayList<>();
        //result.add("user_friends");
        //result.add("user_photos");
        return result;
    }

    @MainThread
    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
        //result.add(Scopes.GAMES);
        //result.add(Scopes.DRIVE_FILE);
        return result;
    }

}
