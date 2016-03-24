package com.github.thecjw.DexMon;

import android.content.Context;
import android.util.Log;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

/**
 * Created by N on 2016/2/2.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "DexMon";

  private String packageName = "";
  private String processName = "";
  private Context context;

  @Override
  public void handleLoadPackage(LoadPackageParam loadPackageParam)
      throws Throwable {

    packageName = loadPackageParam.packageName;
    processName = loadPackageParam.processName;

    findAndHookMethod("android.app.Application",
        loadPackageParam.classLoader,
        "attach",
        Context.class,
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param)
              throws Throwable {
            context = (Context) param.args[0];
          }
        });

    findAndHookConstructor("dalvik.system.BaseDexClassLoader",
        loadPackageParam.classLoader,
        String.class,       // dexPath
        File.class,         // optimizedDirectory
        String.class,       // libraryPath
        ClassLoader.class,  // parent
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param)
              throws Throwable {
            String dexPath = (String) param.args[0];
            File optimizedDirectory = (File) param.args[1];
            String libraryPath = (String) param.args[2];
            ClassLoader parent = (ClassLoader) param.args[3];

            ClassLoader _this = (ClassLoader) param.thisObject;

            Log.d(TAG, String.format("Loader: %s, DexFile: %s", _this.getClass().getName(), dexPath));
          }
        });
  }
}

