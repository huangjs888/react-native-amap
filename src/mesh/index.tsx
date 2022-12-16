/*
 * @Author: Huangjs
 * @Date: 2022-05-19 16:27:41
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-15 17:39:42
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
import type {
  LatLng,
  MeshType,
  MeshData,
  MeshInfoPoint,
  MeshDataRequest,
  MeshDataColor,
} from '../types';

export interface MeshProps extends ViewProps {
  /**
   * mesh坐标，android 不能用 position 作为属性，会发生冲突，也是个蛋疼的问题
   * 坐标改变相当于将整个图层translate
   */
  coordinate?: LatLng;

  /**
   * 是否开启透明度
   */
  transparent?: boolean;

  /**
   * 前面，后面还是两面都渲染
   */
  backOrFront?: MeshType;

  /**
   * 绘制模式
   */
  drawMode?:
    | 'points'
    | 'lines'
    | 'line_strip'
    | 'line_loop'
    | 'triangles'
    | 'triangles_strip'
    | 'triangles_loop';

  /**
   * 深度测试
   */
  depthTest?: boolean;

  /**
   * 图层绕X旋转角度，正数为逆时针
   */
  rotateX?: number;

  /**
   * 图层绕Y旋转角度，正数为逆时针
   */
  rotateY?: number;

  /**
   * 图层绕Z旋转角度，正数为逆时针
   */
  rotateZ?: number;

  /**
   * 图层缩放角度
   */
  scale?: number;

  /**
   * 渲染数据，如果设置了request无需再设置dataSource，如设置会被request结果覆盖
   */
  dataSource?: MeshData | null;

  /**
   * 数据请求
   */
  request?: MeshDataRequest | null;

  /**
   * 数据请求后对值进行转换成颜色，该项设置颜色的值域，色域和透明度
   */
  valueDomain?: MeshDataColor | null;

  /**
   * 渲染结束事件
   */
  onRendered?: (
    e: NativeSyntheticEvent<{
      type: string;
      message?: string;
      position: LatLng;
    }>,
  ) => void;

  /**
   * 点击事件，仅当父组件AMapView设置了openglEventEnabled为true才会有效
   */
  onPress?: (e: NativeSyntheticEvent<MeshInfoPoint>) => void;

  /**
   * 长按事件，仅当父组件AMapView设置了openglEventEnabled为true才会有效
   */
  onLongPress?: (e: NativeSyntheticEvent<MeshInfoPoint>) => void;
}

export type MeshRef = {
  getId: () => number | null;
};
const Mesh = forwardRef((props: MeshProps, ref: ForwardedRef<MeshRef>) => {
  const myRef = useRef<ElementRef<typeof NativeMesh> | null>(null);
  useImperativeHandle(
    ref,
    (): MeshRef => ({
      getId: () => findNodeHandle(myRef.current),
    }),
    [],
  );
  return <NativeMesh {...props} ref={myRef} />;
});

type NativeProps = MeshProps;

const NativeMesh = requireNativeComponent<NativeProps>('AMapMesh');

export default Mesh;
