/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import SharedContainer from '../utils/SharedContainer';
import alert from '../utils/alert';

import UserMenu from './common/UserMenu.react';
import ToolbarSearch from './search/ToolbarSearch.react';

class AppHeader extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  }

  constructor(props) {
    super(props);

    const { appName } = SharedContainer.get();
    this.appName = appName;
  }

  handleWriteButtonClick() {
    alert('writeButtonClick')
      .then(() => console.debug('Alert closed'));
  }

  renderWriteButton() {
    const { delegate } = this.context;

    if (!delegate.features.writeButton) {
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
      <header className="toolbar row">
        <div className="toolbar__aside">
          <span>{this.appName}</span>
        </div>
        <div className="toolbar__controls col-xs">
          {this.renderWriteButton()}
          <ToolbarSearch />
        </div>
        <div className="toolbar__profile">
          <UserMenu className="toolbar__button"/>
        </div>
      </header>
    );
  }
}

export default AppHeader;
