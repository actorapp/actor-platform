/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React, { Component } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import DialogActionCreators from 'actions/DialogActionCreators';

import DialogStore from 'stores/DialogStore';

import RecentSectionItem from './RecentSectionItem.react';

const LoadDialogsScrollBottom = 100;

const getStateFromStore = () => {
  return {
    dialogs: DialogStore.getAll()
  };
};

@ReactMixin.decorate(IntlMixin)
class RecentSection extends Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    DialogStore.addChangeListener(this.onChange);
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

  render() {
    const { dialogs } = this.state;

    const dialogList = _.map(dialogs, (dialog, index) => {
      return (
        <RecentSectionItem dialog={dialog} key={index}/>
      );
    }, this);

    return (
      <section className="sidebar__recent">
        <ul className="sidebar__list sidebar__list--recent" onScroll={this.onScroll}>
          {dialogList}
        </ul>
        <footer>
        </footer>
      </section>
    );
  }
}

export default RecentSection;
