argv = require('yargs').argv
assign = require 'lodash.assign'
autoprefixer = require 'gulp-autoprefixer'
browserify = require 'browserify'
buffer = require 'vinyl-buffer'
coffee = require 'gulp-coffee'
concat = require 'gulp-concat'
connect = require 'gulp-connect'
gulp = require 'gulp'
gutil = require 'gulp-util'
gulpif = require 'gulp-if'
minifycss = require 'gulp-minify-css'
sass = require 'gulp-sass'
source = require 'vinyl-source-stream'
sourcemaps = require 'gulp-sourcemaps'
reactify = require 'reactify'
uglify = require 'gulp-uglify'
usemin = require 'gulp-usemin'
watchify = require 'watchify'
replace = require 'gulp-replace-path'
manifest = require 'gulp-manifest'

jsBundleFile = 'js/app.js'

opts = assign({}, watchify.args, {
  entries: jsBundleFile
  extensions: 'jsx'
  debug: !argv.production
})

bundler = browserify(opts)
bundler.transform(reactify)

gulp.task 'browserify', ->
  bundler
    .bundle()
    .pipe source jsBundleFile
    .pipe buffer()
    .pipe gulpif !argv.production, sourcemaps.init {loadMaps: true}
    .pipe gulpif argv.production, uglify()
    .pipe gulpif !argv.production, sourcemaps.write './'
    .pipe gulp.dest './dist/assets/'
    .pipe connect.reload()

gulp.task 'browserify:watchify', ->
  watcher = watchify(bundler)

  watcher
    .on 'error', gutil.log.bind(gutil, 'Browserify Error')
    .on 'update', ->
      updateStart = Date.now()
      console.log 'Browserify started'
      watcher.bundle()
        .pipe source jsBundleFile
        .pipe buffer()
        .pipe sourcemaps.init {loadMaps: true}
        .pipe sourcemaps.write './'
        .pipe gulp.dest './dist/assets/'
        .pipe connect.reload()
      console.log('Browserify ended', (Date.now() - updateStart) + 'ms')
    .bundle()
    .pipe source jsBundleFile
    .pipe gulp.dest './dist/assets/'


gulp.task 'sass', ->
  gulp.src ['./styles/styles.scss']
    .pipe gulpif !argv.production, sourcemaps.init {loadMaps: true}
    .pipe sass().on('error', gutil.log)
    .pipe autoprefixer()
    .pipe gulpif argv.production, minifycss()
    .pipe gulpif !argv.production, sourcemaps.write './'
    .pipe gulp.dest './dist/assets/css/'
    .pipe connect.reload()

gulp.task 'html', ->
  gulp.src ['./index.html']
    .pipe gulp.dest './dist/'
    .pipe connect.reload()

gulp.task 'push', ->
  gulp.src ['./push/*']
  .pipe gulp.dest './dist/'
  .pipe connect.reload()

gulp.task 'watch', ['server'], ->
  gulp.watch ['./app/**/*.coffee'], ['coffee']
  gulp.watch ['./styles/**/*.scss'], ['sass']
  gulp.watch ['./index.html'], ['html']

gulp.task 'assets', ->
  gulp.src ['./assets/**/*']
    .pipe gulp.dest './dist/assets/'
  gulp.src ['./bower_components/actor/*.js', './bower_components/actor/*.txt', './bower_components/actor/*.txt']
    .pipe gulp.dest './dist/actor/'

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

gulp.task 'manifest:prod', ['assets', 'browserify', 'sass', 'html', 'usemin', 'push'], ->
  gulp.src ['./dist/**/*']
    .pipe manifest {
      hash: true,
      network: ['http://*', 'https://*', '*'],
      filename: 'app.appcache',
      exclude: 'app.appcache'
     }
    .pipe gulp.dest './dist/'
    .pipe connect.reload()

gulp.task 'server', ->
  connect.server
    port: 3000
    root: ['./dist/', './']
    livereload: true

gulp.task 'build', ['assets', 'browserify', 'sass', 'html', 'usemin', 'push', 'manifest:prod']

gulp.task 'build:dev', ['assets', 'browserify:watchify', 'sass', 'html', 'push']

gulp.task 'build:gwt', ['assets', 'browserify', 'sass', 'usemin', 'push']

gulp.task 'dev', ['build:dev', 'server', 'watch']

gulp.task 'default', ['build']
