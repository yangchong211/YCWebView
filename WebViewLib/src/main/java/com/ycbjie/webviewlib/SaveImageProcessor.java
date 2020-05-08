package com.ycbjie.webviewlib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.tencent.smtt.sdk.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SaveImageProcessor {

    /**
     * app保存路径
     * 图片保存位置：x5Web/images   (包含画廊保存图片，list条目点击item按钮保存图片)
     */
    private final static String APP_ROOT_SAVE_PATH = "WebX5";
    private static final String IMAGE_FILE_PATH = "images";
    private final static String PROPERTY = File.separator;
    private Context mContext;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = (Bitmap) msg.obj;
            String imagePath = saveBitmap(bitmap);
            insertMedia(mContext, imagePath);
        }
    };

    public boolean showActionMenu(final WebView webView) {
        final Context context = webView.getContext();
        if (context == null) {
            return false;
        }
        mContext = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(new CharSequence[] {"保存图片"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (hasExtStoragePermission(mContext)){
                    saveImage(mContext, webView);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    private void saveImage(final Context context, final WebView webView) {
        WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        if (hitTestResult == null) {
            Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show();
            return;
        }
        final String imageUrl = hitTestResult.getExtra();
        if (imageUrl == null) {
            Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show();
        } else if (imageUrl.startsWith("data:")) {
            File rootImagePath = getImageDir(context);
            if (rootImagePath == null) {
                Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show();
                return;
            }
            String imageData = imageUrl.replaceFirst("data:image\\/\\w+;base64,", "");
            byte[] imageDataBytes = Base64.decode(imageData, Base64.DEFAULT);
            String fileName = getLocalImgSavePath();
            File imageFile = new File(rootImagePath, fileName);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                fileOutputStream.write(imageDataBytes);
                fileOutputStream.close();
                String imagePath = imageFile.getAbsolutePath();
                insertMedia(context, imagePath);
                Toast.makeText(context, "保存图片成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(context, "保存图片成功", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
            singleThreadPool.execute(new Runnable() {
                @SuppressLint("LongLogTag")
                @Override
                public void run() {
                    Bitmap bitmap = returnBitMap(imageUrl);
                    android.os.Message message = android.os.Message.obtain();
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }
            });
        }
    }


    private File getImageDir(Context context) {
        String path = null;
        try {
            if (isMounted()) {
                File extPath = context.getExternalFilesDir(null);
                if (extPath != null) {
                    path = extPath.getAbsolutePath() + PROPERTY + APP_ROOT_SAVE_PATH + PROPERTY + IMAGE_FILE_PATH;
                }
            }
        } catch (Exception e) { // catch accidental exception
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(path)) { // data storage
            path = context.getFilesDir().getAbsolutePath() + PROPERTY + APP_ROOT_SAVE_PATH + PROPERTY + IMAGE_FILE_PATH;
        }
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return null;
            }
        }
        return file;
    }

    private boolean hasExtStoragePermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }


    private Bitmap returnBitMap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            if (myFileUrl != null) {
                conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(5000);
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    /**
     * 保存bitmap到本地
     */
    public static String saveBitmap(Bitmap mBitmap) {
        if (mBitmap==null){
            return null;
        }
        String savePath = getLocalImgSavePath();
        try {
            File filePic = new File(savePath);
            if (!filePic.exists()) {
                //noinspection ResultOfMethodCallIgnored
                filePic.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            //BaseToast.showRoundRectToast("稍后再试");
            return null;
        }
        return savePath;
    }

    /**
     * 获取本地图片保存路径
     * @return
     */
    public static String getLocalImgSavePath() {
        return getLocalFileSavePathDir(X5WebUtils.getApplication(),IMAGE_FILE_PATH, generateRandomName() + ".png");
    }


    public static String generateRandomName() {
        return UUID.randomUUID().toString();
    }


    /**
     * 保存图片/崩溃日志保存文件目录
     * @param fileName              文件名称：比如crash，image，这个是文件夹
     * @param name                  自己命名，文件名称比如图片  aa.jpg
     * @return                      路径
     */
    public static String getLocalFileSavePathDir(Context context , String fileName , String name){
        //获得SDCard 的路径,storage/sdcard
        String sdPath = getSDCardPath();
        //判断 SD 卡是否可用
        if (!isSDCardEnable(context) || TextUtils.isEmpty(sdPath)) {
            //获取 SD 卡路径
            List<String> sdPathList = getSDCardPaths(context);
            if (sdPathList != null && sdPathList.size() > 0 && !TextUtils.isEmpty(sdPathList.get(0))) {
                sdPath = sdPathList.get(0);
            }
        }
        if (TextUtils.isEmpty(sdPath)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(sdPath);
        sb.append(PROPERTY);
        sb.append(APP_ROOT_SAVE_PATH);
        sb.append(PROPERTY);
        sb.append(fileName);
        //如果name不为空，那么通过该方法是获取到文件的路径
        //否则则是获取文件夹的路径
        if(name!=null && name.length()>0){
            sb.append(PROPERTY);
            sb.append(name);
        }
        return sb.toString();
    }



    /**
     * 判断SDCard是否挂载
     * Environment.MEDIA_MOUNTED,表示SDCard已经挂载
     * Environment.getExternalStorageState()，获得当前SDCard的挂载状态
     */
    private static boolean isMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    /**
     * 获得SDCard 的路径,storage/sdcard
     * @return          路径
     */
    public static String getSDCardPath() {
        String path = null;
        if (isMounted()) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        return path;
    }


    /**
     * 判断 SD 卡是否可用
     *
     * @return true : 可用<br>false : 不可用
     */
    private static boolean isSDCardEnable(Context context) {
        return !getSDCardPaths(context).isEmpty();
    }


    /**
     * 判断是否有sd卡
     * @return                      是否有sd
     */
    private static boolean isExistSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }



    /**
     * 获取 SD 卡路径
     *
     * @return SD 卡路径
     */
    @SuppressWarnings("TryWithIdenticalCatches")
    private static List<String> getSDCardPaths(Context context) {
        StorageManager storageManager = (StorageManager) context.getApplicationContext()
                .getSystemService(Context.STORAGE_SERVICE);
        List<String> paths = new ArrayList<>();
        try {
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
            getVolumePathsMethod.setAccessible(true);
            Object invoke = getVolumePathsMethod.invoke(storageManager);
            paths = Arrays.asList((String[]) invoke);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * 获取外置SD卡路径
     * @return 应该就一条记录或空
     */
    private static ArrayList<String> getExtSDCardPath() {
        ArrayList<String> lResult = new ArrayList<>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception ignored) {
        }
        return lResult;
    }

    private void insertMedia(Context context, String imagePath) {
        if (imagePath==null || imagePath.length()==0 || context==null){
            return;
        }
        try {
            // notify the system media
            MediaStore.Images.Media.insertImage(context.getContentResolver(), imagePath, "", "");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                final Uri contentUri = Uri.parse(imagePath);
                scanIntent.setData(contentUri);
                context.sendBroadcast(scanIntent);
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(imagePath)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

