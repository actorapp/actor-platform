/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { FormattedMessage } from 'react-intl';
import Tooltip from 'rc-tooltip';
import PeerUtils from '../../utils/PeerUtils';
import RecentItem from './RecentItem.react';

class RecentGroup extends Component {
  static propTypes = {
    group: PropTypes.string.isRequired,
    items: PropTypes.array.isRequired,
    archive: PropTypes.object.isRequired,
    currentPeer: PropTypes.object,
    onTitleClick: PropTypes.func,
    onPlusClick: PropTypes.func,
    onItemUpdate: PropTypes.func.isRequired,
    renderEmptyHint: PropTypes.func
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  renderPlusButton() {
    const { group, onPlusClick } = this.props;
    if (!onPlusClick) {
      return null;
    }

    return (
      <Tooltip
        placement="top"
        mouseEnterDelay={0.15}
        mouseLeaveDelay={0}
        overlay={<FormattedMessage id={`tooltip.recent.${group}Create`}/>}>
        <i className="recent__group__plus-button material-icons pull-right" onClick={onPlusClick}>
          add_circle_outline
        </i>
      </Tooltip>
    );
  }

  renderGroupTitle() {
    const { group, onTitleClick } = this.props;

    const titleMessage = <FormattedMessage id={`sidebar.recents.${group}`} />;
    if (!onTitleClick) {
      return (
        <div className="recent__group__header">
          <div className="recent__group__title">
            {titleMessage}
          </div>
        </div>
      );
    }

    const tooltipMessage = <FormattedMessage id={`tooltip.recent.${group}List`}/>;

    return (
      <div className="recent__group__header">
        <div className="recent__group__title recent__group__title--clickable" onClick={onTitleClick}>
          <Tooltip
            placement="right"
            mouseEnterDelay={0.15}
            mouseLeaveDelay={0}
            overlay={tooltipMessage}
          >
            {titleMessage}
          </Tooltip>
        </div>
        {this.renderPlusButton()}
      </div>
    );
  }

  renderGroupList() {
    const { items, archive, currentPeer, onItemUpdate } = this.props;
    if (!items.length) {
      if (this.props.renderEmptyHint) {
        return this.props.renderEmptyHint();
      }

      return null;
    }

    return items.map((dialog) => {
      const peer = dialog.peer.peer;
      const peerKey = PeerUtils.peerToString(peer);
      const isActive = PeerUtils.equals(peer, currentPeer);

      return (
        <RecentItem
          dialog={dialog}
          archiveState={archive[peerKey]}
          isActive={isActive}
          onUpdate={onItemUpdate}
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

export default RecentGroup;
