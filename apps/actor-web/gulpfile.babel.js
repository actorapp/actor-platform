'use strict';

import path from 'path';
import webpack from 'webpack';
import WebpackDevServer from 'webpack-dev-server';
import webpackConfig from './webpack.config.js';
import del from 'del';

const assign = require('lodash.assign');
const gulp = require('gulp');
const gutil = require('gulp-util');
const manifest = require('gulp-manifest');

gulp.task("webpack:build", function(callback) {
  // modify some webpack config options
  var myConfig = Object.create(webpackConfig);
  myConfig.plugins = myConfig.plugins.concat(
    new webpack.DefinePlugin({
      "process.env": {
        // This has effect on the react lib size
        "NODE_ENV": JSON.stringify("production")
      }
    }),
    new webpack.optimize.UglifyJsPlugin()
  );

  // run webpack
  webpack(myConfig, function(err, stats) {
    if(err) throw new gutil.PluginError("webpack:build", err);
    gutil.log("[webpack:build]", stats.toString({
      colors: true
    }));
    callback();
  });
});

gulp.task("webpack-dev-server", function(callback) {
  // modify some webpack config options
  var myConfig = Object.create(webpackConfig);
  myConfig.devtool = "eval";
  myConfig.debug = true;

  // Start a webpack-dev-server
  new WebpackDevServer(webpack(myConfig), {
    publicPath: myConfig.output.publicPath,
    contentBase: './dist',
    stats: {
      colors: true
    }
  }).listen(3000, "localhost", function(err) {
      if(err) throw new gutil.PluginError("webpack-dev-server", err);
      gutil.log("[webpack-dev-server]", "http://localhost:3000/webpack-dev-server/index.html");
    });
});

gulp.task('push', () => {
  gulp.src(['./push/*'])
    .pipe(gulp.dest('./dist/'))
});

gulp.task('actor', () => {
  gulp.src([
    './bower_components/actor/*.js',
    './bower_components/actor/*.txt',
    './bower_components/actor/*.txt'
  ])
    .pipe(gulp.dest('./dist/actor/'));
});

gulp.task('assets', () => {
  gulp.src(['src/assets/**/*'])
    .pipe(gulp.dest('./dist/assets/'))
});

gulp.task('html', () => {
  gulp.src('src/index.html')
    .pipe(gulp.dest('./dist/'));
});

gulp.task(
  'manifest:prod',
  ['html', 'static', 'webpack:build'],
  () => {
    gulp.src(['./dist/**/*'])
      .pipe(manifest({
        hash: true,
        network: ['http://*', 'https://*', '*'],
        filename: 'app.appcache',
        exclude: 'app.appcache'
      }))
      .pipe(gulp.dest('./dist/'))
  });

gulp.task('static', ['assets', 'actor', 'push']);

gulp.task('dev', ['static', 'webpack-dev-server']);

gulp.task('build', ['html', 'static', 'webpack:build', 'manifest:prod']);

gulp.task('build:gwt', ['html', 'static', 'webpack:build']);


