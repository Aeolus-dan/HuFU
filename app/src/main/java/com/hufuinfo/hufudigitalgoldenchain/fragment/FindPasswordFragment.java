package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.CertificateActivity;
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

public class FindPasswordFragment extends Fragment {
    private Activity mActivity;
    private SharedPreferences mUserSharedPre;
    private String mUserPhoneNumber;
    private String antiFake;
    private VirtualCpk mVirtualCpk;
    private EditText newPwdEt;
    private EditText confirmEt;
    private TextView verifCodeBtn;
    private TextView phoneEt;
    private TextInputEditText verifCodeEt;

    public FindPasswordFragment() {
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
        View view = inflater.inflate(R.layout.fragment_find_password, container, false);
        newPwdEt = view.findViewById(R.id.new_password_et);
        confirmEt = view.findViewById(R.id.confirm_password_et);
        view.findViewById(R.id.confirm_change_btn).setOnClickListener(listener -> {

            String newPwd = newPwdEt.getText().toString();
            String confirm = confirmEt.getText().toString();
            if ("".equals(confirm)
                    || "".equals(newPwd)) {
                Toast.makeText(getActivity(), "输入不正确", Toast.LENGTH_SHORT).show();
                return;
            }
            String pin = verifCodeEt.getText().toString();
            if (pin.length() < 5) {
                Toast.makeText(getActivity(), "请输入正确的PIN码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (confirmEt.getText().toString().equals(newPwdEt.getText().toString())) {
                putChangeAccountPassowrd(pin, newPwdEt.getText().toString());
            } else {
                Toast.makeText(getActivity(), "重复输入密码与新密码不正确", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.modify_password_return_tv).setOnClickListener(listener -> {
            getActivity().onBackPressed();
        });
        phoneEt = view.findViewById(R.id.phone_et);
        verifCodeEt = view.findViewById(R.id.verif_code_et);
        if (mUserPhoneNumber != null) {
            phoneEt.setText(mUserPhoneNumber);
        }
        TimeCount  timeCount = new TimeCount(60000, 1000);
        verifCodeBtn = view.findViewById(R.id.verif_code_btn);
        verifCodeBtn.setOnClickListener(listener -> {
           RetrofitUtils.retriveCode(getActivity(), mUserPhoneNumber, mVirtualCpk);
           timeCount.start();
        });
        return view;
    }

    private void putChangeAccountPassowrd(String pin, String newPwd) {
        //TODO 1. AccountPassword 改变映射, 2.接口更改
        AccountPassword mAccountPassword = new AccountPassword(pin, newPwd, mUserPhoneNumber);
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












    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            verifCodeBtn.setBackgroundColor(Color.green(R.color.gray));
            verifCodeBtn.setClickable(false);
            verifCodeBtn.setText("(" + millisUntilFinished / 1000 + ") 秒后可重新发送");
        }

        @Override
        public void onFinish() {
            verifCodeBtn.setText("重新获取验证码");
            verifCodeBtn.setClickable(true);
            verifCodeBtn.setBackgroundColor(Color.parseColor("#ffffff"));

        }
    }
}
