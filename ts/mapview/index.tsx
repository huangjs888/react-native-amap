/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-25 17:32:14
 * @Description: ******
 */

import React, {
  useRef,
  useState,
  useEffect,
  useMemo,
  useCallback,
  useImperativeHandle,
  forwardRef,
} from 'react';
import {
  NativeSyntheticEvent,
  requireNativeComponent,
  ViewProps,
  findNodeHandle,
} from 'react-native';
import { invoke } from '../utils';
import type {
  Point,
  LatLng,
  Location,
  CameraPosition,
  LatLngBounds,
  MapType,
} from '../types';

export interface MeshInfoEvent {
  /**
   * mesh对象三角形面的下标
   */
  faceIndex?: number;

  /**
   * 点坐标
   */
  projectionPoint?: Array<number>;

  /**
   * 视图id
   */
  meshViewId?: number;
}

export interface CameraPositionEvent {
  cameraPosition?: CameraPosition;
  latLngBounds?: LatLngBounds;
}

export interface PositionEvent {
  latLng?: LatLng;
  point?: Point;
}

export interface MeshInfoListEvent {
  meshInfoList?: Array<MeshInfoEvent>;
}

export interface AMapViewProps extends ViewProps {
  /**
   * 样式
   */
  style: any;

  /**
   * 地图类型
   */
  mapType?: MapType;

  /**
   * 初始相机位置
   */
  initialCameraPosition?: CameraPosition;

  /**
   * 相机位置
   */
  cameraPosition?: CameraPosition;

  /**
   * 是否显示当前定位
   */
  locationEnabled?: boolean;

  /**
   * 是否显示定位蓝点
   */
  locationIconEnabled?: boolean;

  /**
   * 是否显示定位按钮
   * @platform android
   */
  locationButtonEnabled?: boolean;

  /**
   * 是否显示室内地图
   */
  indoorViewEnabled?: boolean;

  /**
   * 是否显示3D建筑
   */
  buildingsEnabled?: boolean;

  /**
   * 是否显示标注
   */
  textEnabled?: boolean;

  /**
   * 是否显示指南针
   */
  compassEnabled?: boolean;

  /**
   * 是否显示放大缩小按钮
   *
   * @platform android
   */
  zoomControlsEnabled?: boolean;

  /**
   * 是否显示比例尺
   */
  scaleControlsEnabled?: boolean;

  /**
   * 是否显示路况
   */
  trafficEnabled?: boolean;

  /**
   * 最大缩放级别
   */
  maxZoom?: number;

  /**
   * 最小缩放级别
   */
  minZoom?: number;

  /**
   * 是否启用缩放手势，用于放大缩小
   */
  zoomGesturesEnabled?: boolean;

  /**
   * 是否启用滑动手势，用于平移
   */
  scrollGesturesEnabled?: boolean;

  /**
   * 是否启用旋转手势，用于调整方向
   */
  rotateGesturesEnabled?: boolean;

  /**
   * 是否启用倾斜手势，用于改变视角
   */
  tiltGesturesEnabled?: boolean;

  /**
   * 是否启用opengl图层的事件
   */
  openglEventEnabled?: boolean;

  /**
   * 设定定位的最小更新距离
   *
   * @platform ios
   */
  distanceFilter?: number;

  /**
   * 设定最小更新角度，默认为 1 度
   *
   * @platform ios
   */
  headingFilter?: number;

  /**
   * 地图初始化完成事件
   */
  onLoaded?: (event: NativeSyntheticEvent<void>) => void;

  /**
   * 点击事件
   */
  onClick?: (event: NativeSyntheticEvent<PositionEvent>) => void;

  /**
   * 长按事件
   */
  onLongClick?: (event: NativeSyntheticEvent<PositionEvent>) => void;

  /**
   * 地图状态改变事件，随地图状态变化不停地触发
   */
  onCameraMoving?: (event: NativeSyntheticEvent<CameraPositionEvent>) => void;

  /**
   * 地图状态改变事件，在停止变化后触发
   */
  onCameraChange?: (event: NativeSyntheticEvent<CameraPositionEvent>) => void;

  /**
   * 定位发生改变时触发
   */
  onLocationChange?: (event: NativeSyntheticEvent<Location>) => void;
}

const componentName = 'AMapView';
const NativeAMapView = requireNativeComponent<AMapViewProps>(componentName);

export default forwardRef((props: AMapViewProps, ref) => {
  const myRef = useRef(null);
  const resolveRef = useRef({});
  const { style, onLoaded, ...restProps } = props;
  const [loaded, setLoaded] = useState(false);
  const memoStyle = useMemo(() => {
    return {
      ...(style || {}),
      flex: 1,
      height: !loaded
        ? 1
        : style && style.height !== undefined
        ? style.height
        : '100%',
      width: !loaded
        ? 1
        : style && style.width !== undefined
        ? style.width
        : '100%',
    };
  }, [style, loaded]);
  const eventCallback = useCallback((e) => {
    const key = e.nativeEvent.trigger;
    if (resolveRef.current[key]) {
      resolveRef.current[key].resolve(e.nativeEvent);
    }
  }, []);
  useImperativeHandle(
    ref,
    () => {
      const invokeMethod = (name: string, args?: any[]) =>
        invoke(myRef.current, componentName, name, args);
      const handle = {
        getId: () => findNodeHandle(myRef.current),
      };
      [
        'getCameraPosition',
        'animateCameraPosition',
        'pointToCoordinate',
        'coordinateToPoint',
        'pickMeshInfoByPoint',
      ].forEach((key) => {
        handle[key] = (...args) => {
          invokeMethod(key, [...args]);
          return key === 'animateCameraPosition'
            ? undefined
            : new Promise(
                (resolve, reject) =>
                  (resolveRef.current[key] = { resolve, reject }),
              );
        };
      });
      return handle;
    },
    [],
  );
  useEffect(() => {
    // 无论如何也要在 1 秒后 setLoaded(true) ，防止 onLoad 事件不触发的情况下显示不正常
    const timeout = setTimeout(() => setLoaded(true), 2000);
    return () => clearTimeout(timeout);
  }, []);
  return (
    <NativeAMapView
      {...restProps}
      ref={myRef}
      style={memoStyle}
      onGetCameraPosition={eventCallback}
      onPointToCoordinate={eventCallback}
      onCoordinateToPoint={eventCallback}
      onPickMeshInfo={eventCallback}
      onLoaded={(e) => {
        // android 地图部分控件不显示的问题在重新 layout 之后会恢复正常。
        // 同时也能修复 ios 地图偶尔出现的 layout 异常
        setLoaded(true);
        onLoaded?.call(this, e);
      }}
    />
  );
});
