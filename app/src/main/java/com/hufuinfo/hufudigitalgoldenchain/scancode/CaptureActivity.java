package com.hufuinfo.hufudigitalgoldenchain.scancode;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.hufuinfo.hufudigitalgoldenchain.R;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class CaptureActivity extends Activity implements Callback, View.OnClickListener {
    private final static String THIS_FILE = "CaptureActivity";
    private final static int INTENT_ALBUM_CODES = 101;
    /**
     * 浏览扫描相册中的二维码图像处理成功
     */
    private final static int PARSE_BARCODE_SUC = 300;
    /**
     * 浏览扫描相册中的二维码图像处理失败
     */
    private final static int PARSE_BARCODE_FAIL = 303;

    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private boolean isShow = false;
    private ProgressDialog mProgressDialog;
    private String scanCodePath;
    private Bitmap scanBitmap;
    private CaptureActivityHandler handler;   //二维码扫描结果、相机控制处理类CaptureActivityHandler

    /**
     * 选择相册中的二维码图像进行解析(子线程中完成)，解析返回结果处理类Handler。
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mProgressDialog.dismiss();
            switch (msg.what) {
                case PARSE_BARCODE_SUC://解析相册中二维码图像成功
                    handleDecode((Result) msg.obj, scanBitmap);
                    break;
                case PARSE_BARCODE_FAIL://解析相册中二维码图像失败
                    Toast.makeText(CaptureActivity.this, (String) msg.obj,
                            Toast.LENGTH_LONG).show();
                    break;

            }
        }

    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE );
        setContentView(R.layout.camera_diy);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        findViewById(R.id.select_qrcode_button).setOnClickListener(this);
        findViewById(R.id.btn_light_control).setOnClickListener(this);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            //initCamera(surfaceHolder);
            checkCameraPermission(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result  扫描结果数据
     * @param barcode 扫描Bitmap数据
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        //FIXME
        if (resultString.equals("")) {
            Toast.makeText(CaptureActivity.this, R.string.scan_code_fail_string, Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
        }
        CaptureActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            //initCamera(holder);
            checkCameraPermission(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    private final static int REQUEST_CAMERA_PERMISSION_CODE = 201;
    private final static int REQUEST_STORAGE_PERMISSION_CODE = 202;

    //检查是否拥有Manifest.permission.CAMERA
    private void checkCameraPermission(SurfaceHolder holder) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera(holder);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                initCamera(surfaceHolder);
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Thread(scanFileRunable).start();
            } else {
                mProgressDialog.cancel();
                Toast.makeText(CaptureActivity.this, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_light_control:
                LightControl mLightControl = new LightControl();
                if (isShow) {
                    isShow = false;
                    Toast.makeText(getApplicationContext(), R.string.flash_off, Toast.LENGTH_SHORT).show();
                    mLightControl.turnOff();
                } else {
                    isShow = true;
                    mLightControl.turnOn();
                    Toast.makeText(getApplicationContext(), R.string.flash_on, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.select_qrcode_button:
                //跳转到图片选择界面去选择一张二维码图片
                Intent intentAlbum = new Intent();
                intentAlbum.setAction(Intent.ACTION_PICK);//  ACTION_PICK ACTION_GET_CONTENT
                intentAlbum.setType("image/*");
                Intent intentChooseImage = Intent.createChooser(intentAlbum, getResources().getString(R.string.scan_code_choose_image_string));
                startActivityForResult(intentChooseImage, INTENT_ALBUM_CODES);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case INTENT_ALBUM_CODES:
                    // 获取选中图片的路径
                    Cursor cursor = getContentResolver().query(data.getData(), null
                            , null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        scanCodePath = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }

                    mProgressDialog = new ProgressDialog(CaptureActivity.this);
                    mProgressDialog.setMessage(getResources().getString(R.string.scanning_code_string));
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();

                    //new Thread(scanFileRunable).start();
                    checkScanStoragePermission();
                    break;
            }
        }
    }

    /**
     * 检查是否拥有读取存储权限
     */
    private void checkScanStoragePermission() {
        if (ActivityCompat.checkSelfPermission(CaptureActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED) {
            new Thread(scanFileRunable).start();
        } else {
            ActivityCompat.requestPermissions(CaptureActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
        }
    }

    Runnable scanFileRunable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //根据选择的二维码图像对应的路径，扫描该二维码图像，并得到返回数据
            Result result = scanningImage(scanCodePath);
            //设备相册中二维码图像扫描成功处理
            if (result != null) {
                Message m = mHandler.obtainMessage();
                m.what = PARSE_BARCODE_SUC;
                m.obj = result;
                mHandler.sendMessage(m);
            }
            //相册中二维码图像扫描失败处理
            else {
                Message m = mHandler.obtainMessage();
                m.what = PARSE_BARCODE_FAIL;
                m.obj = getResources().getString(R.string.scan_code_fail);
                mHandler.sendMessage(m);
            }
        }
    };

    /**
     * 根据图像路径，扫描二维码图像，此操作为选择相册中的二维码图像，返回后的扫描处理
     *
     * @param path 相册中二维码图像的路径
     * @return Result 返回扫描结果Result
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Point sizePoint = new Point();
        getWindowManager().getDefaultDisplay().getSize(sizePoint);

        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        Log.e(THIS_FILE, "scanBitmap Bitmap Factory.decodeFile 1");
        scanBitmap = BitmapFactory.decodeFile(path, options);
        int heightRatio = (int) Math.ceil(options.outHeight / (float) sizePoint.y);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) sizePoint.x);
        if (heightRatio > 1 && widthRatio > 1) {
            options.inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }
        options.inJustDecodeBounds = false; // 获取新的大小
        Log.e(THIS_FILE, "scanBitmap Bitmap Factory.decodeFile 2");
        scanBitmap = BitmapFactory.decodeFile(path, options);
        //以下是解析二维码图像操作
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}