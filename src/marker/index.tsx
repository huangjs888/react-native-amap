/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-12 17:46:14
 * @Description: ******
 */

import React, {
  useRef,
  useImperativeHandle,
  forwardRef,
  type ElementRef,
  type ForwardedRef,
} from 'react';
import {
  findNodeHandle,
  requireNativeComponent,
  type ViewProps,
  type NativeSyntheticEvent,
} from 'react-native';
import { invoke } from '../utils';
import type { LatLng, Icon, Point, PointPosition } from '../types';

export interface MarkerProps extends ViewProps {
  /**
   * marker坐标，android 不能用 position 作为属性，会发生冲突，也是个蛋疼的问题
   */
  coordinate: LatLng;

  /**
   * 图标
   */
  icon?: Icon;

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
  onPress?: (e: NativeSyntheticEvent<PointPosition>) => void;

  /**
   * 作为信息窗口的时候的点击事件
   */
  onInfoWindowPress?: (e: NativeSyntheticEvent<PointPosition>) => void;
  /**
   * 拖放开始事件
   */
  onDragStart?: (e: NativeSyntheticEvent<PointPosition>) => void;

  /**
   * 拖放进行事件，类似于 mousemove，在结束之前会不断调用
   */
  onDrag?: (e: NativeSyntheticEvent<PointPosition>) => void;

  /**
   * 拖放结束事件
   */
  onDragEnd?: (e: NativeSyntheticEvent<PointPosition>) => void;
}

export type MarkerRef = {
  getId: () => number | null;
} & {
  [key: string]: (...args: any[]) => void;
};

const Marker = forwardRef(
  (props: MarkerProps, ref: ForwardedRef<MarkerRef>) => {
    const myRef = useRef<ElementRef<typeof NativeMarker> | null>(null);
    useImperativeHandle(
      ref,
      (): MarkerRef => {
        const invokeMethod = (name: string, args: any[]) =>
          invoke(myRef.current, 'AMap.Marker', name, args);
        const handle: any = {
          getId: () => findNodeHandle(myRef.current),
        };
        ['showInfoWindow', 'hideInfoWindow', 'animateToCoordinate'].forEach(
          (key: string) =>
            (handle[key] = (...args: any[]) => invokeMethod(key, [...args])),
        );
        return handle;
      },
      [],
    );

    return <NativeMarker {...props} ref={myRef} />;
  },
);

type NativeProps = MarkerProps;

const NativeMarker = requireNativeComponent<NativeProps>('AMap.Marker');

export default Marker;
