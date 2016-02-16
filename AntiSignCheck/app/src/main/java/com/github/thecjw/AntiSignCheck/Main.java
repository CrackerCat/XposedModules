package com.github.thecjw.AntiSignCheck;

import android.content.pm.Signature;
import android.os.Parcel;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import ru.lanwen.verbalregex.VerbalExpression;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

/**
 * Created by N on 2016/2/16.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "AntiSignCheck";

  private String packageName = "";
  private String processName = "";

  private String pluginResourcePath = "";
  private byte[] signature;

  private void getPluginResourcePath() {
    try {
      InputStream inputStream = new FileInputStream("/proc/self/maps");
      InputStreamReader streamReader = new InputStreamReader(inputStream);
      BufferedReader reader = new BufferedReader(streamReader);
      String line;
      VerbalExpression testRegex = VerbalExpression.regex()
          .find("/data/app/com.github.thecjw.AntiSignCheck-")
          .digit()
          .then(".apk")
          .build();

      while ((line = reader.readLine()) != null) {
        if (testRegex.test(line)) {
          pluginResourcePath = testRegex.getText(line);
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean readSignatureFromPackage() {

    try {
      ZipFile zipFile = new ZipFile(new File(pluginResourcePath));
      ZipEntry entry = zipFile.getEntry(String.format("assets/certs/%s.bin", packageName));

      InputStream inputStream = zipFile.getInputStream(entry);
      signature = new byte[inputStream.available()];
      inputStream.read(signature);
      inputStream.close();

    } catch (IOException e) {
      Log.e(TAG, e.getMessage());
      return false;
    }

    return true;
  }

  @Override
  public void handleLoadPackage(LoadPackageParam loadPackageParam)
      throws Throwable {

    packageName = loadPackageParam.packageName;
    processName = loadPackageParam.processName;

    getPluginResourcePath();

    if (readSignatureFromPackage()) {
      findAndHookConstructor(Signature.class,
          Parcel.class,
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              Signature instance = (Signature) param.thisObject;
              try {
                Field field_mSignature = Signature.class.getDeclaredField("mSignature");
                field_mSignature.setAccessible(true);
                field_mSignature.set(instance, signature);
                Log.d(TAG, String.format("Replacing package cert in %s done.", packageName));
              } catch (Exception e) {
                Log.e(TAG, e.getMessage());
              }
            }
          });
    }
  }
}
