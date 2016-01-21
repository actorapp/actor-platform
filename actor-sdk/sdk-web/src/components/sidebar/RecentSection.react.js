/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component } from 'react';
import Scrollbar from '../common/Scrollbar.react';
import { Container } from 'flux/utils';

import DialogActionCreators from '../../actions/DialogActionCreators';

import AllDialogsStore from '../../stores/AllDialogsStore';

import RecentSectionItem from './RecentSectionItem.react';

const LoadDialogsScrollBottom = 100;

class RecentSection extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [AllDialogsStore];

  static calculateState() {
    return {
      dialogs: AllDialogsStore.getAllDialogs()
    };
  }

  componentDidUpdate() {
    const listRect = React.findDOMNode(this.refs.list).getBoundingClientRect();
    const recentRect = React.findDOMNode(this.refs.recent).getBoundingClientRect();

    if (listRect.height < recentRect.height) {
      DialogActionCreators.onDialogsEnd();
    }
  }

  onScroll = (event) => {
    const { scrollHeight, scrollTop, clientHeight } = event.target;

    if (scrollHeight - scrollTop - clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  };

  render() {
    const { dialogs } = this.state;

    const dialogList = map(dialogs, (dialog, index) => <RecentSectionItem dialog={dialog} key={index}/>);

    return (
      <section className="sidebar__recent" ref="recent">
        <Scrollbar onScroll={this.onScroll}>
          <ul className="sidebar__list" ref="list">
            {dialogList}
          </ul>
        </Scrollbar>
      </section>
    );
  }
}

export default Container.create(RecentSection, { pure: false });
