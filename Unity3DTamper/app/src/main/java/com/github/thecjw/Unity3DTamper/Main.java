package com.github.thecjw.Unity3DTamper;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by N on 2016/2/2.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "Unity3DTamper";
  private static final String LIB_NAME = "libroken.so";
  private static final String HIJACK_LIB_PATH = "/data/local/tmp/" + LIB_NAME;

  private String packageName = "";
  private String processName = "";
  private Context context;

  private static void copy(File src, File dst) throws IOException {
    FileInputStream inStream = new FileInputStream(src);
    FileOutputStream outStream = new FileOutputStream(dst);
    FileChannel inChannel = inStream.getChannel();
    FileChannel outChannel = outStream.getChannel();
    inChannel.transferTo(0, inChannel.size(), outChannel);
    inStream.close();
    outStream.close();
  }

  @Override
  public void handleLoadPackage(LoadPackageParam loadPackageParam)
      throws Throwable {

    packageName = loadPackageParam.packageName;
    processName = loadPackageParam.processName;

    try {
      loadPackageParam.classLoader.loadClass("com.unity3d.player.UnityPlayer");

      findAndHookConstructor("com.unity3d.player.UnityPlayer",
          loadPackageParam.classLoader,
          "android.content.ContextWrapper",
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                throws Throwable {
              Object arg0 = param.args[0]; // android.content.ContextWrapper
              // Log.d(TAG, String.format("Unity3D Game: %s (%s)", packageName, context.getFilesDir().getAbsolutePath()));
              if (context != null) {
                String targetPath = String.format("%s/%s", context.getFilesDir().getAbsolutePath(), LIB_NAME);
                try {
                  copy(new File(HIJACK_LIB_PATH), new File(targetPath));
                  System.load(targetPath);
                } catch (Exception e) {
                  Log.e(TAG, e.getMessage());
                }
              }
            }
          });
    } catch (Exception e) {
      //
    }

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
  }
}

