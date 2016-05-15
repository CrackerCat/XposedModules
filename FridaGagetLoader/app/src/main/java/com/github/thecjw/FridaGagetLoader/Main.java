package com.github.thecjw.FridaGagetLoader;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/5/15.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "FridaGagetLoader";
  private static final String PACKAGE_NAME = "com.gameley.runningman3.video.cm";

  private Context context;

  @Override
  public void handleLoadPackage(LoadPackageParam loadPackageParam)
      throws Throwable {

    if (loadPackageParam.packageName.equals(PACKAGE_NAME)) {
      Log.d(TAG, PACKAGE_NAME + " is Loading..");

      findAndHookMethod("android.app.Application",
          loadPackageParam.classLoader,
          "attach",
          Context.class,
          new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
              String sourceFile = "/sdcard/frida-gadget.so";

              String gadgetFileName = String.format("/data/data/%s/files/frida-gadget.so", PACKAGE_NAME);
              Log.d(TAG, gadgetFileName);

              try {
                File input = new File(sourceFile);
                final FileInputStream fileInputStream = new FileInputStream(input);
                final byte[] content = new byte[fileInputStream.available()];
                fileInputStream.read(content);
                fileInputStream.close();

                File output = new File(gadgetFileName);
                final FileOutputStream fileOutputStream = new FileOutputStream(output);
                fileOutputStream.write(content);
                fileOutputStream.flush();
                fileOutputStream.close();

                // Loading 32bit frida-gadget.so in Nexus 9 success, but failed on I9300I.
                // got error: java.lang.UnsatisfiedLinkError: dlopen failed: empty/missing DT_HASH in "frida-gadget.so" (built with --hash-style=gnu?)
                System.load(gadgetFileName);

              } catch (Exception e) {
                Log.e(TAG, e.getMessage());
              }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                throws Throwable {
              context = (Context) param.args[0];
            }
          });
    }
  }
}

