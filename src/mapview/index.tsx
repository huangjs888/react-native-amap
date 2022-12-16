/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-16 16:26:43
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
  type ElementRef,
  type ForwardedRef,
} from 'react';
import {
  StyleSheet,
  findNodeHandle,
  requireNativeComponent,
  type StyleProp,
  type ViewStyle,
  type ViewProps,
  type NativeSyntheticEvent,
} from 'react-native';
import { invoke } from '../utils';
import {
  MapType,
  type Location,
  type MeshInfoList,
  type AreaPosition,
  type PointPosition,
  type CameraPosition,
} from '../types';

export interface AMapViewProps extends ViewProps {
  testID?: string;
  /**
   * 样式
   */
  style?: StyleProp<ViewStyle>;

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
  onInitialized?: (e: NativeSyntheticEvent<{}>) => void;

  /**
   * 点击事件
   */
  onPress?: (e: NativeSyntheticEvent<PointPosition>) => void;

  /**
   * 长按事件
   */
  onLongPress?: (e: NativeSyntheticEvent<PointPosition>) => void;

  /**
   * 地图状态改变事件，随地图状态变化不停地触发
   */
  onCameraMoving?: (e: NativeSyntheticEvent<AreaPosition>) => void;

  /**
   * 地图状态改变事件，在停止变化后触发
   */
  onCameraChange?: (e: NativeSyntheticEvent<AreaPosition>) => void;

  /**
   * 定位发生改变时触发
   */
  onLocationChange?: (e: NativeSyntheticEvent<Location>) => void;
}

type BaseData = {
  trigger: string;
  error?: string;
};

type CallBackData = MeshInfoList & AreaPosition & PointPosition;

export type AMapRef = {
  getId: () => number | null;
} & {
  [key: string]: (...args: any[]) => Promise<CallBackData>;
};

const AMapView = forwardRef(
  (props: AMapViewProps, ref: ForwardedRef<AMapRef>) => {
    const myRef = useRef<ElementRef<typeof NativeAMapView> | null>(null);
    const resolveRef = useRef<{
      [key: string]: {
        resolve: (data: CallBackData) => void;
        reject: (e: Error) => void;
      };
    }>({});
    const [loaded, setLoaded] = useState<boolean>(false);
    const eventCallback = useCallback(
      (e: NativeSyntheticEvent<BaseData & CallBackData>) => {
        const { trigger: key, error, ...restData } = e.nativeEvent;
        if (resolveRef.current[key]) {
          if (error) {
            resolveRef.current[key].reject(new Error(error));
          } else {
            resolveRef.current[key].resolve(restData);
          }
        }
      },
      [],
    );
    useImperativeHandle(
      ref,
      (): AMapRef => {
        const invokeMethod = (name: string, args: any[]) =>
          invoke(myRef.current, 'AMapView', name, args);
        const handle: any = {
          getId: () => findNodeHandle(myRef.current),
        };
        [
          'getCameraPosition',
          'animateCameraPosition',
          'pointToCoordinate',
          'coordinateToPoint',
          'pickMeshInfoByPoint',
        ].forEach((key: string) => {
          handle[key] = (...args: any[]) => {
            invokeMethod(key, [...args]);
            return new Promise<CallBackData>(
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

    const { style, onInitialized, ...restProps } = props;
    const memoStyle = useMemo(() => {
      const s = StyleSheet.flatten(style);
      return {
        flex: 1,
        height: !loaded ? 1 : s.height !== undefined ? s.height : '100%',
        width: !loaded ? 1 : s.width !== undefined ? s.width : '100%',
      };
    }, [loaded, style]);
    return (
      <NativeAMapView
        {...restProps}
        ref={myRef}
        style={StyleSheet.flatten([style, memoStyle])}
        onGetCameraPosition={eventCallback}
        onPointToCoordinate={eventCallback}
        onAnimateCameraPosition={eventCallback}
        onCoordinateToPoint={eventCallback}
        onPickMeshInfo={eventCallback}
        onLoad={(e: NativeSyntheticEvent<{}>) => {
          // android 地图部分控件不显示的问题在重新 layout 之后会恢复正常。
          // 同时也能修复 ios 地图偶尔出现的 layout 异常
          setLoaded(true);
          onInitialized && onInitialized.call(this, e);
        }}
      />
    );
  },
);

type NativeProps = AMapViewProps & {
  onLoad: (e: NativeSyntheticEvent<{}>) => void;
  onAnimateCameraPosition: (e: NativeSyntheticEvent<BaseData>) => void;
  onGetCameraPosition: (
    e: NativeSyntheticEvent<BaseData & AreaPosition>,
  ) => void;
  onPointToCoordinate: (
    e: NativeSyntheticEvent<BaseData & PointPosition>,
  ) => void;
  onCoordinateToPoint: (
    e: NativeSyntheticEvent<BaseData & PointPosition>,
  ) => void;
  onPickMeshInfo: (e: NativeSyntheticEvent<BaseData & MeshInfoList>) => void;
};

const NativeAMapView = requireNativeComponent<NativeProps>('AMapView');

export default AMapView;
