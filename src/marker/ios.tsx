/*
 * @Author: Huangjs
 * @Date: 2022-12-12 17:34:55
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-16 16:30:11
 * @Description: ******
 */
import React, {
  useState,
  useEffect,
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
import { LatLng, PointPosition } from '../types';
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
    const { style, coordinate, onInfoWindowPress, onPress } = props;
    const [position, setPosition] = useState<LatLng>();
    useEffect(() => {
      setPosition(coordinate);
    }, [coordinate]);
    return (
      <View
        style={StyleSheet.flatten([
          style,
          {
            width: 40,
            height: 40,
            borderRadius: 20,
            backgroundColor: 'yellow',
            justifyContent: 'center',
            alignItems: 'center',
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
            (onInfoWindowPress || onPress || (() => {}))(
              {} as NativeSyntheticEvent<PointPosition>,
            );
          }}>
          <Text
            style={StyleSheet.flatten([
              {
                fontSize: 10,
                color: 'rgba(0,0,0,0.3)',
              },
            ])}>
            {`marker position:[${position?.longitude},${position?.latitude}]`}
          </Text>
        </Pressable>
      </View>
    );
  },
);

export default Marker;
