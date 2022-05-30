package com.huangjs.amap;

import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class AMapMeshManager extends ViewGroupManager<AMapMesh> {
    private static final String REACT_CLASS = "AMap.Mesh";
    private final ReactApplicationContext reactAppContext;

    public AMapMeshManager(ReactApplicationContext context) {
        this.reactAppContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    public AMapMesh createViewInstance(@NonNull ThemedReactContext themedContext) {
        return new AMapMesh(themedContext, reactAppContext);
    }

    @Override
    public void onDropViewInstance(AMapMesh view) {
        view.onDestroy();
        super.onDropViewInstance(view);
    }

    @Override
    public void addView(AMapMesh view, View child, int index) {
        super.addView(view, child, index);
    }

    @Override
    public void removeViewAt(AMapMesh view, int index) {
        super.removeViewAt(view, index);
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        Map export = super.getExportedCustomDirectEventTypeConstants();
        if (export == null) {
            export = MapBuilder.newHashMap();
        }
        export.put("onClick", MapBuilder.of("registrationName", "onClick"));
        export.put("onLongClick", MapBuilder.of("registrationName", "onLongClick"));
        return export;
    }

    @ReactProp(name = "backOrFront")
    public void setBackOrFront(AMapMesh view, int backOrFront) {
        Mesh mesh = view.getMesh();
        if (mesh != null) {
            mesh.setBackOrFront(backOrFront);
        }
    }

    @ReactProp(name = "transparentEnabled", defaultBoolean = true)
    public void transparentEnabled(AMapMesh view, boolean enabled) {
        Mesh mesh = view.getMesh();
        if (mesh != null) {
            mesh.transparentEnabled(enabled);
        }
    }

    @ReactProp(name = "coordinate")
    public void setPosition(AMapMesh view, ReadableMap coordinate) {
        view.setPosition(Types.mapToLatLng(coordinate));
    }

    @ReactProp(name = "dataSource")
    public void setData(AMapMesh view, ReadableMap data) {
        view.setData(data);
    }
}
