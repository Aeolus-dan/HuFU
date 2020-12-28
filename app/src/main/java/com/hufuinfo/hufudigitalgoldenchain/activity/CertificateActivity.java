package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UserLogin;
import com.hufuinfo.hufudigitalgoldenchain.bean.CertificationInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.InstallCert;
import com.hufuinfo.hufudigitalgoldenchain.bean.PhoneNumber;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserInfoListResult;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DeviceUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import org.apache.xerces.impl.dv.util.Base64;
import org.w3c.dom.Text;

import java.util.regex.Pattern;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class CertificateActivity extends BaseDispatchTouchActivity {

    private TextView verifCodeBtn;
    private Button submissionBtn;
    private TextInputEditText verifCodeEt;
    private TextView certPinEt;
    private TextView phoneEt;
    private View obtainCertView, decryptCertView;
    private Button decryptCertBtn;
    private View loadingView;

    private VirtualCpk mVirtualCpk;
    private String phone;
    private String antiFake;
    private String pkm, certStr;
    private String prefectType;

    private TimeCount timeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);
        mVirtualCpk = VirtualCpk.getInstance(getApplicationContext());
        antiFake = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE)
                .getString(ConstantUtils.ANTI_FAKE, null);
        phone = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE)
                .getString(ConstantUtils.USER_ACCOUNT, null);
        initId();
        Intent intent = getIntent();
        prefectType = intent.getStringExtra("PERFECT_TYPE");
        if ("perfect".equals(prefectType)) {
            queryUserInfoList();
        }

        timeCount = new TimeCount(60000, 1000);

        Toast.makeText(this,"尊敬的用户, 每天只可下发3次证书, 请不要频繁获取证书",Toast.LENGTH_LONG).show();
    }

    private void initId() {
        verifCodeBtn = findViewById(R.id.verif_code_btn);
        verifCodeBtn.setOnClickListener(listener -> {
            phone = phoneEt.getText().toString();
            if (phone.length() > 0) {
                RetrofitUtils.retriveCode(this, phone, mVirtualCpk); //获取验证码
                timeCount.start();
            } else {
                Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
            }
        });
        submissionBtn = findViewById(R.id.confirm_cert_btn);
        verifCodeEt = findViewById(R.id.verif_code_et);
        phoneEt = findViewById(R.id.phone_et);

        if (phone != null) {
            phoneEt.setText(phone);
        }
        certPinEt = findViewById(R.id.cert_pin_et);
        submissionBtn.setOnClickListener(listener -> {
            loadingView.setVisibility(View.VISIBLE);
            getCertificationOnline();
        });

        obtainCertView = findViewById(R.id.ll_obtain_cert);
        decryptCertView = findViewById(R.id.ll_decrypt_cert);
        decryptCertBtn = findViewById(R.id.decrypt_cert_btn);
        decryptCertBtn.setOnClickListener(listener -> {
            String pin = certPinEt.getText().toString().trim();
            decryptCertBtn.setEnabled(false);
            loadingView.setVisibility(View.VISIBLE);
            decryptCert(pin, certStr, pkm);
        });
        loadingView = findViewById(R.id.wait_for_progress);
        findViewById(R.id.certificate_return_tv).setOnClickListener(listener -> {
            onBackPressed();
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeCount != null) {
            timeCount.cancel();
            timeCount = null;
        }
    }

    private void queryUserInfoList() {
        PhoneNumber mPhoneNumber = new PhoneNumber(phone);
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
                            Intent perfectIntent = new Intent(CertificateActivity.this, PerfectPersonalInfoActivity.class);
                            perfectIntent.putExtra("PERFECT_TYPE", "certificate");
                            startActivity(perfectIntent);
                            CertificateActivity.this.finish();
                        }
                    } else {
                        if(!MainActivity.isVisitor) {
                            Toast.makeText(this, result.msg, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });

    }

    /**
     * 显示解密证书解密
     */
    private void showDecryptView() {
        if (obtainCertView != null && decryptCertView != null) {
            obtainCertView.setVisibility(View.GONE);
            decryptCertView.setVisibility(View.VISIBLE);
        }
    }


    private void showObtainView() {
        if (obtainCertView != null && decryptCertView != null) {
            obtainCertView.setVisibility(View.VISIBLE);
            decryptCertView.setVisibility(View.GONE);
        }
    }

    private void getCertificationOnline() {
        String keyId = DeviceUtils.getUniqueId(this);
        if (keyId == null) {
            Toast.makeText(this, "获取唯一Id失败", Toast.LENGTH_SHORT).show();
            return;
        }
        phone = phoneEt.getText().toString();
        if (phone.length() < 1) {
            Toast.makeText(this, "输入正确手机号码", Toast.LENGTH_SHORT).show();
            return;
        }
        int name = 3;
        String pin = verifCodeEt.getText().toString();
        if (pin.length() < 5) {
            Toast.makeText(this, "请输入正确的PIN码", Toast.LENGTH_SHORT).show();
            return;
        }
        CertificationInfo certificationInfo = new CertificationInfo(keyId, phone, name);
        certificationInfo.setPin(pin);
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
                    loadingView.setVisibility(View.GONE);
                    if (requestResult.success) {
                        if (requestResult.data.certification != null) {
                            certStr = requestResult.data.certification;
                            certStr = certStr.replaceAll(" ", "+");
                        }
                        if (requestResult.data.pkm != null) {
                            pkm = requestResult.data.pkm;
                            pkm = pkm.replaceAll(" ", "+");
                        }
                        showDecryptView();
                    } else {
                        Toast.makeText(this, "在线得到证书失败！" + requestResult.msg +",请再次尝试", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });
    }


    private void decryptCert(String pin, String certStr, String pkm) {
        if (TextUtils.isEmpty(pin) || pin.length() < 7) {
            Toast.makeText(CertificateActivity.this, "请输入正确的PIN码", Toast.LENGTH_SHORT).show();
            loadingView.setVisibility(View.GONE);
            decryptCertBtn.setEnabled(true);
            return;
        }
        int result = -1;
        pin = pin + "FFFFFFFF";
        if (pkm != null) {
            byte[] pkmCert = mVirtualCpk.SM4Decrypt(pin, Base64.decode(pkm));
            if (pkmCert == null) {
                Toast.makeText(CertificateActivity.this, "预制证书解密失败！\n 请重新获取证书！", Toast.LENGTH_SHORT).show();
                //  showObtainView();
                decryptCertBtn.setEnabled(true);
                loadingView.setVisibility(View.GONE);
                return;
            }

            int[] resultArr = mVirtualCpk.loginSimCos(0, "hfkey000");
            if (resultArr[0] != 0) {
                Toast.makeText(CertificateActivity.this, "登录软盾失败！", Toast.LENGTH_SHORT).show();
                loadingView.setVisibility(View.GONE);
                decryptCertBtn.setEnabled(true);
                return;
            }
            result = mVirtualCpk.ImportPreCert(pkmCert);
        }
        closeVirtualAndRestart();
        if (result != 0) {
            Toast.makeText(CertificateActivity.this, "预制证书解密失败！\n请重新获取证书！", Toast.LENGTH_SHORT).show();
            //  showObtainView();
            loadingView.setVisibility(View.GONE);
            decryptCertBtn.setEnabled(true);
            return;
        }
        if (certStr != null) {
            int[] resultArr = mVirtualCpk.loginSimCos(1, "12345678");
            if (resultArr[0] != 0) {
                Toast.makeText(CertificateActivity.this, "登录软盾失败！", Toast.LENGTH_SHORT).show();
                loadingView.setVisibility(View.GONE);
                decryptCertBtn.setEnabled(true);
                return;
            }
            result = mVirtualCpk.ClearUserCert();
            if (result != 0) {
                Toast.makeText(CertificateActivity.this, "用户证书清除失败！+result =", Toast.LENGTH_SHORT).show();
                loadingView.setVisibility(View.GONE);
                decryptCertBtn.setEnabled(true);
                return;
            }
            result = mVirtualCpk.ImportUserCert(Base64.decode(certStr));
        }
        if (result != 0 && result != 272) {
            Toast.makeText(CertificateActivity.this, "用户证书导入失败！", Toast.LENGTH_SHORT).show();
            loadingView.setVisibility(View.GONE);
            decryptCertBtn.setEnabled(true);
            return;
        }
        receiptCertSuccess();
    }

    private void showDialogInputPin(String certStr, String pkm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setTitle("输入证书PIN码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = editText.getText().toString();
                        if (pin.length() < 7) {
                            Toast.makeText(CertificateActivity.this, "请输入正确的PIN码", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int result = -1;
                        pin = pin + "FFFFFFFF";
                        if (pkm != null) {
                            Log.e("cERTIF", "PKM leng =" + pkm.length());
                            byte[] pkmCert = mVirtualCpk.SM4Decrypt(pin, Base64.decode(pkm));
                            int[] resultArr = mVirtualCpk.loginSimCos(0, "hfkey000");
                            if (resultArr[0] != 0)
                                return;
                            result = mVirtualCpk.ImportPreCert(pkmCert);
                        }
                        mVirtualCpk.closeVirtualCos();

                        if (result != 0) {
                            Toast.makeText(CertificateActivity.this, "预制证书解密失败！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mVirtualCpk = VirtualCpk.getInstance(CertificateActivity.this.getApplicationContext());
                        if (certStr != null) {
                            int[] resultArr = mVirtualCpk.loginSimCos(1, "12345678");
                            if (resultArr[0] != 0)
                                return;
                            result = mVirtualCpk.ImportUserCert(Base64.decode(certStr));
                        }
                       /* if (result != 0) {
                            Toast.makeText(CertificateActivity.this, "用户证书导入失败！", Toast.LENGTH_SHORT).show();
                            return;
                        }*/
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

        InstallCert installCert = new InstallCert(0);
        String keyId = CombinationSecretKey.getSecretKey("installCertificateSuccessful.do");
        String code = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(installCert));
        UserLogin userLogin = RetrofitUtils.create(UserLogin.class);
        HuFuCode huFuCode = new HuFuCode(code);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(huFuCode));
        userLogin.installCertificateSuccess(antiFake, requestBody)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(requestResult -> {
                    loadingView.setVisibility(View.GONE);
                    decryptCertBtn.setEnabled(true);
                    if (requestResult.success) {

                        Toast.makeText(this, "获取证书成功！请及时到设置里修改支付密码.默认支付密码为:12345678", Toast.LENGTH_SHORT).show();
                        if ("perfect".equals(prefectType)) {
                            Intent perfectIntent = new Intent(CertificateActivity.this, PerfectPersonalInfoActivity.class);
                            perfectIntent.putExtra("PERFECT_TYPE", "certificate");
                            startActivity(perfectIntent);
                        } else {
                            closeVirtualAndRestart();
                        }
                        finish();
                    } else {
                        closeVirtualAndRestart();
                        Toast.makeText(this, requestResult.msg, Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    loadingView.setVisibility(View.GONE);
                    decryptCertBtn.setEnabled(true);
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });

    }


    /**
     * 关闭 Virtual  Cos , 退出登录状态
     * init  Cos
     */
    private void closeVirtualAndRestart() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(this);
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
