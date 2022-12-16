/*
 * @Author: Huangjs
 * @Date: 2022-12-12 17:34:55
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-16 16:25:29
 * @Description: ******
 */
import React, {
  useImperativeHandle,
  forwardRef,
  type ForwardedRef,
} from 'react';
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  NativeSyntheticEvent,
} from 'react-native';
import { PointPosition } from '../types';
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
    const { style, onPress, children } = props;
    return (
      <View
        style={StyleSheet.flatten([
          style,
          {
            flex: 1,
            backgroundColor: '#C2DFFF',
          },
        ])}>
        <Pressable
          style={StyleSheet.flatten([
            {
              flex: 1,
              justifyContent: 'center',
              alignItems: 'center',
            },
          ])}
          onPress={() => {
            (onPress || (() => {}))({} as NativeSyntheticEvent<PointPosition>);
          }}>
          {children}
          <Text
            style={StyleSheet.flatten([
              {
                fontSize: 24,
                color: 'rgba(0,0,0,0.3)',
              },
            ])}>
            这是一个地图容器
          </Text>
        </Pressable>
      </View>
    );
  },
);

export default AMapView;
