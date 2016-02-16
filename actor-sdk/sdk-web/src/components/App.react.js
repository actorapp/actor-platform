/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

/**
 * Root react component
 */
class App extends Component {
  static propTypes = {
    delegate: PropTypes.object,
    isExperimental: PropTypes.bool,
    children: PropTypes.oneOfType([
      PropTypes.arrayOf(PropTypes.node),
      PropTypes.node
    ])
  };

  static childContextTypes = {
    delegate: PropTypes.object,
    isExperimental: PropTypes.bool
  };

  getChildContext() {
    const { delegate, isExperimental } = this.props;
    return {
      delegate, isExperimental
    };
  }

  constructor(props) {
    super(props);
  }

  render() {
    return this.props.children;
  }
}

export default App;
