/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { FormattedMessage } from 'react-intl';
import { escapeWithEmoji } from '../utils/EmojiUtils';
import PeerUtils from '../utils/PeerUtils';

import ActivityActionCreators from '../actions/ActivityActionCreators';
import FavoriteActionCreators from '../actions/FavoriteActionCreators';
import CallActionCreators from '../actions/CallActionCreators';

import AvatarItem from './common/AvatarItem.react';
import ToggleFavorite from './common/ToggleFavorite.react';

import DialogInfoStore from '../stores/DialogInfoStore';
import OnlineStore from '../stores/OnlineStore';
import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';
import CallStore from '../stores/CallStore';

class ToolbarSection extends Component {
  static getStores() {
    return [DialogInfoStore, ActivityStore, OnlineStore, DialogStore, CallStore];
  }

  static calculateState() {
    const thisPeer = DialogStore.getCurrentPeer();
    return {
      thisPeer,
      dialogInfo: DialogInfoStore.getInfo(),
      isActivityOpen: ActivityStore.isOpen(),
      message: OnlineStore.getMessage(),
      isFavorite: DialogStore.isFavorite(thisPeer.id),
      call: ToolbarSection.calculateCallState(thisPeer)
    };
  }

  static calculateCallState(thisPeer) {
    const isCalling = CallStore.isOpen();
    if (!isCalling) {
      return {isCalling};
    }

    const callPeer = CallStore.getPeer();
    const isSamePeer = PeerUtils.equals(thisPeer, callPeer);
    if (!isSamePeer) {
      return {isCalling: false};
    }

    return {
      isCalling,
      state: CallStore.getState(),
      isFloating: CallStore.isFloating(),
      time: '00:00'
    };
  }

  static contextTypes = {
    isExperimental: PropTypes.bool
  };

  onFavoriteToggle = () => {
    const { thisPeer, isFavorite } = this.state;
    if (isFavorite) {
      FavoriteActionCreators.unfavoriteChat(thisPeer);
    } else {
      FavoriteActionCreators.favoriteChat(thisPeer);
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

  getMessage() {
    const { call, message } = this.state;
    if (call.isCalling) {
      return (
        <FormattedMessage id={`toolbar.callState.${call.state}`} values={{time: call.time}} />
      );
    }

    return message;
  }

  renderInfoButton() {
    const { call, isActivityOpen } = this.state;

    const activityButtonClassName = classnames('button button--icon', {
      'active': isActivityOpen || (call.isCalling && !call.isFloating)
    });

    if (call.isCalling) {
      return (
        <button className={activityButtonClassName} onClick={this.handleInCallClick}>
          <i className="material-icons">info</i>
        </button>
      )
    }

    return (
      <button className={activityButtonClassName} onClick={this.onClick}>
        <i className="material-icons">info</i>
      </button>
    )
  }

  render() {
    const { dialogInfo, isActivityOpen, isFavorite, call } = this.state;

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
        <AvatarItem image={dialogInfo.avatar}
                    placeholder={dialogInfo.placeholder}
                    size="medium"
                    title={dialogInfo.name}/>


        <div className="toolbar__peer col-xs">
          <header>
            <span className="toolbar__peer__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialogInfo.name)}}/>
            <span className={favoriteClassName}>
              <ToggleFavorite value={isFavorite} onToggle={this.onFavoriteToggle} />
            </span>
          </header>
          <div className="toolbar__peer__message">{message}</div>
        </div>

        <div className="toolbar__controls">
          <div className="toolbar__controls__buttons pull-right">
            {this.renderInfoButton()}
          </div>
        </div>
      </header>
    );
  }
}

export default Container.create(ToolbarSection, {pure: false});
