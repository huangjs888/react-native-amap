// @ts-nocheck
/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-07-27 15:15:07
 * @Description: ******
 */

import PropTypes from 'prop-types';
import React, {
  useRef,
  useState,
  useEffect,
  useMemo,
  useCallback,
  useImperativeHandle,
  forwardRef,
} from 'react';
import { requireNativeComponent, findNodeHandle } from 'react-native';
import { ViewPropTypes } from 'deprecated-react-native-prop-types';
import { invoke } from '../utils';
import { CameraPositionType, MapType } from '../types';

const AMapViewPropTypes = {
  ...ViewPropTypes,

  /**
   * 样式
   */
  style: ViewPropTypes.style,

  /**
   * 地图类型
   */
  mapType: PropTypes.oneOf(Object.values(MapType)),

  /**
   * 初始相机位置
   */
  initialCameraPosition: CameraPositionType,

  /**
   * 相机位置
   */
  cameraPosition: CameraPositionType,

  /**
   * 是否显示当前定位
   */
  locationEnabled: PropTypes.bool,

  /**
   * 是否显示定位蓝点
   */
  locationIconEnabled: PropTypes.bool,

  /**
   * 是否显示定位按钮
   * @platform android
   */
  locationButtonEnabled: PropTypes.bool,

  /**
   * 是否显示室内地图
   */
  indoorViewEnabled: PropTypes.bool,

  /**
   * 是否显示3D建筑
   */
  buildingsEnabled: PropTypes.bool,

  /**
   * 是否显示标注
   */
  textEnabled: PropTypes.bool,

  /**
   * 是否显示指南针
   */
  compassEnabled: PropTypes.bool,

  /**
   * 是否显示放大缩小按钮
   *
   * @platform android
   */
  zoomControlsEnabled: PropTypes.bool,

  /**
   * 是否显示比例尺
   */
  scaleControlsEnabled: PropTypes.bool,

  /**
   * 是否显示路况
   */
  trafficEnabled: PropTypes.bool,

  /**
   * 最大缩放级别
   */
  maxZoom: PropTypes.number,

  /**
   * 最小缩放级别
   */
  minZoom: PropTypes.number,

  /**
   * 是否启用缩放手势，用于放大缩小
   */
  zoomGesturesEnabled: PropTypes.bool,

  /**
   * 是否启用滑动手势，用于平移
   */
  scrollGesturesEnabled: PropTypes.bool,

  /**
   * 是否启用旋转手势，用于调整方向
   */
  rotateGesturesEnabled: PropTypes.bool,

  /**
   * 是否启用倾斜手势，用于改变视角
   */
  tiltGesturesEnabled: PropTypes.bool,

  /**
   * 是否启用opengl图层的事件
   */
  openglEventEnabled: PropTypes.bool,

  /**
   * 设定定位的最小更新距离
   *
   * @platform ios
   */
  distanceFilter: PropTypes.number,

  /**
   * 设定最小更新角度，默认为 1 度
   *
   * @platform ios
   */
  headingFilter: PropTypes.number,

  /**
   * 地图初始化完成事件
   */
  onInitialized: PropTypes.func,

  /**
   * 点击事件
   */
  onClick: PropTypes.func,

  /**
   * 长按事件
   */
  onLongClick: PropTypes.func,

  /**
   * 地图状态改变事件，随地图状态变化不停地触发
   */
  onCameraMoving: PropTypes.func,

  /**
   * 地图状态改变事件，在停止变化后触发
   */
  onCameraChange: PropTypes.func,

  /**
   * 定位发生改变时触发
   */
  onLocationChange: PropTypes.func,
};

const componentName = 'AMapView';
const NativeAMapView = requireNativeComponent(componentName);

const AMapView = forwardRef((props, ref) => {
  const myRef = useRef(null);
  const resolveRef = useRef({});
  const { style, onInitialized, ...restProps } = props;
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
    const { trigger: key, error, ...restData } = e.nativeEvent;
    if (resolveRef.current[key]) {
      if (error) {
        resolveRef.current[key].reject(new Error(error));
      } else {
        resolveRef.current[key].resolve(restData);
      }
    }
  }, []);
  useImperativeHandle(
    ref,
    () => {
      const invokeMethod = (name, args) =>
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
          return new Promise(
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
    const timeout = setTimeout(() => setLoaded(true), 1000);
    return () => clearTimeout(timeout);
  }, []);
  return (
    <NativeAMapView
      {...restProps}
      ref={myRef}
      style={memoStyle}
      onGetCameraPosition={eventCallback}
      onPointToCoordinate={eventCallback}
      onAnimateCameraPosition={eventCallback}
      onCoordinateToPoint={eventCallback}
      onPickMeshInfo={eventCallback}
      onLoad={(e) => {
        // android 地图部分控件不显示的问题在重新 layout 之后会恢复正常。
        // 同时也能修复 ios 地图偶尔出现的 layout 异常
        setLoaded(true);
        onInitialized && onInitialized.call(this, e);
      }}
    />
  );
});
AMapView.propTypes = AMapViewPropTypes;

export default AMapView;
