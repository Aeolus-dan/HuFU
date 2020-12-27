package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.hufuinfo.hufudigitalgoldenchain.activity.MainActivity;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.PhoneNumber;
import com.hufuinfo.hufudigitalgoldenchain.bean.UpdateAccountInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserInfoListResult;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private EditText emailTv;
    private EditText cardIDTv;
    private EditText bankCardTv;

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

        view.findViewById(R.id.confirm_commit_btn).setOnClickListener(listener -> {
            changeAccountInfo();
        });
    }

    /**
     * change account infos
     */
    private void changeAccountInfo() {
       String emailString = emailTv.getText().toString().trim();
       String cardIDString = cardIDTv.getText().toString().trim();
       String bankIDString = bankCardTv.getText().toString().trim();
        //判断所有参数不为空
        if (TextUtils.isEmpty(emailString)
                || TextUtils.isEmpty(cardIDString)
                || TextUtils.isEmpty(bankIDString)){
            Toast.makeText(getActivity(),"请检查修改的内容，内容不能为空",Toast.LENGTH_LONG).show();
            return;
        }
        //判断银行卡号&身份证号格式
        if (!isRealIDCard(cardIDString)){
            Toast.makeText(getActivity(),"身份证号码格式有误，请重新输入。",Toast.LENGTH_LONG).show();
            return;
        }

        if (!matchbankID(bankIDString)){
            Toast.makeText(getActivity(),"银行卡号码格式有误，请重新输入。",Toast.LENGTH_LONG).show();
            return;
        }

        if (!isEmail(emailString)){
            Toast.makeText(getActivity(),"邮箱格式有误，请重新输入。",Toast.LENGTH_LONG).show();
            return;
        }

        //提交请求
        postInfos(accountTv.getText().toString().trim(),cardIDString,bankIDString);
    }

    /**
     * 提交信息修改
     * @param phoneNumberString  手机号
     * @param cardIDString 身份证号码
     * @param bankIDString 银行卡号码
     */
    private void postInfos(@NonNull String phoneNumberString, @NonNull String cardIDString, @NonNull String bankIDString) {
        PhoneNumber mPhoneNumber = new PhoneNumber(mUserPhoneNumber);

        UpdateAccountInfo updateAccountInfo = new UpdateAccountInfo(phoneNumberString,cardIDString,bankIDString);

        QueryUserInfo queryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(updateAccountInfo));

        queryUserInfo.findAccountPassword(antiFake, body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (result.success) {
                        Toast.makeText(getActivity(), "信息修改成功", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getActivity(), "信息修改失败", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

    }


    public  boolean isRealIDCard(String IDCard){
        int[] xishu = new int[] { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7,
                9, 10, 5, 8, 4, 2 };
        char[] last = new char[] { '1', '0', 'X', '9', '8', '7', '6',
                '5', '4', '3', '2' };
        if (null == IDCard || IDCard.length() != 18) {
            return false;
        }
        char[] string = IDCard.toCharArray();
        int sum = 0;
        for (int i = 0; i < string.length - 1; i++) {
            sum += (string[i] - '0') * xishu[i];
        }
        return last[sum % 11] == string[17];

    }
    public  boolean matchbankID(String cardNo) {
        try {
            int[] cardNoArr = new int[cardNo.length()];
            for (int i = 0; i < cardNo.length(); i++) {
                cardNoArr[i] = Integer.valueOf(String.valueOf(cardNo.charAt(i)));
            }
            for (int i = cardNoArr.length - 2; i >= 0; i -= 2) {
                cardNoArr[i] <<= 1;
                cardNoArr[i] = cardNoArr[i] / 10 + cardNoArr[i] % 10;
            }
            int sum = 0;
            for (int i = 0; i < cardNoArr.length; i++) {
                sum += cardNoArr[i];
            }
            return sum % 10 == 0;
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean isEmail(String string) {
        if (string == null)
            return false;
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches())
            return true;
        else
            return false;
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
                        if(!MainActivity.isVisitor){
                            Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                        }
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
