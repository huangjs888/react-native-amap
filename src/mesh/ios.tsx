/*
 * @Author: Huangjs
 * @Date: 2022-12-12 17:34:55
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-16 16:26:34
 * @Description: ******
 */
import React, {
  useState,
  useEffect,
  useImperativeHandle,
  forwardRef,
  type ForwardedRef,
} from 'react';
import { View, Text, StyleSheet, NativeSyntheticEvent } from 'react-native';
import { LatLng } from '../types';
import type { MeshProps, MeshRef } from './index';

const Mesh = forwardRef((props: MeshProps, ref: ForwardedRef<MeshRef>) => {
  useImperativeHandle(
    ref,
    (): MeshRef => {
      return {
        getId: () => -1,
      };
    },
    [],
  );
  const { style, coordinate, dataSource, onRendered } = props;
  const [position, setPosition] = useState<LatLng>();
  const [length, setLength] = useState<number>(0);
  useEffect(() => {
    onRendered &&
      onRendered({
        nativeEvent: {
          type: '',
          position: coordinate,
        },
      } as NativeSyntheticEvent<{
        type: string;
        message?: string;
        position: LatLng;
      }>);
    setLength(dataSource?.point?.length || 0);
    setPosition(coordinate);
  }, [dataSource, coordinate, onRendered]);
  return (
    <View
      style={StyleSheet.flatten([
        style,
        {
          width: 300,
          height: 300,
          borderRadius: 150,
          backgroundColor: '#005AFF',
          justifyContent: 'center',
          alignItems: 'center',
        },
      ])}>
      <Text
        style={StyleSheet.flatten([
          {
            fontSize: 22,
            color: 'rgba(0,0,0,0.3)',
          },
        ])}>
        {`Mesh data length:${length}\n position:[${position?.longitude},${position?.latitude}]`}
      </Text>
    </View>
  );
});

export default Mesh;
