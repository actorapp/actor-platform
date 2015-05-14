gulp = require 'gulp'
gutil = require 'gulp-util'
connect = require 'gulp-connect'
concat = require 'gulp-concat'
sass = require 'gulp-sass'
coffee = require 'gulp-coffee'
autoprefixer = require 'gulp-autoprefixer'
sourcemaps = require 'gulp-sourcemaps'
minifycss = require 'gulp-minify-css'
react = require 'gulp-react'
uglify = require 'gulp-uglify'
usemin = require 'gulp-usemin'

gulp.task 'coffee', ->
  gulp.src ['./app/**/*.coffee']
    .pipe sourcemaps.init()
      .pipe coffee({ bare: true }).on('error', gutil.log)
      .pipe uglify()
      .pipe concat 'app-coffee.js'
    .pipe sourcemaps.write './'
    .pipe gulp.dest './dist/assets/js/'
    .pipe connect.reload()

gulp.task 'jsx', ->
  gulp.src ['app/**/*.jsx']
    .pipe sourcemaps.init()
    .pipe react().on('error', gutil.log)
      .pipe uglify()
      .pipe concat 'app-jsx.js'
    .pipe sourcemaps.write './'
    .pipe gulp.dest './dist/assets/js/'
    .pipe connect.reload()

gulp.task 'js', ->
  gulp.src ['app/**/*.js']
  .pipe sourcemaps.init()
  .pipe uglify()
  .pipe concat 'app-js.js'
  .pipe sourcemaps.write './'
  .pipe gulp.dest './dist/assets/js/'
  .pipe connect.reload()

gulp.task 'sass', ->
  gulp.src ['./app/**/*.scss']
    .pipe sourcemaps.init()
      .pipe sass().on('error', gutil.log)
      .pipe autoprefixer()
      .pipe concat 'styles.css'
      .pipe minifycss()
    .pipe sourcemaps.write './'
    .pipe gulp.dest './dist/assets/css/'
    .pipe connect.reload()

gulp.task 'html', ->
  gulp.src ['./app/**/*.html', './app/**/*.jsx', './app/**/*.js']
    .pipe gulp.dest './dist/app/'
    .pipe connect.reload()
  gulp.src ['./index.html']
    .pipe gulp.dest './dist/'
    .pipe connect.reload()

gulp.task 'watch', ['server'], ->
  gulp.watch ['./app/**/*.coffee'], ['coffee']
  gulp.watch ['./app/**/*.js'], ['js']
  gulp.watch ['./app/**/*.jsx'], ['jsx']
  gulp.watch ['./app/**/*.scss'], ['sass']
  gulp.watch ['./index.html', './app/**/*.html'], ['html']

gulp.task 'assets', ->
  gulp.src ['./assets/**/*']
    .pipe gulp.dest './dist/assets/'
  gulp.src ['./ActorMessenger/**/*.js']
    .pipe gulp.dest './dist/ActorMessenger/'

gulp.task 'usemin', ->
  gulp.src ['./index.html']
    .pipe usemin
      js: [
        sourcemaps.init {loadMaps: true}
        'concat'
        uglify()
        sourcemaps.write './'
      ]
      css: [autoprefixer(), minifycss()]
    .pipe gulp.dest './dist/'
    .pipe connect.reload()

gulp.task 'server', ->
  connect.server
    port: 3000
    root: ['./dist/', './bower_components/']
    livereload: true

gulp.task 'build', ['assets', 'coffee', 'js', 'jsx', 'sass', 'html', 'usemin']

gulp.task 'build:dev', ['assets', 'coffee', 'js', 'jsx', 'sass', 'html']

gulp.task 'dev', ['build:dev', 'server', 'watch']

gulp.task 'default', ['build']
