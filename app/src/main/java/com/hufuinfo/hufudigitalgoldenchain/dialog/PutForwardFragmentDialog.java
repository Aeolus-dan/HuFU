package com.hufuinfo.hufudigitalgoldenchain.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserBalance;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.PutForward;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ValidatorUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import org.apache.xerces.impl.dv.util.Base64;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PutForwardFragmentDialog extends DialogFragment {
    private TextView putforwardTv;
    private TextView putforwardAdavance;
    private TextView  putForwardCostTv;
    private int mPutForwardType = -1;
    private VirtualCpk mVirtualCpk;
    private String mobile;
    private String antiFake;

    public static PutForwardFragmentDialog getInstance(String mobile, String antiFake) {
        PutForwardFragmentDialog putForwardFragmentDialog = new PutForwardFragmentDialog();
        Bundle args = new Bundle();
        args.putString("moblie", mobile);
        args.putString("antiFake", antiFake);
        putForwardFragmentDialog.setArguments(args);
        return putForwardFragmentDialog;

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mobile = bundle.getString("moblie");
            antiFake = bundle.getString("antiFake");
        }
        mVirtualCpk = VirtualCpk.getInstance(getActivity());
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FragmentDialog);
      /*  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog);*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_put_forward, container, false);
        final Button putForwardBtn = view.findViewById(R.id.put_forward__btn);
        putForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PutForward mForwardQuest = new PutForward();
                EditText putFowradNumEt = view.findViewById(R.id.put_forward_num_et);

                String putForwradNumStr = putFowradNumEt.getText().toString();
                if (!ValidatorUtils.checkDecimals(putForwradNumStr)) {
                    Toast.makeText(getActivity(), "请输入正确的数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                mForwardQuest.setPriceNum(Double.parseDouble(putForwradNumStr));
                mForwardQuest.setMobile(mobile);
                mForwardQuest.setType(mPutForwardType);
                showPutForwardPassword(mForwardQuest);
            }
        });
        final ImageButton cancelBtn = view.findViewById(R.id.put_forward_close_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RadioGroup putforwardRg = view.findViewById(R.id.put_forward_type_rg);
        putforwardAdavance = view.findViewById(R.id.hit_advance_tv);
        putforwardTv = view.findViewById(R.id.hit_money_tv);
        putForwardCostTv = view.findViewById(R.id.hit_cost_forward_tv);
        putforwardRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.gold_account_rg:
                        mPutForwardType = 2;
                        break;
                    case R.id.cny_rg:
                        mPutForwardType = 1;
                        break;
                }
                queryPutFoward(mPutForwardType);
            }
        });
        queryPutFoward(2);
        return view;
    }

    /**
     * 查询提现金额
     *
     * @param status 提现类型
     */
    private void queryPutFoward(int status) {
        PutForward putForward = new PutForward();
        putForward.setStatus(status);
        putForward.setMobile(mobile);

        String keyId = CombinationSecretKey.getSecretKey("putForward.do");
        String code = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(putForward));
        HuFuCode mHuFucode = new HuFuCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFucode));
        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.queryPutForward(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.success) {
                        if (putforwardTv != null) {
                            if (status == 1) {
                                putforwardTv.setText("最大提现额度为" + userBalanceResult.data.cny + "元");
                                putForwardCostTv.setVisibility(View.GONE);
                            } else {
                                putforwardTv.setText("最大提现额度为" + userBalanceResult.data.goldNo + "金本币");
                                putforwardAdavance.setText("最大可提前提现额度为"+ userBalanceResult.data.advanceGoldNo+"金本币");
                                putForwardCostTv.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), userBalanceResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void showPutForwardPassword(PutForward putForward) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_payment_confim, null);
        alertDialog.setView(view);

        final Button confinBtn = view.findViewById(R.id.dialog_payment_confirm);
        confinBtn.setOnClickListener(listener -> {
            EditText password = view.findViewById(R.id.dialog_payment_Et);
            String payPassword = password.getText().toString();

            int[] result = mVirtualCpk.loginSimCos(1, payPassword);
            if (result[0] != 0 && result[0] != 20486) {
                Toast.makeText(getActivity(), "支付密码输错错误，还剩余" + result[1] + "次", Toast.LENGTH_SHORT).show();
                return;
            }

            putForwardConfirmRandom(putForward);
            alertDialog.dismiss();

        });
        final Button cancelBtn = view.findViewById(R.id.dialog_payment_cancel);
        cancelBtn.setOnClickListener(listener -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void putForwardConfirmRandom(PutForward putForward) {
        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.queryPutForwardConfirm(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.success) {
                        byte[] deRandom = mVirtualCpk.SM2Decrypt(Base64.decode(userBalanceResult.random));

                        String code = mVirtualCpk.EncryptData(deRandom, new Gson().toJson(putForward));
                        putForwardConfirmSecond(code);
                    } else {
                        closeVirtualAndRestart();
                        Toast.makeText(getActivity(), userBalanceResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void putForwardConfirmSecond(String code) {

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), CombinationSecretKey.assembleJsonCode(code));
        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.queryPutForwardConfirmSecond(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    closeVirtualAndRestart();
                    if (userBalanceResult.get("success").getAsBoolean()) {
                        String message = userBalanceResult.get("data").getAsString();
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        dismiss();
                    } else {
                        String message = userBalanceResult.get("msg").getAsString();
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 关闭 Virtual  Cos , 退出登录状态
     * init  Cos
     */
    private void closeVirtualAndRestart() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(getActivity());
    }
}


