/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { FormattedMessage } from 'react-intl';
import { escapeWithEmoji } from '../utils/EmojiUtils';

import ActivityActionCreators from '../actions/ActivityActionCreators';
import FavoriteActionCreators from '../actions/FavoriteActionCreators';

import AvatarItem from './common/AvatarItem.react';
import ToggleFavorite from './common/ToggleFavorite.react';

import DialogInfoStore from '../stores/DialogInfoStore';
import OnlineStore from '../stores/OnlineStore';
import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';
import CallStore from '../stores/CallStore';

class ToolbarSection extends Component {
  static getStores = () => [DialogInfoStore, ActivityStore, OnlineStore, DialogStore, CallStore];

  static calculateState() {
    const thisPeer = DialogStore.getCurrentPeer();

    return {
      thisPeer,
      dialogInfo: DialogInfoStore.getInfo(),
      isActivityOpen: ActivityStore.isOpen(),
      message: OnlineStore.getMessage(),
      isFavorite: DialogStore.isFavorite(thisPeer.id),
      isCalling: CallStore.isOpen(),
      callState: CallStore.getState()
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

  getMessage() {
    const { isCalling, callState, message } = this.state;
    if (isCalling) {
      return (
        <FormattedMessage id={`toolbar.callState.${callState}`} values={{time: '00:00'}} />
      );
    }

    return message;
  }

  render() {
    const { dialogInfo, isActivityOpen, isFavorite } = this.state;

    if (!dialogInfo) {
      return <header className="toolbar" />;
    }

    const message = this.getMessage();

    const headerClassName = classnames('toolbar row', {
      toolbar__calling: this.state.isCalling
    });

    const infoButtonClassName = classnames('button button--icon', {
      'active': isActivityOpen
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
            <button className={infoButtonClassName} onClick={this.onClick}>
              <i className="material-icons">info</i>
            </button>
            <button className="button button--icon hide">
              <i className="material-icons">more_vert</i>
            </button>
          </div>
        </div>
      </header>
    );
  }
}

export default Container.create(ToolbarSection, {pure: false});
