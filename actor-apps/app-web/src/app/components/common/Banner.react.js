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
    this.onClose();
  };

  render() {
    return (
      <section className="banner">
        <p>
          Welcome to <b>Actor Network</b>! Check out our <a href="//actor.im/ios" onClick={this.onJump.bind(this, 'IOS')} target="_blank">iPhone</a> and <a href="//actor.im/android" onClick={this.onJump.bind(this, 'ANDROID')} target="_blank">Android</a> apps!
        </p>
        <a className="banner__hide" onClick={this.onClose}>
          <i className="material-icons">close</i>
        </a>
      </section>
    );
  }
}

export default Banner;
