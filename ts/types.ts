/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-25 16:59:02
 * @Description: ******
 */

/**
 * 点坐标
 */
export interface Point {
  x?: number;
  y?: number;
}

/**
 * 地理坐标
 */
export interface LatLng {
  /**
   * 纬度
   */
  latitude?: number;

  /**
   * 经度
   */
  longitude?: number;
}


/**
 * 矩形坐标边界
 */
export interface LatLngBounds {
  /**
   * 西南坐标
   */
  southwest?: LatLng;

  /**
   * 东北坐标
   */
  northeast?: LatLng;
}

/**
 * 地图状态
 */
export interface CameraPosition {
  /**
   * 中心坐标
   */
  center?: LatLng;

  /**
   * 缩放级别
   */
  zoom?: number;

  /**
   * 朝向、旋转角度
   */
  rotate?: number;

  /**
   * 倾斜角度
   */
  pitch?: number;
}

/**
 * 定位
 */
export interface Location extends LatLng {
  /**
 * 时间戳
 */
  timestamp?: number;

  /**
   * 精度
   */
  accuracy?: number;

  /**
   * 朝向
   */
  heading?: number;

  /**
   * 海拔
   */
  altitude?: number;

  /**
   * 运动速度
   */
  speed?: number;
}

/**
 * 地图类型
 */
export enum MapType {
  /**
   * 标准地图
   */
  STANDARD,

  /**
   * 卫星地图
   */
  SATELLITE,

  /**
   * 夜间地图
   */
  NIGHT,

  /**
   * 导航地图
   */
  NAVI,

  /**
   * 公交地图
   */
  BUS,
}

/**
 * 坐标类型
 */
export enum CoordinateType {
  /**
   * 阿里云
   */
  ALIYUN,

  /**
   * 百度坐标
   */
  BAIDU,

  /**
   * 谷歌坐标
   */
  GOOGLE,

  /**
   * GPS原始坐标
   */
  GPS,

  /**
   * 图盟坐标
   */
  MAPABC,

  /**
   * 图吧坐标
   */
  MAPBAR,

  /**
   * 搜搜坐标
   */
  SOSOMAP,
}

/**
 * 天气类型
 */
export enum WeatherType {
  /**
   * 实况天气为
   */
  LIVE,

  /**
   * 天气预报为
   */
  FORECAST,
}

/**
 * 地图类型
 */
export enum MeshType {
  /**
   * 前后都渲染
   */
  BOTH,

  /**
   * 渲染前面
   */
  FRONT,

  /**
   * 渲染后面
   */
  BACK,
}
