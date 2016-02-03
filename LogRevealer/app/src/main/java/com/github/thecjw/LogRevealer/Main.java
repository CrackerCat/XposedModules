package com.github.thecjw.LogRevealer;

import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/1/29.
 */
public class Main implements IXposedHookLoadPackage {
  private static final String TAG = "LogRevealer";

  private String packageName = "";
  private String processName = "";
  private Context context;

  private void phase2() {

    if (packageName.equals("com.tencent.mobileqq")) {
      // Hook com.tencent.qphone.base.util.setUIN_REPORTLOG_LEVEL
      try {
        Class<?> class_QLog = context.getClassLoader().loadClass("com.tencent.qphone.base.util.QLog");
        findAndHookMethod(class_QLog,
            "setUIN_REPORTLOG_LEVEL",
            int.class,
            new XC_MethodHook() {
              @Override
              protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = 1000;
              }
            });

      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
      }
    }
  }

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

    packageName = loadPackageParam.packageName;
    processName = loadPackageParam.processName;

    findAndHookMethod("android.app.Application",
        loadPackageParam.classLoader,
        "attach",
        Context.class,
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            context = (Context) param.args[0];
            phase2();
          }
        });
  }
}
