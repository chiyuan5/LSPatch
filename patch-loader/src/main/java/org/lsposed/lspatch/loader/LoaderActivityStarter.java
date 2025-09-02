package org.lsposed.lspatch.loader;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.reflect.Field;
import android.util.Log;

import org.lsposed.lspatch.share.Constants;
import org.lsposed.lspatch.loader.util.XLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LoaderActivityStarter {
    private static final String TAG = "LSPatch/LoaderStarter";

    public static void startLoaderActivity() {
        try {
            ActivityThread activityThread = ActivityThread.currentActivityThread();
            if (activityThread == null) {
                XLog.e(TAG, "ActivityThread is null");
                return;
            }

            // 通过反射获取Application实例（修复getApplication()不存在的问题）
            Field mInitialApplicationField = ActivityThread.class.getDeclaredField("mInitialApplication");
            mInitialApplicationField.setAccessible(true);
            Application app = (Application) mInitialApplicationField.get(activityThread);

            // 获取应用信息
            ApplicationInfo appInfo = app.getApplicationInfo();
            if (appInfo == null) {
                XLog.e(TAG, "ApplicationInfo is null");
                return;
            }

            // 检查是否已加载过LoaderActivity
            if (isLoaderActivityLoaded(activityThread)) {
                XLog.d(TAG, "LoaderActivity already loaded");
                return;
            }

            // 启动LoaderActivity
            Class<?> loaderActivityClass = Class.forName(Constants.LOADER_ACTIVITY_CLASS);
            Intent intent = new Intent();
            intent.setClass(app, loaderActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(intent);

            XLog.i(TAG, "LoaderActivity started successfully");
        } catch (ClassNotFoundException e) {
            XLog.e(TAG, "LoaderActivity class not found", e);
        } catch (NoSuchFieldException e) {
            XLog.e(TAG, "mInitialApplication field not found", e);
        } catch (IllegalAccessException e) {
            XLog.e(TAG, "Failed to access mInitialApplication field", e);
        } catch (Exception e) {
            XLog.e(TAG, "Failed to start LoaderActivity", e);
        }
    }

    private static boolean isLoaderActivityLoaded(ActivityThread activityThread) {
        try {
            // 检查Activity是否已在ActivityThread中注册
            Method getActivityClientRecordMethod = ActivityThread.class.getDeclaredMethod(
                    "getActivityClientRecord", String.class);
            getActivityClientRecordMethod.setAccessible(true);
            Object record = getActivityClientRecordMethod.invoke(activityThread, 
                    Constants.LOADER_ACTIVITY_CLASS);
            return record != null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            XLog.d(TAG, "Check loader activity loaded failed", e);
            return false;
        }
    }
}
