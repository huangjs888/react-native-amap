/*
 * @Author: Huangjs
 * @Date: 2022-06-01 12:40:31
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-16 16:23:17
 * @Description: ******
 */

import React, {
  useRef,
  useState,
  useEffect,
  useCallback,
  type ElementRef,
} from 'react';
import {
  StyleSheet,
  SafeAreaView,
  Platform,
  View,
  Switch,
  ActivityIndicator,
} from 'react-native';
import moment from 'moment';
import AMapView, {
  Mesh,
  Marker,
  AMapModule,
  CoordinateType,
  type LatLng,
  type MeshInfo,
  type MeshDataColor,
} from '@huangjs888/react-native-amap';
import parse from '../amap_local/parse';

const dataTypeSet = [
  {
    unit: 'ug/m³',
    label: 'PM10',
    key: 'pm10',
    domain: {
      range: [0, 20, 40, 60, 80, 100],
      color: [
        'rgb(0,228,0)',
        'rgb(255,255,0)',
        'rgb(255,126,0)',
        'rgb(255,0,0)',
        'rgb(153,0,76)',
        'rgb(126,0,35)',
      ],
      opacity: 0.8,
    },
  },
  {
    unit: 'km⁻¹',
    label: '消光系数',
    key: 'depol',
    domain: {
      range: [0, 0.2, 0.4, 0.6, 0.8, 1],
      color: ['#003ddf', '#00acc0', '#5afa00', '#ffff00', '#ffa500', '#ff0000'],
      opacity: 1,
    },
  },
  {
    unit: 'ug/m³',
    label: 'PM2.5',
    key: 'pm25',
    domain: {
      range: [0, 20, 40, 60, 80, 100],
      color: ['#003ddf', '#00acc0', '#5afa00', '#ffff00', '#ffa500', '#ff0000'],
      opacity: 1,
    },
  },
];
const address = 'http://10.5.13.133:3000';
const defaultMarkerInfo = {
  coordinate: {
    latitude: 0,
    longitude: 0,
  },
  description: '',
};
let whichOne = 0;
export default () => {
  const mapRef = useRef<ElementRef<typeof AMapView>>(null);
  const meshRef = useRef<ElementRef<typeof Mesh>>(null);
  const markerRef = useRef<ElementRef<typeof Marker>>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [infoData, setInfoData] = useState<any>(null);
  const [valueDomain, setValueDomain] = useState<MeshDataColor | null>(null);
  const [markerInfo, setMarkerInfo] = useState<{
    coordinate: LatLng;
    description: string;
  }>(defaultMarkerInfo);
  const onRendered = useCallback(
    (e: any) => {
      console.log(e.nativeEvent);
      setLoading(false);
      const { type, message } = e.nativeEvent;
      if (type === 'error') {
        console.log(message);
      } else {
        setLoading(false);
        mapRef.current &&
          mapRef.current.animateCameraPosition(
            {
              zoom: 10,
              pitch: infoData?.index === 0 ? 0 : 45,
              center: infoData?.position,
            },
            300,
          );
      }
    },
    [infoData],
  );
  const fetchData = useCallback((index: number) => {
    setLoading(true);
    fetch(`${address}/getData`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ type: dataTypeSet[index].key, format: 1 }),
    })
      .then((res) => res.json())
      .then(({ data }) => {
        const source = parse(data.list, dataTypeSet[index].key);
        // const source = buildData(true);
        setValueDomain(dataTypeSet[index].domain);
        setInfoData({
          index,
          ...source,
          coordType: CoordinateType.GPS,
        });
      });
  }, []);
  const mapInitCompleted = useCallback(() => {
    fetchData(whichOne % 3);
    whichOne++;
  }, [fetchData]);
  const pickMeshInfo = useCallback(
    (e: any) => {
      if (mapRef.current && meshRef.current && infoData) {
        const { latLng } = e.nativeEvent;
        const meshId = meshRef.current.getId();
        mapRef.current
          .pickMeshInfoByPoint([meshId], latLng)
          .then(({ meshInfoList = [] }) => {
            const pickInfo = meshInfoList.find(
              (v: MeshInfo) => v && v.meshViewId === meshId,
            );
            // 拾取到信息
            if (pickInfo && markerRef.current) {
              markerRef.current.showInfoWindow();
              setMarkerInfo({
                coordinate: latLng,
                description: '拾取中...',
              });
              const { index, point } = infoData;
              if (!point) {
                return;
              }
              const { faceIndex, projectionDistance } = pickInfo;
              const vlen = point[0].value.length; // 每条数据点的数量
              const fnum = 2 * (vlen - 1); // 每两条数据中两两4个点构成两个三角形面的数量
              const dindex = Math.floor(faceIndex / fnum); // 每两条数据中第一条下标
              const findex = faceIndex % fnum; //  每两条数据中三角形面的序号（第几个三角形）
              const vindex = Math.floor(findex / 2); // 三角形面所在数据下标
              const odd = findex % 2 === 0; // 三角形是否是偶数序号
              const pointCoord = [
                {
                  di: odd ? dindex : dindex + 1,
                  vi: odd ? vindex : vindex + 1,
                },
                {
                  di: odd ? dindex + 1 : dindex,
                  vi: vindex + 1,
                },
                {
                  di: odd ? dindex + 1 : dindex,
                  vi: vindex,
                },
              ]; // index所在面构成的三点数据坐标
              let totalValue = 0;
              const timeRange = [new Date().getTime(), 0];
              pointCoord.forEach(({ di, vi }) => {
                if (point[di].time < timeRange[0]) {
                  timeRange[0] = point[di].time;
                }
                if (point[di].time > timeRange[1]) {
                  timeRange[1] = point[di].time;
                }
                totalValue += point[di].value[vi];
              });
              const value = totalValue / 3;
              const startTime = timeRange[0];
              const endTime = timeRange[1];
              const { zenith } = point[0];
              let description = `中心距离：${
                Math.round((100 * projectionDistance) / Math.sin(zenith)) / 100
              }m\n垂直高度：${
                Math.round((100 * projectionDistance) / Math.tan(zenith)) / 100
              }m\n投影坐标：${
                Math.round(1000000 * latLng.longitude) / 1000000
              },${Math.round(1000000 * latLng.latitude) / 1000000}\n采集时间：${
                startTime === 0 || startTime === endTime
                  ? moment(endTime).format('YYYY-MM-DD HH:mm:ss')
                  : `${moment(startTime).format(
                      'YYYY-MM-DD HH:mm:ss',
                    )}-${moment(endTime).format('YYYY-MM-DD HH:mm:ss')}`
              }\n${dataTypeSet[index].label}：${value} ${
                dataTypeSet[index].unit
              }`;
              AMapModule.resolveAddressByCoordinate(latLng)
                .then((addr) => {
                  setMarkerInfo({
                    coordinate: latLng,
                    description: `${description}\n位置地址：${
                      addr.address || ''
                    }`,
                  });
                })
                .catch((ee) => {
                  setMarkerInfo({
                    coordinate: latLng,
                    description: `${description}\n位置地址：${ee.message}`,
                  });
                });
            }
          });
      }
    },
    [infoData],
  );
  useEffect(() => {
    AMapModule.init(
      Platform.select({
        android: '7baf5432c0bc7010fd21986e57e5c032',
        ios: '7baf5432c0bc7010fd21986e57e5c032',
      }),
    );
  }, []);
  const display = loading ? 'flex' : 'none';
  return (
    <SafeAreaView
      style={{
        ...styles.flex,
      }}>
      <Switch
        value={!(whichOne % 2)}
        onValueChange={() => {
          fetchData(whichOne % 3);
          whichOne++;
        }}
      />
      <AMapView
        ref={mapRef}
        style={styles.flex}
        scaleControlsEnabled
        zoomControlsEnabled
        indoorViewEnabled
        locationEnabled
        compassEnabled
        onLongPress={pickMeshInfo}
        onInitialized={mapInitCompleted}>
        <Mesh
          ref={meshRef}
          dataSource={infoData}
          valueDomain={valueDomain}
          onRendered={onRendered}
        />
        <Marker
          ref={markerRef}
          opacity={0}
          title="拾取信息"
          infoWindowEnable
          infoWindowOffset={{ x: 0, y: 72 }}
          onInfoWindowPress={() =>
            markerRef.current && markerRef.current.hideInfoWindow()
          }
          {...markerInfo}
        />
      </AMapView>
      <View
        style={[
          styles.view,
          {
            display,
          },
        ]}>
        <ActivityIndicator animating={loading} size="large" color="#1890ff" />
      </View>
    </SafeAreaView>
  );
};
const styles = StyleSheet.create({
  flex: { flex: 1 },
  view: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    backgroundColor: 'rgba(0,0,0,0.45)',
    justifyContent: 'center',
    flex: 1,
  },
});
