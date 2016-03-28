'use strict';

import minimist from 'minimist';

import createDevServer from './webpack.server';
import webpackConfig from './webpack.config';

import gulp from 'gulp';
import gutil from 'gulp-util';
import gulpif from 'gulp-if';
import image from 'gulp-image';
import shell from 'gulp-shell';
import rename from 'gulp-rename';

const argv = minimist(process.argv.slice(2));
const isProduction = argv.release || false;

gulp.task('webpack:dev', () => {
  const server = createDevServer(webpackConfig);
  server.listen(3000, '0.0.0.0', (err) => {
    if (err) {
      throw new gutil.PluginError('[webpack:dev]', err);
    }

    gutil.log('[webpack:dev]', 'http://0.0.0.0:3000');
  });
});

gulp.task('assets', ['html', 'sounds', 'images']);

gulp.task('sounds', ['sdk'], () => {
  return gulp.src(['build/assets/sound/**/*'])
    .pipe(gulp.dest('./dist/assets/sound'));
});

gulp.task('images', ['sdk'], () => {
  return gulp.src(['build/assets/images/**/*'])
    .pipe(gulpif(isProduction, image({svgo: false})))
    .pipe(gulp.dest('./dist/assets/images'));
});

gulp.task('html', () => {
  return gulp.src(['devapp/index.html'])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('workers', ['sdk'], () => {
  return gulp.src([
    //'build/workers/offline-worker.*',
    //'build/workers/serviceworker-cache-polyfill.*',
    'node_modules/opus-recorder/libopus.js',
    'node_modules/opus-recorder/oggopusDecoder.js',
    'node_modules/opus-recorder/oggopusEncoder.js',
    'node_modules/opus-recorder/resampler.js'
  ])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('lib:build', shell.task('./gradlew :actor-sdk:sdk-core:core:core-js:buildPackage', {cwd: '../..'}));
gulp.task('lib:copy', ['lib:build'], () => {
  return gulp.src(['../sdk-core/core/core-js/build/package/actor.nocache.js'])
    .pipe(rename('actor.js'))
    .pipe(gulp.dest('./node_modules/actor-js/'));
});
gulp.task('lib', ['lib:build', 'lib:copy']);

gulp.task('sdk', shell.task('npm run build'));

gulp.task('static', ['sdk', 'assets', 'workers']);

gulp.task('dev', ['webpack:dev']);

gulp.task('dev:core', ['lib', 'dev']);

gulp.task('default', ['dev']);
