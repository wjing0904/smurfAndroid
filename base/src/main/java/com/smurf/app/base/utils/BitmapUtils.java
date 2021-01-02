package com.smurf.app.base.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BitmapUtils {
    public static byte[] bmpToByteArray(Bitmap bitmap, boolean bm) {
        int bytes = bitmap.getByteCount();

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buf);

        return buf.array();
    }

    /**
     * 图片按比例大小压缩方法
     *
     * @param image （根据Bitmap图片压缩）
     * @return
     */
    public static Bitmap compressScale(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 3072) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
//         float hh = 800f;// 这里设置高度为800f
//         float ww = 480f;// 这里设置宽度为480f
        float hh = 512f;
        float ww = 512f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be; // 设置缩放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
//        return bitmap;
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static byte[] compressBitmap(Bitmap bitmap, int maxkb) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        int options = 100;
        while (output.toByteArray().length > maxkb && options != 10) {
            output.reset(); //清空output
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
            options -= 10;
        }
        return output.toByteArray();
    }
//    public static String toBase64(String path){
//
//    }
    public static String fileToBase64(String path) {
        if (TextUtils.isEmpty(path)){
            return null;
        }

        FileInputStream fis = null;
        int byteSize = 0;
        try {
            fis = new FileInputStream(path);
            byteSize = fis.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int size = byteSize /1048576;
//        Bitmap bitmap  = BitmapFactory.decodeStream(fis);

        int degree = getBitmapDegree(path);
        Bitmap bitmap = rotateBitmapByDegree(BitmapFactory.decodeFile(path),degree);

        if (size >= 1) {
            // 当图片大于5兆时，进行尺寸压缩(做个压缩处理，转化Base64字符串会快一些)
            bitmap = getScaleBitmap(bitmap);
        }

        String bitmapStr = "";
        if (bitmap != null) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                if (size >=1) {
                    // 当图片大于5兆时，进行质量压缩
                    int multiple = size / 1;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100 / multiple, outputStream);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
                bitmapStr = Base64.encodeToString(outputStream.toByteArray(),Base64.DEFAULT);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmapStr;
    }
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }




    public static Bitmap getScaleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        //将大于1080px的图片转化为1080px的大小，这里可以根据业务做修改
        final int minSize = 1080;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int scaleWidth;
        int scaleHeight;
        int tempSize = Math.min(bitmapWidth, bitmapHeight);
        float scaleFactor = (float) minSize / (float) tempSize;
        //判断最大的长或宽
        if (tempSize == bitmapWidth) {
            if (tempSize > minSize) {
                scaleWidth = minSize;
                scaleHeight = (int) (bitmapHeight * scaleFactor);
            } else {
                return bitmap;
            }
        } else {
            if (tempSize > minSize) {
                scaleHeight = minSize;
                scaleWidth = (int) (bitmapWidth * scaleFactor);
            } else {
                return bitmap;
            }
        }
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
        return scaleBitmap;
    }
    public static void comPressImg(String path) {
        int degree = getBitmapDegree(path);
        int maxWidth = 200, maxHeight = 200;//定义目标图片的最大宽高，若原图高于这个数值，直接赋值为以上的数值
        Bitmap bitmap = rotateBitmapByDegree(BitmapFactory.decodeFile(path),degree);
        int originWidth = bitmap.getWidth();
        int originHeight = bitmap.getHeight();
        if (originWidth < maxWidth && originHeight < maxHeight) {
            return;
        }
        int width = originWidth;
        int height = originHeight;

        if (originWidth > maxWidth) {
            width = maxWidth;
            double i = originWidth * 1.0 / maxWidth;
            height = (int) Math.floor(originHeight / i);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        }
        if (height > maxHeight) {
            height = maxHeight;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}
