package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserBalance;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.PutForward;
import com.hufuinfo.hufudigitalgoldenchain.fragment.HomePageFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.PersonalCenterFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.TransactionFragment;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ValidatorUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import org.apache.xerces.impl.dv.util.Base64;
import org.json.JSONObject;

import java.math.BigDecimal;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MainActivity extends BaseDispatchTouchActivity {

    private BottomNavigationView bottomNavigationView;
    private MenuItem menuItem;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private String mobile;
    private VirtualCpk mVirtualCpk;
    private SharedPreferences mSharedPre;
    private String antiFake;
    private boolean isPartner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPre = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE);
        antiFake = mSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        bottomNavigationView = findViewById(R.id.main_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home_page_menu:
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.trans_page_menu:
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.users_page_menu:
                        mViewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(i);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mVirtualCpk = VirtualCpk.getInstance(this);
        mobile = mSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        queryUserMemberLevel();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isPartner)
            menu.findItem(R.id.action_gold_switch).setVisible(true);
        else
            menu.findItem(R.id.action_gold_switch).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_recharge) {
            showBuyerDialog();
            return true;
        } else if (id == R.id.action_cashes) {
            showCashesDialog();
            return true;
        } else if (id == R.id.action_gold_switch) {
            showGoldConversionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private TransactionFragment mTransactionFragment;
    private PersonalCenterFragment mPersonalCenterFragment;
    private HomePageFragment homePageFragment;

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                if (mTransactionFragment == null) {
                    mTransactionFragment = new TransactionFragment();
                }
                return mTransactionFragment;
            } else if (position == 2) {
                if (mPersonalCenterFragment == null) {
                    mPersonalCenterFragment = new PersonalCenterFragment();
                }
                return mPersonalCenterFragment;
            } else if (position == 0) {
                if (homePageFragment == null) {
                    homePageFragment = new HomePageFragment();
                }
                return homePageFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void showBuyerDialog() {
        final AlertDialog dialogPlusBuilder = new AlertDialog.Builder(this).create();

        View view = View.inflate(this, R.layout.dialog_buyer, null);
        final RadioGroup radioGroup = view.findViewById(R.id.buyer_rg);
        final EditText buyerET = view.findViewById(R.id.buyer_num_et);
        view.findViewById(R.id.dialog_buyer_btn).setOnClickListener(listener -> {
            String goldNumber = buyerET.getText().toString();
            if (!ValidatorUtils.checkDecimals(goldNumber)) {
                Toast.makeText(MainActivity.this, "请输入正确的金本币数", Toast.LENGTH_SHORT).show();
                return;
            }
            String goldPrice = MainActivity.this.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                    .getString(ConstantUtils.GOLD_PRICE, null);
            if (goldPrice == null) return;
            BigDecimal goldPriceBig = new BigDecimal(goldPrice);
            BigDecimal goldNum = new BigDecimal(goldNumber);
            String totalMoney = DateUtils.getStringDouble(goldPriceBig.multiply(goldNum).floatValue());
            Intent payIntent = new Intent(MainActivity.this, PaymentActivity.class);
            Bundle paymentBundle = new Bundle();
            paymentBundle.putString(ConstantUtils.GOLE_NUMBER, DateUtils.getStringDouble(goldNum.doubleValue()));
            paymentBundle.putString(ConstantUtils.GOLD_TOTAL_MONEY, totalMoney);
            paymentBundle.putString(ConstantUtils.BUYER_GOLD_PRICE, DateUtils.getStringDouble(goldPriceBig.doubleValue()));
            paymentBundle.putInt(ConstantUtils.PAYMENT_TYPE, 1);
            payIntent.putExtra(ConstantUtils.PAYMENT_DATA, paymentBundle);
            startActivity(payIntent);
            dialogPlusBuilder.dismiss();
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                String goldNum = radioButton.getText().toString().replace("个", "");
                buyerET.setText(goldNum);
            }
        });
        TextView goldPriceNumberTv = view.findViewById(R.id.buyer_num_price_tv);
        buyerET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String goldPrice = MainActivity.this.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                        .getString("GOLDPRICE", null);
                if (goldPrice == null) return;
                BigDecimal goldPriceBig = new BigDecimal(goldPrice);
                BigDecimal goldNum = new BigDecimal(s.toString());
                float totalPrice = goldPriceBig.multiply(goldNum).floatValue();
                goldPriceNumberTv.setText(goldNum + "金本币*" + goldPrice + "=" + totalPrice + "元");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.dialog_close_btn).setOnClickListener(listener -> dialogPlusBuilder.dismiss());
        dialogPlusBuilder.setView(view);
        dialogPlusBuilder.show();


    }


    private TextView putforwardTv;
    private int mPutForwardType = -1;

    private void showCashesDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_put_forward, null);
        dialog.setView(view);
        final Button putForwardBtn = view.findViewById(R.id.put_forward__btn);
        putForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PutForward mForwardQuest = new PutForward();
                EditText putFowradNumEt = view.findViewById(R.id.put_forward_num_et);

                String putForwradNumStr = putFowradNumEt.getText().toString();
                if (!ValidatorUtils.checkDecimals(putForwradNumStr)) {
                    Toast.makeText(MainActivity.this, "请输入正确的数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                mForwardQuest.setPriceNum(Double.parseDouble(putForwradNumStr));
                mForwardQuest.setMobile(mobile);
                mForwardQuest.setType(mPutForwardType);
                showPutForwardPassword(mForwardQuest);
            }
        });
        final Button cancelBtn = view.findViewById(R.id.put_forward_close_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        RadioGroup putforwardRg = view.findViewById(R.id.put_forward_type_rg);
        putforwardTv = view.findViewById(R.id.hit_money_tv);
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

        dialog.show();
    }

    /**
     * 是否是合伙人权限用于查看是否有充值权限
     */
    private void queryUserMemberLevel() {

        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.queryUserMemberLevel(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.get("success").getAsBoolean()) {
                        isPartner = true;
                    } else {
                        isPartner = false;
                        Toast.makeText(this, userBalanceResult.get("msg").getAsString(), Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
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
                            } else {
                                putforwardTv.setText("最大提现额度为" + userBalanceResult.data.goldNo + "金本币");
                            }
                        }
                    } else {
                        Toast.makeText(this, userBalanceResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
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
                        Toast.makeText(this, userBalanceResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    } else {
                        String message = userBalanceResult.get("msg").getAsString();
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showGoldConversionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("积分转换");
        LayoutInflater inflater = getLayoutInflater();// LayoutInflater.from(R.layout.gold_conversion_dialog, null);
        final View view = inflater.inflate(R.layout.gold_conversion_dialog, null);
        EditText goldConversionEt = view.findViewById(R.id.gold_conversion_num);
        builder.setView(view)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String priceGoldNum = goldConversionEt.getText().toString();
                        if (Double.parseDouble(priceGoldNum) < 1) {
                            Toast.makeText(MainActivity.this, "请输入大于等于1数", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showDialogGoldConPassword(priceGoldNum);
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
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
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        mTransactionFragment.refreshTransactionAndUserInfo();
                    } else {
                        String message = userBalanceResult.get("msg").getAsString();
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(this, R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    /**
     * 显示输入支付密码对话框！
     *
     * @param priceNum 创建订单信息对象
     */
    private void showDialogGoldConPassword(String priceNum) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setTitle("请输入8位支付密码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = editText.getText().toString();
                        int[] loginResult = mVirtualCpk.loginSimCos(1, pin);
                        if (loginResult[0] != 0 && loginResult[0] != 20486) {
                            Toast.makeText(MainActivity.this,
                                    "请输入正确的支付密码码!剩余次数为" + loginResult[1] + "次", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        goldConversionRandom(priceNum);
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


    private void showPutForwardPassword(PutForward putForward) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_payment_confim, null);
        alertDialog.setView(view);

        final Button confinBtn = view.findViewById(R.id.dialog_payment_confirm);
        confinBtn.setOnClickListener(listener -> {
            EditText password = view.findViewById(R.id.dialog_payment_Et);
            String payPassword = password.getText().toString();

            int[] result = mVirtualCpk.loginSimCos(1, payPassword);
            if (result[0] != 0 && result[0] != 20486) {
                Toast.makeText(MainActivity.this, "支付密码输错错误，还剩余" + result[1] + "次", Toast.LENGTH_SHORT).show();
                return;
            }

            putForwardConfirmRandom(putForward);

        });
        final Button cancelBtn = view.findViewById(R.id.dialog_payment_cancel);
        cancelBtn.setOnClickListener(listener -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
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
