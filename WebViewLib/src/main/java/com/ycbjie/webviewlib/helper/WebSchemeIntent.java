package com.ycbjie.webviewlib.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.ycbjie.webviewlib.tools.WebViewException;

/**
 * <pre>
 *     @author  yangchong
 *     email  : yangchong211@163.com
 *     blog   : https://github.com/yangchong211
 *     time   : 2020/04/27
 *     desc   : 统一处理scheme协议
 *     revise:
 * </pre>
 */
public final class WebSchemeIntent {

    private static final String SCHEME_WX_PAY = "weixin";
    private static final String SCHEME_ALI_PAY = "alipays";
    private static final String SCHEME_QQ_IM = "mqqwpa";
    /**
     * sms:协议---短信息
     * 需要权限：
     * <uses-permission android:name="android.permission.SEND_SMS"/>
     * <uses-permission android:name="android.permission.READ_SMS"/>
     * <uses-permission android:name="android.permission.WRITE_SMS"/>
     */
    private static final String SCHEME_SMS = "sms";
    /**
     * tel:协议---拨打电话
     * 需要权限：
     * <uses-permission android:name="android.permission.CALL_PHONE"/>
     */
    private static final String SCHEME_TEL = "tel";
    /**
     * mailto:协议---发邮件窗口
     * 需要权限：
     * <uses-permission android:name="android.permission.SEND_TO"/>
     */
    private static final String SCHEME_MAIL = "mailto";
    /**
     * geo:协议---查看定位信息
     * 需要权限：
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
     */
    private static final String SCHEME_GEO = "geo";
    private static final String MESSAGE_TEL = "网页请求打开应用";
    private static final String MESSAGE_LABEL_OPEN = "打开";
    private static final String MESSAGE_UNKNOWN = "系统未安装相应应用";
    public static final int REQUEST_PHONE = 101;


    private static boolean startWithActivity(Intent intent, Activity activity) throws WebViewException {
        Activity target = activity.getParent();
        if (target == null) {
            target = activity;
        }
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return target.startActivityIfNeeded(intent, -1);
        } catch (ActivityNotFoundException e) {
            throw new WebViewException(3,MESSAGE_UNKNOWN);
        }
    }

    private static void startWithAppContext(Intent intent, Context context) throws WebViewException {
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            throw new WebViewException(3,MESSAGE_UNKNOWN);
        }
    }

    public static boolean isAliveType(@NonNull String scheme) {
        return SCHEME_SMS.equalsIgnoreCase(scheme) || SCHEME_TEL.equalsIgnoreCase(scheme)
                || SCHEME_MAIL.equalsIgnoreCase(scheme) || SCHEME_GEO.equalsIgnoreCase(scheme);
    }

    public static boolean isSilentType(@NonNull String scheme) {
        return SCHEME_WX_PAY.equalsIgnoreCase(scheme)
                || SCHEME_ALI_PAY.equalsIgnoreCase(scheme) || SCHEME_QQ_IM.equalsIgnoreCase(scheme);
    }

    /**
     * 处理会打断的默认scheme
     *
     * @param context   上下文
     * @param uri       链接
     * @return          true表示被处理
     */
    public static boolean handleAlive(@NonNull Context context, Uri uri) {
        final String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme) || !isAliveType(scheme)) {
            return false;
        }
        showUriDialog(context, uri, MESSAGE_TEL);
        return true;
    }

    /**
     * 静默处理内部页面支持的scheme.
     *
     * @param context 上下文
     * @param uri     链接
     * @return true表示被处理
     */
    public static boolean handleSilently(@NonNull Context context, Uri uri) {
        final String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme) || !isSilentType(scheme)) {
            return false;
        }
        try {
            Intent intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // forbid launching activities without BROWSABLE category
            intent.addCategory("android.intent.category.BROWSABLE");
            // forbid explicit call
            intent.setComponent(null);
            // forbid intent with selector intent
            intent.setSelector(null);
            if (context instanceof Activity) {
                return startWithActivity(intent, (Activity) context);
            } else {
                startWithAppContext(intent, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void showUriDialog(final Context context, final Uri uri, String msg) {
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton(MESSAGE_LABEL_OPEN, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String scheme = uri.getScheme();
                        if (scheme != null && scheme.contains(SCHEME_TEL)) {
                            if (!checkReadPermission(context, Manifest.permission.CALL_PHONE, REQUEST_PHONE)) {
                                //打电话需要有打电话权限
                                return;
                            }
                        }
                        startWithActivity(context,uri);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * 开启activity
     * @param context                           context上下文
     * @param uri                               uri
     */
    private static void startWithActivity(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * 判断是否有某项权限
     * @param string_permission                 权限
     * @param request_code                      请求码
     * @return
     */
    public static boolean checkReadPermission(Context context, String string_permission, int request_code) {
        boolean flag = false;
        int permission = ContextCompat.checkSelfPermission(context, string_permission);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //已有权限
            flag = true;
        } else {
            //申请权限
            ActivityCompat.requestPermissions((Activity) context, new String[]{string_permission}, request_code);
        }
        return flag;
    }

}

