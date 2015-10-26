/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React, { Component } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import DialogActionCreators from 'actions/DialogActionCreators';
import FastSwitcherActionCreators from 'actions/FastSwitcherActionCreators';

import DialogStore from 'stores/DialogStore';
import FastSwitcherStore from 'stores/FastSwitcherStore';

import RecentSectionItem from './RecentSectionItem.react';
import ContactsSectionItem from './ContactsSectionItem.react';
import FastSwitcherModal from 'components/modals/FastSwitcher.react';

const LoadDialogsScrollBottom = 100;

const getStateFromStore = () => {
  return {
    dialogs: DialogStore.getAll(),
    isFastSwitcherOpen: FastSwitcherStore.isOpen()
  };
};

@ReactMixin.decorate(IntlMixin)
class RecentSection extends Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    DialogStore.addChangeListener(this.onChange);
    FastSwitcherStore.addListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeChangeListener(this.onChange);
  }

  onChange = () => {
    this.setState(getStateFromStore());
  };

  onScroll = event => {
    const { scrollHeight, scrollTop, clientHeight } = event.target;

    if (scrollHeight - scrollTop - clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  };

  openFastSwitch = () => FastSwitcherActionCreators.show();

  render() {
    const { dialogs, isFastSwitcherOpen } = this.state;

    const dialogList = _.map(dialogs, (dialog, index) => {
      return (
        <RecentSectionItem dialog={dialog} key={index}/>
      );
    }, this);

    const fastSwitch = isFastSwitcherOpen ? <FastSwitcherModal/> : null;

    return (
      <section className="sidebar__recent">
        <div className="sidebar__recent__scroll-container" onScroll={this.onScroll}>
          <ul className="sidebar__list">
            {dialogList}
          </ul>

        </div>

        <footer>
          <button className="button button--rised button--wide" onClick={this.openFastSwitch}>Fast Switch</button>
          {fastSwitch}
        </footer>
      </section>
    );
  }
}

export default RecentSection;
