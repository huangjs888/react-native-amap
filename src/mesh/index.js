/*
 * @Author: Huangjs
 * @Date: 2022-05-19 16:27:41
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-30 16:55:23
 * @Description: ******
 */
// @ts-nocheck

import PropTypes from 'prop-types';
import React, { useRef, useImperativeHandle, forwardRef } from 'react';
import { requireNativeComponent, findNodeHandle } from 'react-native';
import { ViewPropTypes } from 'deprecated-react-native-prop-types';
import { LatLngType, MeshDataType, MeshType } from '../types';

const MeshPropTypes = {
  ...ViewPropTypes,
  /**
   * 是否开启透明度
   */
  transparentEnabled: PropTypes.bool,

  /**
   * 前面，后面还是两面都渲染
   */
  backOrFront: PropTypes.oneOf(Object.values(MeshType)),

  /**
   * mesh坐标，android 不能用 position 作为属性，会发生冲突，也是个蛋疼的问题
   */
  coordinate: LatLngType.isRequired,

  /**
   * 渲染数据
   */
  dataSource: MeshDataType,

  /**
   * 点击事件
   */
  onClick: PropTypes.func,

  /**
   * 长按事件
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
