/*
 * @Author: Huangjs
 * @Date: 2022-08-09 15:48:52
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-12 16:50:46
 * @Description: ******
 */

export default (data: any[], dataType: string) => {
  if (!data) {
    return {
      point: [],
    };
  }
  let position;
  const point = [];
  for (let i = 0, dlen = data.length; i < dlen; i += 1) {
    // hangle/northAngle：平面上与正北顺时针（向右旋转）夹角[0,360)
    let hangle = (+data[i].hangle || 0) + (+data[i].northAngle || 0);
    // 水平角度校验确保在[0,360)
    hangle = (hangle < 0 ? 360 : 0) + (hangle % 360);
    // vangle：空间内与平面逆时针（向上旋转）之间的夹角[0,360)
    let vangle = +data[i].vangle || 0;
    // 垂直角度校验确保在[0,360)
    vangle = (vangle < 0 ? 360 : 0) + (vangle % 360);
    // 垂直角在[90,270)之间时，vangle转换为天顶角[0,180),hangle转换为方位角[0,360),其实是做了个180的旋转
    // 垂直角在[0,90)和[270,360)之间时，vangle转换为天顶角(0,180]，hangle转换为方位角(0,360]
    let zenith = 0;
    let azimuth = 0;
    if (vangle >= 90 && vangle < 270) {
      zenith = vangle - 90;
      azimuth = 270 - hangle + (hangle < 270 ? 0 : 360);
    } else {
      zenith = 90 - vangle + (vangle < 90 ? 0 : 360);
      azimuth = 90 - hangle + (hangle < 90 ? 0 : 360);
    }
    // 天顶角
    zenith = (zenith * Math.PI) / 180;
    // 方位角
    azimuth = (azimuth * Math.PI) / 180;
    // 数据类型值
    const value = data[i][dataType];
    // 中心坐标（GPS坐标需要转为高德坐标）
    const center = {
      latitude: +data[i].lat || 0,
      longitude: +data[i].lon || 0,
    };
    const spacing = +data[i].heightInterval;
    const time = +data[i].dataTime;
    point.push({
      time,
      center,
      zenith,
      azimuth,
      spacing,
      value: value.map((x: string | number) => +x),
    });
    if (i === 0) {
      position = center;
    }
  }
  return {
    position,
    point,
  };
};

export const buildData = (mesh: boolean) => {
  const data = [
    [mesh ? 0 : 20, 40, 60, 80],
    [40, mesh ? 0 : 60, 80, mesh ? 0 : 100],
    [80, 60, mesh ? 0 : 40, 20],
    [100, mesh ? 0 : 80, 60, 40],
    [mesh ? 0 : 20, 40, 60, 80],
    /* [60, 40, mesh ? 0 : 100, mesh ? 0 : 80],
    [40, mesh ? 0 : 60, 80, mesh ? 0 : 100],
    [60, 40, mesh ? 0 : 100, mesh ? 0 : 80],
    [100, mesh ? 0 : 80, 60, 40],
    [60, 40, mesh ? 0 : 100, mesh ? 0 : 80],
    [80, 60, mesh ? 0 : 40, 20],
    [60, 40, mesh ? 0 : 100, mesh ? 0 : 80], */
  ];
  let position;
  const point = [];
  for (let i = 0, dlen = data.length; i < dlen; i += 1) {
    // hangle：平面上与正北顺时针（向右旋转）夹角[0,360)
    let hangle = i * 30;
    // vangle：空间内与平面逆时针（向上旋转）之间的夹角[0,360)
    let vangle = 0;
    // 水平角度校验确保在[0,360)
    hangle = (hangle < 0 ? 360 : 0) + (hangle % 360);
    // 垂直角度校验确保在[0,360)
    vangle = (vangle < 0 ? 360 : 0) + (vangle % 360);
    let zenith = 0;
    let azimuth = 0;
    // 垂直角在[90,270)之间时，vangle转换为天顶角[0,180),hangle转换为方位角[0,360),其实是做了个180的旋转
    // 垂直角在[0,90)和[270,360)之间时，vangle转换为天顶角(0,180]，hangle转换为方位角(0,360]
    if (vangle >= 90 && vangle < 270) {
      zenith = vangle - 90;
      azimuth = 270 - hangle + (hangle < 270 ? 0 : 360);
    } else {
      zenith = 90 - vangle + (vangle < 90 ? 0 : 360);
      azimuth = 90 - hangle + (hangle < 90 ? 0 : 360);
    }
    // zenith：天顶角，从+z轴向-z轴旋转形成的夹角[0,π]
    // azimuth：方位角，从+x轴向+y轴、-x轴，-y轴，+x轴旋转形成的夹角[0,2π]
    zenith = (zenith * Math.PI) / 180;
    azimuth = (azimuth * Math.PI) / 180;
    // 数据类型值
    const value = data[i];
    // 中心坐标（GPS坐标需要转为高德坐标）
    const center = {
      latitude: 31.502206,
      longitude: 120.362698,
    };
    const spacing = 5 * 1000;
    point.push({
      center,
      zenith,
      azimuth,
      spacing,
      value: value.map((x) => +x),
    });
    if (i === 0) {
      position = center;
    }
  }
  return {
    position,
    point,
  };
};
