package com.huangjs.amap;

import android.content.Context;
import android.view.View;

import com.facebook.react.views.view.ReactViewGroup;

public abstract class AMapOverlay extends ReactViewGroup {
  public AMapOverlay(Context context) {
    super(context);
  }

  public abstract void added(View view);

  public abstract void removed(View view);

}
