import path from 'path';
import express from 'express';
import webpack from 'webpack';
import dev from 'webpack-dev-middleware';

function createServer(webpackConfig) {
  const app = express();
  const compiler = webpack(webpackConfig);

  app.use(dev(compiler, {
    publicPath: webpackConfig.publicPath,
    stats: {
      colors: true
    }
  }));

  app.use('/assets', express.static('assets'));
  app.use('/assets/images/emoji', express.static('node_modules/actor-emoji'));

  app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'devapp/index.html'));
  });

  return app;
}

export default createServer;
