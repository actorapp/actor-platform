/* eslint-disable */

// String.includes
if (!String.prototype.includes) {
  String.prototype.includes = () => {
    'use strict';
    return String.prototype.indexOf.apply(this, arguments) !== -1;
  };
}
