package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.AboutActivity;
import com.hufuinfo.hufudigitalgoldenchain.activity.CertificateActivity;
import com.hufuinfo.hufudigitalgoldenchain.activity.MainActivity;
import com.hufuinfo.hufudigitalgoldenchain.activity.TransactionInfoActivity;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserBalance;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.AccountPassword;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.PaymentPassword;
import com.hufuinfo.hufudigitalgoldenchain.bean.PhoneNumber;
import com.hufuinfo.hufudigitalgoldenchain.bean.PinPassword;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserBalanceResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserInfoListResult;
import com.hufuinfo.hufudigitalgoldenchain.dialog.PutForwardFragmentDialog;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.QRCodeUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import org.apache.xerces.impl.dv.util.Base64;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PersonalCenterFragment extends Fragment {
    private final static String THIS_FILE = "PersonalCenterFragment";
    private Activity mActivity;
    private String mUserPhoneNumber;
    private VirtualCpk mVirtualCpk;
    private SharedPreferences mUserSharedPre;
    private String antiFake;

    private View mRechargeView;

    private TextView accountBalanceTv, accountIntegralTv, accountLineCreditTv, rewardAmountTv;
    private TextView userNameTv;

    private UserBalanceResult.Data mUserBalanceData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mUserSharedPre = getActivity().getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
        queryUserBalance();  //查询账号信息

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_personal_center, container, false);
        initView(root);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (userNameTv.getText() == null || "".equals(userNameTv.getText().toString())) {
            queryUserInfoList();  //查询个人信息
        }
    }

    /**
     * 通过设置全屏，设置状态栏透明
     *
     * @param activity
     */
    private void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                Window window = activity.getWindow();
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                //导航栏颜色也可以正常设置
//                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = activity.getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                attributes.flags |= flagTranslucentStatus;
//                attributes.flags |= flagTranslucentNavigation;
                window.setAttributes(attributes);
            }
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            queryUserBalance();
        }
    }

    private void initView(View view) {

       /* mRechargeView = view.findViewById(R.id.recharge_ll);
        mRechargeView.setOnClickListener(listener -> {
            RechargeFragmentDialog.getInstance().show(getFragmentManager(), "buyerDialog");
        });*/
        view.findViewById(R.id.withdraw_ll).setOnClickListener(listener -> {
            PutForwardFragmentDialog.getInstance(mUserPhoneNumber, antiFake).show(getFragmentManager(), "putFowrdDialog");
        });

        view.findViewById(R.id.gold_change_ll).setOnClickListener(listener -> {
            showGoldConversionDialog();
        });
        view.findViewById(R.id.my_team_ll).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "teamDetail");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.recharge_record_ll).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "recharge");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.platform_order_ll).setOnClickListener(listener -> {
            Intent platformIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            platformIntent.putExtra("TRANSACTIONTYPE", "platform");
            startActivity(platformIntent);
        });
        view.findViewById(R.id.personal_order_ll).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "personal");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.total_bill_ll).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "allRecord");
            startActivity(personalIntent);
        });

        view.findViewById(R.id.perfect_user_basic_tv).setOnClickListener(listener -> {
            Intent perfectIntent = new Intent(getActivity(), CertificateActivity.class);
            perfectIntent.putExtra("PERFECT_TYPE", "perfect");
            startActivity(perfectIntent);
        });
        view.findViewById(R.id.certificate_tv).setOnClickListener(listener -> {
            againUserCert(mUserPhoneNumber);
        });
        view.findViewById(R.id.friendship_sharing_tv).setOnClickListener(listener -> {
            String url = "http://www.gold2040.com";
            showSharingRQDialog("友情分享", url);
        });

        view.findViewById(R.id.about_tv).setOnClickListener(listener -> {
            Intent intent = new Intent(mActivity, AboutActivity.class);
            startActivity(intent);
        });
        view.findViewById(R.id.setting_tv).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "Setting");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.title_setting_iv).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "Setting");
            startActivity(personalIntent);
        });

        accountBalanceTv = view.findViewById(R.id.user_account_balance_tv);
        accountIntegralTv = view.findViewById(R.id.personal_account_integral_tv);
        accountLineCreditTv = view.findViewById(R.id.personal_line_credit_tv);
        rewardAmountTv = view.findViewById(R.id.personal_reward_tv);
        userNameTv = view.findViewById(R.id.center_user_name_tv);
        view.findViewById(R.id.receiver_tv).setOnClickListener(listener -> {
            showDialogReceiverPassword();
        });

    }

    /**
     * 查询用户账号状态信息
     */
    private void queryUserBalance() {
        if (mUserPhoneNumber == null || mUserPhoneNumber.isEmpty()) {
            return;
        }
        PhoneNumber phoneNumber = new PhoneNumber(mUserPhoneNumber);
        String keyId = CombinationSecretKey.getSecretKey("userBalance.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(phoneNumber));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.queryUserBalance(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.success) {
                        mUserBalanceData = userBalanceResult.data;
                        showUserBalance();
                    } else {
                       if (!MainActivity.isVisitor){
                           Toast.makeText(getActivity(), "查询账号状态结果失败!", Toast.LENGTH_LONG).show();
                       }
                    }
                }, error -> {
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    //"未发放奖励额度：" + DateUtils.getStringFloat(mUserBalanceData.userInfo.totalReward - mUserBalanceData.userInfo.rewardAmount)
    private void showUserBalance() {
     /*   String[] groupData = {"账号状态"};
        String[][] childData = {{"账户余额：" + mUserBalanceData.userInfo.balance + "金本币", "可用信用额度：" + mUserBalanceData.userInfo.creditAmount + "金本币",

                "积分账户：" + mUserBalanceData.userInfo.freezeAmount + "积分", "已发放奖励额度：" +
        }};*/
        accountBalanceTv.setText(String.format("%.2f",mUserBalanceData.userInfo.balance));

        accountIntegralTv.setText(String.format("%.2f",mUserBalanceData.userInfo.freezeAmount));
        accountLineCreditTv.setText(String.format("%.2f",mUserBalanceData.userInfo.creditAmount));
        rewardAmountTv.setText(String.format("%.2f",mUserBalanceData.userInfo.rewardAmount));
    }


    private void showGoldConversionDialog() {
        android.support.v7.app.AlertDialog builder = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getLayoutInflater();// LayoutInflater.from(R.layout.gold_conversion_dialog, null);
        final View view = inflater.inflate(R.layout.gold_conversion_dialog, null);
        EditText goldConversionEt = view.findViewById(R.id.gold_conversion_num);
        view.findViewById(R.id.change_gold_btn).setOnClickListener(listener -> {
            String priceGoldNum = goldConversionEt.getText().toString();
         /*   if (Double.parseDouble(priceGoldNum) < 1) {
                Toast.makeText(getActivity(), "请输入大于等于1数", Toast.LENGTH_SHORT).show();
                return;
            }*/
            showDialogGoldConPassword(priceGoldNum);
            builder.dismiss();
        });
        view.findViewById(R.id.change_gold_close_btn).setOnClickListener(listener -> {
            builder.dismiss();
        });
        builder.setView(view);
        builder.show();
    }

    /**
     * 显示输入支付密码对话框！
     *
     * @param priceNum 创建订单信息对象
     */
    private void showDialogGoldConPassword(String priceNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final EditText editText = new EditText(mActivity);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setTitle("请输入8位支付密码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = editText.getText().toString();
                        int[] loginResult = mVirtualCpk.loginSimCos(1, pin);
                        if (loginResult[0] != 0 && loginResult[0] != 20486) {
                            Toast.makeText(mActivity,
                                    "请输入正确的支付密码码!剩余次数为" + loginResult[1] + "次", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        goldConversionRandom(priceNum);
                        closeVirtualAndRestart();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * 人民币转换金本币第一请求
     *
     * @param priceNum 转换人民的数量
     */
    private void goldConversionRandom(String priceNum) {
        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.goldConversionFirst(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.get("success").getAsBoolean()) {
                        String random = userBalanceResult.get("random").getAsString();
                        byte[] deRandom = mVirtualCpk.SM2Decrypt(Base64.decode(random));
                        JSONObject jsonObject = new JSONObject();
                        double pricedounle = Double.parseDouble(priceNum);
                        jsonObject.put("priceNum", pricedounle);
                        String plainText = jsonObject.toString();
                        String code = mVirtualCpk.EncryptData(deRandom, plainText);
                        goldConversionSecond(code);
                    } else {
                        closeVirtualAndRestart();
                        String message = userBalanceResult.get("msg").getAsString();
                        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(mActivity, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void goldConversionSecond(String code) {
        //   HuFuCode mHuFuCode = new HuFuCode(code);new Gson().toJson(mHuFuCode)


        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), CombinationSecretKey.assembleJsonCode(code));
        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.goldConversionSecond(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    closeVirtualAndRestart();
                    if (userBalanceResult.get("success").getAsBoolean()) {
                        String message = userBalanceResult.get("data").getAsString();
                        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                        queryUserBalance();
                        // mTransactionFragment.refreshTransactionAndUserInfo();
                    } else {
                        String message = userBalanceResult.get("msg").getAsString();
                        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(mActivity, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void setBasicUserInfo(UserInfoListResult.UserInfo mUserInfo) {
        TextView accountName = getActivity().findViewById(R.id.user_basic_account_tv);
        accountName.setText(mUserInfo.mobile);
        TextView email = getActivity().findViewById(R.id.email_til);
        email.setText(mUserInfo.email);
        TextView realName = getActivity().findViewById(R.id.user_basic_name_tv);
        realName.setText(mUserInfo.name);
     /*   TextView sexType = getActivity().findViewById(R.id.user_info_tv);
        if (mUserInfo.sex == 0)
            sexType.setText("男");
        else sexType.setText("女");*/
        TextView cardId = getActivity().findViewById(R.id.user_basic_cardid_tv);
        cardId.setText(mUserInfo.idCard);
    }

    /**
     * 显示输入支付密码对话框！
     */
    private void showDialogReceiverPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final EditText editText = new EditText(mActivity);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setTitle("请输入8位支付密码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = editText.getText().toString();
                        int[] loginResult = mVirtualCpk.loginSimCos(1, pin);
                        if (loginResult[0] != 0 && loginResult[0] != 20486) {
                            Toast.makeText(mActivity,
                                    "请输入正确的支付密码码!剩余次数为" + loginResult[1] + "次", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String recevierQr = generateReceiverQr();
                        if (TextUtils.isEmpty(recevierQr)) {
                            Toast.makeText(mActivity, "验签错误，检验是否有证书", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            return;
                        }
                        showSharingRQDialog("收款码", recevierQr);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private String generateReceiverQr() {
        StringBuilder receiverQr = new StringBuilder("buyer:");
        receiverQr.append(mUserPhoneNumber);
        String receiverSign = mVirtualCpk.SM2Sign(receiverQr.toString());
        if (TextUtils.isEmpty(receiverSign)) {
            return null;
        }
        receiverQr.append("$safeBuyerStr:").append(receiverSign);
        String key = DateUtils.getYear() + "Income&Collection&By&MErchants";
        return mVirtualCpk.EncryptData(key.getBytes(), receiverQr.toString());
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
                    userNameTv.setText(userInfo.name);
                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

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
                    if (!(result.success == true)) {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();

                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

    }


    private void showPaymentPwdDialog() {
        final AlertDialog dialogPlusBuilder = new AlertDialog.Builder(mActivity).create();
        View view = View.inflate(mActivity, R.layout.dialog_payment_password, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view.getId() == R.id.confirm_btn) {
                    EditText oldPwdEt = getActivity().findViewById(R.id.old_pwd_et);
                    EditText newPwdEt = getActivity().findViewById(R.id.new_pwd_et);
                    EditText confirmEt = getActivity().findViewById(R.id.confirm_et);
                    String oldPwd = oldPwdEt.getText().toString();
                    String newPwd = newPwdEt.getText().toString();
                    String confirm = confirmEt.getText().toString();
                    if ("".equals(oldPwd) || "".equals(confirm)
                            || "".equals(newPwd)) {
                        Toast.makeText(getActivity(), "输入内容不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPwd.equals(confirm)) {
                        int[] resultLogin = mVirtualCpk.loginSimCos(1, oldPwd);
                        if (resultLogin[0] != 0 && resultLogin[0] != 20486) {
                            Toast.makeText(getActivity(), "登录软盾失败！", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int result = mVirtualCpk.reviseCosLoginPwd(oldPwd, newPwd);
                        closeVirtualAndRestart();
                        if (result != 0) {
                            Toast.makeText(getActivity(), "修改密码失败！errrCode=" + result, Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getActivity(), "修改成功", Toast.LENGTH_SHORT).show();
                        }
                        dialogPlusBuilder.dismiss();
                    } else {
                        Toast.makeText(getActivity(), "重复输入密码与新密码不正确", Toast.LENGTH_SHORT).show();
                    }
                } else if (view.getId() == R.id.find_payment_pwd_btn) {
                    dialogPlusBuilder.dismiss();
                    showPaymentPinChangePwdDialog();
                }
            }
        });
        dialogPlusBuilder.setView(view);

        dialogPlusBuilder.show();

    }

    private void putChangePaymentPassowrd(String oldPwd, String newPwd) {
        PaymentPassword mPaymentPassword = new PaymentPassword(mUserPhoneNumber, newPwd);
        String keyId = CombinationSecretKey.getSecretKey("updatePin.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(mPaymentPassword));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);
        QueryUserInfo mQueryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(mHuFoCode));

        mQueryUserInfo.changePaymentPassword(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (result.success) {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                        mUserSharedPre.edit().putString(ConstantUtils.PAYMENT_PWD, newPwd);
                    } else {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

    }


    private void showPaymentPinChangePwdDialog() {
        final AlertDialog builder = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_pin_change_payment_password, null);
        builder.setView(view);
        Button pinBtn = view.findViewById(R.id.pin_btn);
        pinBtn.setOnClickListener(listener -> {
            RetrofitUtils.retriveCode(getActivity(), mUserPhoneNumber, mVirtualCpk);
        });
        final EditText pinEt = view.findViewById(R.id.pin_et);
        final EditText newPwdEt = view.findViewById(R.id.new_pwd_et);
        final EditText confirmEt = view.findViewById(R.id.confirm_pwd_et);
        final Button confirmChangeBtn = view.findViewById(R.id.confirm_change_btn);
        confirmChangeBtn.setOnClickListener(Listener -> {
            String pin = pinEt.getText().toString();
            String newPwd = newPwdEt.getText().toString();
            String confirmPwd = confirmEt.getText().toString();
            if ("".equals(pin) || "".equals(newPwd) || "".equals(confirmPwd)) {
                Toast.makeText(getActivity(), "输入内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPwd.equals(confirmPwd))
                putChangePinPaymentPassword(pin, newPwd);
            else
                Toast.makeText(getActivity(), "两次密码不相同", Toast.LENGTH_SHORT).show();
        });

        final Button dirctChangeBtn = view.findViewById(R.id.direct_change_btn);
        dirctChangeBtn.setOnClickListener(Listener -> {
            showPaymentPwdDialog();
            builder.dismiss();
        });

        builder.show();
    }


    private void putChangePinPaymentPassword(String pin, String newPwd) {
        PinPassword mPinPassword = new PinPassword(mUserPhoneNumber, newPwd);

        String keyId = CombinationSecretKey.getSecretKey("verifyPin.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(mPinPassword));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);
        QueryUserInfo mQueryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(mHuFoCode));

        mQueryUserInfo.pinChangePaymentPassword(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (!(result.success == true)) {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();

                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

    }

    /**
     * 关闭 Virtual  Cos , 退出登录状态
     * init  Cos
     */
    private void closeVirtualAndRestart() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }


    private void showSharingRQDialog(String title, String sharingUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_sharing_image, null);
        final ImageView sharingUrlIv = view.findViewById(R.id.friendship_sharing_iv);
        Glide.with(this)
                .load(QRCodeUtils.createQRCodeBitmap(sharingUrl, 800))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.no_banner))
                .into(sharingUrlIv);
        TextView textView = new TextView(mActivity);
        textView.setGravity(Gravity.CENTER);
        textView.setText(title);
        textView.setTextSize(18);
        builder.setCustomTitle(textView)
                .setView(view)
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();

    }


    /**
     * 查询个人信息
     */
    private void againUserCert(String userAccount) {
        PhoneNumber mPhoneNumber = new PhoneNumber(userAccount);
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
                    if (result.success) {
                        if (Integer.parseInt(result.data.userInfo.status) != 1) {
                            Intent perfectIntent = new Intent(getActivity(), CertificateActivity.class);
                            perfectIntent.putExtra("PERFECT_TYPE", "cert");
                            startActivity(perfectIntent);
                        } else {
                            showHintDialog("提示信息", "请先完善个人资料!");
                        }
                    }
                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });

    }


    private void showHintDialog(String title, String msg) {
        android.support.v7.app.AlertDialog.Builder cancelAlertBuilder = new android.support.v7.app.AlertDialog.Builder(mActivity);
        cancelAlertBuilder.setTitle(title);
        cancelAlertBuilder.setMessage(msg);
        cancelAlertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        cancelAlertBuilder.create().show();
    }
}
