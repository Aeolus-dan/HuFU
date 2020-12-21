package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UserLogin;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UserRegister;
import com.hufuinfo.hufudigitalgoldenchain.bean.CertificationInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.Code;
import com.hufuinfo.hufudigitalgoldenchain.bean.ConsummateInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.PhoneNumber;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserInfoListResult;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DeviceUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.QRCodeUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ValidatorUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import org.apache.xerces.impl.dv.util.Base64;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PerfectPersonalInfoActivity extends BaseDispatchTouchActivity implements View.OnClickListener {

    private TextInputLayout realNameTl;
    private TextInputEditText realNameTe;
    private TextInputLayout cardIdTl;
    private TextInputEditText cardIdTe;
    private TextInputLayout emailTl;
    private TextInputEditText emailTe;
    private TextInputLayout bankCardTl;
    private TextInputEditText bankCardTe;
    private TextInputLayout passwordTl;
    private TextInputEditText passwordTe;
    private TextInputLayout confirmPwdTl;
    private TextInputEditText confirmPwdTe;
    private Button mConfirmBtn, mSkipBtn;
    private Button perfectInfoQRBtn;
    private VirtualCpk mVirtualCpk;
    private String mUserMobile;
    private TextView perfectTv;
    private View perfectView;
    private SharedPreferences mUserSharedPre;
    private String antiFake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVirtualCpk = VirtualCpk.getInstance(this);
        setContentView(R.layout.activity_perfer_personal_info);
        mUserSharedPre = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE);
        mUserMobile = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        initId();
        Intent intent = getIntent();
        String prefectType = intent.getStringExtra("PERFECT_TYPE");
        if ("register".equals(prefectType)) {
            getCertificationOnline();
        } else {
            queryUserInfoList();
        }
    }

    private void initId() {
        realNameTl = findViewById(R.id.real_name_til);
        realNameTe = findViewById(R.id.real_name_tie);
        cardIdTl = findViewById(R.id.card_id_til);
        cardIdTe = findViewById(R.id.card_id_tie);
        emailTl = findViewById(R.id.email_til);
        emailTe = findViewById(R.id.email_tie);
        bankCardTl = findViewById(R.id.bank_card_til);
        bankCardTe = findViewById(R.id.bank_card_tie);
        passwordTl = findViewById(R.id.pwd_til);
        passwordTe = findViewById(R.id.pwd_tie);
        confirmPwdTl = findViewById(R.id.confirm_pwd_til);
        confirmPwdTe = findViewById(R.id.confirm_pwd_tie);
        mConfirmBtn = findViewById(R.id.confirm_btn);
        mConfirmBtn.setOnClickListener(this);
        mSkipBtn = findViewById(R.id.skip_btn);
        mSkipBtn.setOnClickListener(this);
        perfectTv = findViewById(R.id.perfect_tv);
        perfectView = findViewById(R.id.perfect_ll);
        perfectInfoQRBtn = findViewById(R.id.perfect_info_qr_btn);
        perfectInfoQRBtn.setOnClickListener(this);
        findViewById(R.id.perfect_info_return_tv).setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_btn) {
            if (!validatorPrefectInfo()) {
                return;
            }
            ConsummateInfo mConInfo = new ConsummateInfo();
            mConInfo.setName(realNameTe.getText().toString());
            mConInfo.setEmail(emailTe.getText().toString());
            mConInfo.setIdCard(cardIdTe.getText().toString());
            mConInfo.setPayPwd(passwordTe.getText().toString());
            mConInfo.setMobile(mUserMobile);
            mConInfo.setBankCard(bankCardTe.getText().toString());
            requestPeferInfoRandom(mConInfo);
        } else if (v.getId() == R.id.skip_btn) {
            showCancelDialog();
        } else if (v.getId() == R.id.perfect_info_qr_btn) {
            String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxe3eba854bd74203d&redirect_uri=https://www.hufuinfo.net?mobile="
                    + mUserMobile + "&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
            showSharingRQDialog(url);
        } else if (v.getId() == R.id.perfect_info_return_tv) {
            onBackPressed();
        }
    }

    private void queryUserInfoList() {
        PhoneNumber mPhoneNumber = new PhoneNumber(mUserMobile);
        String keyId = CombinationSecretKey.getSecretKey("userInfoList.do");
        if (keyId == null)
            return;
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
                        UserInfoListResult.UserInfo userInfo = result.data.getUserInfo();
                        if (Integer.parseInt(userInfo.status) != 1) {
                            perfectView.setVisibility(View.GONE);
                            perfectTv.setVisibility(View.VISIBLE);
                        } else {
                            perfectTv.setVisibility(View.GONE);
                            perfectView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, result.msg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });

    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("取消完善个人信息")
                .setMessage("是否取消完善个人信息？\n提示：个人信息不完善，无法进行正常的交易！！！");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PerfectPersonalInfoActivity.this.finish();
            }
        });

        builder.create().show();

    }

    private void requestPeferInfoRandom(ConsummateInfo mConInfo) {
        PhoneNumber mPhoneMobile = new PhoneNumber(mUserMobile);
        mPhoneMobile.setKeyId(mVirtualCpk.getSimID());

        UserRegister mUserRegister = RetrofitUtils.create(UserRegister.class);

        mUserRegister.perfectPersonalInfoRandom(antiFake)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (result.success) {
                        mConInfo.setKeyId(DeviceUtils.getUniqueId(PerfectPersonalInfoActivity.this));
                        byte[] random = Base64.decode(result.random);
                        byte[] keyId = mVirtualCpk.SM2Decrypt(random);
                        String code = mVirtualCpk.EncryptData(keyId, new Gson().toJson(mConInfo));
                        requestPeferInfoSecond(code);
                    } else {
                        Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_LONG).show();
                    }

                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });

    }

    private void requestPeferInfoSecond(String code) {
        PhoneNumber phoneNumber = new PhoneNumber(mUserMobile);
        phoneNumber.setCode(code);
        UserRegister mUserRegister = RetrofitUtils.create(UserRegister.class);
        Code reCode = new Code();
        reCode.setCode(code);
        String sonCode = new Gson().toJson(reCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , sonCode);

        mUserRegister.perfectPersonalInfo(antiFake, body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (result.success) {
                        Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_LONG).show();
                        mUserSharedPre.edit().putString(ConstantUtils.PAYMENT_PWD, passwordTe.getText().toString()).apply();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_LONG).show();
                    }

                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();

                });

    }


    private void getCertificationOnline() {
        String keyId = DeviceUtils.getUniqueId(this);
        if (keyId == null) {
            Toast.makeText(this, "获取唯一Id失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mUserMobile.length() < 1) {
            Toast.makeText(this, "输入正确手机号码", Toast.LENGTH_SHORT).show();
            return;
        }
        int name = 2;
        CertificationInfo certificationInfo = new CertificationInfo(keyId, mUserMobile, name);
        String symKeyId = CombinationSecretKey.getSecretKey("getCertificationOnline.do");
        if (symKeyId == null) {
            Toast.makeText(this, "计算对称秘钥错误", Toast.LENGTH_SHORT).show();
            return;
        }
        String code = mVirtualCpk.EncryptData(symKeyId.getBytes(), new Gson().toJson(certificationInfo));
        HuFuCode huFuCode = new HuFuCode(code);
        UserLogin userLogin = RetrofitUtils.create(UserLogin.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                new Gson().toJson(huFuCode));
        userLogin.getCertificationOnline(antiFake, body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(requestResult -> {
                    if (requestResult.success) {

                        String certStr = null;
                        if (requestResult.data.certification != null) {
                            certStr = requestResult.data.certification;
                        }
                        String pkm = null;
                        if (requestResult.data.pkm != null) {
                            pkm = requestResult.data.pkm;
                        }
                        showDialogInputPin(certStr, pkm);
                    } else {
                        Toast.makeText(this, "在线得到证书失败！", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });
    }


    private void showDialogInputPin(String certStr, String pkm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        builder.setTitle("输入证书PIN码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = editText.getText().toString();
                        if (pin.length() < 4) {
                            Toast.makeText(PerfectPersonalInfoActivity.this, "请输入正确的PIN码", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int result = -1;
                        if (pkm != null) {
                            String pkmCert = mVirtualCpk.EncryptData(pin.getBytes(), Base64.decode(pkm).toString());
                            int[] resultArr = mVirtualCpk.loginSimCos(0, "hfkey000");
                            if (resultArr[0] != 0)
                                return;
                            result = mVirtualCpk.ImportPreCert(Base64.decode(pkmCert));
                        }
                        mVirtualCpk.closeVirtualCos();

                        if (result != 0) {
                            Toast.makeText(PerfectPersonalInfoActivity.this, "预制证书解密失败！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mVirtualCpk = VirtualCpk.getInstance(PerfectPersonalInfoActivity.this.getApplicationContext());
                        if (certStr != null) {
                            int[] resultArr = mVirtualCpk.loginSimCos(1, "12345678");
                            if (resultArr[0] != 0 && resultArr[0] != 20486) {
                                Toast.makeText(PerfectPersonalInfoActivity.this, "登录软盾失败！", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            result = mVirtualCpk.ImportUserCert(certStr.getBytes());
                        }
                        if (result != 0) {
                            Toast.makeText(PerfectPersonalInfoActivity.this, "用户证书导入失败！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        receiptCertSuccess();
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


    private void receiptCertSuccess() {

        int associatedId = 0;
        String keyId = CombinationSecretKey.getSecretKey("installCertificateSuccessful.do");
        String code = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(associatedId));
        UserLogin userLogin = RetrofitUtils.create(UserLogin.class);
        HuFuCode huFuCode = new HuFuCode(code);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(huFuCode));
        userLogin.installCertificateSuccess(antiFake, requestBody)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(requestResult -> {
                    if (requestResult.success) {
                        Toast.makeText(this, requestResult.msg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, requestResult.msg, Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });

    }


    private void showSharingRQDialog(String sharingUrl) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_sharing_image, null);
        final ImageView sharingUrlIv = view.findViewById(R.id.friendship_sharing_iv);
        Glide.with(this)
                .load(QRCodeUtils.createQRCodeBitmap(sharingUrl, 800))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.no_banner))
                .into(sharingUrlIv);
        builder.setTitle("完善个人信息需要微信扫码二维码")
                .setView(view)
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();

    }


    private boolean validatorPrefectInfo() {
        realNameTl.setErrorEnabled(false);
        cardIdTl.setErrorEnabled(false);
        emailTl.setErrorEnabled(false);
        bankCardTl.setErrorEnabled(false);
        passwordTl.setErrorEnabled(false);
        confirmPwdTl.setErrorEnabled(false);
        String realName = realNameTe.getText().toString();
        if (!ValidatorUtils.isChinese(realName)) {
            showError(realNameTl, "请输入中文名字");
            return false;
        }
        String cadrdId = cardIdTe.getText().toString();
        if (!ValidatorUtils.isIDCard(cadrdId)) {
            showError(cardIdTl, "请输入正确的身份证！");
            return false;
        }
        String bankCard = bankCardTe.getText().toString();
        if (!ValidatorUtils.isBankCard(bankCard)) {
            showError(bankCardTl, "请输入正确的银行卡号");
            return false;
        }
        String password = passwordTe.getText().toString();
        if (!ValidatorUtils.isNumberEight(password)) {
            showError(passwordTl, "请输入8位数字密码");
            return false;
        }

        String confirmPwd = confirmPwdTe.getText().toString();
        if (!confirmPwd.equals(password)) {
            showError(passwordTl, "两次输入的密码不相同");
            return false;
        }
        return true;
    }

    /**
     * 显示错误提示，并获取焦点
     *
     * @param textInputLayout
     * @param error
     */
    private void showError(TextInputLayout textInputLayout, String error) {
        textInputLayout.setError(error);
        textInputLayout.getEditText().setFocusable(true);
        textInputLayout.getEditText().setFocusableInTouchMode(true);
        textInputLayout.getEditText().requestFocus();
    }
}
