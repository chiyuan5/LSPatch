package org.lsposed.lspatch.loader;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.lsposed.lspatch.loader.util.XLog;

public class LoaderActivityStarter {
    private static final String TAG = "LSPatch/LoaderStarter";
    // 直接使用字符串定义目标Activity的完整类名
    private static final String LOADER_ACTIVITY_CLASS = "org.lsposed.lspatch.loader.LoaderActivity";

    public static void startLoaderActivity() {
        try {
            ActivityThread activityThread = ActivityThread.currentActivityThread();
            if (activityThread == null) {
                XLog.e(TAG, "ActivityThread is null");
                return;
            }

            // 通过反射获取Application实例
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

            // 启动LoaderActivity（直接使用字符串类名）
            Class<?> loaderActivityClass = Class.forName(LOADER_ACTIVITY_CLASS);
            Intent intent = new Intent();
            intent.setClass(app, loaderActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(intent);

            XLog.i(TAG, "LoaderActivity started successfully");
        } catch (ClassNotFoundException e) {
            XLog.e(TAG, "LoaderActivity class not found: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            XLog.e(TAG, "mInitialApplication field not found: " + e.getMessage());
        } catch (IllegalAccessException e) {
            XLog.e(TAG, "Failed to access mInitialApplication field: " + e.getMessage());
        } catch (Exception e) {
            XLog.e(TAG, "Failed to start LoaderActivity: " + e.getMessage());
        }
    }

    private static boolean isLoaderActivityLoaded(ActivityThread activityThread) {
        try {
            // 检查Activity是否已在ActivityThread中注册
            Method getActivityClientRecordMethod = ActivityThread.class.getDeclaredMethod(
                    "getActivityClientRecord", String.class);
            getActivityClientRecordMethod.setAccessible(true);
            Object record = getActivityClientRecordMethod.invoke(activityThread, 
                    LOADER_ACTIVITY_CLASS); // 使用字符串类名
            return record != null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            XLog.d(TAG, "Check loader activity loaded failed: " + e.getMessage());
            return false;
        }
    }
}
    
