import android.app.Activity;
import android.app.ActivityThread;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import dalvik.system.DexClassLoader;

public class LoaderActivityStarter {
    private static final String TAG = "LoaderActivityStarter";
    private static final String LOADER_DEX_PATH = "path/to/loader.dex"; // loader.dex的实际路径
    private static final String TARGET_ACTIVITY_CLASS = "org.lsposed.lspatch.loader.LoaderActivity"; // 目标Activity完整类名

    public static void startLoaderActivity() {
        try {
            // 1. 获取当前应用的上下文
            ActivityThread activityThread = ActivityThread.currentActivityThread();
            ApplicationInfo appInfo = activityThread.getApplication().getApplicationInfo();
            File dexOutputDir = new File(appInfo.dataDir, "dex");
            if (!dexOutputDir.exists()) {
                dexOutputDir.mkdirs();
            }

            // 2. 加载loader.dex（使用DexClassLoader）
            DexClassLoader dexClassLoader = new DexClassLoader(
                    LOADER_DEX_PATH,
                    dexOutputDir.getAbsolutePath(),
                    null,
                    LoaderActivityStarter.class.getClassLoader()
            );

            // 3. 反射获取目标Activity类
            Class<?> loaderActivityClass = dexClassLoader.loadClass(TARGET_ACTIVITY_CLASS);

            // 4. 构建启动Intent
            Intent intent = new Intent();
            intent.setClass(activityThread.getApplication(), loaderActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 非Activity环境需添加此标志

            // 5. 启动Activity
            activityThread.getApplication().startActivity(intent);
            Log.i(TAG, "LoaderActivity started successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to start LoaderActivity", e);
        }
    }
}
