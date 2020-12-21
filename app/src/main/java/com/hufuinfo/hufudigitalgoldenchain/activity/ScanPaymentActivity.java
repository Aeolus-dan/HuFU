package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryAuthPrice;
import com.hufuinfo.hufudigitalgoldenchain.bean.AttornGoldRequest;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ScanPaymentActivity extends AppCompatActivity {

    private VirtualCpk mVirtualCpk;
    private String mReceiverInfo = null;

    private Button confirmBtn;
    private EditText inputIntegralEt;

    private SharedPreferences mUserSharedPre;
    private String mUserPhoneNumber;
    private String antiFake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_payment);
        mVirtualCpk = VirtualCpk.getInstance(this);
        mUserSharedPre = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        Intent intent = getIntent();
        if (intent != null) {
            mReceiverInfo = intent.getStringExtra("receiverInfo");
        }
        inputIntegralEt = findViewById(R.id.attorn_et);
        confirmBtn = findViewById(R.id.attorn_confirm_btn);
        confirmBtn.setOnClickListener(listener -> {
            String inputIntegraStr = inputIntegralEt.getText().toString();
            if (TextUtils.isEmpty(inputIntegraStr)) {
                Toast.makeText(this, "请输入正确的积分数", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentPassword(inputIntegraStr);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showPaymentPassword(String inputIntegralStr) {
        AlertDialog paymentAlertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_payment_confim, null);
        paymentAlertDialog.setView(view);

        final Button confinBtn = view.findViewById(R.id.dialog_payment_confirm);
        confinBtn.setOnClickListener(listener -> {
            EditText password = view.findViewById(R.id.dialog_payment_Et);
            String payPassword = password.getText().toString();

            int[] result = mVirtualCpk.loginSimCos(1, payPassword);
            if (result[0] != 0 && result[0] != 20486) {
                Toast.makeText(this, "支付密码输错错误，还剩余" + result[1] + "次", Toast.LENGTH_SHORT).show();
                paymentAlertDialog.dismiss();
                return;
            }
            AttornGoldRequest attornGoldRequest = new AttornGoldRequest();
            String[] receiverInofArr = mReceiverInfo.split("\\$|:");
            attornGoldRequest.setGathUsr(receiverInofArr[1]);
            attornGoldRequest.setSafeBuyStr(receiverInofArr[3]);
            if (!mVirtualCpk.SM2VerifySign("buyer:" + receiverInofArr[1], receiverInofArr[3])) {
                Toast.makeText(this, "验签没通过", Toast.LENGTH_SHORT).show();
                paymentAlertDialog.dismiss();
                return;
            }
            attornGoldRequest.setIntegral(Double.valueOf(inputIntegralStr));
            String userOldStr = "seller:" + mUserPhoneNumber + "$integral:" + DateUtils.getStringDouble(Double.valueOf(inputIntegralStr));
            String sellSafeStr = mVirtualCpk.SM2Sign(userOldStr);
            if (TextUtils.isEmpty(sellSafeStr)) {
                Toast.makeText(this, "签名错误", Toast.LENGTH_SHORT).show();
                return;
            }
            attornGoldRequest.setSafeSellStr(sellSafeStr);
            requestAttornGold(attornGoldRequest);
            paymentAlertDialog.dismiss();
        });
        final Button cancelBtn = view.findViewById(R.id.dialog_payment_cancel);
        cancelBtn.setOnClickListener(listener -> {
            paymentAlertDialog.dismiss();
        });
        paymentAlertDialog.show();
    }

    private void requestAttornGold(AttornGoldRequest attornGoldRequest) {
        String keyId = CombinationSecretKey.getSecretKey("attornGold.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(attornGoldRequest));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));
        closeVirtualAndRestart();
        QueryAuthPrice queryAuthPrice = RetrofitUtils.create(QueryAuthPrice.class);
        queryAuthPrice.attornGold(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.success) {
                        showDialog();
                    } else {
                        showCancelDialog(userBalanceResult.msg);
                    }
                }, error -> {
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("付款结果");
        alertDialog.setMessage("付款成功!");

        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ScanPaymentActivity.this.finish();
            }
        })
                .setOnCancelListener(dialog -> {
                    dialog.dismiss();
                    ScanPaymentActivity.this.finish();
                });
        alertDialog.create().show();
        alertDialog.setCancelable(false);
    }

    private void showCancelDialog(String message) {
        AlertDialog.Builder cancelAlertBuilder = new AlertDialog.Builder(this);
        cancelAlertBuilder.setTitle("付款結果");
        cancelAlertBuilder.setMessage("付款失败！" + message);
        cancelAlertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ScanPaymentActivity.this.finish();
            }
        });
        cancelAlertBuilder.setOnCancelListener(dialog -> {
            dialog.dismiss();
            ScanPaymentActivity.this.finish();
        });
        cancelAlertBuilder.create().show();
    }


    /**
     * 关闭 Virtual  Cos , 退出登录状态
     * init  Cos
     */
    private void closeVirtualAndRestart() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(this);
    }
}
