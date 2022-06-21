package com.huangjs.amap;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class AMapMarkerManager extends ViewGroupManager<AMapMarker> {
  private static final String REACT_CLASS = "AMap.Marker";
  private static final int SHOW_INFO_WINDOW = 0;
  private static final int HIDE_INFO_WINDOW = 1;
  private static final int ANIMATE_TO_COORDINATE = 2;
  private final ReactApplicationContext reactAppContext;

  public AMapMarkerManager(ReactApplicationContext context) {
    this.reactAppContext = context;
  }

  @NonNull
  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @NonNull
  @Override
  public AMapMarker createViewInstance(@NonNull ThemedReactContext themedContext) {
    return new AMapMarker(themedContext, reactAppContext);
  }

  @Override
  public void onDropViewInstance(@NonNull AMapMarker view) {
    super.onDropViewInstance(view);
  }

  @Override
  public void addView(AMapMarker view, View child, int index) {
    super.addView(view, child, index);
  }

  @Override
  public void removeViewAt(AMapMarker view, int index) {
    super.removeViewAt(view, index);
  }

  @Override
  public Map getExportedCustomDirectEventTypeConstants() {
    Map export = super.getExportedCustomDirectEventTypeConstants();
    if (export == null) {
      export = MapBuilder.newHashMap();
    }
    export.put("onClick", MapBuilder.of("registrationName", "onClick"));
    export.put("onInfoWindowClick", MapBuilder.of("registrationName", "onInfoWindowClick"));
    export.put("onDragStart", MapBuilder.of("registrationName", "onDragStart"));
    export.put("onDrag", MapBuilder.of("registrationName", "onDrag"));
    export.put("onDragEnd", MapBuilder.of("registrationName", "onDragEnd"));
    return export;
  }

  @Override
  public Map<String, Integer> getCommandsMap() {
    return MapBuilder.of(
      "showInfoWindow", SHOW_INFO_WINDOW,
      "hideInfoWindow", HIDE_INFO_WINDOW,
      "animateToCoordinate", ANIMATE_TO_COORDINATE
    );
  }

  @Override
  public void receiveCommand(@NonNull AMapMarker view, String commandId, @Nullable ReadableArray args) {
    int commandIdInt = Integer.parseInt(commandId);
    switch (commandIdInt) {
      case SHOW_INFO_WINDOW:
        if (view.getMarker() != null) {
          view.getMarker().showInfoWindow();
        }
        break;

      case HIDE_INFO_WINDOW:
        if (view.getMarker() != null) {
          view.getMarker().hideInfoWindow();
        }
        break;

      case ANIMATE_TO_COORDINATE:
        if (args != null)
          view.animateToCoordinate(Types.mapToLatLng(args.getMap(0)), args.getInt(1));
        break;
    }
  }

  @ReactProp(name = "title")
  public void setTitle(AMapMarker view, String title) {
    view.getMarkerOptions().title(title);
    view.updateMarker();
  }

  @ReactProp(name = "description")
  public void setDescription(AMapMarker view, String description) {
    view.getMarkerOptions().snippet(description);
    view.updateMarker();
  }

  @ReactProp(name = "coordinate")
  public void setPosition(AMapMarker view, ReadableMap coordinate) {
    view.getMarkerOptions().position(Types.mapToLatLng(coordinate));
    view.updateMarker();
  }

  @ReactProp(name = "flat")
  public void setFlat(AMapMarker view, boolean flat) {
    view.getMarkerOptions().setFlat(flat);
    view.updateMarker();
  }

  @ReactProp(name = "opacity")
  public void setOpacity(AMapMarker view, float opacity) {
    view.getMarkerOptions().alpha(opacity);
    view.updateMarker();
  }

  @ReactProp(name = "rotateAngle")
  public void rotateAngle(AMapMarker view, float angle) {
    view.getMarkerOptions().rotateAngle(angle);
    view.updateMarker();
  }

  @ReactProp(name = "draggable")
  public void setDraggable(AMapMarker view, boolean draggable) {
    view.getMarkerOptions().draggable(draggable);
    view.updateMarker();
  }

  @ReactProp(name = "zIndex")
  public void setIndex(AMapMarker view, float zIndex) {
    view.getMarkerOptions().zIndex(zIndex);
    view.updateMarker();
  }

  @ReactProp(name = "visible")
  public void setVisible(AMapMarker view, boolean visible) {
    view.getMarkerOptions().visible(visible);
    view.updateMarker();
  }

  @ReactProp(name = "anchor")
  public void setAnchor(AMapMarker view, ReadableMap anchor) {
    view.getMarkerOptions().anchor((float) anchor.getDouble("x"), (float) anchor.getDouble("y"));
    view.updateMarker();
  }

  @ReactProp(name = "icon")
  public void setIcon(AMapMarker view, ReadableMap icon) {
    view.setIcon(icon);
  }

  @ReactProp(name = "infoWindowEnable")
  public void setInfoWindowEnable(AMapMarker view, boolean enable) {
    view.getMarkerOptions().infoWindowEnable(enable);
    view.updateMarker();
  }

  @ReactProp(name = "infoWindowOffset")
  public void setInfoWindowOffset(AMapMarker view, ReadableMap offset) {
    view.getMarkerOptions().setInfoWindowOffset(offset.getInt("x"), offset.getInt("y"));
    view.updateMarker();
  }
}
