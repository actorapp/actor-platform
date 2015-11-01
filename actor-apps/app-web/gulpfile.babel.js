'use strict';

import webpack from 'webpack';
import WebpackDevServer from 'webpack-dev-server';
import webpackConfig from './webpack.config.js';
import { assign } from 'lodash';
import gulp from 'gulp';
import gutil from 'gulp-util';
import manifest from 'gulp-manifest';
import shell from 'gulp-shell';
import minimist from 'minimist';
//import asar from  'asar';
import svgSprite from 'gulp-svg-sprite';
import image from 'gulp-image';
import gulpif from 'gulp-if';
import path from 'path';

const argv = minimist(process.argv.slice(2));
const isProduction = argv.release || false;

gulp.task('webpack:build', (callback) => {
  // modify some webpack config options
  var myConfig = Object.create(webpackConfig);
  myConfig.plugins = myConfig.plugins.concat(
    new webpack.DefinePlugin({
      'process.env': {
        // This has effect on the react lib size
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin()
  );

  // run webpack
  webpack(myConfig, (err, stats) => {
    if (err) {
      throw new gutil.PluginError('webpack:build', err);
    }
    gutil.log('[webpack:build]', stats.toString({
      colors: true
    }));
    callback();
  });
});

gulp.task('webpack-dev-server', () => {
  // modify some webpack config options
  assign(webpackConfig, {
    resolve: {
      modulesDirectories: ['node_modules'],
      root: [
        path.join(__dirname, 'src/app')
      ]
    },
    plugins: [
      new webpack.ResolverPlugin([
        new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('package.json', ['main'])
      ]),
      new webpack.optimize.DedupePlugin(),
      new webpack.HotModuleReplacementPlugin(),
      new webpack.NoErrorsPlugin()
    ]
  });
  var myConfig = Object.create(webpackConfig);

  // Start a webpack-dev-server
  new WebpackDevServer(webpack(myConfig), {
    publicPath: '/assets/',
    contentBase: './dist',
    hot: true,
    historyApiFallback: true,
    stats: {
      colors: true
    }
  }).listen(3000, 'localhost', function(err) {
    if (err) {
      throw new gutil.PluginError('webpack-dev-server', err);
    }
    gutil.log('[webpack-dev-server]', 'http://localhost:3000');
  });
});

gulp.task('push', () => {
  gulp.src(['./push/*'])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('assets', ['sounds', 'images', 'locale-data']);

gulp.task('sounds', () => {
  gulp.src(['src/assets/sound/**/*'])
    .pipe(gulp.dest('./dist/assets/sound'));
});

gulp.task('images', ['sprite', 'emoji'], () => {
  gulp.src(['src/assets/img/**/*', '!src/assets/img/svg/', '!src/assets/img/svg/**'])
    .pipe(gulpif(isProduction, image()))
    .pipe(gulp.dest('./dist/assets/img'));
});

gulp.task('sprite', () => {
  gulp.src('src/assets/img/svg/*.svg')
    .pipe(svgSprite({
      shape: {
        dimension: {
          maxWidth: 24,
          maxHeight: 24
        }
      },
      mode: {
        symbol: {
          dest: 'sprite',
          sprite: 'icons',
          scss: true
        }
      }
    }))
    .pipe(gulp.dest('./dist/assets/img'));
});

gulp.task('emoji', () => {
  gulp.src([
    './node_modules/emoji-data/sheet_apple_64.png',
    './node_modules/emoji-data/sheet_emojione_64.png',
    './node_modules/emoji-data/sheet_google_64.png',
    './node_modules/emoji-data/sheet_twitter_364.png'
  ], {base: './node_modules/emoji-data'})
    .pipe(gulpif(isProduction, image()))
    .pipe(gulp.dest('./dist/assets/img/emoji'));
});

gulp.task('html', () => {
  gulp.src(['src/index.html'])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('locale-data', () => {
  gulp.src(['node_modules/intl/locale-data/json/**/*'])
    .pipe(gulp.dest('./dist/assets/locale-data'));
});

gulp.task('lib', () => {
  gulp.src([
    '../../actor-sdk/sdk-core/runtime/runtime-js/src/main/javascript/interval.js'
  ])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('manifest:prod', ['static', 'webpack:build'], () => {
  gulp.src(['./dist/**/*'])
    .pipe(manifest({
      hash: true,
      network: ['http://*', 'https://*', '*'],
      filename: 'app.appcache',
      exclude: ['assets/*.map']
    }))
    .pipe(gulp.dest('./dist/'));
});

const staticTasksBase = ['html', 'assets', 'push'];
const staticTasks = staticTasksBase.concat(['lib']);
const staticTasksDev = staticTasksBase.concat(['lib']);

gulp.task('static', staticTasks);
gulp.task('static:dev', staticTasksDev);

gulp.task('dev', ['static:dev', 'webpack-dev-server']);

gulp.task('build', ['static', 'webpack:build']);

gulp.task('build:gwt', ['static', 'webpack:build']);

gulp.task('dist', ['build']);
