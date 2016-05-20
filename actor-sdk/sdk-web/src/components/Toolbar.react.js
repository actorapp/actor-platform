/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import Tooltip from 'rc-tooltip';
import { debounce } from 'lodash';

import { escapeWithEmoji } from '../utils/EmojiUtils';
import PeerUtils from '../utils/PeerUtils';

import CallActionCreators from '../actions/CallActionCreators';
import ActivityActionCreators from '../actions/ActivityActionCreators';
import FavoriteActionCreators from '../actions/FavoriteActionCreators';
import SearchMessagesActionCreators from '../actions/SearchMessagesActionCreators';

import SearchMessagesStore from '../stores/SearchMessagesStore';
import DialogInfoStore from '../stores/DialogInfoStore';
import OnlineStore from '../stores/OnlineStore';
import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';
import CallStore from '../stores/CallStore';

import AvatarItem from './common/AvatarItem.react';
import ToggleFavorite from './common/ToggleFavorite.react';
import SearchInput from './search/SearchInput.react';

class ToolbarSection extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static getStores() {
    return [DialogInfoStore, ActivityStore, OnlineStore, DialogStore, CallStore, SearchMessagesStore];
  }

  static calculateState() {
    const peer = DialogStore.getCurrentPeer();
    if (!peer) {
      return {
        dialogInfo: null
      };
    }

    return {
      peer,
      dialogInfo: DialogInfoStore.getState(),
      isActivityOpen: ActivityStore.isOpen(),
      message: OnlineStore.getMessage(),
      isFavorite: DialogStore.isFavorite(peer.id),
      search: SearchMessagesStore.getState(),
      call: ToolbarSection.calculateCallState(peer)
    };
  }

  static calculateCallState(peer) {
    const call = CallStore.getState();
    if (!call.isOpen || !PeerUtils.equals(peer, call.peer)) {
      return {
        isCalling: false
      };
    }

    return {
      isCalling: true,
      time: call.time,
      state: call.state,
      isFloating: call.isFloating
    };
  }

  constructor(props, context) {
    super(props, context);

    this.onSearch = debounce(this.onSearch.bind(this), 300);
    this.onSearchChange = this.onSearchChange.bind(this);
    this.onSearchToggleOpen = this.onSearchToggleOpen.bind(this);
    this.onSearchToggleFocus = this.onSearchToggleFocus.bind(this);
  }

  onFavoriteToggle = () => {
    const { peer, isFavorite } = this.state;
    if (isFavorite) {
      FavoriteActionCreators.unfavoriteChat(peer);
    } else {
      FavoriteActionCreators.favoriteChat(peer);
    }
  };

  onClick = () => {
    if (!this.state.isActivityOpen) {
      ActivityActionCreators.show();
    } else {
      ActivityActionCreators.hide();
    }
  };

  handleInCallClick = () => CallActionCreators.toggleFloating();

  onSearch(query) {
    SearchMessagesActionCreators.findAllText(query);
  }

  onSearchChange(query) {
    SearchMessagesActionCreators.setQuery(query);
    this.onSearch(query);
  }

  onSearchToggleOpen(isOpen) {
    SearchMessagesActionCreators.toggleOpen(isOpen);
  }

  onSearchToggleFocus(isEnabled) {
    SearchMessagesActionCreators.toggleFocus(isEnabled);
  }

  getMessage() {
    const { call, message } = this.state;
    if (call.isCalling) {
      return (
        <FormattedMessage id={`call.state.${call.state}`} values={{ time: call.time }} />
      );
    }

    return message;
  }

  renderSearch() {
    if (!this.context.delegate.features.search) {
      return;
    }

    const { search: { query, isOpen, isFocused } } = this.state;
    return (
      <SearchInput
        className="toolbar__controls__search pull-left"
        value={query}
        isOpen={isOpen}
        isFocused={isFocused}
        onChange={this.onSearchChange}
        onToggleOpen={this.onSearchToggleOpen}
        onToggleFocus={this.onSearchToggleFocus}
      />
    );
  }

  renderInfoButton() {
    const { call, isActivityOpen } = this.state;

    const activityButtonClassName = classnames('button button--icon', {
      'active': isActivityOpen || (call.isCalling && !call.isFloating)
    });

    if (call.isCalling) {
      return (
        <Tooltip
          placement="left"
          mouseEnterDelay={0.15} mouseLeaveDelay={0}
          overlay={<FormattedMessage id="tooltip.toolbar.info"/>}
        >
          <button className={activityButtonClassName} onClick={this.handleInCallClick}>
            <i className="material-icons">info</i>
          </button>
        </Tooltip>
      )
    }

    return (
      <Tooltip
        placement="left"
        mouseEnterDelay={0.15} mouseLeaveDelay={0}
        overlay={<FormattedMessage id="tooltip.toolbar.info"/>}
      >
        <button className={activityButtonClassName} onClick={this.onClick}>
          <i className="material-icons">info</i>
        </button>
      </Tooltip>
    )
  }

  renderVerified() {
    if (!this.state.dialogInfo.isVerified) {
      return null;
    }

    return (
      <span className="toolbar__peer__verified">
        <i className="material-icons">verified_user</i>
      </span>
    );
  }

  render() {
    const { dialogInfo, isFavorite, call } = this.state;

    if (!dialogInfo) {
      return <header className="toolbar" />;
    }

    const message = this.getMessage();

    const headerClassName = classnames('toolbar row', {
      toolbar__calling: call.isCalling
    });

    const favoriteClassName = classnames('toolbar__peer__favorite', {
      'toolbar__peer__favorite--active': isFavorite
    });

    return (
      <header className={headerClassName}>
        <AvatarItem
          className="toolbar__avatar"
          size="medium"
          image={dialogInfo.avatar}
          placeholder={dialogInfo.placeholder}
          title={dialogInfo.name}
        />
        <div className="toolbar__peer col-xs">
          <header>
            <span className="toolbar__peer__title" dangerouslySetInnerHTML={{ __html: escapeWithEmoji(dialogInfo.name) }}/>
            {this.renderVerified()}
            <Tooltip
              placement="bottom"
              mouseEnterDelay={0.15} mouseLeaveDelay={0}
              overlay={<FormattedMessage id="tooltip.toolbar.favorite"/>}
            >
              <span className={favoriteClassName}>
                <ToggleFavorite value={isFavorite} onToggle={this.onFavoriteToggle} />
              </span>
            </Tooltip>
          </header>
          <div className="toolbar__peer__message">{message}</div>
        </div>

        <div className="toolbar__controls">
          {this.renderSearch()}
          <div className="toolbar__controls__buttons pull-right">
            {this.renderInfoButton()}
          </div>
        </div>
      </header>
    );
  }
}

export default Container.create(ToolbarSection, { pure: false });
