/*
 * @Author: Huangjs
 * @Date: 2022-06-01 12:40:31
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-06-21 11:31:01
 * @Description: ******
 */

import React, {
  useRef,
  useMemo,
  useState,
  useEffect,
  useCallback,
} from 'react';
import moment from 'moment';
import {
  useColorScheme,
  StyleSheet,
  SafeAreaView,
  Platform,
  Switch,
  View,
  ActivityIndicator,
} from 'react-native';
import { Colors } from 'react-native/Libraries/NewAppScreen';
import AMapView, {
  Mesh,
  Marker,
  AMapModule,
  AMapType,
} from '@huangjs888/react-native-amap';

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

const defaultMarkerInfo = {
  coordinate: {
    latitude: 0,
    longitude: 0,
  },
  description: '',
};

const address = 'http://10.5.13.133:3000';

const defaultRequestInfo = {
  url: `${address}/getData`,
  data: {},
  method: 'POST',
  type: 'json',
  timeout: 1000 * 60,
  dataParse: {
    dataKey: 'data',
    pointMode: 'spherical',
    valueMode: 'value',
    centerMode: 'multiple',
    coordType: AMapType.CoordinateType.GPS,
  },
};

let whichOne = 0;

export default () => {
  const mapRef = useRef(null);
  const meshRef = useRef(null);
  const markerRef = useRef(null);
  const [index, setIndex] = useState(-1);
  const [loading, setLoading] = useState(false);
  const [markerInfo, setMarkerInfo] = useState(defaultMarkerInfo);
  const requestInfo = useMemo(
    () =>
      index == -1
        ? null
        : {
            ...defaultRequestInfo,
            data: { ...defaultRequestInfo.data, type: dataTypeSet[index].key },
          },
    [index],
  );
  const valueDomain = useMemo(
    () => (loading || index == -1 ? null : dataTypeSet[index].domain),
    [index, loading],
  );
  const onRequested = useCallback(
    (e) => {
      setLoading(false);
      mapRef.current &&
        mapRef.current.animateCameraPosition(
          {
            zoom: 10,
            pitch: index === 0 ? 0 : 45,
            center: e.nativeEvent.position,
          },
          300,
        );
    },
    [index],
  );
  const mapInitCompleted = useCallback(() => {
    setLoading(true);
    setIndex(whichOne % 3);
    whichOne++;
  }, []);
  const pickMeshInfo = useCallback(
    (e) => {
      if (index != -1 && mapRef.current && meshRef.current) {
        const { latLng } = e.nativeEvent;
        const meshId = meshRef.current.getId();
        mapRef.current
          .pickMeshInfoByPoint([meshId], latLng)
          .then(({ meshInfoList }) => {
            const pickInfo = meshInfoList.find(
              (v) => v && v.meshViewId === meshId,
            );
            // 拾取到信息
            if (pickInfo && markerRef.current) {
              markerRef.current.showInfoWindow();
              setMarkerInfo({
                coordinate: latLng,
                description: '拾取中...',
              });
              const { faceIndex, projectionDistance } = pickInfo;
              fetch(
                `${address}/pickInfo?index=${faceIndex}&type=${dataTypeSet[index].key}`,
              )
                .then((data) => data.json())
                .then((data) => {
                  console.log(data);
                  const { value, zenith, startTime, endTime } = data;
                  let description = `中心距离：${
                    Math.round((100 * projectionDistance) / Math.sin(zenith)) /
                    100
                  }m\n垂直高度：${
                    Math.round((100 * projectionDistance) / Math.tan(zenith)) /
                    100
                  }m\n投影坐标：${
                    Math.round(1000000 * latLng.longitude) / 1000000
                  },${
                    Math.round(1000000 * latLng.latitude) / 1000000
                  }\n采集时间：${
                    startTime === 0 || startTime === endTime
                      ? moment(endTime).format('YYYY-MM-DD HH:mm:ss')
                      : `${moment(startTime).format(
                          'YYYY-MM-DD HH:mm:ss',
                        )}-${moment(endTime).format('YYYY-MM-DD HH:mm:ss')}`
                  }\n${dataTypeSet[index].label}：${value}${
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
                });
            }
          });
      }
    },
    [index],
  );
  useEffect(() => {
    AMapModule.init(
      Platform.select({
        android: '7baf5432c0bc7010fd21986e57e5c032',
        ios: '7baf5432c0bc7010fd21986e57e5c032',
      }),
    );
  }, []);

  const isDarkMode = useColorScheme() === 'dark';
  return (
    <SafeAreaView
      style={{
        backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
        ...styles.flex,
      }}>
      <Switch
        value={!(whichOne % 2)}
        onValueChange={() => {
          setLoading(true);
          setIndex(whichOne % 3);
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
        onLongClick={pickMeshInfo}
        onLoaded={mapInitCompleted}>
        <Mesh
          ref={meshRef}
          request={requestInfo}
          valueDomain={valueDomain}
          onRequested={onRequested}
        />
        <Marker
          ref={markerRef}
          opacity={0}
          title="拾取信息"
          infoWindowEnable
          infoWindowOffset={{ x: 0, y: 72 }}
          onInfoWindowClick={() =>
            markerRef.current && markerRef.current.hideInfoWindow()
          }
          {...markerInfo}
        />
      </AMapView>
      <View
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0,0,0,0.45)',
          display: loading ? 'flex' : 'none',
          justifyContent: 'center',
          flex: 1,
        }}>
        <ActivityIndicator animating={loading} size="large" color="#1890ff" />
      </View>
    </SafeAreaView>
  );
};
const styles = StyleSheet.create({
  flex: { flex: 1 },
});
