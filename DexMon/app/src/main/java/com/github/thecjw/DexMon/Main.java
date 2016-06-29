package com.github.thecjw.DexMon;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

/**
 * Created by N on 2016/2/2.
 */
public class Main implements IXposedHookLoadPackage {

  private static final String TAG = "DexMon";

  private String packageName = "";
  private String processName = "";
  private Context context;

  // dalvik.system.BaseDexClassLoader$pathList
  private Field fieldPathList;
  // [Ldalvik.system.DexPathList$Element;$dexElements
  private Field fieldDexElements;
  // dalvik.system.DexPathList$Element
  private Field fieldDexFile;
  //
  private Field fieldmCookie;
  //
  private Field fieldmFileName;

  private Method methodGetClassNameList;


  @Override
  public void handleLoadPackage(LoadPackageParam loadPackageParam)
      throws Throwable {

    packageName = loadPackageParam.packageName;
    processName = loadPackageParam.processName;

    // TODO: Load so and patch _Z16dvmOptimizeClassP11ClassObjectb and _Z17dvmUpdateCodeUnitPK6MethodPtt

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

    findAndHookConstructor("dalvik.system.BaseDexClassLoader",
        loadPackageParam.classLoader,
        String.class,       // dexPath
        File.class,         // optimizedDirectory
        String.class,       // libraryPath
        ClassLoader.class,  // parent
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param)
              throws Throwable {
            String dexPath = (String) param.args[0];
            File optimizedDirectory = (File) param.args[1];
            String libraryPath = (String) param.args[2];
            ClassLoader parent = (ClassLoader) param.args[3];

            ClassLoader _this = (ClassLoader) param.thisObject;

            // Log.d(TAG, String.format("Loader: %s, DexFile: %s", _this.getClass().getName(), dexPath));

            resolveAllClasses(_this);
          }
        });
  }

  private void resolveAllClasses(ClassLoader classLoader) {

    Log.d(TAG, String.format("Current classloader: %s", classLoader.getClass().getName()));

    try {
      methodGetClassNameList = getMethod(Class.forName("dalvik.system.DexFile"), "getClassNameList");
      fieldPathList = getField(Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");

      Object pathList = fieldPathList.get(classLoader);
      fieldDexElements = getField(pathList.getClass(), "dexElements");
      Object dexElements = fieldDexElements.get(pathList);

      int dexElementsLength = Array.getLength(dexElements);
      Log.d(TAG, String.format(" Loaded dex files: %d", dexElementsLength));

      for (int i = 0; i < dexElementsLength; i++) {
        Object dexElement = Array.get(dexElements, i);
        fieldDexFile = getField(dexElement.getClass(), "dexFile");
        Object dexFile = fieldDexFile.get(dexElement);

        fieldmCookie = getField(dexFile.getClass(), "mCookie");
        Object mCookie = fieldmCookie.get(dexFile);

        fieldmFileName = getField(dexFile.getClass(), "mFileName");
        String fileName = (String)fieldmFileName.get(dexFile);

        String[] classNames = (String[]) methodGetClassNameList.invoke(dexFile, mCookie);
        Log.d(TAG, String.format("  %d): %s, Classes: %d", i, fileName, classNames.length));

        // resolve all.
        for (String className : classNames) {
          try {
            classLoader.loadClass(className);
          } catch (Exception e) {
            Log.e(TAG, String.format("  Resolve %s failed, classLoader: %s", className, classLoader.getClass().getName()));
          }
        }
      }

    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }

  private static Field getField(Class<?> clazz, String fieldName)
      throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field;
  }

  private static Method getMethod(Class<?> clazz, String methodName)
      throws NoSuchMethodException {
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.getName().equals(methodName)) {
        method.setAccessible(true);
        return method;
      }
    }
    return null;
  }
}

