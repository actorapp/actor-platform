/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import DialogActionCreators from '../../actions/DialogActionCreators';
import QuickSearchActionCreators from '../../actions/QuickSearchActionCreators';

import DialogStore from '../../stores/DialogStore';
import QuickSearchStore from '../../stores/QuickSearchStore';

import RecentSectionItem from './RecentSectionItem.react';
import ContactsSectionItem from './ContactsSectionItem.react';
import QuickSearch from '../modals/QuickSearch.react';

const LoadDialogsScrollBottom = 100;

const getStateFromStore = () => {
  return {
    dialogs: DialogStore.getAll(),
    isQuickSearchOpen: QuickSearchStore.isOpen()
  };
};

class RecentSection extends Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    DialogStore.addChangeListener(this.onChange);
    QuickSearchStore.addListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeChangeListener(this.onChange);
  }

  onChange = () => this.setState(getStateFromStore());

  onScroll = event => {
    const { scrollHeight, scrollTop, clientHeight } = event.target;

    if (scrollHeight - scrollTop - clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  };

  openQuickSearch = () => QuickSearchActionCreators.show();

  render() {
    const { dialogs, isQuickSearchOpen } = this.state;

    const dialogList = map(dialogs, (dialog, index) => {
      return (
        <RecentSectionItem dialog={dialog} key={index}/>
      );
    }, this);

    return (
      <section className="sidebar__recent">
        <div className="sidebar__recent__scroll-container" onScroll={this.onScroll}>
          <ul className="sidebar__list">
            {dialogList}
          </ul>
        </div>

        <footer>
          <a className="sidebar__recent__quick-search" onClick={this.openQuickSearch}>
            <div className="icon-holder">
              <i className="material-icons">search</i>
            </div>
            Quick Search
          </a>
        </footer>

        {isQuickSearchOpen ? <QuickSearch/> : null}

      </section>
    );
  }
}

ReactMixin.onClass(RecentSection, IntlMixin);

export default RecentSection;
