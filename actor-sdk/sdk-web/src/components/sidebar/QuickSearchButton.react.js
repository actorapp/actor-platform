/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { FormattedMessage } from 'react-intl';
import Tooltip from 'rc-tooltip';
import EventListener from 'fbjs/lib/EventListener';
import { KeyCodes } from '../../constants/ActorAppConstants';
import QuickSearchActionCreators from '../../actions/QuickSearchActionCreators';

class QuickSearchButton extends Component {
  constructor(props) {
    super(props);

    this.openQuickSearch = this.openQuickSearch.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
  }

  componentDidMount() {
    this.setListeners();
  }

  componentWillUnmount() {
    this.cleanListeners();
  }

  setListeners() {
    this.cleanListeners();
    this.listeners = [
      EventListener.listen(document, 'keydown', this.handleKeyDown)
    ];
  }

  cleanListeners() {
    if (this.listeners) {
      this.listeners.forEach((listener) => listener.remove());
      this.listeners = null;
    }
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.K && event.metaKey) {
      event.stopPropagation();
      event.preventDefault();
      this.openQuickSearch()
    }
  }

  openQuickSearch() {
    QuickSearchActionCreators.show();
  }

  render() {
    return (
      <footer className="sidebar__quick-search">
        <Tooltip
          placement="top"
          mouseEnterDelay={0.15}
          mouseLeaveDelay={0}
          overlay={<FormattedMessage id="tooltip.quicksearch"/>}>
          <a onClick={this.openQuickSearch}>
            <div className="icon-holder"><i className="material-icons">search</i></div>
            <FormattedMessage id="button.quickSearch"/>
          </a>
        </Tooltip>
      </footer>
    )
  }
}

export default QuickSearchButton;
