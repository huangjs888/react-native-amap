/*
 * @Author: Huangjs
 * @Date: 2022-05-19 16:27:41
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-26 09:26:01
 * @Description: ******
 */

import React, { useRef, useImperativeHandle, forwardRef } from 'react';
import {
  NativeSyntheticEvent,
  requireNativeComponent,
  ViewProps,
  findNodeHandle,
} from 'react-native';
import type { LatLng, MeshType } from '../types';
import type { MeshInfoEvent, PositionEvent } from '../mapview';

export interface MeshInfoPointEvent {
  meshInfo?: MeshInfoEvent;
  position?: PositionEvent;
}

export interface MeshData {
  vertices?: number[];
  vertexColors?: number[];
  faces?: number[];
}

export interface MeshProps extends ViewProps {
  /**
   * 是否开启透明度
   */
  transparentEnabled?: boolean;

  /**
   * 前面，后面还是两面都渲染
   */
  backOrFront?: MeshType;

  /**
   * mesh坐标，android 不能用 position 作为属性，会发生冲突，也是个蛋疼的问题
   */
  coordinate?: LatLng;

  /**
   * 渲染数据
   */
  dataSource?: MeshData;

  /**
   * 点击事件
   */
  onClick?: (event: NativeSyntheticEvent<MeshInfoPointEvent>) => void;

  /**
   * 长按事件
   */
  onLongClick?: (event: NativeSyntheticEvent<MeshInfoPointEvent>) => void;
}

const componentName = 'AMap.Mesh';
const NativeMesh = requireNativeComponent<MeshProps>(componentName);

export default forwardRef((props: MeshProps, ref) => {
  const myRef = useRef(null);
  useImperativeHandle(
    ref,
    () => ({
      getId: () => findNodeHandle(myRef.current),
    }),
    [],
  );
  return <NativeMesh {...props} ref={myRef} />;
});
