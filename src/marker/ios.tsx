/*
 * @Author: Huangjs
 * @Date: 2022-12-12 17:34:55
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-12 17:48:46
 * @Description: ******
 */
import React, {
  useImperativeHandle,
  forwardRef,
  type ForwardedRef,
} from 'react';
import { View, Text, StyleSheet } from 'react-native';
import type { MarkerProps, MarkerRef } from './index';

const Marker = forwardRef(
  (props: MarkerProps, ref: ForwardedRef<MarkerRef>) => {
    useImperativeHandle(
      ref,
      (): MarkerRef => {
        const handle: any = {
          getId: () => -1,
        };
        ['showInfoWindow', 'hideInfoWindow', 'animateToCoordinate'].forEach(
          (key: string) => {
            handle[key] = () =>
              new Promise((_, reject) =>
                setTimeout(() => reject(new Error('no this operation')), 10),
              );
          },
        );
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
            width: 10,
            height: 10,
            backgroundColor: 'yellow',
          },
        ])}>
        <Text>marker</Text>
      </View>
    );
  },
);

export default Marker;
