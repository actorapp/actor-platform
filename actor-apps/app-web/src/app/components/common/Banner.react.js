import React from 'react';
import BannerActionCreators from 'actions/BannerActionCreators';

class Banner extends React.Component {
  constructor(props) {
    super(props);

    if (window.localStorage.getItem('banner_jump') === null) {
      BannerActionCreators.show();
    }
  }

  onClose = () => {
    BannerActionCreators.hide();
  };

  onJump = (os) => {
    BannerActionCreators.jump(os);
  };

  render() {
    return (
      <section className="banner">
        <p>
          Welcome to <b>Actor Network</b>! Don't forget to install mobile apps!
          &nbsp;
          <a href="//actor.im/ios" onClick={this.onJump.bind(this, 'IOS')} target="_blank">iPhone</a>
          &nbsp;|&nbsp;
          <a href="//actor.im/android" onClick={this.onJump.bind(this, 'ANDROID')} target="_blank">Android</a>
        </p>
        <a className="banner__hide" onClick={this.onClose}>
          <i className="material-icons">close</i>
        </a>
      </section>
    );
  }
}

export default Banner;
