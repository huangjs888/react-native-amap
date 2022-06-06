package com.huangjs.amap;

import android.animation.TypeEvaluator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;
import android.animation.ObjectAnimator;
import android.util.Property;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.Map;

public class AMapMarker extends AMapOverlay {
  private final MarkerOptions markerOptions = new MarkerOptions();
  private final Map<String, Float> ICON_TYPES = MapBuilder.of(
    "azure", BitmapDescriptorFactory.HUE_AZURE,
    "blue", BitmapDescriptorFactory.HUE_BLUE,
    "cyan", BitmapDescriptorFactory.HUE_CYAN,
    "green", BitmapDescriptorFactory.HUE_GREEN,
    "magenta", BitmapDescriptorFactory.HUE_MAGENTA,
    "red", BitmapDescriptorFactory.HUE_RED,
    "rose", BitmapDescriptorFactory.HUE_ROSE
  );
  private Marker marker = null;

  public AMapMarker(ThemedReactContext themedContext, ReactApplicationContext reactAppContext) {
    super(themedContext);
    ICON_TYPES.putAll(MapBuilder.of(
      "violet", BitmapDescriptorFactory.HUE_VIOLET,
      "orange", BitmapDescriptorFactory.HUE_ORANGE,
      "yellow", BitmapDescriptorFactory.HUE_YELLOW));
  }

  public Marker getMarker() {
    return marker;
  }

  public MarkerOptions getMarkerOptions() {
    return markerOptions;
  }

  public void updateMarker() {
    if (marker != null) {
      marker.setMarkerOptions(fillMarkerOptions(new MarkerOptions()));
    }
  }

  public void setIcon(ReadableMap icon) {
    if (icon == null) return;
    String uri = icon.getString("uri");
    int width = icon.hasKey("width") ? icon.getInt("width") : 24;
    int height = icon.hasKey("height") ? icon.getInt("height") : 24;
    if (uri != null && (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("data:"))) {
      // 请求图片资源
      ImageRequestBuilder irb = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri));
      irb.setPostprocessor(new BasePostprocessor() {
        @Override
        public void process(@NonNull Bitmap bitmap) {
          Bitmap combinedBitmap = Bitmap.createBitmap(width, height, bitmap.getConfig());
          Canvas canvas = new Canvas(combinedBitmap);
          canvas.drawBitmap(bitmap, 0, 0, null);
          markerOptions.icon(BitmapDescriptorFactory.fromBitmap(combinedBitmap));
          updateMarker();
        }
      });
      irb.setResizeOptions(new ResizeOptions(width, height));
      ImageRequest imageRequest = irb.build();
      Fresco.getImagePipeline().fetchDecodedImage(imageRequest, this);
    } else {
      // 使用内置或默认的
      BitmapDescriptorFactory.defaultMarker();
      Object markerType = ICON_TYPES.get(uri);
      markerOptions.icon(BitmapDescriptorFactory.defaultMarker(markerType == null ? BitmapDescriptorFactory.HUE_AZURE : (float) markerType));
      updateMarker();
    }
  }


  @Override
  public void added(View view) {
    AMap map = ((AMapView) view).getMap();
    marker = map.addMarker(fillMarkerOptions(new MarkerOptions()));
  }

  @Override
  public void removed(View view) {
    if (marker != null) {
      marker.destroy();
    }
    marker = null;
  }

  public View getInfoWindow() {
    return null;
  }

  public View getInfoContents() {
    return null;
  }

  public void animateToCoordinate(LatLng finalPosition, int duration) {
    if (finalPosition == null) return;
    TypeEvaluator<LatLng> typeEvaluator = (f, a, b) -> new LatLng((b.latitude - a.latitude) * f + a.latitude, (b.longitude - a.longitude) * f + a.longitude);
    Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
    ObjectAnimator animator = ObjectAnimator.ofObject(
      marker,
      property,
      typeEvaluator,
      finalPosition);
    animator.setDuration(duration);
    animator.start();
  }

  private MarkerOptions fillMarkerOptions(MarkerOptions options) {
    options.title(markerOptions.getTitle());
    options.snippet(markerOptions.getSnippet());
    options.rotateAngle(markerOptions.getRotateAngle());
    options.position(markerOptions.getPosition());
    options.setFlat(markerOptions.isFlat());
    options.alpha(markerOptions.getAlpha());
    options.draggable(markerOptions.isDraggable());
    options.zIndex(markerOptions.getZIndex());
    options.visible(markerOptions.isVisible());
    options.infoWindowEnable(markerOptions.isInfoWindowEnable());
    options.setInfoWindowOffset(markerOptions.getInfoWindowOffsetX(), markerOptions.getInfoWindowOffsetY());
    options.anchor(markerOptions.getAnchorU(), markerOptions.getAnchorV());
    options.icon(markerOptions.getIcon());
    return options;
  }

}
