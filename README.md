<!--
 * @Author: Huangjs
 * @Date: 2022-05-27 10:16:38
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-08-15 15:58:08
 * @Description: ******
-->
# react-native-amap

## Getting started

`$ npm install @huangjs888/react-native-amap --save`

## Usage
```javascript

import React, { useRef, useState, useEffect, useCallback } from 'react';
import { Platform, View } from 'react-native';
import AMapView, {
  Mesh,
  Marker,
  AMapModule,
} from '@huangjs888/react-native-amap';

export default () => {
  const markerRef = useRef(null);
  const [infoData, setInfoData] = useState(null);
  const [valueDomain, setValueDomain] = useState(null);
  const onRendered = useCallback((e) => {
    console.log(e.nativeEvent);
    setLoading(false);
    const { type, message } = e.nativeEvent;
    if (type === 'error') {
      console.log(message);
    } else {
    }
  }, []);
  const mapInitCompleted = useCallback(() => {
    fetch().then(() => {
      setValueDomain({
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
      });
      setInfoData({
        position: {
          latitude: 31.552206,
          longitude: 120.262698,
        },
        point: [],
      });
    });
  }, []);
  useEffect(() => {
    AMapModule.init(
      Platform.select({
        android: '7baf5432c0bc7010fd21986e57e5c032',
        ios: '7baf5432c0bc7010fd21986e57e5c032',
      }),
    );
  }, []);
  return (
    <View>
      <AMapView
        scaleControlsEnabled
        zoomControlsEnabled
        indoorViewEnabled
        locationEnabled
        compassEnabled
        onLongClick={(e) => {
          console.log(e.nativeEvent);
        }}
        onInitialized={mapInitCompleted}>
        <Mesh
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
          description="哈哈哈"
        />
      </AMapView>
    </View>
  );
};

```
