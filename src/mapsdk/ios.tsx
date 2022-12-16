/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-16 16:28:56
 * @Description: ******
 */

import type { CoordinateType, WeatherType, LatLng } from '../types';

// 初始化地图，并设置key
export function init(apiKey?: string): void {
  if (!apiKey) {
    return;
  }
}

// 获取地图版本号
export function getVersion(): Promise<string> {
  return new Promise((resolve) => setTimeout(() => resolve('1.0.0'), 10));
}

// 根据地址和地址所在城市查询该地址的坐标
export function resolveCoordinateByAddress(
  address: string,
  city: string,
): Promise<any> {
  return new Promise((resolve) =>
    setTimeout(() => resolve({ address, city }), 10),
  );
}

// 根据坐标查询坐标的地址相关信息
export function resolveAddressByCoordinate(coordinate: LatLng): Promise<any> {
  return new Promise((resolve) =>
    setTimeout(() => resolve({ coordinate }), 10),
  );
}

// 查询某个城市的天气情况type为0是实时天气，1是预报天气
export function queryWeatherByCity(
  type: WeatherType,
  city: string,
): Promise<any> {
  return new Promise((resolve) =>
    setTimeout(() => resolve({ type, city }), 10),
  );
}

// 将其他坐标类型（coordType）转到高德坐标
export function coordinateConvert(
  coordType: CoordinateType,
  coordinate: LatLng,
): Promise<LatLng> {
  return new Promise((resolve) =>
    setTimeout(() => resolve({ ...coordinate }), 10),
  );
}

// 计算给定的一组经纬度距离
export function calculateLineDistance(
  coordinate1: LatLng,
  coordinate2: LatLng,
): Promise<number> {
  return new Promise((resolve) =>
    setTimeout(() => resolve(coordinate1 ? 0 : coordinate2 ? 1 : 2), 10),
  );
}
