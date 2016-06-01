import path from 'path';
import webpack from 'webpack';
import ExtractTextPlugin from 'extract-text-webpack-plugin';

const sassLoaders = [
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
      test: /\.css$/,
      loader: 'style!css?modules&localIdentName=[name]__[local]!postcss?pack=cssnext',
      include: path.join(__dirname, 'src/components')
    }, {
      test: /\.(scss|css)$/,
      loader: ExtractTextPlugin.extract('style', sassLoaders.join('!'))
    }, {
      test: /\.js$/,
      loader: 'babel?cacheDirectory',
      exclude: /(node_modules|vendor)/
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
      __ACTOR_SDK_VERSION__: JSON.stringify(require('./package.json').version),
      __ACTOR_CORE_VERSION__: JSON.stringify(require('actor-js/package.json').version),
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
  postcss(webpack) {
    return {
      defaults: [
        require('autoprefixer')
      ],
      cssnext: [
        require('postcss-import')({ addDependencyTo: webpack }),
        require('postcss-cssnext')(),
        require('postcss-browser-reporter')(),
        require('postcss-reporter')()
      ]
    };
  }
};
