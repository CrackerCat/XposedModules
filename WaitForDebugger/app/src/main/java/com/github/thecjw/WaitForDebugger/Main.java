package com.github.thecjw.WaitForDebugger;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/2/3.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "WaitForDebugger";
  private static final int COUNTDOWN = 10;
  private String packageName = "";
  private String processName = "";

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

    packageName = loadPackageParam.packageName;
    processName = loadPackageParam.processName;

    try {
      // No special reason.
      if (packageName.equals(processName)) {
        // for libjiagu v1.0.5.0 ~ v1.0.9.0
        Class<?> qihooEntryClass = loadPackageParam.classLoader.loadClass("com.qihoo.util.StubApplication");
        // Hook protected void attachBaseContext(Context arg7)
        findAndHookMethod(qihooEntryClass,
            "attachBaseContext",
            Context.class,
            new XC_MethodHook() {
              @Override
              protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.args[0];
                Log.d(TAG, String.format("%s(%d) is protected by libjiagu.", packageName, Process.myPid()));
                Log.d(TAG, String.format("Waiting for %ds.", COUNTDOWN));
                SystemClock.sleep(COUNTDOWN * 1000);
              }
            });
      }

    } catch (Exception e) {
      // Ignore.
    }
  }
}
