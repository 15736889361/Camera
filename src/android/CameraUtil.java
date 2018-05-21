package com.chinamobile.gdwy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Log;


import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liangzhongtai on 2018/5/17.
 */

public class CameraUtil {
    public static String fileName;
    public static String filePath;
    public static String FILEPROVIDER  = ".provider";
    public final static int RESULTCODE_CAMERA = 10;
    public static final int RESULTCODE_PERMISSION = 20;
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static int dataType;

    //打开系统相机
    public static  void showCamera(CordovaInterface cordova, CordovaPlugin plugin, int resultCode){
        LogUtil.setLog(true);
        // 调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 取当前时间为照片名
        fileName = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+ ".jpg";
        filePath = getPhotoPath() + fileName;
        // 通过文件创建一个uri中
        Uri imageUri = null;
        if(Build.VERSION.SDK_INT>=24) {
            try {
                File imageFile = new File(filePath);
                imageUri= FileProvider.getUriForFile(cordova.getActivity(), cordova.getActivity().getApplication().getPackageName() + FILEPROVIDER, imageFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            imageUri = Uri.fromFile(new File(filePath));
        }
        LogUtil.d("Camera_","filePath="+filePath);
        // 保存uri对应的照片于指定路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        LogUtil.d("Camera_",""+resultCode);
        cordova.setActivityResultCallback(plugin);
        cordova.getActivity().startActivityForResult(intent, resultCode);
    }

    //获得照片路径
    public static String getPhotoPath() {
        return Environment.getExternalStorageDirectory() + "/DCIM/";
    }

    //压缩图片
    public static Bitmap decodeSampleBitmap(String path, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width  = options.outWidth;
        reqHeight = reqHeight<=0?height/2:reqHeight;
        reqWidth  = reqWidth<=0?width/2:reqWidth;
        int inSampleSize;
        final int heightRatio = Math.round((float)height/(float)reqHeight);
        final int widthRatio = Math.round((float)width/(float)reqWidth);
        inSampleSize = heightRatio<widthRatio?heightRatio:widthRatio;
        return inSampleSize;
    }

    /**
    * 保存压缩质量的Bitmap
    *
    *
    * */
    public static String saveBitmap(Bitmap bitmap,int quality,String cachePath,String fileName) {
        String filePath = null;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //检查路径是否存在
                File dir = new File(cachePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                //检查文件是否存在
                File file = new File(cachePath, fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                filePath = file.getAbsolutePath();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return filePath;
        }

    }


    /**
     * 水印_绘制文字到左下方
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingLeft
     * @param paddingBottom
     * @return
     */
    public static  Bitmap drawTextToLeftBottom(Context context, Bitmap bitmap, String text,
                                               int size, int color, int paddingLeft, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(bitmap, text, paint, dp2px(context, paddingLeft),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 绘制图片文字
     * @param bitmap
     * @param text
     * @param paint
     * @param paddingLeft
     * @param paddingTop
     * @return
     */
    public static  Bitmap drawTextToBitmap(Bitmap bitmap, String text,
                                           Paint paint,int paddingLeft, int paddingTop) {
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(text, paddingLeft, paddingTop, paint);
        return bitmap;
    }


    /**
     * dip转pix
     * @param context
     * @param dp
     * @return
     */
    public static  int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 根据时间戳转成指定的format格式
     * @param format
     * @return
     */
    public static String formatDate(String format) {
        Date date = new Date();
        final SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

}
