/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import alert from '../utils/alert';

import UserMenu from './common/UserMenu.react';
import ToolbarSearch from './search/ToolbarSearch.react';

class AppHeader extends Component {
  static contextTypes = {
    isExperimental: PropTypes.bool
  }

  constructor(props) {
    super(props);
  }

  handleWriteButtonClick() {
    alert('writeButtonClick')
      .then(() => console.debug('Alert closed'));
  }

  renderWriteButton() {
    const { isExperimental } = this.context;

    if (!isExperimental) {
      return null;
    }

    return (
      <button className="toolbar__button" onClick={this.handleWriteButtonClick}>
        <i className="material-icons">edit</i>
      </button>
    );
  }

  render() {
    return (
      <header className={`toolbar row`}>
        <div className="toolbar__aside">
          <span>Actor</span>
        </div>
        <div className="toolbar__controls col-xs">
          {this.renderWriteButton()}
          <ToolbarSearch className="toolbar__button"/>
        </div>
        <div className="toolbar__profile">
          <UserMenu className="toolbar__button"/>
        </div>
      </header>
    );
  }
}

export default AppHeader;
