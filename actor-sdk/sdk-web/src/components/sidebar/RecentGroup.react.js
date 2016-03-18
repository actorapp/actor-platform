/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import classnames from 'classnames';
import PeerUtils from '../../utils/PeerUtils';
import RecentItem from './RecentItem.react';

export default class RecentGroup extends Component {
  constructor(props) {
    super(props);

    this.handleTitleClick = this.handleTitleClick.bind(this);
  }

  static propTypes = {
    titleClickHandler: PropTypes.func,
    dialogKey: PropTypes.string.isRequired,
    shorts: PropTypes.array.isRequired,
    archive: PropTypes.object.isRequired,
    currentPeer: PropTypes.object
  };

  handleTitleClick(event) {
    const { titleClickHandler } = this.props;
    event.preventDefault();
    titleClickHandler && titleClickHandler(event);
  }

  renderGroupTitle() {
    const { dialogKey, titleClickHandler } = this.props;
    if (!dialogKey) return null;

    const titleClassName = classnames('recent__group__title', {
      'recent__group__title--clickable': titleClickHandler
    });

    return (
      <div className={titleClassName} onClick={this.handleTitleClick}>
        <FormattedMessage id={`sidebar.recents.${this.props.dialogKey}`}/>
      </div>
    );
  }

  renderGroupList() {
    const { shorts, archive, currentPeer } = this.props;

    return map(shorts, (dialog) => {
      const peer = dialog.peer.peer;
      const peerKey = PeerUtils.peerToString(peer);
      const isActive = PeerUtils.equals(peer, currentPeer);

      return (
        <RecentItem
          dialog={dialog}
          archiveState={archive[peerKey]}
          // archiveState={archive[dialog.peer.peer.key]}
          isActive={isActive}
          key={peerKey}
        />
      );
    });
  }

  render() {
    return (
      <div className="recent__group">
        {this.renderGroupTitle()}
        {this.renderGroupList()}
      </div>
    );
  }
}
