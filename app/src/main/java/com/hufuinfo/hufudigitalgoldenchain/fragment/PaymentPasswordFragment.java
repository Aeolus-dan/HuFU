package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

public class PaymentPasswordFragment extends Fragment {
    private Activity mActivity;
    private VirtualCpk mVirtualCpk;
    private EditText oldPwdEt;
    private EditText newPwdEt;
    private EditText confirmEt;

    public PaymentPasswordFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_password, container, false);
        oldPwdEt = view.findViewById(R.id.old_password_et);
        newPwdEt = view.findViewById(R.id.new_password_et);
        confirmEt = view.findViewById(R.id.confirm_password_et);
        view.findViewById(R.id.confirm_change_btn).setOnClickListener(listener -> {
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
                    mActivity.onBackPressed();
                }
            } else {
                Toast.makeText(getActivity(), "重复输入密码与新密码不正确", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.modify_payment_return_tv).setOnClickListener(listener -> {
            mActivity.onBackPressed();
        });
        return view;
    }

    /**
     * 关闭 Virtual  Cos , 退出登录状态
     * init  Cos
     */
    private void closeVirtualAndRestart() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

}
