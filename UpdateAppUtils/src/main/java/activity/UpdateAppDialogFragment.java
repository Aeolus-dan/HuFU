package activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import customview.ConfirmDialog;
import model.UpdateBean;
import teprinciple.updateapputils.R;
import util.DownloadAppUtils;
import util.UpdateAppService;
import util.UpdateAppUtils;

public class UpdateAppDialogFragment extends DialogFragment {

    private static String KEY_OF_INTENT_UPDATE_BEAN = "KEY_OF_INTENT_UPDATE_BEAN";

    private Activity mActivity;

    public static UpdateAppDialogFragment launch(Activity activity, UpdateBean updateBean) {
        UpdateAppDialogFragment updateAppDialogFragment = new UpdateAppDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_OF_INTENT_UPDATE_BEAN, updateBean);
        updateAppDialogFragment.setArguments(args);
        return updateAppDialogFragment;
    }

    private TextView content;
    private TextView sureBtn;
    private TextView cancleBtn;

    private UpdateBean updateBean;


    private static final int PERMISSION_CODE = 1001;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateBean = getArguments().getParcelable(KEY_OF_INTENT_UPDATE_BEAN);
        mActivity = getActivity();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_version_tips_dialog, container, false);
        initView(view);
        initOperation();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (updateBean.getForce()) {
                        System.exit(0);
                    } else {
                        dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }

    private void initView(View view) {
        sureBtn = view.findViewById(R.id.dialog_confirm_sure);
        cancleBtn = view.findViewById(R.id.dialog_confirm_cancle);
        content = view.findViewById(R.id.dialog_confirm_title);


        String contentStr = "发现新版本:" + updateBean.getServerVersionName() + "\n是否下载更新?";
        if (!TextUtils.isEmpty(updateBean.getUpdateInfo())) {
            contentStr = "发现新版本:" + updateBean.getServerVersionName() + "是否下载更新?\n\n" + updateBean.getUpdateInfo();
        }

        content.setText(contentStr);

        if (updateBean.getForce()) {
            cancleBtn.setText("退出");
        } else {
            cancleBtn.setText("取消");
        }

    }


    private void initOperation() {


        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateBean.getForce()) {
                    System.exit(0);
                } else {
                    dismiss();
                }
            }
        });

        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preDownLoad();
            }
        });
    }


    /**
     * 预备下载 进行 6.0权限检查
     */
    private void preDownLoad() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            download();
        } else {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                download();

            } else {//申请权限
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
            }
        }
    }


    private void download() {

        getActivity().startService(new Intent(getActivity(), UpdateAppService.class));

        if (updateBean.getDownloadBy() == UpdateAppUtils.DOWNLOAD_BY_APP) {
            if (isWifiConnected(getActivity())) {

                DownloadAppUtils.download(getActivity(), updateBean.getApkPath(), updateBean.getServerVersionName());
            } else {
                new ConfirmDialog(mActivity, new ConfirmDialog.Callback() {
                    @Override
                    public void callback(int position) {
                        if (position == 1) {
                            DownloadAppUtils.download(getActivity(), updateBean.getApkPath(), updateBean.getServerVersionName());
                        } else {
                            if (updateBean.getForce()) {
                                System.exit(0);
                            } else {
                                dismiss();
                            }
                        }
                    }
                }).setContent("目前手机不是WiFi状态\n确认是否继续下载更新？").show();
            }
        } else if (updateBean.getDownloadBy() == UpdateAppUtils.DOWNLOAD_BY_BROWSER) {
            DownloadAppUtils.downloadForWebView(getActivity(), updateBean.getApkPath());
        }

        dismiss();
    }


    /**
     * 权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    download();
                } else {
                    new ConfirmDialog(mActivity, new ConfirmDialog.Callback() {
                        @Override
                        public void callback(int position) {
                            if (position == 1) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + mActivity.getPackageName())); // 根据包名打开对应的设置界面
                                startActivity(intent);
                            }
                        }
                    }).setContent("暂无读写SD卡权限\n是否前往设置？").show();
                }
                break;
        }
    }

    /**
     * 检测wifi是否连接
     */
    private boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

}
