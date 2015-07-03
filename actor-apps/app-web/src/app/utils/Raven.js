import Raven from 'raven-js';

Raven.config('https://f35bd82f5a7a44cea27c0d7f09322c4f@app.getsentry.com/47478').install();

export default Raven;
