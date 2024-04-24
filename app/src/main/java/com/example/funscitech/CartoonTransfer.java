package com.example.funscitech;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class CartoonTransfer extends AppCompatActivity
{
    private static final int SELECT_IMAGE = 1;
    private static final int len = 1000;
    private ImageView img, dst_img;
    //    private Button btn_load, btn_transfer;
    private Bitmap bitmap = null;
    private Bitmap dst_bitmap = null;
    private com.example.funscitech.OnnxInference onnxInfer = new com.example.funscitech.OnnxInference();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cartoontransfer);
        getSupportActionBar().setTitle("漫画生成");

        img = (ImageView) findViewById(R.id.img);
        dst_img = (ImageView) findViewById(R.id.dst_img);

        boolean ret_init = onnxInfer.Init(getAssets());
        if (!ret_init)
        {
            Log.e("MainActivity", "model Init failed");
        }

        Button buttonImage = (Button) findViewById(R.id.btn_load);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });

        Button buttonDetectGPU = (Button) findViewById(R.id.btn_transfer);
        buttonDetectGPU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (bitmap == null)
                    return;

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                new Thread(new Runnable() {
                    public void run() {
                        dst_bitmap = runStyleTransfer(true);
                        dst_img.post(new Runnable() {
                            public void run() {
                                dst_img.setImageBitmap(dst_bitmap);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        });
                    }
                }).start();
                Button btnSave = (Button) findViewById(R.id.btn_save);
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (dst_bitmap != null) {
                            saveBitmap(dst_bitmap);
                        }
                        dst_bitmap = null;
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            try
            {
                if (requestCode == SELECT_IMAGE) {
                    bitmap = decodeUri(selectedImage);
                    if (bitmap == null) {
                        Log.e("MainActivity", "qdq bitmapnull");
                    }
                    Log.i("MainActivity", "qdq bitmapsize " + bitmap.getAllocationByteCount());
                    img.setImageBitmap(bitmap);
                }
            }
            catch (FileNotFoundException e)
            {
                Log.e("MainActivity", "FileNotFoundException");
            }
        }
    }

    private Bitmap runStyleTransfer(boolean use_gpu)
    {
//        Bitmap src = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap src = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        onnxInfer.inference(src, true);
        return src;
    }

    private void saveBitmap(Bitmap bitmap)
    {
        try {
            //获取要保存的图片的位图
            //创建一个保存的Uri
            ContentValues values = new ContentValues();
            //设置图片名称
            values.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".png");
            //设置图片格式
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            //设置图片路径
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri saveUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (TextUtils.isEmpty(saveUri.toString())) {
                Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            OutputStream outputStream = getContentResolver().openOutputStream(saveUri);
            //将位图写出到指定的位置
            //第一个参数：格式JPEG 是可以压缩的一个格式 PNG 是一个无损的格式
            //第二个参数：保留原图像90%的品质，压缩10% 这里压缩的是存储大小
            //第三个参数：具体的输出流
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException
    {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true; // 先设置为true再decode图片资源，节省内存
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        Log.i("MainActivity", String.format("qdq bitmap height %d width %d ", o.outHeight, o.outWidth));
        if (o.outHeight * o.outWidth > len) {
            int maxLen = o.outHeight > o.outWidth ? o.outHeight : o.outWidth;
            o2.inSampleSize = (int)maxLen / len;
        } else {
            o2.inSampleSize = 1;
        }
        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        // Rotate according to EXIF
        int rotate = 0;
        try
        {
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(selectedImage));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        }
        catch (IOException e)
        {
            Log.e("MainActivity", "ExifInterface IOException");
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
