package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UpdateApp;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UserLogin;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.LoginInfoConvention;
import com.hufuinfo.hufudigitalgoldenchain.bean.LoginInfoVerification;
import com.hufuinfo.hufudigitalgoldenchain.fragment.LoginConventionFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.LoginVerificationFragment;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ServerDomain;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import java.io.File;
import java.util.List;

import activity.UpdateAppDialogFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import util.UpdateAppUtils;

public class LoginActivity extends AppCompatActivity implements OnClickListener, EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    private static final String[] LOCATION_AND_CONTACTS =
            {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS};

    private static final int RC_LOCATION_CONTACTS_PERM = 124;

    private FragmentManager supportFragmentManager;
    private FragmentTransaction fragmentTransaction;
    private LoginConventionFragment mFragmentLoginConvention;
    private LoginVerificationFragment mLoginVerificationFragment;

    private Button mLoginBtn, mRegisterBtn;
    private TextView mChangeLoginTypeTv;
    private CheckBox mRememberPwdCb;

    private View mProgressView;
    private VirtualCpk mVirtualCpk;

    private SharedPreferences mUserSharedPre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUserSharedPre = getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE);
        findId();
        File filesDir = getFilesDir();
        String filesDirPath = filesDir.getPath();
        mUserSharedPre.edit().putString(ConstantUtils.VIRTUAL_PATH, filesDirPath).apply();
        mVirtualCpk = VirtualCpk.getInstance(this);
        supportFragmentManager = getSupportFragmentManager();
        mFragmentLoginConvention = new LoginConventionFragment();
        mLoginVerificationFragment = new LoginVerificationFragment();
        fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mFragmentLoginConvention)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        mProgressView = findViewById(R.id.login_progress);
        locationAndContactsTask();
        queryAppVersion();
    }

    private void findId() {
        mLoginBtn = findViewById(R.id.login_button);
        mLoginBtn.setOnClickListener(this);
        mRegisterBtn = findViewById(R.id.registration_button);
        mRegisterBtn.setOnClickListener(this);
        mChangeLoginTypeTv = findViewById(R.id.change_login_type_button);
        SpannableString spannableString = new SpannableString("忘记密码？动态登录");
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorLoginText)), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mChangeLoginTypeTv.setText(spannableString);
        mChangeLoginTypeTv.setOnClickListener(this);
        mRememberPwdCb = findViewById(R.id.remember_pwd_cb);
        boolean isRemember = mUserSharedPre.getBoolean(ConstantUtils.REMEMBER_PWD, false);
        mRememberPwdCb.setChecked(isRemember);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String phoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        String password = mUserSharedPre.getString(ConstantUtils.ACCOUNT_PWD, null);
        if (mFragmentLoginConvention != null) {

            if (!"".equals(phoneNumber) && phoneNumber != null) {
                mFragmentLoginConvention.setUserNumberEdit(phoneNumber);

            }

            if (!"".equals(password) && password != null) {
                mFragmentLoginConvention.setPasswordEdit(password);
            }
        } else {
            if (!"".equals(phoneNumber) && phoneNumber != null) {
                mLoginVerificationFragment.setPhoneNumberEdit(phoneNumber);
            }
        }

    }


    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        setButtonEnabled(false);
        switch (viewId) {
            case R.id.login_button:
                if (mFragmentLoginConvention.isVisible()) {
                    String userNumber = mFragmentLoginConvention.getUserNumber();
                    String password = mFragmentLoginConvention.getPassword();
                    loginConventior(userNumber, password);
                } else {
                    String phoneNumber = mLoginVerificationFragment.getPhoneNumber();
                    String pinVerification = mLoginVerificationFragment.getpinVerification();
                    loginVerification(phoneNumber, pinVerification);
                }
                break;
            case R.id.registration_button:
                Intent registerIntent = new Intent();
                registerIntent.setClass(this, RegisterActivity.class);
                startActivity(registerIntent);
              /*  try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");

                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(cmp);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "检查到您手机没有安装微信，请安装后使用该功能", Toast.LENGTH_LONG).show();
                }*/
                break;
            case R.id.change_login_type_button:
                fragmentTransaction = supportFragmentManager.beginTransaction();
                if (mLoginVerificationFragment.isVisible()) {
                    fragmentTransaction.replace(R.id.fragment_container, mFragmentLoginConvention)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                    SpannableString spannableString = new SpannableString("忘记密码？动态登录");
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorLoginText)), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mChangeLoginTypeTv.setText(spannableString);
                } else {
                    fragmentTransaction.replace(R.id.fragment_container, mLoginVerificationFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

                    SpannableString spannableString = new SpannableString("密码登录");
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorLoginText)), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mChangeLoginTypeTv.setText(spannableString);
                    String userAccount = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
                    mLoginVerificationFragment.setPhoneNumberEdit(userAccount);
                }
                break;
            default:
                break;
        }
        setButtonEnabled(true);
    }


    private void setButtonEnabled(boolean isClick) {
        mLoginBtn.setEnabled(isClick);
        mRegisterBtn.setEnabled(isClick);
        mChangeLoginTypeTv.setEnabled(isClick);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    /**
     * 密码登录
     *
     * @param userNumber 用户账号
     * @param password   用户密码
     */
    private void loginConventior(@NonNull String userNumber, @NonNull String password) {
        //show progress
        // mLoginRooot.setBackgroundColor(Color.GRAY);
        mProgressView.setVisibility(View.VISIBLE);

        UserLogin userLogin = RetrofitUtils.create(UserLogin.class);

        LoginInfoConvention infoConvention = new LoginInfoConvention(userNumber, password);

        String keyId = CombinationSecretKey.getSecretKey("userLogin.do");
        String code = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(infoConvention));
        HuFuCode mhufuCode = new HuFuCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(mhufuCode));

        userLogin.login(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    mProgressView.setVisibility(View.GONE);
                    if (!result.success) {
                        Toast.makeText(getApplicationContext(), "请重新尝试登陆！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String antiFake = result.data.antiFake;
                    if (antiFake.length() > 0) {
                        antiFake = antiFake.trim();
                    }
                    SharedPreferences.Editor mEditor = mUserSharedPre.edit();
                    mEditor.putString(ConstantUtils.USER_ACCOUNT, userNumber);

                    if (mRememberPwdCb.isChecked()) {
                        mEditor.putString(ConstantUtils.ACCOUNT_PWD, password);
                    } else {
                        mEditor.putString(ConstantUtils.ACCOUNT_PWD, null);
                    }
                    mEditor.putBoolean(ConstantUtils.REMEMBER_PWD, mRememberPwdCb.isChecked());

                    mEditor.putString(ConstantUtils.ANTI_FAKE, antiFake);

                    mEditor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                     LoginActivity.this.finish();

                }, error -> {
                    Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    mProgressView.setVisibility(View.GONE);
                });


    }

    /**
     * 动态登录
     *
     * @param phoneNumber     手机号码
     * @param verificationPin 手机验证码
     */
    private void loginVerification(@NonNull String phoneNumber, @NonNull String verificationPin) {
        mProgressView.setVisibility(View.VISIBLE);
        UserLogin userLogin = RetrofitUtils.create(UserLogin.class);

        LoginInfoVerification infoConvention = new LoginInfoVerification(phoneNumber, verificationPin);
        String keyId = CombinationSecretKey.getSecretKey("userLogin.do");
        String code = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(infoConvention));
        HuFuCode mhufuCode = new HuFuCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , new Gson().toJson(mhufuCode));

        userLogin.login(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    mProgressView.setVisibility(View.GONE);
                    //success
                    if (!result.success) {
                        Toast.makeText(getApplicationContext(), "请重新尝试登陆！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String antiFake = result.data.antiFake.trim();
                    SharedPreferences.Editor editor = mUserSharedPre.edit();
                    editor.putString(ConstantUtils.USER_ACCOUNT, phoneNumber).apply();
                    editor.putString(ConstantUtils.ANTI_FAKE, antiFake).apply();
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                     LoginActivity.this.finish();

                }, error -> {
                    Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    mProgressView.setVisibility(View.GONE);
                });


    }


   /* private void showBluetoothDialog() {
        final DialogPlusBuilder dialogPlusBuilder = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(R.layout.dialog_bluetooth_setting));
        dialogPlusBuilder.setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT).setGravity(Gravity.CENTER);
        dialogPlusBuilder.setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogPlusBuilder.setOnClickListener(new com.orhanobut.dialogplus.OnClickListener() {
            @Override
            public void onClick(DialogPlus dialog, View view) {
                if (view.getId() == R.id.dialog_bluetooth_cancel) {
                    dialog.dismiss();
                    return;
                } else if (view.getId() == R.id.dialog_bluetooth_confirm) {
                    TextInputEditText bluetoothAccount = findViewById(R.id.bluetooth_account);
                    TextInputEditText bluetoothPassword = findViewById(R.id.bluetooth_password);
                    TextInputEditText paymentPasswordTe = findViewById(R.id.payment_password_Te);
                    CheckBox isStorageBluetoothCb = findViewById(R.id.is_store_blue_cb);
                    String simAccount = bluetoothAccount.getText().toString();
                    String simPassword = bluetoothPassword.getText().toString();
                    String paymentPassword = paymentPasswordTe.getText().toString();
                    TextInputLayout bluetoothPassIl = findViewById(R.id.bluetooth_password_il);
                    TextInputLayout blueAccountIl = findViewById(R.id.bluetooth_account_il);
                    bluetoothAccount.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (validateStringNoEmpty(s.toString()))
                                blueAccountIl.setErrorEnabled(false);

                        }
                    });
                    bluetoothPassword.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (validateStringNoEmpty(s.toString()))
                                bluetoothPassIl.setErrorEnabled(false);
                        }
                    });
                    if (!validateStringNoEmpty(simAccount))
                        blueAccountIl.setError("蓝牙卡名称长度错误");
                    else if (!validateStringNoEmpty(simPassword))
                        bluetoothPassIl.setError("蓝牙卡密码长度错误");
                    else {
                        if (isStorageBluetoothCb.isChecked()) {
                            SharedPreferences.Editor sharePreEd = mUserSharedPre.edit();
                            sharePreEd.putString(ConstantUtils.BLE_SIMA, simAccount);
                            sharePreEd.putString(ConstantUtils.BLE_SIMB, simPassword);
                            sharePreEd.putString(ConstantUtils.PAYMENT_PWD, paymentPassword);
                            sharePreEd.apply();
                        }
                        mVirtualCpk = VirtualCpk.getInstance(LoginActivity.this);
                        dialog.dismiss();
                    }
                }
            }
        });
        dialogPlusBuilder.setCancelable(true);
        dialogPlusBuilder.create().show();
        final CheckBox isStorageBluetoothCb = findViewById(R.id.is_store_blue_cb);
        isStorageBluetoothCb.setChecked(true);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private boolean validateStringNoEmpty(String valString) {
        return valString.length() > 5;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {

    }

    @Override
    public void onRationaleDenied(int requestCode) {

    }

    private boolean hasLocationAndContactsPermissions() {
        return EasyPermissions.hasPermissions(this, LOCATION_AND_CONTACTS);
    }

    @AfterPermissionGranted(RC_LOCATION_CONTACTS_PERM)
    public void locationAndContactsTask() {
        if (hasLocationAndContactsPermissions()) {
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location_contacts),
                    RC_LOCATION_CONTACTS_PERM,
                    LOCATION_AND_CONTACTS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void queryAppVersion() {
        UpdateApp updateApp = RetrofitUtils.create(UpdateApp.class);
        updateApp.queryVersion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (!result.success) {
                        Toast.makeText(this, result.msg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateApp(result.data.versionCode, result.data.versionName,
                            result.data.updateInfo, result.data.enforcement);

                }, error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                });


    }


    private void updateApp(int versionCode, String versionName, String updateInfo, boolean isForce) {
        UpdateAppUtils updateAppUtils = UpdateAppUtils.from(this)
                .serverVersionCode(versionCode)
                .serverVersionName(versionName)
                .updateInfo(updateInfo)
                .isForce(isForce)
                .apkPath(ServerDomain.APP_UPDATE_PATH);
        if (updateAppUtils.update() != null) {
            UpdateAppDialogFragment updateAppDialogFragment = updateAppUtils.update();
            updateAppDialogFragment.show(getSupportFragmentManager(), "updateApp");
        }

    }
}

