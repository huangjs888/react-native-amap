package com.huangjs.amap;

import android.view.View;

import com.amap.api.maps.model.LatLng;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.HashMap;

public class AMapMesh extends AMapOverlay {

    private SubMesh mesh = null;
    private LatLng position = null;
    private HashMap<String, Object> meshData = null;

    public AMapMesh(ThemedReactContext themedContext, ReactApplicationContext reactAppContext) {
        super(themedContext);
    }

    public void setPosition(LatLng latLng) {
        position = latLng;
        if (mesh != null) {
            mesh.setPosition(latLng);
        }
    }

    public void setData(ReadableMap data) {
        meshData = parseMeshData(data);
        if (mesh != null && meshData != null) {
            mesh.setData((float[]) meshData.get("vertices"),
                    (float[]) meshData.get("vertexColors"),
                    (int[]) meshData.get("faces"));
        }
    }

    private HashMap<String, Object> parseMeshData(ReadableMap data) {
        if (data == null) return null;
        ReadableArray verticesList = data.getArray("vertices");
        ReadableArray colorsList = data.getArray("vertexColors");
        ReadableArray facesList = data.getArray("faces");
        float[] vertices = new float[0];
        if (verticesList != null) {
            vertices = new float[verticesList.size()];
            for (int i = 0, len = verticesList.size(); i < len; i++) {
                vertices[i] = (float) verticesList.getDouble(i);
            }
        }
        float[] vertexColors = new float[0];
        if (colorsList != null) {
            vertexColors = new float[colorsList.size()];
            for (int i = 0, len = colorsList.size(); i < len; i++) {
                vertexColors[i] = (float) colorsList.getDouble(i);
            }
        }
        int[] faces = new int[0];
        if (facesList != null) {
            faces = new int[facesList.size()];
            for (int i = 0, len = facesList.size(); i < len; i++) {
                faces[i] = facesList.getInt(i);
            }
        }
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("vertices", vertices);
        dataMap.put("vertexColors", vertexColors);
        dataMap.put("faces", faces);
        return dataMap;
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void added(View view) {
        MeshRenderer openglRender = ((AMapView) view).getOpenglRender();
        if (meshData != null) {
            mesh = openglRender.addMesh(
                    position,
                    (float[]) meshData.get("vertices"),
                    (float[]) meshData.get("vertexColors"),
                    (int[]) meshData.get("faces")
            );
        } else {
            mesh = openglRender.addMesh(position);
        }

    }

    @Override
    public void removed(View view) {
        MeshRenderer openglRender = ((AMapView) view).getOpenglRender();
        openglRender.removeMesh(mesh);
        mesh = null;
    }

    public void onDestroy() {
        if (mesh != null) {
            mesh.destroy();
        }
        mesh = null;
    }
}
