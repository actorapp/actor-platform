/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { escapeWithEmoji } from '../utils/EmojiUtils';

import ActivityActionCreators from '../actions/ActivityActionCreators';
import FavoriteActionCreators from '../actions/FavoriteActionCreators';

import AvatarItem from '../components/common/AvatarItem.react';

import DialogInfoStore from '../stores/DialogInfoStore';
import OnlineStore from '../stores/OnlineStore';
import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';

class ToolbarSection extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [DialogInfoStore, ActivityStore, OnlineStore, DialogStore];

  static calculateState() {
    const thisPeer = DialogStore.getCurrentPeer();

    return {
      thisPeer,
      dialogInfo: DialogInfoStore.getInfo(),
      isActivityOpen: ActivityStore.isOpen(),
      message: OnlineStore.getMessage(),
      isFavorite: DialogStore.isFavorite(thisPeer.id)
    };
  }

  static contextTypes = {
    isExperimental: PropTypes.bool
  };

  handleFavorite = (event) => {
    const { thisPeer } = this.state;
    FavoriteActionCreators.favoriteChat(thisPeer);
  };

  handleUnfavorite = (event) => {
    const { thisPeer } = this.state;
    FavoriteActionCreators.unfavoriteChat(thisPeer);
  };

  onClick = () => {
    if (!this.state.isActivityOpen) {
      ActivityActionCreators.show();
    } else {
      ActivityActionCreators.hide();
    }
  };

  render() {
    const { dialogInfo, isActivityOpen, message, isFavorite } = this.state;
    const { isExperimental } = this.context;

    const infoButtonClassName = classnames('button button--icon', {
      'active': isActivityOpen
    });

    const favoriteClassName = classnames('toolbar__peer__favorite', {
      'toolbar__peer__favorite--active': isFavorite
    });

    if (dialogInfo !== null) {
      return (
        <header className="toolbar row">
          <AvatarItem image={dialogInfo.avatar}
                      placeholder={dialogInfo.placeholder}
                      size="medium"
                      title={dialogInfo.name}/>


          <div className="toolbar__peer col-xs">
            <header>
              <span className="toolbar__peer__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialogInfo.name)}}/>
              <span className={favoriteClassName}>
                {
                  isFavorite
                    ? <i className="material-icons" onClick={this.handleUnfavorite}>star</i>
                    : <i className="material-icons" onClick={this.handleFavorite}>star_border</i>
                }
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
    } else {
      return (
        <header className="toolbar"/>
      );
    }
  }
}

export default Container.create(ToolbarSection, {pure: false});
