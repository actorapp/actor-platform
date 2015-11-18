import path from 'path';
import webpack from 'webpack';

export default {
  devtool: 'source-map',
  entry: {
    app: ['./src/index.js'],
    styles: ['./src/styles']
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
    root: [
      path.join(__dirname, 'src')
    ]
  },
  resolveLoader: {
    modulesDirectories: ['node_modules']
  },
  module: {
    preLoaders: [
      {
        test: /\.js$/,
        loaders: ['eslint', 'source-map'],
        exclude: /node_modules/
      }
    ],

    loaders: [
      // Styles
      {
        test: /\.(scss|css)$/,
        loaders: [
          'style',
          'css',
          'autoprefixer?browsers=last 3 versions',
          'sass?outputStyle=compressed&includePaths[]=' + path.resolve(__dirname, 'node_modules')
        ]
      },
      // JavaScript
      {
        test: /\.js$/,
        loaders: [
          'babel'
        ],
        exclude: /(node_modules)/
      },
      {
        test: /\.json$/,
        loaders: ['json']
      },
      // Assets
      {
        test: /\.(png|mp3|svg)$/,
        loaders: ['file?name=assets/[name].[ext]']
      },
      // Fonts
      {
        test: /\.(ttf|eot|svg|woff|woff2)$/,
        loaders: ['file?name=assets/fonts/[name].[ext]']
      }
    ]
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
  ],
  eslint: {
    configFile: './.eslintrc'
  }
};
