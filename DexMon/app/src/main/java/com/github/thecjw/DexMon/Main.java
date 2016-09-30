package com.github.thecjw.DexMon;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/2/2.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "DexMon";

  private String packageName = "";
  private String processName = "";
  private Context context;

  public static DE3 DE3Instance = DE3.INSTANCE;

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
            resolveAllClasses(XposedBridge.BOOTCLASSLOADER);
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

            resolveAllClasses(_this);
          }
        });
  }

  private void resolveAllClasses(ClassLoader classLoader) {

    Log.d(TAG, String.format("Current classloader: %s", classLoader.getClass().getName()));

    try {
      Object pathList = XposedHelpers.getObjectField(classLoader, "pathList");
      Object dexElements = XposedHelpers.getObjectField(pathList, "dexElements");
      int dexElementsLength = Array.getLength(dexElements);

      for (int i = 0; i < dexElementsLength; i++) {
        Object dexElement = Array.get(dexElements, i);
        Object dexFile = XposedHelpers.getObjectField(dexElement, "dexFile");
        Object cookie = XposedHelpers.getObjectField(dexFile, "mCookie");
        String dexFileName = (String) XposedHelpers.getObjectField(dexFile, "mFileName");

        Log.d(TAG, String.format("DexFile: %s, %08x", dexFileName, cookie));

        String[] classNames = (String[]) XposedHelpers.callMethod(dexFile, "getClassNameList", cookie);

        for (String className : classNames) {
          try {
            classLoader.loadClass(className);
          } catch (Exception e) {
            Log.e(TAG, String.format("  Resolve %s failed, classLoader: %s",
                className, classLoader.getClass().getName()));
          }
        }
      }

    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }
}

