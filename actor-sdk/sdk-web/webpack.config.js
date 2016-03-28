import path from 'path';
import webpack from 'webpack';
import autoprefixer from 'autoprefixer';
import ExtractTextPlugin from 'extract-text-webpack-plugin';

const sassLoaders = [
  // 'css?sourceMap&modules',
  'css?sourceMap',
  'postcss',
  'sass?sourceMap&outputStyle=expanded&includePaths[]=' + path.resolve(__dirname, 'node_modules')
];

export default {
  cache: true,
  debug: true,
  devtool: '#inline-source-map',
  entry: {
    app: [
      './devapp/index.js',
      './src/styles/index.scss'
    ]
  },
  output: {
    path: path.join(__dirname, 'dist'),
    publicPath: '/',
    filename: '[name].js',
    chunkFilename: '[chunkhash].js',
    sourceMapFilename: '[file][hash].map'
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
    noParse: [
      /languages\/autoit\.js/
    ],
    loaders: [{
      test: /\.(scss|css)$/,
      loader: ExtractTextPlugin.extract('style', sassLoaders.join('!'))
    }, {
      test: /\.js$/,
      loaders: [
        'babel?cacheDirectory'
      ],
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
        'NODE_ENV': JSON.stringify('development')
      }
    }),
    new ExtractTextPlugin('index.css', { allChunks: true }),
    new webpack.ResolverPlugin([
      new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('package.json', ['main'])
    ], ['context']),
    new webpack.NoErrorsPlugin()
  ],
  postcss: [
    autoprefixer({browsers: ['last 3 versions']})
  ]
};
