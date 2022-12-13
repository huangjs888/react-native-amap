/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-12 18:01:49
 * @Description: ******
 */
import { Platform } from 'react-native';
import * as AMapAndroidModule from './mapsdk';
import * as AMapIOSModule from './mapsdk/ios';
import AMapView, { type AMapViewProps } from './mapview';
import AMapIOSView from './mapview/ios';
import MeshAndroid, { type MeshProps } from './mesh';
import MeshIOS from './mesh/ios';
import MarkerAndroid, { type MarkerProps } from './marker';
import MarkerIOS from './marker/ios';

import {
  MapType,
  CoordinateType,
  WeatherType,
  MeshType,
  type Point,
  type LatLng,
  type LatLngBounds,
  type CameraPosition,
  type Location,
  type MeshInfo,
  type AreaPosition,
  type PointPosition,
  type Icon,
  type MeshInfoList,
  type MeshInfoPoint,
  type MeshData,
  type MeshDataRequest,
  type MeshDataColor,
} from './types';

export default Platform.OS === 'android' ? AMapView : AMapIOSView;
export const Mesh = Platform.OS === 'android' ? MeshAndroid : MeshIOS;
export const Marker = Platform.OS === 'android' ? MarkerAndroid : MarkerIOS;
export const AMapModule =
  Platform.OS === 'android' ? AMapAndroidModule : AMapIOSModule;
export { MapType, CoordinateType, WeatherType, MeshType };
export type {
  AMapViewProps,
  MarkerProps,
  MeshProps,
  Point,
  LatLng,
  LatLngBounds,
  CameraPosition,
  Location,
  MeshInfo,
  AreaPosition,
  PointPosition,
  Icon,
  MeshInfoList,
  MeshInfoPoint,
  MeshData,
  MeshDataRequest,
  MeshDataColor,
};
