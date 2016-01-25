/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component } from 'react';
import Scrollbar from '../common/Scrollbar.react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl'

import DialogActionCreators from '../../actions/DialogActionCreators';
import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';

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
    const { dialogs } = this.state;
    if (dialogs.length !== 0) {
      const listRect = React.findDOMNode(this.refs.list).getBoundingClientRect();
      const recentRect = React.findDOMNode(this.refs.recent).getBoundingClientRect();

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
                  <p>{this.getIntlMessage('sidebar.recents.empty.first')}</p>
                  <p>
                    {this.getIntlMessage('sidebar.recents.empty.second.start')}
                    <a onClick={this.handleCreateGroupClick}>{this.getIntlMessage('sidebar.recents.newDialog')}</a>
                    {this.getIntlMessage('sidebar.recents.empty.second.or')}
                    <a onClick={this.handleAddPeopleClick}>{this.getIntlMessage('sidebar.recents.addPeople')}</a>
                    {this.getIntlMessage('sidebar.recents.empty.second.end')}
                  </p>
                </div>
              </div>
        }
      </section>
    );
  }
}

ReactMixin.onClass(RecentSection, IntlMixin);

export default Container.create(RecentSection, { pure: false });
