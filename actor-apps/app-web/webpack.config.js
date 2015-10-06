import minimist from 'minimist';
import path from 'path';
import webpack from 'webpack';

const argv = minimist(process.argv.slice(2));

const DEBUG = !argv.release;

export default {
  cache: DEBUG,
  debug: DEBUG,
  devtool: DEBUG ? 'inline-source-map' : 'source-map',
  hotComponents: DEBUG,
  entry: {
    app: DEBUG ? [
      'webpack-dev-server/client?http://localhost:3000',
      'webpack/hot/dev-server',
      './src/app/index.js'
    ] : ['./src/app/index.js'],
    styles: DEBUG ? [
      'webpack/hot/dev-server',
      './src/styles'
    ] : ['./src/styles']
  },
  output: {
    path: path.join(__dirname, 'dist/assets'),
    publicPath: 'assets/',
    filename: '[name].js',
    chunkFilename: '[chunkhash].js',
    sourceMapFilename: '[name].map'
  },
  resolve: {
    modulesDirectories: ['node_modules'],
    root: [
      path.join(__dirname, 'src/app')
    ]
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
          'react-hot',
          'style',
          'css',
          'autoprefixer?browsers=last 3 versions',
          'sass?outputStyle=expanded&indentedSyntax' +
            'includePaths[]=' + (path.resolve(__dirname, './node_modules'))
        ]
      },
      // JavaScript
      {
        test: /\.js$/,
        loaders: [
          'react-hot',
          'babel?optional[]=es7.classProperties' +
            '&optional[]=es7.decorators'
        ],
        exclude: /(node_modules)/
      },{
        test: /\.json$/,
        loaders: ['json']
      },
      // Assets
      {
        test: /\.(png|mp3)$/,
        loaders: ['file?name=[name].[ext]']
      },
      // Fonts
      {
        test: /\.(ttf|eot|svg|woff|woff2)$/,
        loaders: ['file?name=fonts/[name].[ext]']
      }
    ]
  },
  plugins: [
    new webpack.ResolverPlugin([
      new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('package.json', ['main'])
    ]),
    new webpack.optimize.DedupePlugin()
  ],
  eslint: {
    configFile: './.eslintrc'
  }
};
