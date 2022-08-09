// @ts-nocheck
/*
 * @Author: Huangjs
 * @Date: 2022-02-24 16:27:38
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-07-27 15:15:32
 * @Description: ******
 */
import React, { useRef, useEffect, useState } from 'react';
import { Platform, StyleSheet, View, Switch } from 'react-native';
import AMapView, {
  Mesh,
  Marker,
  AMapModule,
  AMapType,
} from '@huangjs888/react-native-amap';

const opacity = 0.9;
const colorVal = [0, 20, 40, 60, 80, 100];
const colorOne = [
  [0, 0.23921568627450981, 0.8745098039215686],
  [0, 0.6745098039215687, 0.7529411764705882],
  [0.35294117647058826, 0.9803921568627451, 0],
  [1, 0.6470588235294118, 0],
  [1, 0, 0],
];

const parseColor = (value) => {
  const len = colorOne.length;
  let tempNext = colorOne[len - 1];
  if (value >= colorVal[len - 1]) {
    return [tempNext[0], tempNext[1], tempNext[2], opacity];
  }
  let nColor = tempNext;
  for (let i = len - 2; i >= 0; i -= 1) {
    let prev = colorOne[i];
    let next = tempNext;
    const pval = colorVal[i];
    const nval = colorVal[i + 1];
    // 如果有两种颜色对应的值一样，应该取一种颜色，默认取后面一个
    if (pval === nval) {
      prev = next;
    }
    if (value === pval) {
      nColor = prev;
      break;
    }
    if (value > pval) {
      const rate = (value - pval) / (nval - pval);
      nColor = [
        prev[0] + rate * (next[0] - prev[0]),
        prev[1] + rate * (next[1] - prev[1]),
        prev[2] + rate * (next[2] - prev[2]),
      ];
      break;
    }
    tempNext = prev;
  }
  if (value < colorVal[0]) {
    return [tempNext[0], tempNext[1], tempNext[2], opacity];
  }
  return [nColor[0], nColor[1], nColor[2], opacity];
};

const buildData = (data) => {
  const distance = 5 * 1000;
  // 障碍物
  const invalid = false; // i === 6 || i === 7 || i === 8 || i === 9 || i === 10 || i === 11 || i === 18 || i === 19 || i === 20 ? true : false;

  const vertices = [];
  const vertexColors = [];
  const faces = [];
  // 中心点位置和颜色，这里和web端有些不一样
  // 这里是把原点坐标只加入一次，web端是每条数据都加入一次，想象成网格状，只是第一个点重合了
  vertices.push(0.0);
  vertices.push(0.0);
  vertices.push(0.0);
  vertexColors.push(colorOne[0][0]);
  vertexColors.push(colorOne[0][1]);
  vertexColors.push(colorOne[0][2]);
  vertexColors.push(opacity);
  let zenith = 0;
  let azimuth = 0;
  for (let i = 0, dlen = data.length; i < dlen; i += 1) {
    const value = data[i];
    const vlen = value.length;
    // hangle：平面上与正北顺时针（向右旋转）夹角[0,360)
    let hangle = i * 30;
    // vangle：空间内与平面逆时针（向上旋转）之间的夹角[0,360)
    let vangle = 60;
    // 水平角度校验确保在[0,360)
    hangle = (hangle < 0 ? 360 : 0) + (hangle % 360);
    // 垂直角度校验确保在[0,360)
    vangle = (vangle < 0 ? 360 : 0) + (vangle % 360);
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
    // 此处为正值，因为地图三维坐标x轴正方向是向东
    const xRatio = Math.sin(zenith) * Math.cos(azimuth);
    // 此处为负值，因为地图三维坐标y轴正方向是向南而不是向北
    const yRatio = -Math.sin(zenith) * Math.sin(azimuth);
    // 此处为负值，因为地图三维坐标z轴正方向是向下而不是向上
    const zRatio = Math.cos(zenith);
    if (i !== 0) {
      const tIndex = i * vlen + 1;
      faces.push(tIndex);
      faces.push(tIndex - vlen);
      faces.push(0);
    }
    for (let j = 0; j < vlen; j += 1) {
      const color = parseColor(+value[j]);
      vertexColors.push(color[0]);
      vertexColors.push(color[1]);
      vertexColors.push(color[2]);
      vertexColors.push(color[3]);
      vertices.push(distance * (j + 1) * xRatio);
      vertices.push(distance * (j + 1) * yRatio);
      vertices.push(distance * (j + 1) * zRatio);
      // invalid是判断是否有断面，如果断面采用不加入face方式，则数据拾取计算需要考虑缺失部分
      // 如果加入进去，都设置索引为0，则可按现有方法（web端方式）
      if (i !== 0 && j !== 0 && !invalid) {
        const tIndex = i * vlen + j + 1;
        const rightTop = tIndex;
        const leftTop = rightTop - vlen;
        const rightBottom = rightTop - 1;
        const leftBottom = leftTop - 1;
        faces.push(rightTop);
        faces.push(leftTop);
        faces.push(leftBottom);
        faces.push(leftBottom);
        faces.push(rightTop);
        faces.push(rightBottom);
      }
    }
  }
  return { vertices, vertexColors, faces };
};
let k = true;

export default () => {
  const mapRef = useRef(null);
  const meshRef = useRef(null);
  const markerRef = useRef(null);
  const [mesh, setMesh] = useState(true);
  useEffect(() => {
    AMapModule.init(
      Platform.select({
        android: '7baf5432c0bc7010fd21986e57e5c032',
        ios: '7baf5432c0bc7010fd21986e57e5c032',
      }),
    );
    AMapModule.getVersion().then((v) => console.log('version: ', v));
    if (mapRef.current) {
      mapRef.current.getCameraPosition().then((data) => {
        console.log('getCameraPosition', data);
      });
      mapRef.current
        .pointToCoordinate({
          x: 10,
          y: 10,
        })
        .then((data) => {
          console.log('pointToCoordinate', data);
        });
      mapRef.current
        .coordinateToPoint({ latitude: 31.502206, longitude: 120.362698 })
        .then((data) => {
          console.log('coordinateToPoint', data);
        });
      setTimeout(() => {
        console.log('animateCameraPosition', 'xxxxxxxxxxxxxxxxxxxxxxx');
        mapRef.current.animateCameraPosition(
          {
            zoom: 10,
            pitch: 90,
            center: { latitude: 31.502206, longitude: 120.362698 },
          },
          300,
        );
      }, 3000);
    }
  }, []);
  return (
    <View style={styles.view}>
      <Switch value={mesh} onChange={() => setMesh(!mesh)} />
      <AMapView
        ref={mapRef}
        onClick={(e) => {
          console.log(111, e.nativeEvent);
          if (k) {
            markerRef.current.showInfoWindow();
            k = false;
          } else {
            markerRef.current.hideInfoWindow();
            k = true;
          }
        }}
        onLongClick={(e) => {
          console.log(222, e.nativeEvent);
          const { latLng } = e.nativeEvent;
          mapRef.current
            .pickMeshInfoByPoint([meshRef.current.getId()], latLng)
            .then((data) => {
              console.log('pickMeshInfoByPoint', data);
              AMapModule.resolveCoordinateByAddress(
                '江苏省无锡市新吴区中国传感网国际创新园C1栋',
                '无锡',
              )
                .then((v) => console.log('resolveCoordinate: ', v))
                .catch((ee) => {
                  console.log('resolveCoordinate: ', ee.message);
                });
              AMapModule.resolveAddressByCoordinate({
                latitude: 31.502206,
                longitude: 120.362698,
              })
                .then((v) => console.log('resolveAddress: ', v))
                .catch((ee) => {
                  console.log('resolveAddress: ', ee.message);
                });
              AMapModule.queryWeatherByCity(AMapType.WeatherType.LIVE, '无锡')
                .then((v) => console.log('LIVE: ', v))
                .catch((ee) => {
                  console.log('LIVE: ', ee.message);
                });
              AMapModule.coordinateConvert(AMapType.CoordinateType.GPS, {
                latitude: 31.492974, // 31.491077
                longitude: 120.307316, // 120.311918
              }).then((v) => console.log('latLng: ', v));
              AMapModule.calculateLineDistance(
                { latitude: 31.502206, longitude: 120.362698 },
                latLng,
              ).then((v) => console.log('distance: ', v));
              AMapModule.queryWeatherByCity(
                AMapType.WeatherType.FORECAST,
                '无锡',
              )
                .then((v) => console.log('FORECAST: ', v))
                .catch((ee) => {
                  console.log('FORECAST: ', ee.message);
                });
            });
          markerRef.current.animateToCoordinate(
            { latitude: 31.502206, longitude: 120.362698 },
            500,
          );
        }}
        onInitialized={(e) => {
          console.log(333, e.nativeEvent);
        }}
        onLocationChange={(e) => {
          console.log(666, e.nativeEvent);
        }}
        style={styles.view}
        scaleControlsEnabled
        zoomControlsEnabled
        indoorViewEnabled
        locationEnabled
        compassEnabled>
        <Mesh
          ref={meshRef}
          depthTest={!mesh}
          coordinate={{
            latitude: 31.502206,
            longitude: 120.362698,
          }}
          dataSource={buildData([
            [mesh ? 0 : 20, 40, 60, 80],
            [40, mesh ? 0 : 60, 80, mesh ? 0 : 100],
            [80, 60, mesh ? 0 : 40, 20],
            [100, mesh ? 0 : 80, 60, 40],
            [mesh ? 0 : 20, 40, 60, 80],
            [60, 40, mesh ? 0 : 100, mesh ? 0 : 80],
            [40, mesh ? 0 : 60, 80, mesh ? 0 : 100],
            [60, 40, mesh ? 0 : 100, mesh ? 0 : 80],
            [100, mesh ? 0 : 80, 60, 40],
            [60, 40, mesh ? 0 : 100, mesh ? 0 : 80],
            [80, 60, mesh ? 0 : 40, 20],
            [60, 40, mesh ? 0 : 100, mesh ? 0 : 80],
          ])}
        />
        <Marker
          ref={markerRef}
          icon={{
            uri: mesh
              ? 'camera'
              : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADMAAAAzCAYAAAA6oTAqAAAAEXRFWHRTb2Z0d2FyZQBwbmdjcnVzaEB1SfMAAABQSURBVGje7dSxCQBACARB+2/ab8BEeQNhFi6WSYzYLYudDQYGBgYGBgYGBgYGBgYGBgZmcvDqYGBgmhivGQYGBgYGBgYGBgYGBgYGBgbmQw+P/eMrC5UTVAAAAABJRU5ErkJggg==',
            width: 60,
            height: 60,
          }}
          coordinate={{
            latitude: 31.552206,
            longitude: 120.262698,
          }}
        />
      </AMapView>
    </View>
  );
};

const styles = StyleSheet.create({
  view: { display: 'flex', width: '100%', height: '80%' },
});
