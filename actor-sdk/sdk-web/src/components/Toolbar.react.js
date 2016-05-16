/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import UserMenu from './common/UserMenu.react';

class AppHeader extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <header className={`toolbar row`}>
        <div className="toolbar__menu">
          <span>Actor</span>
        </div>
        <div className="toolbar__controls col-xs">
          <button className="button">
            <i className="material-icons">edit</i>
          </button>
        </div>
        <div className="toolbar__profile">
          <UserMenu/>
        </div>
      </header>
    );
  }
}

export default AppHeader;
