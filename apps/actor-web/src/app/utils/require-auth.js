import React from 'react';

import LoginStore from '../stores/LoginStore';

const requireAuth = (Component) => {
  return class Authenticated extends React.Component {
    static willTransitionTo(transition) {
      if (!LoginStore.isLoggedIn()) {
        transition.redirect('/auth', {}, {'nextPath': transition.path});
      }
    }

    render() {
      return <Component {...this.props}/>
    }
  }
};

module.exports = requireAuth;
