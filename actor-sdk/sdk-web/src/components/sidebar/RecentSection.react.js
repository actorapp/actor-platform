/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component } from 'react';
import Scrollbar from '../common/Scrollbar.react';

import DialogActionCreators from '../../actions/DialogActionCreators';

import AllDialogsStore from '../../stores/AllDialogsStore';

import RecentSectionItem from './RecentSectionItem.react';

const LoadDialogsScrollBottom = 100;

const getStateFromStore = () => {
  return {
    dialogs: AllDialogsStore.getAllDialogs()
  };
};

class RecentSection extends Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    AllDialogsStore.addListener(this.onChange);
  }

  onChange = () => this.setState(getStateFromStore());

  onScroll = event => {
    const { scrollHeight, scrollTop, clientHeight } = event.target;

    if (scrollHeight - scrollTop - clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  };

  render() {
    const { dialogs } = this.state;

    const dialogList = map(dialogs, (dialog, index) => <RecentSectionItem dialog={dialog} key={index}/>);

    return (
      <section className="sidebar__recent">
        <Scrollbar onScroll={this.onScroll}>
          <ul className="sidebar__list">
            {dialogList}
          </ul>
        </Scrollbar>
      </section>
    );
  }
}

export default RecentSection;
