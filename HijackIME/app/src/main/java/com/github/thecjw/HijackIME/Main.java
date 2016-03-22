package com.github.thecjw.HijackIME;

import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/2/2.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "HijackIME";

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
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            context = (Context) param.args[0];
          }
        });

    if (packageName.equals("com.tencent.qqpinyin")) {

      findAndHookMethod("com.tencent.qqpinyin.QQPYInputMethodService",
          loadPackageParam.classLoader,
          "sendCommitStr",
          CharSequence.class,
          int.class,
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              CharSequence chars = (CharSequence) param.args[0];
              Log.d(TAG, "sendCommitStr -> " + chars);
            }
          });
    }
  }
}

