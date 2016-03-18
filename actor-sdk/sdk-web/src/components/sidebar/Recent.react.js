/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { FormattedMessage } from 'react-intl';
import history from '../../utils/history';

import GroupListActionCreators from '../../actions/GroupListActionCreators';
import ContactActionCreators from '../../actions/ContactActionCreators';

import RecentGroup from './RecentGroup.react';

class Recent extends Component {
  static propTypes = {
    currentPeer: PropTypes.object,
    dialogs: PropTypes.array.isRequired,
    archive: PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      haveUnreadAbove: false,
      haveUnreadBelow: false,
      lastUnreadBelow: null,
      firstUnreadAbove: null
    };

    this.getScrollNodeAndRect = this.getScrollNodeAndRect.bind(this);
    this.checkInvisibleCounters = debounce(this.checkInvisibleCounters.bind(this), 50, {maxWait: 150, leading: true});
    this.scrollToFirstHiddenAbove = this.scrollToFirstHiddenAbove.bind(this);
    this.scrollToLastHiddenBelow = this.scrollToLastHiddenBelow.bind(this);
    this.handleGroupListTitleClick = this.handleGroupListTitleClick .bind(this);
    this.handlePrivateListTitleClick = this.handlePrivateListTitleClick.bind(this);
    this.handleHistoryClick = this.handleHistoryClick.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.dialogs !== this.props.dialogs) this.checkInvisibleCounters();
  }

  handleGroupListTitleClick () {
    GroupListActionCreators.open();
  }
  handlePrivateListTitleClick() {
    ContactActionCreators.open();
  }
  handleHistoryClick() {
    history.push('/im/history')
  }

  checkInvisibleCounters() {
    const { recentNode, recentRect } = this.getScrollNodeAndRect();
    const unreadNodes = recentNode.getElementsByClassName('recent__item--unread');

    let haveUnreadAbove = false,
        haveUnreadBelow = false,
        lastUnreadBelow = null,
        firstUnreadAbove = null;

    forEach(unreadNodes, (node) => {
      const rect = node.getBoundingClientRect();
      if ((recentRect.top + recentRect.height) < rect.top) {
        haveUnreadBelow = true;
        lastUnreadBelow = node;
      }
      if (recentRect.top > (rect.top + rect.height)) {
        haveUnreadAbove = true;
        if (!firstUnreadAbove) {
          firstUnreadAbove = node;
        }
      }
    });

    this.setState({ haveUnreadAbove, haveUnreadBelow, firstUnreadAbove, lastUnreadBelow });
  };

  getScrollNodeAndRect() {
    const recentNode = findDOMNode(this.refs.scroll);
    const recentRect = recentNode.getBoundingClientRect();

    return {recentNode, recentRect}
  }

  scrollToFirstHiddenAbove() {
    const { firstUnreadAbove } = this.state;
    const rect = firstUnreadAbove.getBoundingClientRect();
    const { recentNode, recentRect } = this.getScrollNodeAndRect();

    recentNode.scrollTop = recentNode.scrollTop + rect.top - recentRect.top;
  };

  scrollToLastHiddenBelow() {
    const { lastUnreadBelow } = this.state;
    const rect = lastUnreadBelow.getBoundingClientRect();
    const { recentNode, recentRect } = this.getScrollNodeAndRect();

    recentNode.scrollTop = recentNode.scrollTop + rect.top - (recentRect.top + recentRect.height - rect.height);
  };

  renderRecentGroups() {
    const { currentPeer, archive } = this.props;
    return this.props.dialogs.map((dialogGroup) => {

      let titleClickHandler;
      switch (dialogGroup.key) {
        case 'groups':
          titleClickHandler = this.handleGroupListTitleClick;
          break;
        case 'privates':
          titleClickHandler = this.handlePrivateListTitleClick;
          break;
      }

      return (
        <RecentGroup
          {...dialogGroup}
          dialogKey={dialogGroup.key}
          currentPeer={currentPeer}
          archive={archive}
          titleClickHandler={titleClickHandler}
        />
      );
    });
  }

  renderUnreadAbove() {
    if (!this.state.haveUnreadAbove) return null;

    return (
      <div className="recent__unread recent__unread--above" onClick={this.scrollToFirstHiddenAbove}>
        <i className="material-icons">keyboard_arrow_up</i>
      </div>
    )
  };

  renderUnreadBelow() {
    if (!this.state.haveUnreadBelow) return null;

    return (
      <div className="recent__unread recent__unread--below" onClick={this.scrollToLastHiddenBelow}>
        <i className="material-icons">keyboard_arrow_down</i>
      </div>
    )
  };

  renderHistoryButton() {
    const isArchiveEmpty = false; // TODO: Use real flag
    if (isArchiveEmpty) return null;

    return (
      <div className="recent__history" onClick={this.handleHistoryClick}>
        <div className="recent__history__icon">
          <i className="material-icons">history</i>
        </div>
        <div className="recent__history__title">
          <FormattedMessage id="sidebar.recents.history"/>
        </div>
      </div>
    );
  }

  render() {
    return (
      <section className="recent">
        {this.renderUnreadAbove()}
        <div className="recent__container fill" ref="scroll" onScroll={this.checkInvisibleCounters}>
          {this.renderRecentGroups()}
          {this.renderHistoryButton()}
        </div>
        {this.renderUnreadBelow()}
      </section>
    );
  }
}

export default Recent;
