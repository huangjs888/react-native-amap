/*
 * @Author: Huangjs
 * @Date: 2022-05-24 15:05:05
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-25 10:00:41
 * @Description: ******
 */

import { findNodeHandle, UIManager } from 'react-native';

export function invoke(view: any, componentName: string, name: string, args?: any[]) {
  const viewId = findNodeHandle(view);
  if (viewId) {
    const command = UIManager.getViewManagerConfig(componentName).Commands[name].toString();
    UIManager.dispatchViewManagerCommand(
      viewId,
      command,
      [...args]
    );
  }
}