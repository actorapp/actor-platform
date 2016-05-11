/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach, throttle } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

import GroupListActionCreators from '../../actions/GroupListActionCreators';
import ContactActionCreators from '../../actions/ContactActionCreators';
import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';

import CustomScroller from '../common/CustomScroller.react';
import RecentGroup from './RecentGroup.react';
import SidebarLink from './SidebarLink.react';

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

    this.checkInvisibleCounters = throttle(this.checkInvisibleCounters.bind(this), 300);
    this.scrollToFirstHiddenAbove = this.scrollToFirstHiddenAbove.bind(this);
    this.scrollToLastHiddenBelow = this.scrollToLastHiddenBelow.bind(this);
    this.handleGroupListTitleClick = this.handleGroupListTitleClick.bind(this);
    this.handlePrivateListTitleClick = this.handlePrivateListTitleClick.bind(this);
  }

  handleGroupListTitleClick () {
    GroupListActionCreators.open();
  }

  handlePrivateListTitleClick() {
    ContactActionCreators.open();
  }

  handleAddContact() {
    AddContactActionCreators.open();
  }

  handleCreateGroup() {
    CreateGroupActionCreators.open();
  }

  checkInvisibleCounters() {
    const { scroller } = this.refs;
    const recentRect = scroller.getBoundingClientRect();
    // TODO: refactor this
    const unreadNodes = scroller.container.getElementsByClassName('recent__item--unread');

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
  }

  scrollToFirstHiddenAbove() {
    const { scroller } = this.refs;
    const { firstUnreadAbove } = this.state;
    const rect = firstUnreadAbove.getBoundingClientRect();
    const dimensions = scroller.getDimensions();
    const scrollerRect = scroller.getBoundingClientRect();

    scroller.scrollTo(dimensions.scrollTop + rect.top - scrollerRect.top);
  }

  scrollToLastHiddenBelow() {
    const { scroller } = this.refs;
    const { lastUnreadBelow } = this.state;
    const rect = lastUnreadBelow.getBoundingClientRect();
    const dimensions = scroller.getDimensions();
    const scrollerRect = scroller.getBoundingClientRect();

    scroller.scrollTo(dimensions.scrollTop + rect.top - (scrollerRect.top + scrollerRect.height - rect.height));
  }

  getGroupProps(group) {
    switch (group.key) {
      case 'groups':
        return {
          onTitleClick: this.handleGroupListTitleClick,
          onPlusClick: this.handleCreateGroup,
          renderEmptyHint: this.renderGroupHint
        };

      case 'privates':
        return {
          onTitleClick: this.handlePrivateListTitleClick,
          onPlusClick: this.handleAddContact,
          renderEmptyHint: this.renderPrivateHint
        };

      default:
        return {};
    }
  }

  renderGroupHint() {
    return (
      <div className="recent__group__hint">
        <FormattedMessage id="sidebar.group.empty"/>
        <div className="stem"/>
      </div>
    );
  }

  renderPrivateHint() {
    return (
      <div className="recent__group__hint">
        <FormattedMessage id="sidebar.private.empty"/>
        <button className="button button--outline button--wide hide">
          <FormattedMessage id="button.invite"/>
        </button>
      </div>
    );
  }

  renderRecentGroups() {
    const { currentPeer, archive } = this.props;
    return this.props.dialogs.map((group) => (
      <RecentGroup
        items={group.shorts}
        key={group.key}
        group={group.key}
        currentPeer={currentPeer}
        archive={archive}
        {...this.getGroupProps(group)}
        onItemUpdate={this.checkInvisibleCounters}
      />
    ));
  }

  renderUnreadAbove() {
    if (!this.state.haveUnreadAbove) return null;

    return (
      <div className="recent__unread recent__unread--above" onClick={this.scrollToFirstHiddenAbove}>
        <i className="material-icons">keyboard_arrow_up</i>
      </div>
    )
  }

  renderUnreadBelow() {
    if (!this.state.haveUnreadBelow) return null;

    return (
      <div className="recent__unread recent__unread--below" onClick={this.scrollToLastHiddenBelow}>
        <i className="material-icons">keyboard_arrow_down</i>
      </div>
    )
  }

  renderHistoryButton() {
    // actually this is hack, but it's ok while we haven't real flag
    const isArchiveEmpty = this.props.dialogs.some((group) => !group.shorts.length);
    if (isArchiveEmpty) {
      return null;
    }

    return (
      <SidebarLink
        className="sidebar__history"
        to="/im/history"
        title={<FormattedMessage id="sidebar.recents.history" />}
        glyph="history"
        key="history"
      />
    );
  }

  renderScrollableContent() {
    return [
      this.renderRecentGroups(),
      this.renderHistoryButton()
    ];
  }

  render() {
    return (
      <section className="recent">
        {this.renderUnreadAbove()}
        <CustomScroller
          className="recent__container"
          ref="scroller"
          onScroll={this.checkInvisibleCounters}
          onResize={this.checkInvisibleCounters}
        >
          {this.renderScrollableContent()}
        </CustomScroller>
        {this.renderUnreadBelow()}
      </section>
    );
  }
}

export default Recent;
