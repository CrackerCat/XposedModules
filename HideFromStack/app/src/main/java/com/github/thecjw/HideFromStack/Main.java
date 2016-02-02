package com.github.thecjw.HideFromStack;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/2/2.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "HideFromStack";

  boolean checkCaller(StackTraceElement element) {
    // Remove Xposed from caller.
    if (element.getClassName().startsWith("de.robv.android.xposed")) {
      return true;
    }

    // Or dexposed.
    if (element.getClassName().startsWith("com.taobao.android.dexposed")) {
      return true;
    }

    // Even any classes/methods/sources contains "hook".
    if (element.toString().toLowerCase().contains("hook")) {
      return true;
    }

    return false;
  }

  @Override
  public void handleLoadPackage(LoadPackageParam loadPackageParam)
      throws Throwable {

    findAndHookMethod(Throwable.class,
        "getStackTrace",
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            StackTraceElement[] oldCallStack = (StackTraceElement[]) param.getResult();
            List<StackTraceElement> newCallStack = new ArrayList<>();
            for (StackTraceElement element : oldCallStack) {
              if (checkCaller(element)) {
                continue;
              }
              newCallStack.add(element);
            }

            param.setResult(newCallStack.toArray(new StackTraceElement[newCallStack.size()]));
          }
        });
  }
}

