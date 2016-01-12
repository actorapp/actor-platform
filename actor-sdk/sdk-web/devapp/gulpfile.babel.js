'use strict';

import minimist from 'minimist';

import webpack from 'webpack';
import webpackDevServer from 'webpack-dev-server';
import webpackProdConfig from './webpack.prod.config.js';
import webpackDevConfig from './webpack.dev.config.js';

import gulp from 'gulp';
import gutil from 'gulp-util';
import gulpif from 'gulp-if';
import image from 'gulp-image';

const argv = minimist(process.argv.slice(2));
const isProduction = argv.release || false;

gulp.task('webpack:build', (callback) => {
  webpack(webpackProdConfig, (err, stats) => {
    if (err) {
      throw new gutil.PluginError('[webpack:build]', err);
    }
    gutil.log('[webpack:build]', stats.toString({
      colors: true
    }));
    callback();
  });
});

gulp.task('webpack:dev', () => {
  new webpackDevServer(webpack(webpackDevConfig), {
    publicPath: '/',
    contentBase: './dist',
    hot: true,
    historyApiFallback: true,
    stats: {
      colors: true
    }
  }).listen(3000, 'localhost', (err) => {
    if (err) {
      throw new gutil.PluginError('[webpack:dev]', err);
    }
    gutil.log('[webpack:dev]', 'http://localhost:3000');
  });
});

gulp.task('assets', ['sounds', 'images']);

gulp.task('sounds', () => {
  gulp.src(['../build/assets/sound/**/*'])
    .pipe(gulp.dest('./dist/assets/sound'));
});

gulp.task('images', () => {
  gulp.src(['../build/assets/images/**/*'])
    .pipe(gulpif(isProduction, image({svgo: false})))
    .pipe(gulp.dest('./dist/assets/images'));
});

gulp.task('html', () => {
  gulp.src(['src/index.html'])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('workers', () => {
  gulp.src([
    '../build/workers/offline-worker.*',
    '../build/workers/serviceworker-cache-polyfill.*',
    'node_modules/opus-recorder/libopus.js',
    'node_modules/opus-recorder/oggopusDecoder.js',
    'node_modules/opus-recorder/oggopusEncoder.js',
    'node_modules/opus-recorder/resampler.js',
  ])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('static', ['html', 'assets', 'workers']);

gulp.task('dev', ['static', 'webpack:dev']);

gulp.task('build', ['static', 'webpack:build']);

gulp.task('default', ['build']);
