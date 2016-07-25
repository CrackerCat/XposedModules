package com.github.thecjw.WaitForDebugger;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/2/3.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "WaitForDebugger";
  private static final int COUNTDOWN = 10;

  private static final String[] ENTRY_CLASSES = {
      "com.qihoo.util.StubApplication",
      "com.tencent.StubShell.TxAppEntry",
      "com.seworks.medusah.app",
      "com.shell.SuperApplication"};

  private String packageName = "";
  private String processName = "";

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

    packageName = loadPackageParam.packageName;
    processName = loadPackageParam.processName;

    if (packageName.equals(processName)) {

      for (final String className : ENTRY_CLASSES) {
        try {
          Class<?> entryClass = loadPackageParam.classLoader.loadClass(className);

          findAndHookMethod(entryClass,
              "attachBaseContext",
              Context.class,
              new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                  try {
                    Object dexmon = findDexMon();
                    if (dexmon != null) {
                      Class<?> clazz_dexmon = dexmon.getClass();
                      Field field_instance = clazz_dexmon.getField("DE3Instance");
                      Object de3 = field_instance.get(dexmon);
                      Class<?> clazz_de3 = de3.getClass();
                      Method method_init = clazz_de3.getMethod("init");
                      method_init.invoke(de3);
                    }
                  } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                  }

                  Log.d(TAG, String.format("%s(%d) is protected by %s.", packageName, Process.myPid(), className));
                  Log.d(TAG, String.format("Waiting for %ds.", COUNTDOWN));
                  // SystemClock.sleep(COUNTDOWN * 1000);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                }
              });
          // Exit loop
          break;

        } catch (Exception e) {
          //
        }
      }
    }
  }

  private Object findDexMon() {
    Object result = null;
    try {
      Field fieldLoadedPackageCallbacks = XposedBridge.class.getDeclaredField("sLoadedPackageCallbacks");
      fieldLoadedPackageCallbacks.setAccessible(true);
      XposedBridge.CopyOnWriteSortedSet<XC_LoadPackage> loadedPackageCallbacks =
          (XposedBridge.CopyOnWriteSortedSet<XC_LoadPackage>) fieldLoadedPackageCallbacks.get(null);

      for (Object callback : loadedPackageCallbacks.getSnapshot()) {
        Class<?> clazz = callback.getClass();
        Field field_instance = clazz.getDeclaredField("instance");
        field_instance.setAccessible(true);
        Object temp = field_instance.get(callback);
        if (temp.getClass().getName().equals("com.github.thecjw.DexMon.Main")) {
          result = temp;
          break;
        }
      }
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
    return result;
  }
}
