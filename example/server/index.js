// @ts-nocheck
/*
 * @Author: Huangjs
 * @Date: 2021-05-20 09:41:55
 * @LastEditors: Huangjs
 * @LastEditTime: 2022-12-16 16:26:53
 * @Description: ******
 */

const http = require('http');
const fs = require('fs');

const rootDirectory = __dirname;

const log = {
  info: (s) => console.log(s),
  success: (s) => global.console.log(`\x1B[32m${s}\x1B[39m`),
  error: (s) => global.console.log(`\x1B[31m${s}\x1B[39m`),
  warn: (s) => global.console.log(`\x1B[33m${s}\x1B[39m`),
};

const dataToString = (fd, key) => {
  const res = {};
  let data = JSON.parse(fd.toString());
  if (data.data && data.data.list) {
    res.data = { position: {}, point: [], faces: [] };
    data = data.data.list;
    for (let i = 0, dlen = data.length; i < dlen; i += 1) {
      // 中心坐标（GPS坐标需要转为高德坐标）
      res.data.position = {
        latitude: +data[i].lat || 0,
        longitude: +data[i].lon || 0,
      };
      // 间隔距离
      const distance = data[i].heightInterval;
      // 数据类型值
      const value = data[i][key];
      const vlen = value.length;
      // hangle/northAngle：平面上与正北顺时针（向右旋转）夹角[0,360)
      let hangle = (+data[i].hangle || 0) + (+data[i].northAngle || 0);
      // 水平角度校验确保在[0,360)
      hangle = (hangle < 0 ? 360 : 0) + (hangle % 360);
      // vangle：空间内与平面逆时针（向上旋转）之间的夹角[0,360)
      let vangle = 60; // +data[i].vangle || 0;
      // 垂直角度校验确保在[0,360)
      vangle = (vangle < 0 ? 360 : 0) + (vangle % 360);
      // 垂直角在[90,270)之间时，vangle转换为天顶角[0,180),hangle转换为方位角[0,360),其实是做了个180的旋转
      // 垂直角在[0,90)和[270,360)之间时，vangle转换为天顶角(0,180]，hangle转换为方位角(0,360]
      let zenith = 0;
      let azimuth = 0;
      if (vangle >= 90 && vangle < 270) {
        zenith = vangle - 90;
        azimuth = 270 - hangle + (hangle < 270 ? 0 : 360);
      } else {
        zenith = 90 - vangle + (vangle < 90 ? 0 : 360);
        azimuth = 90 - hangle + (hangle < 90 ? 0 : 360);
      }
      // zenith：天顶角，从+z轴向-z轴旋转形成的夹角[0,π]
      // azimuth：方位角，从+x轴向+y轴、-x轴，-y轴，+x轴旋转形成的夹角[0,2π]
      zenith = (zenith * Math.PI) / 180;
      azimuth = (azimuth * Math.PI) / 180;
      for (let j = 0; j < vlen; j += 1) {
        res.data.point.push({
          d: j * distance,
          t: zenith,
          p: azimuth,
          v: +value[j],
        });
        if (i !== 0 && j !== 0) {
          const tIndex = i * vlen + j;
          const rightTop = tIndex;
          const leftTop = rightTop - vlen;
          const rightBottom = rightTop - 1;
          const leftBottom = leftTop - 1;
          res.data.faces.push(rightTop);
          res.data.faces.push(leftTop);
          res.data.faces.push(leftBottom);
          res.data.faces.push(leftBottom);
          res.data.faces.push(rightTop);
          res.data.faces.push(rightBottom);
        }
      }
    }
  }
  return JSON.stringify(res);
};

const dataToString2 = (fd, key) => {
  const res = {};
  let data = JSON.parse(fd.toString());
  if (data.data && data.data.list) {
    res.data = { position: {}, point: [], faces: [] };
    data = data.data.list;
    for (let i = 0, dlen = data.length; i < dlen; i += 1) {
      // 中心坐标（GPS坐标需要转为高德坐标）
      if (i === 0) {
        res.data.position = {
          latitude: +data[i].lat || 0,
          longitude: +data[i].lon || 0,
        };
      }
      // 间隔距离
      const distance = data[i].heightInterval;
      // 数据类型值
      const value = data[i][key];
      const vlen = value.length;
      // hangle/northAngle：平面上与正北顺时针（向右旋转）夹角[0,360)
      let hangle = (+data[i].hangle || 0) + (+data[i].northAngle || 0);
      // 水平角度校验确保在[0,360)
      hangle = (hangle < 0 ? 360 : 0) + (hangle % 360);
      // vangle：空间内与平面逆时针（向上旋转）之间的夹角[0,360)
      let vangle = +data[i].vangle || 0;
      // 垂直角度校验确保在[0,360)
      vangle = (vangle < 0 ? 360 : 0) + (vangle % 360);
      // 垂直角在[90,270)之间时，vangle转换为天顶角[0,180),hangle转换为方位角[0,360),其实是做了个180的旋转
      // 垂直角在[0,90)和[270,360)之间时，vangle转换为天顶角(0,180]，hangle转换为方位角(0,360]
      let zenith = 0;
      let azimuth = 0;
      if (vangle >= 90 && vangle < 270) {
        zenith = vangle - 90;
        azimuth = 270 - hangle + (hangle < 270 ? 0 : 360);
      } else {
        zenith = 90 - vangle + (vangle < 90 ? 0 : 360);
        azimuth = 90 - hangle + (hangle < 90 ? 0 : 360);
      }
      // zenith：天顶角，从+z轴向-z轴旋转形成的夹角[0,π]
      // azimuth：方位角，从+x轴向+y轴、-x轴，-y轴，+x轴旋转形成的夹角[0,2π]
      zenith = (zenith * Math.PI) / 180;
      azimuth = (azimuth * Math.PI) / 180;
      res.data.point[i] = {
        center: {
          latitude: +data[i].lat || 0,
          longitude: +data[i].lon || 0,
        },
        children: [],
      };
      for (let j = 0; j < vlen; j += 1) {
        res.data.point[i].children[j] = {
          d: j * distance,
          t: zenith,
          p: azimuth,
          v: +value[j],
        };
        if (i !== 0 && j !== 0) {
          const tIndex = i * vlen + j;
          const rightTop = tIndex;
          const leftTop = rightTop - vlen;
          const rightBottom = rightTop - 1;
          const leftBottom = leftTop - 1;
          res.data.faces.push(rightTop);
          res.data.faces.push(leftTop);
          res.data.faces.push(leftBottom);
          res.data.faces.push(leftBottom);
          res.data.faces.push(rightTop);
          res.data.faces.push(rightBottom);
        }
      }
    }
  }
  return JSON.stringify(res);
};

const queryInfo = (fd, key, index) => {
  const res = {};
  let data = JSON.parse(fd.toString());
  if (data.data && data.data.list && data.data.list.length > 1) {
    data = data.data.list;
    const vlen = data[0][key].length; // 每条数据点的数量
    const fnum = 2 * (vlen - 1); // 每两条数据中两两4个点构成两个三角形面的数量
    const dindex = Math.floor(index / fnum); // 每两条数据中第一条下标
    const findex = index % fnum; //  每两条数据中三角形面的序号（第几个三角形）
    const vindex = Math.floor(findex / 2); // 三角形面所在数据下标
    const odd = findex % 2 === 0; // 三角形是否是偶数序号
    const pointCoord = [
      {
        di: odd ? dindex : dindex + 1,
        vi: odd ? vindex : vindex + 1,
      },
      {
        di: odd ? dindex + 1 : dindex,
        vi: vindex + 1,
      },
      {
        di: odd ? dindex + 1 : dindex,
        vi: vindex,
      },
    ]; // index所在面构成的三点数据坐标
    let totalValue = 0;
    const timeRange = [new Date().getTime(), 0];
    pointCoord.forEach(({ di, vi }) => {
      if (data[di].dataTime < timeRange[0]) {
        timeRange[0] = data[di].dataTime;
      }
      if (data[di].dataTime > timeRange[1]) {
        timeRange[1] = data[di].dataTime;
      }
      totalValue += +data[di][key][vi];
    });
    // hangle/northAngle：平面上与正北顺时针（向右旋转）夹角[0,360)
    let hangle = (+data[0].hangle || 0) + (+data[0].northAngle || 0);
    // 水平角度校验确保在[0,360)
    hangle = (hangle < 0 ? 360 : 0) + (hangle % 360);
    // vangle：空间内与平面逆时针（向上旋转）之间的夹角[0,360)
    let vangle = +data[0].vangle || 0;
    // 垂直角度校验确保在[0,360)
    vangle = (vangle < 0 ? 360 : 0) + (vangle % 360);
    // 垂直角在[90,270)之间时，vangle转换为天顶角[0,180),hangle转换为方位角[0,360),其实是做了个180的旋转
    // 垂直角在[0,90)和[270,360)之间时，vangle转换为天顶角(0,180]，hangle转换为方位角(0,360]
    let zenith = 0;
    let azimuth = 0;
    if (vangle >= 90 && vangle < 270) {
      zenith = vangle - 90;
      azimuth = 270 - hangle + (hangle < 270 ? 0 : 360);
    } else {
      zenith = 90 - vangle + (vangle < 90 ? 0 : 360);
      azimuth = 90 - hangle + (hangle < 90 ? 0 : 360);
    }
    // zenith：天顶角，从+z轴向-z轴旋转形成的夹角[0,π]
    // azimuth：方位角，从+x轴向+y轴、-x轴，-y轴，+x轴旋转形成的夹角[0,2π]
    res.zenith = (zenith * Math.PI) / 180;
    res.azimuth = (azimuth * Math.PI) / 180;
    res.value = totalValue / 3;
    res.startTime = timeRange[0];
    res.endTime = timeRange[1];
  }
  return JSON.stringify(res);
};

// 解析流的读取，request和response实际也是流的示例
const resolveStream = (stream) =>
  new Promise((resolve, reject) => {
    const buffers = [];
    // 每读一块数据触发data事件，chunk是Buffer实例
    stream.on('data', (chunk) => buffers.push(chunk));
    // 读完数据，触发end事件，这里可以处理结束逻辑
    stream.on('end', () => resolve(global.Buffer.concat(buffers)));
    stream.on('error', (e) => reject(e));
  });
// 读取文件流
const readFileStream = (filePath) =>
  new Promise((resolve, reject) =>
    fs.promises
      // 检查文件是否可读
      .access(filePath, fs.constants.R_OK)
      .then(() =>
        // 解析文件流
        resolveStream(fs.createReadStream(filePath))
          .then((buffer) => resolve(buffer))
          .catch((e) => reject(e)),
      )
      .catch(() => reject(new Error(`Cannot read file:${filePath}`))),
  );

// 创建服务，并监听端口
http
  .createServer((request, response) => {
    const url = new URL(request.url || '', `http://${request.headers.host}`);
    log.success(`${url.href} request start...`);
    new Promise((resolve) => {
      if (request.method === 'POST') {
        resolveStream(request).then((buffer) =>
          resolve(JSON.parse(decodeURIComponent(buffer.toString()))),
        );
      } else if (request.method === 'GET') {
        const data = {};
        url.searchParams.forEach((v, k) => (data[k] = decodeURIComponent(v)));
        resolve(data);
      }
    })
      .then((data) => {
        if (url.pathname === '/getData') {
          const { type, format } = data;
          return readFileStream(`${rootDirectory}/data/${type}.json`).then(
            (fileData) => {
              response.writeHead(200, { 'Content-type': 'application/json' });
              const result =
                +format === 2
                  ? dataToString(fileData, type)
                  : +format === 1
                  ? fileData
                  : dataToString2(fileData, type);
              response.write(result);
              response.end();
              log.success('Request success...');
            },
          );
        } else if (url.pathname === '/pickInfo') {
          const { index, type } = data;
          return readFileStream(`${rootDirectory}/data/${type}.json`).then(
            (fileData) => {
              response.writeHead(200, { 'Content-type': 'application/json' });
              response.write(queryInfo(fileData, type, +index));
              response.end();
              log.success('Request success...');
            },
          );
        } else {
          log.error(`${url.href} is invalid request...`);
          response.end(`${url.href} is invalid request...`);
        }
      })
      .catch((e) => {
        log.error(`Request error: ${e.message} ...`);
        response.end(`Request error: ${e.message} ...`);
      });
  })
  .listen(3000, () => {
    log.success('Server is ready...');
  });
