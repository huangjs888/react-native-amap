// @ts-nocheck
/*
 * @Author: Huangjs
 * @Date: 2022-06-01 12:40:31
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-08-11 16:38:40
 * @Description: ******
 */

import React, { useRef, useState, useEffect, useCallback } from 'react';
import {
  useColorScheme,
  StyleSheet,
  SafeAreaView,
  Platform,
  View,
  Switch,
  ActivityIndicator,
} from 'react-native';
import { Colors } from 'react-native/Libraries/NewAppScreen';
import AMapView, {
  Mesh,
  Marker,
  AMapModule,
  AMapType,
} from '@huangjs888/react-native-amap';
import parse from './parse';

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
let whichOne = 0;
export default () => {
  const mapRef = useRef(null);
  const meshRef = useRef(null);
  const markerRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [infoData, setInfoData] = useState(null);
  const [valueDomain, setValueDomain] = useState(null);
  const [markerInfo, setMarkerInfo] = useState(defaultMarkerInfo);
  const onRendered = useCallback((e) => {
    const { type, message } = e.nativeEvent;
    if (type === 'error') {
      console.log(message);
    }
  }, []);
  const fetchData = useCallback((index) => {
    setLoading(true);
    fetch('http://10.5.13.133:3000/getData', {
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
        mapRef.current &&
          mapRef.current
            .animateCameraPosition(
              {
                zoom: 10,
                pitch: index === 0 ? 0 : 45,
                center: source.position,
              },
              300,
            )
            .then(() => {
              setLoading(false);
              setValueDomain(dataTypeSet[index].domain);
              setInfoData({
                ...source,
                coordType: AMapType.CoordinateType.GPS,
              });
            });
      });
  }, []);
  const mapInitCompleted = useCallback(() => {
    fetchData(whichOne % 3);
    whichOne++;
  }, [fetchData]);
  const pickMeshInfo = useCallback((e) => {
    if (mapRef.current && meshRef.current) {
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
            let description = `投影距离：${projectionDistance}m\n三角形面：第${
              faceIndex + 1
            }个`;
            console.log(5);
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
  }, []);
  useEffect(() => {
    AMapModule.init(
      Platform.select({
        android: '7baf5432c0bc7010fd21986e57e5c032',
        ios: '7baf5432c0bc7010fd21986e57e5c032',
      }),
    );
  }, []);
  const isDarkMode = useColorScheme() === 'dark';
  const display = loading ? 'flex' : 'none';
  return (
    <SafeAreaView
      style={{
        backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
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
        onLongClick={pickMeshInfo}
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
          onInfoWindowClick={() =>
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
