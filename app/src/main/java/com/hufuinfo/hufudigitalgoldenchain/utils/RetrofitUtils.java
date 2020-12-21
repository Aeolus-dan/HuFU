package com.hufuinfo.hufudigitalgoldenchain.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.RequestPhonePin;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.PhoneNumber;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils {

    private static Retrofit build;

    public static <T> T create(Class<T> cls) {
        return buildRetrofit().create(cls);
    }

    private static Retrofit buildRetrofit() {

        if (build == null) {
            build = new Retrofit.Builder()
                    .baseUrl(ServerDomain.DGC_SERVER_ADDRESS)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return build;
    }

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    public static String retriveCode(@NonNull Context context, String mobile, VirtualCpk virtualCpk) {
        RequestPhonePin requestPhonePin = create(RequestPhonePin.class);
        if (mobile == null || "".equals(mobile)) {
            Toast.makeText(context, "请输入正确的手机号码！", Toast.LENGTH_SHORT).show();
            return null;
        }
        String jsonMobile = CombinationSecretKey.assembleJsonMobile(mobile);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonMobile);

        requestPhonePin.requestPhonePin(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Result -> {
                    if (Result.success)
                        Toast.makeText(context, "发送成功！", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(context, Result.msg, Toast.LENGTH_LONG).show();
                }, error -> {
                    Toast.makeText(context, R.string.net_error, Toast.LENGTH_SHORT).show();
                });
        return null;
    }
}
