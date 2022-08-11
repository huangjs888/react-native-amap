/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-08-09 17:31:24
 * @Description: ******
 */

import PropTypes from 'prop-types';

/**
 * 点坐标
 */
export const PointType = PropTypes.shape({
  x: PropTypes.number.isRequired,
  y: PropTypes.number.isRequired,
});

/**
 * 地理坐标
 */
export const LatLngType = PropTypes.shape({
  /**
   * 纬度
   */
  latitude: PropTypes.number.isRequired,

  /**
   * 经度
   */
  longitude: PropTypes.number.isRequired,
});

/**
 * 矩形坐标边界
 */
export const LatLngBoundsType = PropTypes.shape({
  /**
   * 西南坐标
   */
  southwest: LatLngType,

  /**
   * 东北坐标
   */
  northeast: LatLngType,
});

/**
 * 地图状态
 */
export const CameraPositionType = PropTypes.shape({
  /**
   * 中心坐标
   */
  center: LatLngType,

  /**
   * 缩放级别
   */
  zoom: PropTypes.number,

  /**
   * 朝向、旋转角度
   */
  rotate: PropTypes.number,

  /**
   * 倾斜角度
   */
  pitch: PropTypes.number,
});

/**
 * 定位
 */
export const LocationType = PropTypes.shape({
  /**
   * 纬度
   */
  latitude: PropTypes.number.isRequired,

  /**
   * 经度
   */
  longitude: PropTypes.number.isRequired,
  /**
   * 时间戳
   */
  timestamp: PropTypes.number,

  /**
   * 精度
   */
  accuracy: PropTypes.number,

  /**
   * 朝向
   */
  heading: PropTypes.number,

  /**
   * 海拔
   */
  altitude: PropTypes.number,

  /**
   * 运动速度
   */
  speed: PropTypes.number,
});

/**
 * 地图类型
 */
export const MapType = {
  /**
   * 标准地图
   */
  STANDARD: 0,

  /**
   * 卫星地图
   */
  SATELLITE: 1,

  /**
   * 夜间地图
   */
  NIGHT: 2,

  /**
   * 导航地图
   */
  NAVI: 3,

  /**
   * 公交地图
   */
  BUS: 4,
};

/**
 * 坐标类型
 */
export const CoordinateType = {
  /**
   * 阿里云
   */
  ALIYUN: 0,

  /**
   * 百度坐标
   */
  BAIDU: 1,

  /**
   * 谷歌坐标
   */
  GOOGLE: 2,

  /**
   * GPS原始坐标
   */
  GPS: 3,

  /**
   * 图盟坐标
   */
  MAPABC: 4,

  /**
   * 图吧坐标
   */
  MAPBAR: 5,

  /**
   * 搜搜坐标
   */
  SOSOMAP: 6,
};

/**
 * 天气类型
 */
export const WeatherType = {
  /**
   * 实况天气为
   */
  LIVE: 0,

  /**
   * 天气预报为
   */
  FORECAST: 1,
};

/**
 * 地图类型
 */
export const MeshType = {
  /**
   * 前后都渲染
   */
  BOTH: 0,

  /**
   * 渲染前面
   */
  FRONT: 1,

  /**
   * 渲染后面
   */
  BACK: 2,
};

export const MeshInfoType = PropTypes.shape({
  /**
   * mesh对象三角形面的下标
   */
  faceIndex: PropTypes.number,

  /**
   * 点坐标
   */
  projectionPoint: PropTypes.arrayOf(PropTypes.number),

  /**
   * 视图id
   */
  meshViewId: PropTypes.number,
});

export const CameraPositionChangeType = PropTypes.shape({
  cameraPosition: CameraPositionType,
  latLngBounds: LatLngBoundsType,
});

export const PositionType = PropTypes.shape({
  latLng: LatLngType,
  point: PointType,
});

export const IconType = PropTypes.shape({
  uri: PropTypes.string,
  width: PropTypes.number,
  height: PropTypes.number,
});

export const MeshInfoListType = PropTypes.shape({
  meshInfoList: PropTypes.arrayOf(MeshInfoType),
});

export const MeshInfoPointType = PropTypes.shape({
  meshInfo: MeshInfoType,
  position: PositionType,
});

export const MeshDataType = PropTypes.shape({
  vertices: PropTypes.arrayOf(PropTypes.number),
  vertexColors: PropTypes.arrayOf(PropTypes.number),
  faces: PropTypes.arrayOf(PropTypes.number),
  zenith: PropTypes.number,
  azimuth: PropTypes.number,
  spacing: PropTypes.number,
  position: LatLngType,
  coordType: PropTypes.number,
  point: PropTypes.arrayOf(
    PropTypes.shape({
      zenith: PropTypes.number,
      azimuth: PropTypes.number,
      spacing: PropTypes.number,
      center: LatLngType,
      value: PropTypes.arrayOf(PropTypes.number),
    }),
  ),
});

export const MeshDataRequestType = PropTypes.shape({
  url: PropTypes.string,
  method: PropTypes.oneOf(['POST', 'GET']),
  data: PropTypes.object,
  headers: PropTypes.object,
  type: PropTypes.oneOf(['form', 'json']),
  timeout: PropTypes.number,
  dataParse: PropTypes.shape({
    dataKey: PropTypes.string,
    centerMode: PropTypes.oneOf(['single', 'multiple']), // 解析数据时，有多个或单个中心点
    pointMode: PropTypes.oneOf(['spherical', 'cartesian']), // 球坐标系，笛卡尔坐标系（空间直角坐标系）
    valueMode: PropTypes.oneOf(['color', 'value']), // color：代表返回的是已经算好的rgba值，value代表返回值，然后计算出rgba，设置value需要定义valueDomain
    coordType: PropTypes.number, // 获取的数据坐标是哪一种，会转换成高德坐标，如果本身就是高德坐标则不传
  }),
});

export const MeshDataColorType = PropTypes.shape({
  range: PropTypes.arrayOf(PropTypes.number),
  color: PropTypes.arrayOf(PropTypes.string),
  opacity: PropTypes.number,
});
