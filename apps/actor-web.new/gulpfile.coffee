gulp = require 'gulp'
gutil = require 'gulp-util'
connect = require 'gulp-connect'
concat = require 'gulp-concat'
sass = require 'gulp-sass'
coffee = require 'gulp-coffee'
sourcemaps = require 'gulp-sourcemaps'

gulp.task 'coffee', ->
  gulp.src ['./app/**/*.coffee']
  .pipe sourcemaps.init()
  .pipe coffee({ bare: true }).on('error', gutil.log)
  .pipe concat 'app.js'
  .pipe sourcemaps.write()
  .pipe gulp.dest './assets/js'
  .pipe connect.reload()

gulp.task 'sass', ->
  gulp.src ['./app/**/*.scss']
  .pipe sourcemaps.init()
  .pipe sass().on('error', gutil.log)
  .pipe concat 'styles.css'
  .pipe sourcemaps.write()
  .pipe gulp.dest './assets/css'
  .pipe connect.reload()

gulp.task 'html', ->
  gulp.src ['*.html', './app/**/*.html']
  .pipe connect.reload()

gulp.task 'watch', ->
  gulp.watch ['./app/**/*.coffee'], ['coffee']
  gulp.watch ['./app/**/*.scss'], ['sass']
  gulp.watch ['*.html', './app/**/*.html'], ['html']

gulp.task 'server', ->
  connect.server
    port: 3000
    livereload: true

gulp.task 'build', ['coffee', 'sass', 'html']

gulp.task 'default', ['build', 'server', 'watch']
