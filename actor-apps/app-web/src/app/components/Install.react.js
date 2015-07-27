import React from 'react';

export default class Install extends React.Component {
  render() {
    return (
      <section className="mobile-placeholder col-xs row center-xs middle-xs">
        <div>
          <img alt="Actor messenger"
               className="logo"
               src="assets/img/logo.png"
               srcSet="assets/img/logo@2x.png 2x"/>


          <h1>Web version of <b>Actor</b> works only on desktop browsers at this time</h1>
          <h3>Please install our apps for using <b>Actor</b> on your phone.</h3>
          <p>
            <a href="//actor.im/ios">iPhone</a> | <a href="//actor.im/android">Android</a>
          </p>
        </div>
      </section>
    );
  }
}
