package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.PhoneNumber;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserInfoListResult;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class UserBasicInfoFragment extends Fragment {
    private Activity mActivity;

    private SharedPreferences mUserSharedPre;
    private String mUserPhoneNumber;
    private String antiFake;
    private VirtualCpk mVirtualCpk;

    private TextView accountTv;
    private TextView realNameTv;
    private TextView emailTv;
    private TextView cardIDTv;
    private TextView bankCardTv;

    @Override
    public void onAttach(Context context) {
        mActivity = getActivity();
        mUserSharedPre = getActivity().getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_basic_user_info, container, false);
        initView(root);
        queryUserInfoList();
        return root;
    }


    private void initView(View view) {
        accountTv = view.findViewById(R.id.user_basic_account_tv);
        realNameTv = view.findViewById(R.id.user_basic_name_tv);
        emailTv = view.findViewById(R.id.user_basic_email_tv);
        cardIDTv = view.findViewById(R.id.user_basic_cardid_tv);
        bankCardTv = view.findViewById(R.id.user_bank_card_tv);
        view.findViewById(R.id.user_basic_info_return_tv).setOnClickListener(listener ->
                mActivity.onBackPressed());
    }


    /**
     * 查询个人信息
     */
    private void queryUserInfoList() {
        PhoneNumber mPhoneNumber = new PhoneNumber(mUserPhoneNumber);
        String keyId = CombinationSecretKey.getSecretKey("userInfoList.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(mPhoneNumber));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);

        QueryUserInfo queryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(mHuFoCode));

        queryUserInfo.queryUserInfoList(antiFake, body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (!(result.success)) {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UserInfoListResult.UserInfo userInfo = result.data.getUserInfo();
                    setBasicUserInfo(userInfo);

                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

    }

    private void setBasicUserInfo(UserInfoListResult.UserInfo mUserInfo) {
        accountTv.setText(mUserInfo.mobile);
        emailTv.setText(mUserInfo.email);
        realNameTv.setText(mUserInfo.name);
        cardIDTv.setText(mUserInfo.idCard);
        bankCardTv.setText(mUserInfo.bankCard);
    }
}
