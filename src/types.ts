/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-29 13:22:11
 * @Description: ******
 */

/**
 * 点坐标
 */
export type Point = {
  x: number;
  y: number;
};

/**
 * 地理坐标
 */
export type LatLng = {
  /**
   * 纬度
   */
  latitude: number;

  /**
   * 经度
   */
  longitude: number;
};

/**
 * 矩形坐标边界
 */
export type LatLngBounds = {
  /**
   * 西南坐标
   */
  southwest: LatLng;

  /**
   * 东北坐标
   */
  northeast: LatLng;
};

/**
 * 地图状态
 */
export type CameraPosition = {
  /**
   * 中心坐标
   */
  center: LatLng;

  /**
   * 缩放级别
   */
  zoom: number;

  /**
   * 朝向、旋转角度
   */
  rotate: number;

  /**
   * 倾斜角度
   */
  pitch: number;
};

/**
 * OpenGL拾取信息
 */
export type MeshInfo = {
  /**
   * mesh对象三角形面的下标
   */
  faceIndex: number;

  /**
   * 点坐标
   */
  projectionPoint: number[];

  /**
   * 投影距离
   */
  projectionDistance: number;

  /**
   * 视图id
   */
  meshViewId: number;
};

/**
 * 定位
 */
export interface Location extends LatLng {
  /**
   * 时间戳
   */
  timestamp: number;

  /**
   * 精度
   */
  accuracy: number;

  /**
   * 朝向
   */
  heading: number;

  /**
   * 海拔
   */
  altitude: number;

  /**
   * 运动速度
   */
  speed: number;
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
 * Mesh渲染类型
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

export type AreaPosition = {
  cameraPosition?: CameraPosition;
  latLngBounds?: LatLngBounds;
};

export type PointPosition = {
  latLng?: LatLng;
  point?: Point;
};

export type Icon = {
  uri?: string;
  width?: number;
  height?: number;
};

export type MeshInfoList = {
  meshInfoList?: MeshInfo[];
};

export type MeshInfoPoint = {
  meshInfo?: MeshInfo;
  position?: PointPosition;
};

export type MeshData = {
  vertices?: number[];
  vertexColors?: number[];
  faces?: number[];
  zenith?: number;
  azimuth?: number;
  spacing?: number;
  position?: LatLng;
  coordType?: number;
  point?: Array<{
    zenith: number;
    azimuth: number;
    spacing: number;
    center: LatLng;
    value: number[];
  }>;
};

export type MeshDataRequest = {
  url?: string;
  method?: 'POST' | 'GET';
  data?: any;
  headers?: any;
  type?: 'form' | 'json';
  timeout?: number;
  dataParse?: {
    dataKey: string;
    centerMode: 'single' | 'multiple'; // 解析数据时，有多个或单个中心点
    pointMode: 'spherical' | 'cartesian'; // 球坐标系，笛卡尔坐标系（空间直角坐标系）
    valueMode: 'color' | 'value'; // color：代表返回的是已经算好的rgba值，value代表返回值，然后计算出rgba，设置value需要定义valueDomain
    coordType: number; // 获取的数据坐标是哪一种，会转换成高德坐标，如果本身就是高德坐标则不传
  };
};

export type MeshDataColor = {
  range?: number[];
  color?: string[];
  opacity?: number;
};
