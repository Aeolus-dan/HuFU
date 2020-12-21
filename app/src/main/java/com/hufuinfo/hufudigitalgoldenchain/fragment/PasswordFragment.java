package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.AccountPassword;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PasswordFragment extends Fragment {
    private Activity mActivity;
    private SharedPreferences mUserSharedPre;
    private String mUserPhoneNumber;
    private String antiFake;
    private VirtualCpk mVirtualCpk;
    private EditText oldPwdEt;
    private EditText newPwdEt;
    private EditText confirmEt;

    public PasswordFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mUserSharedPre = getActivity().getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, container, false);
        oldPwdEt = view.findViewById(R.id.old_password_et);
        newPwdEt = view.findViewById(R.id.new_password_et);
        confirmEt = view.findViewById(R.id.confirm_password_et);
        view.findViewById(R.id.confirm_change_btn).setOnClickListener(listener -> {

            String oldPwd = oldPwdEt.getText().toString();
            String newPwd = newPwdEt.getText().toString();
            String confirm = confirmEt.getText().toString();
            if ("".equals(oldPwd) || "".equals(confirm)
                    || "".equals(newPwd)) {
                Toast.makeText(getActivity(), "输入不正确", Toast.LENGTH_SHORT).show();
                return;
            }
            if (confirmEt.getText().toString().equals(newPwdEt.getText().toString())) {
                putChangeAccountPassowrd(oldPwdEt.getText().toString(), newPwdEt.getText().toString());
            } else {
                Toast.makeText(getActivity(), "重复输入密码与新密码不正确", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.modify_password_return_tv).setOnClickListener(listener -> {
            getActivity().onBackPressed();
        });
        return view;
    }

    private void putChangeAccountPassowrd(String oldPwd, String newPwd) {

        AccountPassword mAccountPassword = new AccountPassword(oldPwd, newPwd, mUserPhoneNumber);
        String keyId = CombinationSecretKey.getSecretKey("verifyPassword.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(mAccountPassword));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);


        QueryUserInfo mQueryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(mHuFoCode));

        mQueryUserInfo.changeAccountPassword(antiFake, body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (!(result.success)) {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();

                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

    }

}
