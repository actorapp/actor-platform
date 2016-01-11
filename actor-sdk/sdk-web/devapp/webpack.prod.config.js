import path from 'path';
import webpack from 'webpack';

export default {
  devtool: 'source-map',
  entry: {
    app: ['./src/index.js'],
    styles: ['./src/styles.js']
  },
  output: {
    path: path.join(__dirname, 'dist'),
    publicPath: './',
    filename: '[name].js',
    chunkFilename: '[chunkhash].js',
    sourceMapFilename: '[name].map'
  },
  resolve: {
    modulesDirectories: ['node_modules'],
    root: [path.join(__dirname, 'src')],
    fallback: [path.join(__dirname, 'node_modules')]
  },
  resolveLoader: {
    modulesDirectories: ['node_modules'],
    fallback: [path.join(__dirname, 'node_modules')]
  },
  module: {
    preLoaders: [{
      test: /\.js$/,
      loaders: ['eslint'],
      include: [path.resolve(__dirname, 'src')]
    }, {
      test: /\.js$/,
      loaders: ['source-map'],
      include: [path.resolve(__dirname, 'node_modules/actor-sdk')],
      exclude: /(node_modules)/
    }],
    loaders: [{
      test: /\.(scss|css)$/,
      loaders: [
        'style',
        'css',
        'autoprefixer?browsers=last 3 versions',
        'sass?outputStyle=compressed&includePaths[]=' + path.resolve(__dirname, 'node_modules')
      ]
    }, {
      test: /\.js$/,
      loaders: ['babel'],
      exclude: /(node_modules)/
    }, {
      test: /\.json$/,
      loaders: ['json']
    }, {
      test: /\.(png|svg)$/,
      loaders: ['file?name=assets/images/[name].[ext]']
    }, {
      test: /\.(mp3)$/,
      loaders: ['file?name=assets/sounds/[name].[ext]']
    }, {
      test: /\.(ttf|eot|svg|woff|woff2)$/,
      loaders: ['file?name=assets/fonts/[name].[ext]']
    }]
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.ResolverPlugin([
      new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('package.json', ['main'])
    ]),
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.optimize.DedupePlugin(),
    new webpack.optimize.UglifyJsPlugin({
      compressor: {
        warnings: false
      }
    })
  ]
};
