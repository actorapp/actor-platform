argv = require('yargs').argv
autoprefixer = require 'gulp-autoprefixer'
browserify = require 'browserify'
buffer = require 'vinyl-buffer'
coffee = require 'gulp-coffee'
concat = require 'gulp-concat'
connect = require 'gulp-connect'
gulp = require 'gulp'
gutil = require 'gulp-util'
gulpif = require 'gulp-if'
assign = require 'lodash.assign'
minifycss = require 'gulp-minify-css'
sass = require 'gulp-sass'
source = require 'vinyl-source-stream'
sourcemaps = require 'gulp-sourcemaps'
reactify = require 'reactify'
uglify = require 'gulp-uglify'
usemin = require 'gulp-usemin'
watchify = require 'watchify'

gulp.task 'coffee', ->
  gulp.src ['./app/**/*.coffee']
    .pipe sourcemaps.init()
      .pipe coffee({ bare: true }).on('error', gutil.log)
      .pipe uglify()
      .pipe concat 'app-coffee.js'
    .pipe sourcemaps.write './'
    .pipe gulp.dest './dist/assets/js/'
    .pipe connect.reload()


opts = assign({}, watchify.args, {
  entries: 'app/main.js',
  extensions: 'jsx',
  debug: !argv.production
})

bundler = browserify(opts)
bundler.transform(reactify)

jsBundleFile = 'app-js.js'

gulp.task 'js', ->
  bundler.bundle()
    .pipe(source(jsBundleFile))
    .pipe(buffer())
    .pipe(gulpif(!argv.production, sourcemaps.init({loadMaps: true})))
      .pipe(gulpif(argv.production, uglify()))
    .pipe(gulpif(!argv.production, sourcemaps.write('./')))
    .pipe(gulp.dest('./dist/assets/js'))

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
  gulp.src ['./app/**/*.html']
    .pipe gulp.dest './dist/app/'
    .pipe connect.reload()
  gulp.src ['./index.html']
    .pipe gulp.dest './dist/'
    .pipe connect.reload()

gulp.task 'watchify', ->
  watcher = watchify(bundler)
  watcher
    .on 'error', gutil.log.bind(gutil, 'Browserify Error')
    .on 'update', ->
      watcher.bundle()
        .pipe(source(jsBundleFile))
        .pipe(buffer())
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest('./dist/assets/js'))

      gutil.log("Updated JavaScript sources")
    .bundle()
    .pipe(source(jsBundleFile))
    .pipe(gulp.dest('./dist/assets/js'))
    .pipe connect.reload()

gulp.task 'watch', ['server'], ->
  gulp.watch ['./app/**/*.coffee'], ['coffee']
  gulp.watch ['./app/**/*.scss'], ['sass']
  gulp.watch ['./app/**/*.js', './app/**/*.jsx'], ['watchify']
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
    root: ['./dist/', './bower_components/', './node_modules/']
    livereload: true

gulp.task 'build', ['assets', 'coffee', 'js', 'sass', 'html', 'usemin']

gulp.task 'build:dev', ['assets', 'coffee', 'js', 'sass', 'html']

gulp.task 'dev', ['build:dev', 'server', 'watch']

gulp.task 'default', ['build']
