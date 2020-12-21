package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UserLogin;
import com.hufuinfo.hufudigitalgoldenchain.bean.PaymentRequest;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import org.apache.xerces.impl.dv.util.Base64;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PaymentActivity extends BaseDispatchTouchActivity {

    private TextView mGoldNumTv;
    private TextView mMonyNum;
    private TextView mGoldPriceTv;
    private Button mImmediatePayBtn;
    private View mProgressView;
    private String mobile;
    private VirtualCpk mVirtualCpk;
    private SharedPreferences mSharedPre;
    private String goldNumStr;  //金本币数量
    private String mMoneyStr;   //充值的total价值
    private String mGoldPrice;   //充值的 金本币单价
    private int paymentType;   //充值类型
    private String antiFake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initView();
        mSharedPre = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE);
        mVirtualCpk = VirtualCpk.getInstance(this);
        mobile = mSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        Bundle mBundle = getIntent().getBundleExtra(ConstantUtils.PAYMENT_DATA);
        goldNumStr = mBundle.getString(ConstantUtils.GOLE_NUMBER);
        mMoneyStr = mBundle.getString(ConstantUtils.GOLD_TOTAL_MONEY);
        paymentType = mBundle.getInt(ConstantUtils.PAYMENT_TYPE);
        mGoldPrice = mBundle.getString(ConstantUtils.BUYER_GOLD_PRICE);  //购买金本币的单价
        if (goldNumStr != null || mMoneyStr != null) {
            mGoldNumTv.setText("" + goldNumStr);
            mMonyNum.setText( mMoneyStr+"积分");
            //Todo  gold  price  is   no  setting
            mGoldPriceTv.setText(mGoldPrice);
        }
    }

    private void initView() {
        mGoldNumTv = findViewById(R.id.buyer_gold_num_tv);
        mMonyNum = findViewById(R.id.order_amount_num_tv);
        mGoldPriceTv = findViewById(R.id.gold_price_num_tv);
        mImmediatePayBtn = findViewById(R.id.immediate_payment_btn);
        mImmediatePayBtn.setOnClickListener(listener -> {
            showDialogPutPaymentPwd();
        });

        mProgressView = findViewById(R.id.payment_progress);
        findViewById(R.id.payment_return_tv).setOnClickListener(listenner -> {
            onBackPressed();
        });
    }

    /**
     * 组装 支付请求信息
     *
     * @return null: 获取硬件Id 失败  或签名失败
     * paymentRequest  请求支付数据信息
     */
    private PaymentRequest assemblePaymentReques() {
        PaymentRequest paymentRequest = new PaymentRequest();
        String goldNo = DateUtils.getStringDouble(Double.parseDouble(goldNumStr));
        paymentRequest.setGoldNo(goldNo);
        paymentRequest.setPrice(Float.parseFloat(mMoneyStr));
        String keyId = mVirtualCpk.getSimID();
        if (keyId == null) {
            return null;
        }
        paymentRequest.setKeyId(keyId);
        paymentRequest.setMobile(mobile);
        paymentRequest.setType(paymentType);
        String safeOld = CombinationSecretKey.getSignPaymentData(mobile, goldNo, paymentType);
        String safeStr = mVirtualCpk.SM2Sign(safeOld);
        if (safeStr == null) {
            return null;
        }
        paymentRequest.setSafeStr(safeStr);
        return paymentRequest;
    }

    private void requestPaymentFirst(PaymentRequest paymentRequest) {
        UserLogin queryUserLogin = RetrofitUtils.create(UserLogin.class);
        queryUserLogin.getQRCode(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryPaymentResult -> {
                    if (queryPaymentResult.success) {
                        byte[] smRandom = mVirtualCpk.SM2Decrypt(Base64.decode(queryPaymentResult.random));
                        String code = mVirtualCpk.EncryptData(smRandom, new Gson().toJson(paymentRequest));
                        requestPaymentSecond(code);
                    } else {
                        mProgressView.setVisibility(View.GONE);
                        Toast.makeText(this, queryPaymentResult.msg, Toast.LENGTH_LONG).show();
                        closeVirtualAndInstance();
                    }
                }, error -> {
                    closeVirtualAndInstance();
                    mProgressView.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });

    }


    private void requestPaymentSecond(String code) {
        String requestCode = CombinationSecretKey.assembleJsonCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), requestCode);
        UserLogin queryUserLogin = RetrofitUtils.create(UserLogin.class);
        queryUserLogin.getQRCode(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryPaymentResult -> {
                    mProgressView.setVisibility(View.GONE);
                    JsonObject paymentRsultJson = queryPaymentResult;
                    if (paymentRsultJson.get("success").getAsBoolean()) {
                        if (paymentType == 2) {
                            if (!paymentRsultJson.get("data").isJsonObject()) {
                                Intent reCreateIntent = new Intent();
                                reCreateIntent.putExtra(ConstantUtils.PAYMENT_RESULT, true);
                                setResult(ConstantUtils.PAYMENT_RESULT_CODE, reCreateIntent);
                                PaymentActivity.this.finish();
                            } else {
                                Intent RGIntent = new Intent(PaymentActivity.this, RQShowActivity.class);
                                Bundle paymentBundle = new Bundle();
                                paymentBundle.putString("QRCODE", paymentRsultJson.get("data").getAsJsonObject().get("QRCode").getAsString());
                                paymentBundle.putString("ORDERID", paymentRsultJson.get("data").getAsJsonObject().get("orderId").getAsString());
                                paymentBundle.putString("QRTOTALMONEY", mMoneyStr);
                                RGIntent.putExtra("QRPAYMENT", paymentBundle);
                                startActivityForResult(RGIntent, ConstantUtils.REQUEST_CODE_PAYMENT);
                            }
                        } else {
                            closeVirtualAndInstance();
                            if (!paymentRsultJson.get("data").isJsonObject()) {
                                Toast.makeText(PaymentActivity.this, "支付成功！", Toast.LENGTH_LONG).show();
                                PaymentActivity.this.finish();
                            } else {
                                Intent RGIntent = new Intent(PaymentActivity.this, RQShowActivity.class);
                                Bundle paymentBundle = new Bundle();
                                paymentBundle.putString("QRCODE", paymentRsultJson.get("data").getAsJsonObject().get("QRCode").getAsString());
                                paymentBundle.putString("ORDERID", paymentRsultJson.get("data").getAsJsonObject().get("orderId").getAsString());
                                paymentBundle.putString("QRTOTALMONEY", mMoneyStr);
                                RGIntent.putExtra("QRPAYMENT", paymentBundle);
                                startActivity(RGIntent);
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(this, paymentRsultJson.get("msg").getAsString(), Toast.LENGTH_LONG).show();
                        closeVirtualAndInstance();
                    }
                }, error -> {
                    closeVirtualAndInstance();
                    mProgressView.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });

    }


    private void showDialogPutPaymentPwd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setTitle("请输入8位支付密码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = editText.getText().toString();
                        int result = -1;
                        int[] loginRe = mVirtualCpk.loginSimCos(1, pin);
                        if (loginRe[0] != 0 && loginRe[0] != 20486) {
                            Toast.makeText(PaymentActivity.this, "请输入正确的PIN码,剩余重试次数为"
                                    + loginRe[1] + "数", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            return;
                        }
                        PaymentRequest paymentRequest = assemblePaymentReques();
                        if (paymentRequest == null) {
                            Toast.makeText(PaymentActivity.this, "获取加密设备失败！请确保已经完善资料。"
                                    , Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            return;
                        }
                        requestPaymentFirst(paymentRequest);
                        mProgressView.setVisibility(View.VISIBLE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstantUtils.REQUEST_CODE_PAYMENT) {
            if (resultCode == ConstantUtils.PAYMENT_RESULT_CODE && data != null) {
                boolean paymentResult = data.getBooleanExtra(ConstantUtils.PAYMENT_RESULT, false);
                Intent reCreateIntent = new Intent();
                reCreateIntent.putExtra(ConstantUtils.PAYMENT_RESULT, paymentResult);
                setResult(ConstantUtils.PAYMENT_RESULT_CODE, reCreateIntent);
                finish();
            } else {
                Intent reCreateIntent = new Intent();
                reCreateIntent.putExtra(ConstantUtils.PAYMENT_RESULT, false);
                setResult(ConstantUtils.PAYMENT_RESULT_CODE, reCreateIntent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void closeVirtualAndInstance() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(this);
    }
}
