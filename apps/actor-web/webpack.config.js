var path = require("path");
var webpack = require("webpack");

module.exports = {
  cache: true,
  entry: {
    app: "./src/app",
    styles: './src/styles'
  },
  output: {
    path: path.join(__dirname, "dist/assets"),
    publicPath: "/assets/",
    filename: "[name].js",
    chunkFilename: "[chunkhash].js"
  },
  resolve: {
    root: [path.join(__dirname, "bower_components")]
  },
  module: {
    preLoaders: [
      {test: /\.js$/, loader: "eslint-loader", exclude: /node_modules/}
    ],

    loaders: [
      {
        test: /\.scss|\.css$/,
        loader: "style!css!sass?outputStyle=expanded&indentedSyntax" +
        "includePaths[]=" +
        (path.resolve(__dirname, "./bower_components")) + "&" +
        "includePaths[]=" +
        (path.resolve(__dirname, "./node_modules"))
      },

      {
        test: /\.png$/,
        loader: "file-loader"
      },

      // required for bootstrap icons
      { test: /\.woff$/, loader: "url-loader?prefix=font/&limit=5000&mimetype=application/font-woff" },
      { test: /\.ttf$/, loader: "file-loader?prefix=font/" },
      { test: /\.eot$/, loader: "file-loader?prefix=font/" },
      { test: /\.svg$/, loader: "file-loader?prefix=font/" },

      {
        test: /\.js$/,
        loader: "babel",
        exclude: /(node_modules|bower_components)/,
        query: {
          optional: ['strict']
        }
      }
    ]
  },
  plugins: [
    new webpack.ResolverPlugin([
      new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin("package.json", ["main"]),
      new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin("bower.json", ["main"])
    ]),
    new webpack.optimize.DedupePlugin()
  ],
  eslint: {
    configFile: './.eslintrc'
  }
};
