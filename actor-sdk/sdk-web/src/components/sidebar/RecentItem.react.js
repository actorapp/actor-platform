/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import history from '../../utils/history';
import { FormattedMessage } from 'react-intl';
import { escapeWithEmoji } from '../../utils/EmojiUtils';

import { AsyncActionStates } from '../../constants/ActorAppConstants';

import DropdownActionCreators from '../../actions/DropdownActionCreators';

import AvatarItem from '../common/AvatarItem.react';
import Stateful from '../common/Stateful.react';

class RecentItem extends Component {
  static propTypes = {
    isActive: PropTypes.bool,
    dialog: PropTypes.object.isRequired,
    archiveState: PropTypes.number.isRequired
  };

  static defaultProps = {
    archiveState: AsyncActionStates.PENDING
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  constructor(props) {
    super(props);

    this.handleClick = this.handleClick.bind(this);
    this.handleOpenContextMenu = this.handleOpenContextMenu.bind(this);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.dialog !== this.props.dialog ||
           nextProps.isActive !== this.props.isActive ||
           nextProps.archiveState !== this.props.archiveState;
  }

  handleOpenContextMenu(event) {
    event.preventDefault();
    const { peer } = this.props.dialog.peer;
    const contextPos = {
      x: event.pageX || event.clientX,
      y: event.pageY || event.clientY
    };
    DropdownActionCreators.openRecentContextMenu(contextPos, peer);
  };

  handleClick() {
    const { dialog } = this.props;
    history.push(`/im/${dialog.peer.peer.key}`);
  }

  renderTitle() {
    const { dialog } = this.props;
    return (
      <div className="recent__item__title col-xs"
           dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialog.peer.title)}}/>
    );
  }

  renderCounter() {
    const { dialog } = this.props;
    if (dialog.counter === 0) return null;

    return (
      <span className="recent__item__counter">{dialog.counter}</span>
    )
  }

  renderArchiveState() {
    const { archiveState } = this.props;
    if (archiveState === AsyncActionStates.PENDING) return null;

    return (
      <Stateful
        currentState={archiveState}
        processing={
            <div className="archive archive--in-progress">
              <i className="icon material-icons spin">autorenew</i>
            </div>
          }
        success={
            <div className="archive archive--in-progress">
              <i className="icon material-icons">check</i>
            </div>
          }
        failure={
            <div className="archive archive--failure">
              <i className="icon material-icons">warning</i>
            </div>
          }
      />
    );
  }

  render() {
    const { dialog, isActive } = this.props;

    const recentItemClassName = classnames('recent__item', 'row', {
      'recent__item--active': isActive,
      'recent__item--unread': dialog.counter !== 0
    });

    return (
      <div onContextMenu={this.handleOpenContextMenu} onClick={this.handleClick} className={recentItemClassName}>

        <div className="recent__item__avatar">
          <AvatarItem image={dialog.peer.avatar}
                      placeholder={dialog.peer.placeholder}
                      size="tiny"
                      title={dialog.peer.title}/>
        </div>

        {this.renderTitle()}
        {this.renderCounter()}
        {this.renderArchiveState()}
      </div>
    );
  }
}

export default RecentItem;
