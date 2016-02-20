/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach, map, throttle } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import Scrollbar from '../common/Scrollbar.react';

import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import ContactActionCreators from '../../actions/ContactActionCreators';
import GroupListActionCreators from '../../actions/GroupListActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';

import AllDialogsStore from '../../stores/AllDialogsStore';

import RecentItem from './RecentItem.react';

class Recent extends Component {
  constructor(props) {
    super(props);
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores = () => [AllDialogsStore];

  static calculateState() {
    return {
      allDialogs: AllDialogsStore.getAllDialogs()
    };
  }

  componentDidUpdate() {
    setTimeout(() => this.checkInvisibleCounters(), 1)
  }

  handleCreateGroup = () => CreateGroupActionCreators.open();

  handleCreatePrivate = () => AddContactActionCreators.open();

  handleGroupListClick = () => GroupListActionCreators.open();

  handlePrivateListClick = () => ContactActionCreators.open();

  handleRecentScroll = throttle((event) => {
    this.checkInvisibleCounters();
  }, 100, {trailing: true});

  checkInvisibleCounters = () => {
    const unreadNodes = document.getElementsByClassName('sidebar__list__item--unread');
    const scrollNode = findDOMNode(this.refs.container);
    const scrollNodeRect = scrollNode.getBoundingClientRect();

    let haveUnreadAbove = false,
        haveUnreadBelow = false,
        lastUnreadBelow,
        firstUnreadAbove;

    forEach(unreadNodes, (node) => {
      const rect = node.getBoundingClientRect();
      if ((scrollNodeRect.top + scrollNodeRect.height) < rect.top) {
        haveUnreadBelow = true;
        lastUnreadBelow = node;
      }
      if (scrollNodeRect.top > (rect.top + rect.height)) {
        haveUnreadAbove = true;
        if (!firstUnreadAbove) {
          firstUnreadAbove = node;
        }
      }
    });

    this.setState({haveUnreadAbove, haveUnreadBelow, firstUnreadAbove, lastUnreadBelow})
  };

  scrollToFirstHiddenAbove = () => {
    const { firstUnreadAbove } = this.state;
    const rect = firstUnreadAbove.getBoundingClientRect();
    const scrollNode = findDOMNode(this.refs.container).getElementsByClassName('ss-content')[0];
    const scrollNodeRect = scrollNode.getBoundingClientRect();

    this.refs.container.scrollTo(scrollNode.scrollTop + rect.top - scrollNodeRect.top)
  };

  scrollToLastHiddenBelow = () => {
    const { lastUnreadBelow } = this.state;
    const rect = lastUnreadBelow.getBoundingClientRect();
    const scrollNode = findDOMNode(this.refs.container).getElementsByClassName('ss-content')[0];
    const scrollNodeRect = scrollNode.getBoundingClientRect();

    this.refs.container.scrollTo(scrollNode.scrollTop + rect.top - (scrollNodeRect.top + scrollNodeRect.height - rect.height));
  };

  render() {
    const { allDialogs, haveUnreadAbove, haveUnreadBelow } = this.state;
    const { intl } = this.context;

    const recentGroups = map(allDialogs, (dialogGroup, index) => {
      let groupTitle;
      switch (dialogGroup.key) {
        case 'groups':
          groupTitle = (
            <li className="sidebar__list__title">
              <a onClick={this.handleGroupListClick}>{intl.messages[`sidebar.recents.${dialogGroup.key}`]}</a>
              <i className="material-icons sidebar__list__title__icon pull-right"
                 onClick={this.handleCreateGroup}>add_circle_outline</i>
            </li>
          );
          break;
        case 'privates':
          groupTitle = (
            <li className="sidebar__list__title">
              <a onClick={this.handlePrivateListClick}>{intl.messages[`sidebar.recents.${dialogGroup.key}`]}</a>
              <i className="material-icons sidebar__list__title__icon pull-right"
                 onClick={this.handleCreatePrivate}>add_circle_outline</i>
            </li>
          );
          break;
        default:
          groupTitle = <li className="sidebar__list__title">{intl.messages[`sidebar.recents.${dialogGroup.key}`]}</li>;
      }

      const groupList = map(dialogGroup.shorts, (dialog, index) => <RecentItem dialog={dialog}
                                                                               key={index}
                                                                               type={dialogGroup.key}/>);

      return (
        <ul className={`sidebar__list sidebar__list--${dialogGroup.key}`} key={index}>
          {groupTitle}
          {groupList}
        </ul>
      )
    });

    return (
      <section className="sidebar__recent">
        {
          haveUnreadAbove
            ? <div className="sidebar__recent__unread sidebar__recent__unread--above" onClick={this.scrollToFirstHiddenAbove}>
            <i className="material-icons">keyboard_arrow_up</i>
          </div>
            : null
        }
        <Scrollbar ref="container" onScroll={this.handleRecentScroll}>
          <div>{recentGroups}</div>
        </Scrollbar>
        {
          haveUnreadBelow
            ? <div className="sidebar__recent__unread sidebar__recent__unread--below" onClick={this.scrollToLastHiddenBelow}>
            <i className="material-icons">keyboard_arrow_down</i>
          </div>
            : null
        }
      </section>
    );
  }
}

export default Container.create(Recent, {pure: false});
