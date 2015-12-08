// Intl polyfill
if (!global.Intl) {
  require('intl');
  require('intl/locale-data/jsonp/en');
}
