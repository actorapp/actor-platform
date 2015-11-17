import path from 'path';
import webpack from 'webpack';

export default {
  cache: true,
  debug: true,
  devtool: 'inline-source-map',
  hotComponents: true,
  entry: {
    app: [
      'webpack-dev-server/client?http://localhost:3000',
      'webpack/hot/dev-server',
      './src/app/index.js'
    ],
    styles: [
      'webpack-dev-server/client?http://localhost:3000',
      'webpack/hot/dev-server',
      './src/styles'
    ]
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
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('development')
      }
    }),
    new webpack.ResolverPlugin([
      new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('package.json', ['main'])
    ]),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.NoErrorsPlugin()
  ],
  eslint: {
    configFile: './.eslintrc'
  }
};
