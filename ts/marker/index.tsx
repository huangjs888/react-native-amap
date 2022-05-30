/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-26 12:16:45
 * @Description: ******
 */

import React, { useRef, useImperativeHandle, forwardRef } from 'react';
import {
  ImageSourcePropType,
  NativeSyntheticEvent,
  requireNativeComponent,
  ViewProps,
  findNodeHandle,
} from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';
import { invoke } from '../utils';
import type { Point, LatLng } from '../types';
import type { PositionEvent } from '../mapview';

export interface MarkerProps extends ViewProps {
  /**
   * marker坐标，android 不能用 position 作为属性，会发生冲突，也是个蛋疼的问题
   */
  coordinate: LatLng;

  /**
   * 图标
   */
  icon?: ImageSourcePropType;

  /**
   * marker标题
   */
  title?: string;

  /**
   * marker内容描述
   */
  description?: string;

  /**
   * 是否平贴地图
   *
   * @platform android
   */
  flat?: boolean;

  /**
   * 透明度 [0, 1]
   *
   * @platform android
   */
  opacity?: number;

  /**
   * 可见度
   */
  visible?: boolean;

  /**
   * 旋转角度
   */
  rotateAngle?: number;

  /**
   * 是否可拖拽
   */
  draggable?: boolean;

  /**
   * 层级
   */
  zIndex?: number;

  /**
   * 覆盖物锚点比例
   *
   * @platform android
   */
  anchor?: Point;

  /**
   * marker是否作为信息窗口
   */
  infoWindowEnable?: boolean;

  /**
   * marker作为信息窗口的时候的偏移量
   */
  infoWindowOffset?: Point;

  /**
   * 点击事件
   */
  onClick?: (event: NativeSyntheticEvent<PositionEvent>) => void;

  /**
   * 作为信息窗口的时候的点击事件
   */
  onInfoWindowClick?: (event: NativeSyntheticEvent<PositionEvent>) => void;

  /**
   * 拖放开始事件
   */
  onDragStart?: (event: NativeSyntheticEvent<PositionEvent>) => void;

  /**
   * 拖放进行事件，类似于 mousemove，在结束之前会不断调用
   */
  onDrag?: (event: NativeSyntheticEvent<PositionEvent>) => void;

  /**
   * 拖放结束事件
   */
  onDragEnd?: (event: NativeSyntheticEvent<PositionEvent>) => void;
}

const componentName = 'AMap.Marker';
const NativeMarker = requireNativeComponent<MarkerProps>(componentName);

export default forwardRef((props: MarkerProps, ref) => {
  const myRef = useRef(null);
  const { icon, ...restProps } = props;
  useImperativeHandle(
    ref,
    () => {
      const invokeMethod = (name: string, args?: any[]) =>
        invoke(myRef.current, componentName, name, args);
      const handle = {
        getId: () => findNodeHandle(myRef.current),
      };
      ['showInfoWindow', 'hideInfoWindow', 'animateToCoordinate'].forEach(
        (key) => (handle[key] = (...args) => invokeMethod(key, [...args])),
      );
      return handle;
    },
    [],
  );
  return (
    <NativeMarker
      {...restProps}
      ref={myRef}
      icon={!icon ? undefined : resolveAssetSource(icon)}
    />
  );
});
