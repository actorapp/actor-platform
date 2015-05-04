gulp = require 'gulp'
gutil = require 'gulp-util'
connect = require 'gulp-connect'
concat = require 'gulp-concat'
sass = require 'gulp-sass'
coffee = require 'gulp-coffee'
autoprefixer = require 'gulp-autoprefixer'
sourcemaps = require 'gulp-sourcemaps'
minifycss = require 'gulp-minify-css'
uglify = require 'gulp-uglify'
usemin = require 'gulp-usemin'

gulp.task 'coffee', ->
  gulp.src ['./app/**/*.coffee']
    .pipe sourcemaps.init()
      .pipe coffee({ bare: true }).on('error', gutil.log)
      .pipe uglify()
      .pipe concat 'app.js'
    .pipe sourcemaps.write()
    .pipe gulp.dest './dist/assets/js/'
    .pipe connect.reload()

gulp.task 'sass', ->
  gulp.src ['./app/**/*.scss']
    .pipe sourcemaps.init()
      .pipe sass().on('error', gutil.log)
      .pipe autoprefixer()
      .pipe concat 'styles.css'
      .pipe minifycss()
    .pipe sourcemaps.write()
    .pipe gulp.dest './dist/assets/css/'
    .pipe connect.reload()

gulp.task 'html', ->
  gulp.src ['*.html']
    .pipe gulp.dest './dist/'
    .pipe connect.reload()
  gulp.src ['./app/**/*.html']
    .pipe gulp.dest './dist/app/'
    .pipe connect.reload()

gulp.task 'watch', ->
  gulp.watch ['./app/**/*.coffee'], ['coffee']
  gulp.watch ['./app/**/*.scss'], ['sass']
  gulp.watch ['./app/**/*.html'], ['html']
  gulp.watch ['./index.html'], ['usemin']

gulp.task 'assets', ->
  gulp.src ['./assets/**/*']
    .pipe gulp.dest './dist/assets/'
  gulp.src ['./ActorMessenger/**/*.js']
    .pipe gulp.dest './dist/ActorMessenger/'

gulp.task 'usemin', ->
  gulp.src ['./index.html']
    .pipe usemin
      js: [uglify()]
      css: [autoprefixer(), minifycss()]
    .pipe gulp.dest './dist/'

gulp.task 'server', ->
  connect.server
    port: 3000
    root: 'dist'
    livereload: true

gulp.task 'build', ['assets', 'coffee', 'sass', 'html', 'usemin']

gulp.task 'dev', ['build', 'server', 'watch']

gulp.task 'default', ['build']
