package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.hufuinfo.hufudigitalgoldenchain.R;

public class LoginConventionFragment extends Fragment {
    private EditText userNumberEdit;
    private EditText passwordEdit;
    private Activity mActivity;

    public LoginConventionFragment() {
    }

    @Override
    public void onAttach(Context context) {
        mActivity = getActivity();
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login_convention, container, false);
        userNumberEdit = root.findViewById(R.id.user_number);
        passwordEdit = root.findViewById(R.id.password);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public String getUserNumber() {
        return userNumberEdit.getText().toString();
    }

    public String getPassword() {
        return passwordEdit.getText().toString();
    }

    public void setUserNumberEdit(String userNumberStr) {
        this.userNumberEdit.setText(userNumberStr);
    }

    public void setPasswordEdit(String passwordStr) {
        this.passwordEdit.setText(passwordStr);
    }
}
