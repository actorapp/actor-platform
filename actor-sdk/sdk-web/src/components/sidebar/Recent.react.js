/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach, map, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Link } from 'react-router';
import { FormattedMessage } from 'react-intl';
import  classnames from 'classnames';

import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import ContactActionCreators from '../../actions/ContactActionCreators';
import GroupListActionCreators from '../../actions/GroupListActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';

import Scrollbar from '../common/Scrollbar.react';
import RecentItem from './RecentItem.react';

class Recent extends Component {
  constructor(props) {
    super(props);

    this.checkInvisibleCounters = debounce(this.checkInvisibleCounters, 50, {
      maxWait: 150,
      leading: true
    });

    this.state = {
      haveUnreadAbove: false,
      haveUnreadBelow: false,
      lastUnreadBelow: null,
      firstUnreadAbove: null
    }
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  static propTypes = {
    dialogs: PropTypes.array.isRequired
  };

  componentDidUpdate(prevProps) {
    if (prevProps !== this.props) {
      this.checkInvisibleCounters();
    }
  }

  handleCreateGroup = () => CreateGroupActionCreators.open();

  handleCreatePrivate = () => AddContactActionCreators.open();

  handleGroupListClick = () => GroupListActionCreators.open();

  handlePrivateListClick = () => ContactActionCreators.open();

  handleRecentScroll = () => this.checkInvisibleCounters();

  checkInvisibleCounters = () => {
    const unreadNodes = document.getElementsByClassName('sidebar__list__item--unread');
    const scrollNode = findDOMNode(this.refs.container);
    const scrollNodeRect = scrollNode.getBoundingClientRect();

    let haveUnreadAbove = false,
        haveUnreadBelow = false,
        lastUnreadBelow = null,
        firstUnreadAbove = null;

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

    this.setState({ haveUnreadAbove, haveUnreadBelow, firstUnreadAbove, lastUnreadBelow });
  };

  scrollToFirstHiddenAbove = () => {
    const { firstUnreadAbove } = this.state;
    const rect = firstUnreadAbove.getBoundingClientRect();
    const scrollNode = findDOMNode(this.refs.container).getElementsByClassName('ss-scrollarea')[0];
    const scrollNodeRect = scrollNode.getBoundingClientRect();

    this.refs.container.scrollTo(scrollNode.scrollTop + rect.top - scrollNodeRect.top)
  };

  scrollToLastHiddenBelow = () => {
    const { lastUnreadBelow } = this.state;
    const rect = lastUnreadBelow.getBoundingClientRect();
    const scrollNode = findDOMNode(this.refs.container).getElementsByClassName('ss-scrollarea')[0];
    const scrollNodeRect = scrollNode.getBoundingClientRect();

    this.refs.container.scrollTo(scrollNode.scrollTop + rect.top - (scrollNodeRect.top + scrollNodeRect.height - rect.height));
  };

  render() {
    const { dialogs } = this.props;
    const { haveUnreadAbove, haveUnreadBelow } = this.state;
    const { intl } = this.context;

    const recentGroups = map(dialogs, (dialogGroup, index) => {
      const isEmpty = dialogGroup.shorts.length === 0;
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

      const groupClassname = classnames(`sidebar__list sidebar__list--${dialogGroup.key}`, {
       'sidebar__list--empty': isEmpty
      });

      const getEmptyMessage = () => {
        switch (dialogGroup.key) {
          case 'groups':
            return (
              <li className="sidebar__list__item sidebar__list__item--empty">
                <FormattedMessage id="sidebar.group.empty"/>
                <div className="stem"/>
              </li>
            )
          case 'privates':
            return (
              <li className="sidebar__list__item sidebar__list__item--empty">
                <FormattedMessage id="sidebar.private.empty"/>
                <button className="button button--outline button--wide hide"><FormattedMessage id="button.invite"/></button>
              </li>
            )
          default:
            return null;
        }
      }

      return (
        <ul className={groupClassname} key={index}>
          {groupTitle}
          {
            isEmpty
              ? getEmptyMessage()
              : groupList
          }
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
          <div>
            {recentGroups}

            <footer>
              <Link to="/im/archive" className="button button--rised button--wide">
                <FormattedMessage id="button.archive"/>
              </Link>
            </footer>
          </div>
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

export default Recent;
