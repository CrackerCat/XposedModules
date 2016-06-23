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

  private static final String[] ENTRY_CLASSES = {
      "com.qihoo.util.StubApplication",
      "com.tencent.StubShell.TxAppEntry",
      "com.seworks.medusah.app"};

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
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                  Context context = (Context) param.args[0];
                  Log.d(TAG, String.format("%s(%d) is protected by %s.", packageName, Process.myPid(), className));
                  Log.d(TAG, String.format("Waiting for %ds.", COUNTDOWN));
                  SystemClock.sleep(COUNTDOWN * 1000);
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
}
