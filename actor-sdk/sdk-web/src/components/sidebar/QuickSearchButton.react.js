/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';

import QuickSearchActionCreators from '../../actions/QuickSearchActionCreators';

import QuickSearchStore from '../../stores/QuickSearchStore';

import QuickSearch from '../modals/QuickSearch.react';
import Tooltip from 'rc-tooltip';

class QuickSearchButton extends Component {
  static getStores() {
    return [QuickSearchStore]
  }

  static calculateState() {
    return {
      isQuickSearchOpen: QuickSearchStore.isOpen()
    };
  }

  openQuickSearch = () => QuickSearchActionCreators.show();

  render() {
    const { isQuickSearchOpen } = this.state;

    return (
      <footer className="sidebar__quick-search">
        <Tooltip
          placement="top"
          mouseEnterDelay={0.15} mouseLeaveDelay={0}
          overlay={<FormattedMessage id="tooltip.quicksearch"/>}
        >
          <a onClick={this.openQuickSearch}>
            <div className="icon-holder"><i className="material-icons">search</i></div>
            <FormattedMessage id="button.quickSearch"/>
          </a>
        </Tooltip>

        {isQuickSearchOpen ? <QuickSearch/> : null}
      </footer>
    )
  }
}

export default Container.create(QuickSearchButton, {pure: false});
