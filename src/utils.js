/*
 * @Author: Huangjs
 * @Date: 2022-05-24 15:05:05
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-30 16:39:44
 * @Description: ******
 */
// @ts-nocheck

import { findNodeHandle, UIManager } from 'react-native';

export function invoke(view, componentName, name, args) {
  const viewId = findNodeHandle(view);
  if (viewId) {
    const command =
      UIManager.getViewManagerConfig(componentName).Commands[name].toString();
    UIManager.dispatchViewManagerCommand(viewId, command, [...args]);
  }
}
