/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { FormattedMessage } from 'react-intl';

import GroupListActionCreators from '../../actions/GroupListActionCreators';

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
    this.handleGroupListClick = this.handleGroupListClick.bind(this);
  }

  // componentWillReceiveProps(nextProps) {
  //   if (nextProps.dialogs !== this.props.dialogs) this.checkInvisibleCounters();
  // }

  handleGroupListClick() {
    GroupListActionCreators.open();
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

      if (dialogGroup.key === 'groups') {
        return (
          <RecentGroup
            {...dialogGroup}
            currentPeer={currentPeer}
            archive={archive}
            titleClickHandler={this.handleGroupListClick}
            dialogKey={dialogGroup.key}
          />
        );
      }

      return (
        <RecentGroup
          {...dialogGroup}
          currentPeer={currentPeer}
          archive={archive}
          dialogKey={dialogGroup.key}
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

  render() {
    return (
      <section className="recent">
        {this.renderUnreadAbove()}
        <div className="recent__container fill" ref="scroll" onScroll={this.checkInvisibleCounters}>
          {this.renderRecentGroups()}
        </div>
        {this.renderUnreadBelow()}
      </section>
    );
  }
}

export default Recent;
