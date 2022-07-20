/*
 * @Author: Huangjs
 * @Date: 2022-05-24 15:05:05
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-07-20 10:51:56
 * @Description: ******
 */

import { findNodeHandle, UIManager } from 'react-native';

export function invoke(
  view: any,
  componentName: string,
  name: string,
  args?: any[],
) {
  const viewId = findNodeHandle(view);
  if (viewId) {
    const commad =
      UIManager.getViewManagerConfig(componentName).Commands[name].toString();
    UIManager.dispatchViewManagerCommand(viewId, commad, [...args]);
  }
}
