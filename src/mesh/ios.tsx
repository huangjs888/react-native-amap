/*
 * @Author: Huangjs
 * @Date: 2022-12-12 17:34:55
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-12 17:52:21
 * @Description: ******
 */
import React, {
  useImperativeHandle,
  forwardRef,
  type ForwardedRef,
} from 'react';
import { View, Text, StyleSheet } from 'react-native';
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
  const { style } = props;
  return (
    <View
      style={StyleSheet.flatten([
        style,
        {
          alignItems: 'center',
          justifyContent: 'center',
          width: 100,
          height: 100,
          backgroundColor: '#8FC5DF',
        },
      ])}>
      <Text>mesh</Text>
    </View>
  );
});

export default Mesh;
