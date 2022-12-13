/*
 * @Author: Huangjs
 * @Date: 2022-05-24 15:05:05
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-13 13:37:35
 * @Description: ******
 */

import { findNodeHandle, UIManager } from 'react-native';
import type { Component } from 'react';

export function invoke(
  view: Component | null,
  componentName: string,
  methodName: string,
  args: any[],
) {
  const viewId = findNodeHandle(view);
  if (viewId) {
    const command =
      UIManager.getViewManagerConfig(componentName).Commands[
        methodName
      ].toString();
    UIManager.dispatchViewManagerCommand(viewId, command, [...args]);
  }
}
