package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UserLogin;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.QueryPaymentResult;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.QRCodeUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RQShowActivity extends BaseDispatchTouchActivity {

    private ImageView mRQIv;
    private TextView mTimeTv;
    private TextView mTotalMoney;
    private VirtualCpk mVirtualCpk;


    private String qRcode;
    private String orderId;
    private String totalMoney;

    private Timer timer;
    private Task task;
    private Timer mCancelTimer;
    private TaskTimeLeng mLengTask;
    private int timeLeng = 121;
    private String antiFake;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    showCancelDialog();
                    break;
                case 100:
                    mTimeTv.setText("剩余付费时间为：" + timeLeng + "s");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rqshow);
        mVirtualCpk = VirtualCpk.getInstance(this);
        Bundle mBundle = getIntent().getBundleExtra("QRPAYMENT");
        qRcode = mBundle.getString("QRCODE");
        orderId = mBundle.getString("ORDERID");
        totalMoney = mBundle.getString("QRTOTALMONEY");
        antiFake = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE)
                .getString(ConstantUtils.ANTI_FAKE, null);
        mRQIv = findViewById(R.id.payment_rq_iv);
        Glide.with(this).load(QRCodeUtils.createQRCodeBitmap(qRcode, 800)).into(mRQIv);
        //  mRQIv.setImageBitmap(QRCodeUtils.createQRCodeBitmap(qRcode, 800));
        mTimeTv = findViewById(R.id.payment_time_tv);
        timer = new Timer();
        task = new Task();
        timer.schedule(task, 5000, 5000);
        mCancelTimer = new Timer();
        mLengTask = new TaskTimeLeng();
        mCancelTimer.schedule(mLengTask, 0, 1000);
        findViewById(R.id.payment_return_tv).setOnClickListener(listener -> onBackPressed());
        mTotalMoney = findViewById(R.id.show_rq_money_tv);
        mTotalMoney.setText("￥" + totalMoney);
    }

    private void queryPaymentResult(String orderId) {
        QueryPaymentResult queryPaymentData = new QueryPaymentResult(orderId);
        String keyId = CombinationSecretKey.getSecretKey("confirmationPay.do");
        String code = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(queryPaymentData));
        HuFuCode huFuCode = new HuFuCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(huFuCode));
        UserLogin queryUserLogin = RetrofitUtils.create(UserLogin.class);
        queryUserLogin.confirmationPay(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryPaymentResult -> {
                    if (queryPaymentResult.success) {
                        cancelTimer();
                        showDialog();
                    } else {
                        Log.e("RQShowActivity", queryPaymentResult.msg);
                    }
                }, error -> {
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (mLengTask != null) {
            mLengTask.cancel();
            mLengTask = null;
        }
        if (mCancelTimer != null) {
            mCancelTimer.cancel();
            mCancelTimer = null;
        }
    }

    public class TaskTimeLeng extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(100);
            timeLeng--;
            if (timeLeng == 0) {
                cancelTimer();
                mHandler.sendEmptyMessage(200);
            }
        }
    }

    public class Task extends TimerTask {
        @Override
        public void run() {
            queryPaymentResult(orderId);
        }
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("支付结果");
        alertDialog.setMessage("支付成功!");

        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent reCreateIntent = new Intent();
                reCreateIntent.putExtra(ConstantUtils.PAYMENT_RESULT, true);
                setResult(ConstantUtils.PAYMENT_RESULT_CODE, reCreateIntent);
                RQShowActivity.this.finish();
            }
        })
                .setOnCancelListener(dialog -> {
                    dialog.dismiss();
                    Intent reCreateIntent = new Intent();
                    reCreateIntent.putExtra(ConstantUtils.PAYMENT_RESULT, true);
                    setResult(ConstantUtils.PAYMENT_RESULT_CODE, reCreateIntent);
                    RQShowActivity.this.finish();
                });
        alertDialog.create().show();
        alertDialog.setCancelable(false);
    }

    private void showCancelDialog() {
        AlertDialog.Builder cancelAlertBuilder = new AlertDialog.Builder(this);
        cancelAlertBuilder.setTitle("支付结果");
        cancelAlertBuilder.setMessage("支付失败！，如果微信支付成功了，请联系客服。");
        cancelAlertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent reCreateIntent = new Intent();
                reCreateIntent.putExtra(ConstantUtils.PAYMENT_RESULT, false);
                setResult(ConstantUtils.PAYMENT_RESULT_CODE, reCreateIntent);
                RQShowActivity.this.finish();
            }
        });
        cancelAlertBuilder.setOnCancelListener(dialog -> {
            dialog.dismiss();
            Intent reCreateIntent = new Intent();
            reCreateIntent.putExtra(ConstantUtils.PAYMENT_RESULT, false);
            setResult(ConstantUtils.PAYMENT_RESULT_CODE, reCreateIntent);
            RQShowActivity.this.finish();
        });
        cancelAlertBuilder.create().show();
    }

}
