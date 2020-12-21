package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.UpdateApp;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ServerDomain;

import activity.UpdateAppDialogFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import util.UpdateAppUtils;

public class SettingFragment extends Fragment {
    private TextView versionNameTv;


    public SettingFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        view.findViewById(R.id.user_info_tv).setOnClickListener(listener -> {
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.container_transaction, new UserBasicInfoFragment()).addToBackStack(null).commit();
        });
        view.findViewById(R.id.change_login_password_tv).setOnClickListener(listener -> {
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.container_transaction, new PasswordFragment()).addToBackStack(null).commit();
        });
        view.findViewById(R.id.change_payment_pwd_tv).setOnClickListener(listener -> {
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.container_transaction, new PaymentPasswordFragment()).addToBackStack(null).commit();
        });
        view.findViewById(R.id.setting_return_tv).setOnClickListener(listener -> {
            getActivity().onBackPressed();
        });
        view.findViewById(R.id.update_app).setOnClickListener(listener -> {
            queryAppVersion();
        });
        versionNameTv = view.findViewById(R.id.version_name);
        PackageManager manager = getActivity().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            versionNameTv.setText("当前版本Version: " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return view;
    }

    public void queryAppVersion() {
        UpdateApp updateApp = RetrofitUtils.create(UpdateApp.class);
        updateApp.queryVersion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (!result.success) {
                        Toast.makeText(getActivity(), result.msg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateApp(result.data.versionCode, result.data.versionName,
                            result.data.updateInfo, result.data.enforcement);

                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                });


    }


    private void updateApp(int versionCode, String versionName, String updateInfo, boolean isForce) {
        UpdateAppDialogFragment updateAppDialogFragment = UpdateAppUtils.from(getActivity())
                .serverVersionCode(versionCode)
                .serverVersionName(versionName)
                .updateInfo(updateInfo)
                .isForce(false)
                .isShowCheckVersionInfo(isForce)
                .apkPath(ServerDomain.APP_UPDATE_PATH)
                .update();
        if (updateAppDialogFragment != null) {
            updateAppDialogFragment.show(getChildFragmentManager(), "updateApp");
        }
    }
}
