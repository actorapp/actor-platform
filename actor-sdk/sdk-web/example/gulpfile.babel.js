'use strict';

import minimist from 'minimist';

import webpack from 'webpack';
import webpackDevServer from 'webpack-dev-server';
import webpackConfigProd from './webpack.config.prod.js';
import webpackConfigDev from './webpack.config.dev.js';

import gulp from 'gulp';
import gutil from 'gulp-util';
import gulpif from 'gulp-if';
import image from 'gulp-image';

const argv = minimist(process.argv.slice(2));
const isProduction = argv.release || false;

gulp.task('webpack:build', (callback) => {
  webpack(webpackConfigProd, (err, stats) => {
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
  new webpackDevServer(webpack(webpackConfigDev), {
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

gulp.task('assets', ['sounds', 'images', 'locale-data']);

gulp.task('sounds', () => {
  gulp.src(['node_modules/actor-sdk/build/assets/sound/**/*'])
    .pipe(gulp.dest('./dist/assets/sound'));
});

gulp.task('images', () => {
  gulp.src(['node_modules/actor-sdk/build/assets/images/**/*'])
    .pipe(gulpif(isProduction, image({svgo: false})))
    .pipe(gulp.dest('./dist/assets/images'));
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
  gulp.src(['node_modules/actor-js/interval.js'])
    .pipe(gulp.dest('./dist/'));
});

gulp.task('static', ['html', 'assets', 'lib']);

gulp.task('dev', ['static', 'webpack:dev']);

gulp.task('build', ['static', 'webpack:build']);

gulp.task('dist', ['build']);
