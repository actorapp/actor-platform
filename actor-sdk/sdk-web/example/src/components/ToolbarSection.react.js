/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classnames from 'classnames';
import { escapeWithEmoji } from 'actor-sdk/build/utils/EmojiUtils';

import ActivityActionCreators from 'actor-sdk/build/actions/ActivityActionCreators';

import DialogStore from 'actor-sdk/build/stores/DialogStore';
import ActivityStore from 'actor-sdk/build/stores/ActivityStore';

import AvatarItem from 'actor-sdk/build/components/common/AvatarItem.react';

const getStateFromStores = () => {
  return {
    dialogInfo: DialogStore.getSelectedDialogInfo(),
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

    DialogStore.addSelectedChangeListener(this.onChange);
    ActivityStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectedChangeListener(this.onChange);
    ActivityStore.removeChangeListener(this.onChange);
  }

  onClick = () => {
    if (this.state.isActivityOpen) {
      ActivityActionCreators.hide();
    } else {
      ActivityActionCreators.show();
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
          <AvatarItem image={dialogInfo.avatar}
                      placeholder={dialogInfo.placeholder}
                      title={dialogInfo.name}/>

          <div className="toolbar__peer col-xs">
            <span className="toolbar__peer__title"
                  dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialogInfo.name)}}/>
            <span className="toolbar__peer__presence">{dialogInfo.presence}</span>
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

export default ToolbarSection;
