/*
 * @Author: Huangjs
 * @Date: 2022-12-12 17:34:55
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-12 17:50:39
 * @Description: ******
 */
import React, {
  useImperativeHandle,
  forwardRef,
  type ForwardedRef,
} from 'react';
import { View, Text, StyleSheet } from 'react-native';
import type { AMapViewProps, AMapRef } from './index';

const AMapView = forwardRef(
  (props: AMapViewProps, ref: ForwardedRef<AMapRef>) => {
    useImperativeHandle(
      ref,
      (): AMapRef => {
        const handle: any = {
          getId: () => -1,
        };
        [
          'getCameraPosition',
          'animateCameraPosition',
          'pointToCoordinate',
          'coordinateToPoint',
          'pickMeshInfoByPoint',
        ].forEach((key: string) => {
          handle[key] = () =>
            new Promise((_, reject) =>
              setTimeout(() => reject(new Error('no this operation')), 10),
            );
        });
        return handle;
      },
      [],
    );
    const { style } = props;
    return (
      <View
        style={StyleSheet.flatten([
          style,
          {
            alignItems: 'center',
            justifyContent: 'center',
            flex: 1,
            opacity: 0.25,
            backgroundColor: 'rgba(0,0,0,0.25)',
          },
        ])}>
        <Text>这是一个地图容器</Text>
      </View>
    );
  },
);

export default AMapView;
