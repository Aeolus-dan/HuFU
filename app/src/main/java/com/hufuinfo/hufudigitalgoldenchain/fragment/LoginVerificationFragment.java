package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.RequestPhonePin;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ValidatorUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class LoginVerificationFragment extends Fragment {
    private EditText phoneNumberEdit;
    private EditText pinVerificationEdit;
    private TextView sendVerificationTv;
    private Activity mActivity;
    private VirtualCpk mVirtualCpk;
    private TimeCount pinTimeCount;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login_verification, container, false);
        phoneNumberEdit = root.findViewById(R.id.phone_number);
        pinVerificationEdit = root.findViewById(R.id.pin_Verification);
        sendVerificationTv = root.findViewById(R.id.send_verification_btn);
        sendVerificationTv.setOnClickListener(Listener -> {
            String phoneNumber = getPhoneNumber();
            if (!ValidatorUtils.isMobile(phoneNumber)) {
                Toast.makeText(mActivity, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            retriveCode(mActivity, getPhoneNumber());

        });
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pinTimeCount = new TimeCount(60000, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pinTimeCount != null) {
            pinTimeCount.cancel();
            pinTimeCount = null;
        }
    }

    public String getPhoneNumber() {
        return phoneNumberEdit.getText().toString();
    }

    public String getpinVerification() {
        return pinVerificationEdit.getText().toString();
    }

    public void setPhoneNumberEdit(String userNumberStr) {
        if (phoneNumberEdit != null)
            this.phoneNumberEdit.setText(userNumberStr);
    }

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    public String retriveCode(@NonNull Context context, String mobile) {
        RequestPhonePin requestPhonePin = RetrofitUtils.create(RequestPhonePin.class);
        if (mobile == null || "".equals(mobile)) {
            Toast.makeText(context, "请输入正确的手机号码！", Toast.LENGTH_SHORT).show();
            return null;
        }
        String jsonMobile = CombinationSecretKey.assembleJsonMobile(mobile);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonMobile);

        requestPhonePin.requestPhonePin(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Result -> {
                    if (Result.success) {
                        Toast.makeText(context, "发送成功！", Toast.LENGTH_LONG).show();
                        pinTimeCount.start();
                    } else
                        Toast.makeText(context, Result.msg, Toast.LENGTH_LONG).show();
                }, error -> {
                    Toast.makeText(context, R.string.net_error, Toast.LENGTH_SHORT).show();
                });
        return null;
    }


    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            sendVerificationTv.setClickable(false);
            sendVerificationTv.setText(millisUntilFinished / 1000 + "秒后重新获取");
        }

        @Override
        public void onFinish() {
            sendVerificationTv.setText("重新获取验");
            sendVerificationTv.setClickable(true);
        }
    }
}
