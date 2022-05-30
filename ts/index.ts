/*
 * @Author: Huangjs
 * @Date: 2022-05-11 17:49:45
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-05-26 09:23:00
 * @Description: ******
 */

import * as AMapModule from './mapsdk';
import AMapView, * as mapTypes from './mapview';
import Mesh, * as meshTypes from './mesh';
import Marker, * as markerTypes from './marker';
import * as allTypes from './types';

const AMapType = { ...allTypes, ...mapTypes, ...meshTypes, ...markerTypes };

export { Marker, Mesh, AMapType, AMapModule };
export default AMapView;
