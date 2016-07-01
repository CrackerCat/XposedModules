package com.github.thecjw.DexMon;

import android.util.Log;

/**
 * Created by N on 2016/7/1.
 */
@SuppressWarnings("JniMissingFunction")
public enum DE3 {

  INSTANCE;
  public final static String TAG = DE3.class.getSimpleName();

  DE3() {
  }

  public void init() {
    Log.d(TAG, "DE3 init.");

    try {
      System.load("/data/local/tmp/libde3.so");
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }

  public native void enum_loaded_dexfiles();
}
