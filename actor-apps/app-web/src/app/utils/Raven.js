import Raven from 'raven-js';
//import Console from 'raven-js/plugins/console'; // eslint-disable-line

Raven.config('https://f35bd82f5a7a44cea27c0d7f09322c4f@app.getsentry.com/47478', {
  logger: 'javascript',
  collectWindowErrors: true
}).install();

export default Raven;
