package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PathEffect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.PerfectPersonalInfoActivity;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UserRegister;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.RegisterInfoUser;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static android.content.Context.MODE_PRIVATE;

/**
 * 邀请人注册
 */
public class RegisterUserFragment extends Fragment {

    private Activity mActivity;
    private Button mRegisterBtn, mRequestVerificationBtn;
    private EditText passwordEdit, repeatPasswodEdit;
    private EditText phoneNumberEdit, invitationPhoneNumeberEdit;
    private EditText pinVerificationEdit;
    private VirtualCpk mVirtualCpk;

    public RegisterUserFragment() {
    }

    public static RegisterUserFragment newInstance() {
        RegisterUserFragment fragment = new RegisterUserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register_user, container, false);
        findViewId(root);
        return root;
    }

    private void findViewId(View view) {
        mRegisterBtn = view.findViewById(R.id.register_button);
        mRequestVerificationBtn = view.findViewById(R.id.request_verification_button);
        phoneNumberEdit = view.findViewById(R.id.phone_number);
        passwordEdit = view.findViewById(R.id.password);
        repeatPasswodEdit = view.findViewById(R.id.repeat_password);
        invitationPhoneNumeberEdit = view.findViewById(R.id.invitation_phone_number);
        pinVerificationEdit = view.findViewById(R.id.pin_Verification);

        mRegisterBtn.setOnClickListener(Listener -> {
            String phoneNumber = phoneNumberEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            String repeatPassword = passwordEdit.getText().toString();
            String invitationCode = invitationPhoneNumeberEdit.getText().toString();
            String pinVerification = pinVerificationEdit.getText().toString();
            register(phoneNumber, password, invitationCode, pinVerification);
        });
        mRequestVerificationBtn.setOnClickListener(Listener -> {
            String moblie = phoneNumberEdit.getText().toString();
            RetrofitUtils.retriveCode(mActivity, moblie, mVirtualCpk);
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

    /**
     * 注册函数, 邀请人注册
     * 根据用户的输入信息向服务发送注册请求，并根据服务的返回结果处理
     * 后展示给用信息
     *
     * @param phoneNumber     用户手机号码
     * @param password        用户密码
     * @param associatedId    推存人手机号码
     * @param verificationPin 手机短信验证码
     */
    private void register(@NonNull String phoneNumber, @NonNull String password,
                          @NonNull String associatedId, @NonNull String verificationPin) {

        UserRegister userRegister = RetrofitUtils.create(UserRegister.class);

        RegisterInfoUser registerInfoUser = new RegisterInfoUser(phoneNumber, password, associatedId, verificationPin);
        String keyId = CombinationSecretKey.getSecretKey("userRegister.do");
        String code = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(registerInfoUser));
        HuFuCode mhufuCode = new HuFuCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(mhufuCode));
        userRegister.userRegister(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (result.success) {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                        String antiFake = result.data.antiFake.trim();
                        SharedPreferences.Editor userPreferences = mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE).edit();
                        userPreferences.putString(ConstantUtils.USER_ACCOUNT, phoneNumber);
                        userPreferences.putString(ConstantUtils.ANTI_FAKE, antiFake);
                        userPreferences.apply();
                        //跳转到完善个人信息Activity
                        Intent perfectIntent = new Intent(getActivity(), PerfectPersonalInfoActivity.class);
                        perfectIntent.putExtra("PERFECT_TYPE", "register");
                        startActivity(perfectIntent);
                    } else {
                        Snackbar.make(mRegisterBtn, "注册失败！" + result.msg, Snackbar.LENGTH_INDEFINITE).show();
                        return;
                    }

                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });
    }

}
