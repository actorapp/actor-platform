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


jsBundleFile = 'js/app.js'

opts = assign({}, watchify.args, {
  entries: jsBundleFile,
  extensions: 'jsx',
  debug: !argv.production
})

bundler = browserify(opts)
bundler.transform(reactify)

gulp.task 'browserify', ->
  bundler
    .bundle()
    .pipe(source(jsBundleFile))
    .pipe(buffer())
    .pipe(gulpif(!argv.production, sourcemaps.init({loadMaps: true})))
    .pipe(gulpif(!argv.production, sourcemaps.write('./')))
    .pipe(gulpif(argv.production, uglify()))
    .pipe(gulp.dest('./dist/assets'))
    .pipe(connect.reload())

gulp.task 'browserify:watchify', ->
  watcher = watchify(bundler)

  watcher
    .on 'error', gutil.log.bind(gutil, 'Browserify Error')
    .on 'update', ->
      updateStart = Date.now()
      console.log('Browserify started')
      watcher.bundle()
        .pipe(source(jsBundleFile))
        .pipe(buffer())
        .pipe(sourcemaps.init({loadMaps: true}))
          ## uglify
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest('./dist/assets'))
        .pipe(connect.reload())
      console.log('Browserify ended', (Date.now() - updateStart) + 'ms')
    .bundle()
    .pipe(source(jsBundleFile))
    .pipe(gulp.dest('./dist/assets'))


gulp.task 'sass', ->
  gulp.src ['./styles/**/*.scss']
    .pipe sourcemaps.init()
      .pipe sass().on('error', gutil.log)
      .pipe autoprefixer()
      .pipe concat 'styles.css'
      .pipe minifycss()
    .pipe sourcemaps.write './'
    .pipe gulp.dest './dist/assets/css/'
    .pipe connect.reload()

gulp.task 'html', ->
  gulp.src ['./index2.html']
    .pipe gulp.dest './dist'
    .pipe connect.reload()


gulp.task 'watch', ['server'], ->
  gulp.watch ['./app/**/*.coffee'], ['coffee']
  gulp.watch ['./styles/**/*.scss'], ['sass']
  gulp.watch ['./index2.html', './app/**/*.html'], ['html']

gulp.task 'assets', ->
  gulp.src ['./assets/**/*']
    .pipe gulp.dest './dist/assets/'
  gulp.src ['./bower_components/actor/**/*.js']
    .pipe gulp.dest './dist/assets/js/actor'
  gulp.src ['./bower_components/angular/angular.js']
    .pipe gulp.dest './dist/assets/js'

gulp.task 'usemin', ->
  gulp.src ['./index2.html']
    .pipe usemin
      js: [
        sourcemaps.init {loadMaps: true}
        'concat'
        uglify()
        sourcemaps.write './'
      ]
      css: [autoprefixer(), minifycss()]
    .pipe gulp.dest './dist'
    .pipe connect.reload()

gulp.task 'server', ->
  connect.server
    port: 3000
    root: ['./dist/', './']
    livereload: true

gulp.task 'build', ['assets', 'browserify', 'sass', 'html', 'usemin']

gulp.task 'build:dev', ['assets', 'browserify:watchify', 'sass', 'html']

gulp.task 'dev', ['build:dev', 'server', 'watch']

gulp.task 'default', ['build']
