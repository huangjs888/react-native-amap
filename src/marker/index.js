/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-30 16:55:16
 * @Description: ******
 */
// @ts-nocheck

import PropTypes from 'prop-types';
import React, { useRef, useImperativeHandle, forwardRef } from 'react';
import { requireNativeComponent, findNodeHandle } from 'react-native';
import { ViewPropTypes } from 'deprecated-react-native-prop-types';
import { invoke } from '../utils';
import { PointType, IconType, LatLngType } from '../types';

const MarkerPropTypes = {
  ...ViewPropTypes,
  /**
   * marker坐标，android 不能用 position 作为属性，会发生冲突，也是个蛋疼的问题
   */
  coordinate: LatLngType.isRequired,

  /**
   * 图标
   */
  icon: IconType,

  /**
   * marker标题
   */
  title: PropTypes.string,

  /**
   * marker内容描述
   */
  description: PropTypes.string,

  /**
   * 是否平贴地图
   *
   * @platform android
   */
  flat: PropTypes.bool,

  /**
   * 透明度 [0, 1]
   *
   * @platform android
   */
  opacity: PropTypes.number,

  /**
   * 可见度
   */
  visible: PropTypes.bool,

  /**
   * 旋转角度
   */
  rotateAngle: PropTypes.number,

  /**
   * 是否可拖拽
   */
  draggable: PropTypes.bool,

  /**
   * 层级
   */
  zIndex: PropTypes.number,

  /**
   * 覆盖物锚点比例
   *
   * @platform android
   */
  anchor: PointType,

  /**
   * marker是否作为信息窗口
   */
  infoWindowEnable: PropTypes.bool,

  /**
   * marker作为信息窗口的时候的偏移量
   */
  infoWindowOffset: PointType,

  /**
   * 点击事件
   */
  onClick: PropTypes.func,

  /**
   * 作为信息窗口的时候的点击事件
   */
  onInfoWindowClick: PropTypes.func,

  /**
   * 拖放开始事件
   */
  onDragStart: PropTypes.func,

  /**
   * 拖放进行事件，类似于 mousemove，在结束之前会不断调用
   */
  onDrag: PropTypes.func,

  /**
   * 拖放结束事件
   */
  onDragEnd: PropTypes.func,
};

const componentName = 'AMap.Marker';
const NativeMarker = requireNativeComponent(componentName);

const Marker = forwardRef((props, ref) => {
  const myRef = useRef(null);
  const { icon, ...restProps } = props;
  useImperativeHandle(
    ref,
    () => {
      const invokeMethod = (name, args) =>
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
    <NativeMarker {...restProps} ref={myRef} icon={!icon ? undefined : icon} />
  );
});
Marker.propTypes = MarkerPropTypes;

export default Marker;
