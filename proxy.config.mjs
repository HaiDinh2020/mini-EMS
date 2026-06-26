const backendHost = '127.0.0.1';
const backendPort = 8080;

/**
 * @type {import('vite').CommonServerOptions['proxy']}
 */
export default {
  '^/(api|management|v3/api-docs|websocket)': {
    target: `http://${backendHost}:${backendPort}`,
    changeOrigin: true,
    secure: false,
    xfwd: true,
    ws: true,
  },
};
