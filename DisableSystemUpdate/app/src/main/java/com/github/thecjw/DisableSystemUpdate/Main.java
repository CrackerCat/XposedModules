package com.github.thecjw.DisableSystemUpdate;

import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by N on 2016/1/8.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "DisableSystemUpdate";
  private static final String PACKAGE_NAME = "com.google.android.gms";

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam)
      throws Throwable {

    if (loadPackageParam.processName.equals(PACKAGE_NAME)) {
      Log.d(TAG, PACKAGE_NAME + " is Loading..");
      try {
        Class<?> classSystemUpdateService = loadPackageParam.classLoader.loadClass("com.google.android.gms.update.SystemUpdateService");
        Field field = classSystemUpdateService.getDeclaredField("b");
        field.setAccessible(true);
        field.set(null, true);
      } catch (Exception e) {
        // pass
      }
    }
  }
}
