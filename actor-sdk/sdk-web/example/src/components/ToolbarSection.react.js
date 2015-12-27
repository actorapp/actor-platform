/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { escapeWithEmoji } from 'actor-sdk/build/utils/EmojiUtils';

import ActivityActionCreators from 'actor-sdk/build/actions/ActivityActionCreators';

import DialogInfoStore from 'actor-sdk/build/stores/DialogInfoStore';
import ActivityStore from 'actor-sdk/build/stores/ActivityStore';
import OnlineStore from 'actor-sdk/build/stores/OnlineStore';

import AvatarItem from 'actor-sdk/build/components/common/AvatarItem.react';

class ToolbarSection extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [DialogInfoStore, ActivityStore, OnlineStore];

  static calculateState() {
    return {
      dialogInfo: DialogInfoStore.getInfo(),
      isActivityOpen: ActivityStore.isOpen(),
      message: OnlineStore.getMessage()
    };
  }

  onClick = () => {
    if (!this.state.isActivityOpen) {
      ActivityActionCreators.show();
    } else {
      ActivityActionCreators.hide();
    }
  };

  render() {
    const { dialogInfo, isActivityOpen, message } = this.state;

    const infoButtonClassName = classnames('button button--icon', {
      'active': isActivityOpen
    });

    if (dialogInfo !== null) {
      return (
        <header className="toolbar row">
          <AvatarItem image={dialogInfo.avatar}
                      placeholder={dialogInfo.placeholder}
                      title={dialogInfo.name}/>

          <div className="toolbar__peer col-xs">
            <span className="toolbar__peer__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialogInfo.name)}}/>
            <span className="toolbar__peer__message">{message}</span>
          </div>

          <div className="toolbar__controls">
            <div className="toolbar__controls__buttons pull-right">
              <button className={infoButtonClassName} onClick={this.onClick}>
                <i className="material-icons">info</i>
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
