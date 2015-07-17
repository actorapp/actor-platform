'use strict';

//import path from 'path';
import webpack from 'webpack';
import WebpackDevServer from 'webpack-dev-server';
import webpackConfig from './webpack.config.js';
//import del from 'del';
import { assign } from 'lodash';
import gulp from 'gulp';
import gutil from 'gulp-util';
import manifest from 'gulp-manifest';
import shell from 'gulp-shell';
import minimist from 'minimist';
//import asar from  'asar';

gulp.task('webpack:build', function(callback) {
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
  webpack(myConfig, function(err, stats) {
    if (err) {
      throw new gutil.PluginError('webpack:build', err);
    }
    gutil.log('[webpack:build]', stats.toString({
      colors: true
    }));
    callback();
  });
});

gulp.task('webpack-dev-server', function(callback) {
  // modify some webpack config options
  assign(webpackConfig, {
    plugins: [
      new webpack.ResolverPlugin([
        new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('package.json', ['main']),
        new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('bower.json', ['main'])
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

gulp.task('emoji', () => {
  gulp.src(['./node_modules/emojify.js/dist/images/basic/*'])
    .pipe(gulp.dest('./dist/assets/img/emoji'));
});

gulp.task('push', () => {
  gulp.src(['./push/*'])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('assets', () => {
  gulp.src(['src/assets/**/*'])
    .pipe(gulp.dest('./dist/assets/'));
});

gulp.task('html', () => {
  gulp.src(['src/index.html'])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('lib:build', shell.task(['cd ../ && ./gradlew :core-js:buildPackage']));
gulp.task('lib', ['lib:build'], () => {
  const stream =
    gulp.src('../core-js/build/package/*')
      .pipe(gulp.dest('./dist/actor/'));

  return stream;
});

gulp.task('manifest:prod', ['static', 'webpack:build'], () => {
  gulp.src(['./dist/**/*'])
    .pipe(manifest({
      hash: true,
      network: ['http://*', 'https://*', '*'],
      filename: 'app.appcache'
    }))
    .pipe(gulp.dest('./dist/'));
});

gulp.task('electron:prepare', ['build'], () => {
  gulp.src(['dist/**/*'])
    .pipe(gulp.dest('./electron_dist/app'));
});

gulp.task('electron:app', () => {
  gulp.src(['electron/**/*'])
    .pipe(gulp.dest('./electron_dist/app'));
});

gulp.task('electron', ['electron:prepare', 'electron:app'], shell.task(['asar pack electron_dist/app electron_dist/app.asar']));

gulp.task('static', ['html', 'assets', 'lib', 'push', 'emoji']);

gulp.task('dev', ['static', 'webpack-dev-server']);

gulp.task('build', ['static', 'webpack:build', 'manifest:prod']);

gulp.task('build:gwt', ['static', 'webpack:build']);

gulp.task('dist', ['build', 'electron']);
