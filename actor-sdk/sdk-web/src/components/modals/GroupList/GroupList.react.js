/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedHTMLMessage } from 'react-intl';

import GroupListActionCreators from '../../../actions/GroupListActionCreators'
import DialogActionCreators from '../../../actions/DialogActionCreators'

import GroupListStore from '../../../stores/GroupListStore';

import Group from './Group.react';

class GroupList extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [GroupListStore];

  static calculateState() {
    return {
      groups: GroupListStore.getList(),
      searchQuery: GroupListStore.getSearchQuery()
    };
  }

  componentDidMount() {
    React.findDOMNode(this.refs.search).focus();
  }

  handleClose = () => GroupListActionCreators.close();
  handleSearchChange = (event) => GroupListActionCreators.search(event.target.value);

  handleGroupSelect = (peer) => {
    DialogActionCreators.selectDialogPeer(peer);
    this.handleClose()
  };


  render() {
    const { groups, searchQuery } = this.state;

    let groupList = [];

    forEach(groups, (group, i) => {
      const title = group.peerInfo.title.toLowerCase();
      if (title.includes(searchQuery.toLowerCase())) {
        groupList.push(
          <Group group={group} key={i} onClick={this.handleGroupSelect}/>
        );
      }
    }, this);

    if (groupList.length === 0) {
      groupList.push(
        <li className="group__list__item group__list__item--empty text-center">
          <FormattedHTMLMessage
            message={this.getIntlMessage('modal.groups.notFound')}
            query={searchQuery} />
        </li>
      );
    }

    return (
      <div className="newmodal newmodal__groups">
        <header className="newmodal__header">
          <h2>{this.getIntlMessage('modal.groups.title')}</h2>
        </header>

        <section className="newmodal__search">
          <input className="newmodal__search__input"
                 onChange={this.handleSearchChange}
                 placeholder={this.getIntlMessage('modal.groups.search')}
                 type="search"
                 ref="search"
                 value={searchQuery}/>
        </section>

        <ul className="newmodal__result group__list">
          {
            groups.length === 0
              ? <div>{this.getIntlMessage('modal.groups.loading')}</div>
              : groupList
          }
        </ul>
      </div>
    )
  }
}

ReactMixin.onClass(GroupList, IntlMixin);

export default Container.create(GroupList, {pure: false});
