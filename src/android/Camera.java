package com.chinamobile.gdwy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by liangzhongtai on 2018/5/17.
 */

public class Camera extends CordovaPlugin{
    public final static String TAG = "Camera_Plugin";
    public final static int FILE_PATH = 0;
    public final static int BASE_64 = 1;

    public static String name;
    public static int mQuality=80;
    public static int targetWidth;
    public static int targetHeight;
    public CordovaInterface cordova;
    public CordovaWebView webView;
    private CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        this.webView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if("coolMethod".equals(action)){
            name = args.getString(0);
            LogUtil.d(TAG,"相机name="+name);
            if(args.length()>1)
            CameraUtil.dataType = args.getInt(1);
            //权限
            try {
                if(!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        ||!PermissionHelper.hasPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ||!PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                        ||!PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    PermissionHelper.requestPermissions(this,CameraUtil.RESULTCODE_PERMISSION,new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                }else{
                    CameraUtil.showCamera(cordova,this,CameraUtil.RESULTCODE_CAMERA);
                }
            }catch (Exception e){
                //权限异常
                callbackContext.error("Illegal Argument Exception");
                PluginResult r = new PluginResult(PluginResult.Status.ERROR);
                callbackContext.sendPluginResult(r);
                return true;
            }
            //桥接
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            return true;
        }
        return super.execute(action, args, callbackContext);
    }

    @Override
    public Bundle onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }


    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                PluginResult errorR = new PluginResult(PluginResult.Status.ERROR, CameraUtil.PERMISSION_DENIED_ERROR);
                this.callbackContext.sendPluginResult(errorR);
                return;
            }
        }
        switch (requestCode) {
            case CameraUtil.RESULTCODE_PERMISSION:
                CameraUtil.showCamera(cordova,this,CameraUtil.RESULTCODE_CAMERA);
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        LogUtil.d(TAG,"相机关闭requestCode="+requestCode);
        LogUtil.d(TAG,"相机关闭resultCode="+resultCode);
        LogUtil.d(TAG,"相机关闭CameraUtil.filePath="+CameraUtil.filePath);
        LogUtil.d(TAG,"相机关闭new File(CameraUtil.filePath).exists()="+new File(CameraUtil.filePath).exists());
        //相机关闭后返回
        if(requestCode == CameraUtil.RESULTCODE_CAMERA) {
            if(TextUtils.isEmpty(CameraUtil.filePath)||!new File(CameraUtil.filePath).exists()){
                Toast.makeText(cordova.getActivity().getApplicationContext(),"图片路径已丢失!", Toast.LENGTH_SHORT);
                //UIModel.post(Tags.ACTVIEW,"", WebActPresenter.class.getName(), IEventHelper.BITMAP_FOR_WEB_CANCEL);
                return;
            }

            //压缩图片
            Bitmap bitmap = CameraUtil.decodeSampleBitmap(CameraUtil.filePath,targetWidth,targetHeight);

            LogUtil.d(TAG,"图片bitmap="+bitmap);

            //时间
            String date = CameraUtil.formatDate("yyyy-MM-dd HH:mm:ss");

            LogUtil.d(TAG,"时间date="+date);

            //经度-维度
            Location location = LocationUtils.getInstance(cordova.getActivity()).showLocation();
            String address = "";
            if (location != null) {
                 address = location.getLatitude() + ":" + location.getLongitude();
            }

            LogUtil.d(TAG,"经纬度address="+address);

            //添加水印
            CameraUtil.drawTextToLeftBottom(cordova.getActivity().getApplicationContext(),bitmap,date+address,12,0xff9900,14,42);
            CameraUtil.drawTextToLeftBottom(cordova.getActivity().getApplicationContext(),bitmap,name,12,0xff9900,14,14);

            //将图片返回
            try {
                processResultFromCamera(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(cordova.getActivity().getApplicationContext(),"图片处理异常!", Toast.LENGTH_SHORT);
            }
        }
    }

    private void processResultFromCamera(Bitmap bitmap) throws IOException {
        //重新保存压缩过的图片
        String filePath = CameraUtil.saveBitmap(bitmap,mQuality,CameraUtil.getPhotoPath(),CameraUtil.fileName);
        LogUtil.d(TAG,"图片的路径="+filePath);
        if(filePath==null){
            callbackContext.error("Camera Error!");
        }else{
            callbackContext.success(filePath);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationUtils.getInstance(cordova.getActivity()).removeLocationUpdatesListener();
    }
}
