if (!global.Intl) {
  require.ensure([
    'intl'
  ], function (require) {
    require('intl');
  });
}
