/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classnames from 'classnames';
import { escapeWithEmoji } from '../utils/EmojiUtils';

import ActivityActionCreators from '../actions/ActivityActionCreators';

import DialogStore from '../stores/DialogStore';
import ActivityStore from '../stores/ActivityStore';

const getStateFromStores = () => {
  return {
    dialogInfo: DialogStore.getInfo(),
    isActivityOpen: ActivityStore.isOpen()
  };
};

class ToolbarSection extends Component {
  constructor(props) {
    super(props);

    this.state = {
      dialogInfo: null,
      isActivityOpen: false
    };

    DialogStore.addListener(this.onChange);
    ActivityStore.addListener(this.onChange);
  }

  onClick = () => {
    if (!this.state.isActivityOpen) {
      ActivityActionCreators.show();
    } else {
      ActivityActionCreators.hide();
    }
  };

  onChange = () => this.setState(getStateFromStores());

  render() {
    const { dialogInfo, isActivityOpen } = this.state;

    const infoButtonClassName = classnames('button button--icon', {
      'active': isActivityOpen
    });

    if (dialogInfo !== null) {
      return (
        <header className="toolbar row">
          <div className="toolbar__peer col-xs">
            <span className="toolbar__peer__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialogInfo.name)}}/>
            <span className="toolbar__peer__presence">{dialogInfo.presence}</span>
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

export default ToolbarSection;
