/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

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

  openQuickSearch = () => QuickSearchActionCreators.show();

  render() {
    const { isQuickSearchOpen } = this.state;

    return (
      <footer className="sidebar__quick-search" >
        <a onClick={this.openQuickSearch}>
          <div className="icon-holder"><i className="material-icons">search</i></div>
          {this.getIntlMessage('button.quickSearch')}
        </a>
        {isQuickSearchOpen ? <QuickSearch/> : null}
      </footer>
    )
  }
}

ReactMixin.onClass(QuickSearchButton, IntlMixin);

export default Container.create(QuickSearchButton, {pure: false});
