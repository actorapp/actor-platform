'use strict';

Object.defineProperty(exports, '__esModule', {
  value: true
});
exports.ActorSDKDelegate = exports.ActorSDK = undefined;

var _actorSdk = require('./build/sdk/actor-sdk');

var _actorSdk2 = _interopRequireDefault(_actorSdk);

var _actorSdkDelegate = require('./build/sdk/actor-sdk-delegate');

var _actorSdkDelegate2 = _interopRequireDefault(_actorSdkDelegate);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.ActorSDK = _actorSdk2.default;
exports.ActorSDKDelegate = _actorSdkDelegate2.default;
exports.default = {
  ActorSDK: _actorSdk2.default, ActorSDKDelegate: _actorSdkDelegate2.default
};
