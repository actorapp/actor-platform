/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import QuickSearchActionCreators from '../../actions/QuickSearchActionCreators';

import QuickSearchStore from '../../stores/QuickSearchStore';

import QuickSearch from '../modals/QuickSearch.react';

class QuickSearchButton extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [QuickSearchStore];

  static calculateState() {
    return {
      isQuickSearchOpen: QuickSearchStore.isOpen()
    };
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  openQuickSearch = () => QuickSearchActionCreators.show();

  render() {
    const { isQuickSearchOpen } = this.state;
    const { intl } = this.context;

    return (
      <footer className="sidebar__quick-search" >
        <a onClick={this.openQuickSearch}>
          <div className="icon-holder"><i className="material-icons">search</i></div>
          {intl.messages['button.quickSearch']}
        </a>
        {isQuickSearchOpen ? <QuickSearch/> : null}
      </footer>
    )
  }
}

export default Container.create(QuickSearchButton, {pure: false});
