/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';

import DialogActionCreators from '../../actions/DialogActionCreators';
import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';

import AllDialogsStore from '../../stores/AllDialogsStore';

import Scrollbar from '../common/Scrollbar.react';
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

  static contextTypes = {
    intl: PropTypes.object
  };

  componentDidUpdate() {
    const { dialogs } = this.state;
    if (dialogs.length !== 0) {
      const listRect = findDOMNode(this.refs.list).getBoundingClientRect();
      const recentRect = findDOMNode(this.refs.recent).getBoundingClientRect();

      if (listRect.height < recentRect.height) {
        DialogActionCreators.onDialogsEnd();
      }
    }
  }

  onScroll = (event) => {
    const { scrollHeight, scrollTop, clientHeight } = event.target;

    if (scrollHeight - scrollTop - clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  };

  handleCreateGroupClick = () => CreateGroupActionCreators.open();
  handleAddPeopleClick = () => AddContactActionCreators.open();

  render() {
    const { dialogs } = this.state;
    const { intl } = this.context;

    const dialogList = map(dialogs, (dialog, index) => <RecentSectionItem dialog={dialog} key={index}/>);

    return (
      <section className="sidebar__recent" ref="recent">
        {
          dialogList.length !== 0
            ? <Scrollbar onScroll={this.onScroll}>
                <ul className="sidebar__list" ref="list">
                  {dialogList}
                </ul>
              </Scrollbar>
            : <div className="sidebar__recent__empty row center-xs middle-xs">
                <div>
                  <p>{intl.messages['sidebar.recents.empty.first']}</p>
                  <p>
                    {intl.messages['sidebar.recents.empty.second.start']}
                    <a onClick={this.handleCreateGroupClick}>{intl.messages['sidebar.recents.newDialog']}</a>
                    {intl.messages['sidebar.recents.empty.second.or']}
                    <a onClick={this.handleAddPeopleClick}>{intl.messages['sidebar.recents.addPeople']}</a>
                    {intl.messages['sidebar.recents.empty.second.end']}
                  </p>
                </div>
              </div>
        }
      </section>
    );
  }
}

export default Container.create(RecentSection, { pure: false });
