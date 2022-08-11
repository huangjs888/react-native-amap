// @ts-nocheck
/*
 * @Author: Huangjs
 * @Date: 2022-05-19 16:27:41
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-08-11 15:14:29
 * @Description: ******
 */

import PropTypes from 'prop-types';
import React, { useRef, useImperativeHandle, forwardRef } from 'react';
import { requireNativeComponent, findNodeHandle } from 'react-native';
import { ViewPropTypes } from 'deprecated-react-native-prop-types';
import {
  LatLngType,
  MeshType,
  MeshDataType,
  MeshDataRequestType,
  MeshDataColorType,
} from '../types';

const MeshPropTypes = {
  ...ViewPropTypes,
  /**
   * 是否开启透明度
   */
  transparent: PropTypes.bool,

  /**
   * 前面，后面还是两面都渲染
   */
  backOrFront: PropTypes.oneOf(Object.values(MeshType)),

  /**
   * 绘制模式
   */
  drawMode: PropTypes.oneOf([
    'points',
    'lines',
    'line_strip',
    'line_loop',
    'triangles',
    'triangles_strip',
    'triangles_loop',
  ]),

  /**
   * 深度测试
   */
  depthTest: PropTypes.bool,

  /**
   * mesh坐标，android 不能用 position 作为属性，会发生冲突，也是个蛋疼的问题
   * 坐标改变相当于将整个图层translate
   */
  coordinate: LatLngType,

  /**
   * 图层绕X旋转角度，正数为逆时针
   */
  rotateX: PropTypes.number,

  /**
   * 图层绕Y旋转角度，正数为逆时针
   */
  rotateY: PropTypes.number,

  /**
   * 图层绕Z旋转角度，正数为逆时针
   */
  rotateZ: PropTypes.number,

  /**
   * 图层缩放角度
   */
  scale: PropTypes.number,

  /**
   * 渲染数据，如果设置了request无需再设置dataSource，如设置会被request结果覆盖
   */
  dataSource: MeshDataType,

  /**
   * 数据请求
   */
  request: MeshDataRequestType,

  /**
   * 数据请求后对值进行转换成颜色，该项设置颜色的值域，色域和透明度
   */
  valueDomain: MeshDataColorType,

  /**
   * 渲染结束事件
   */
  onRendered: PropTypes.func,

  /**
   * 点击事件，仅当父组件AMapView设置了openglEventEnabled为true才会有效
   */
  onClick: PropTypes.func,

  /**
   * 长按事件，仅当父组件AMapView设置了openglEventEnabled为true才会有效
   */
  onLongClick: PropTypes.func,
};

const componentName = 'AMap.Mesh';
const NativeMesh = requireNativeComponent(componentName);

const Mesh = forwardRef((props, ref) => {
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
Mesh.propTypes = MeshPropTypes;

export default Mesh;
