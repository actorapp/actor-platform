/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';
import PeerUtils from '../../utils/PeerUtils';
import { escapeWithEmoji } from '../../utils/EmojiUtils';
import { Link } from 'react-router';

import { AsyncActionStates } from '../../constants/ActorAppConstants';

import DropdownActionCreators from '../../actions/DropdownActionCreators';

import AvatarItem from '../common/AvatarItem.react';
import Stateful from '../common/Stateful.react';

class RecentItem extends Component {
  static propTypes = {
    isActive: PropTypes.bool.isRequired,
    dialog: PropTypes.object.isRequired,
    archiveState: PropTypes.number.isRequired
  };

  static defaultProps = {
    archiveState: AsyncActionStates.PENDING
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  shouldComponentUpdate(nextProps) {
    return nextProps.dialog !== this.props.dialog ||
           nextProps.isActive !== this.props.isActive ||
           nextProps.archiveState !== this.props.archiveState;
  }

  onContextMenu = (event) => {
    event.preventDefault();
    const { peer } = this.props.dialog.peer;
    const contextPos = {
      x: event.pageX || event.clientX,
      y: event.pageY || event.clientY
    };
    DropdownActionCreators.openRecentContextMenu(contextPos, peer);
  };

  render() {
    const { dialog, archiveState } = this.props;
    const toPeer = PeerUtils.peerToString(dialog.peer.peer);

    const recentClassName = classnames('sidebar__list__item', 'row', {
      'sidebar__list__item--unread': dialog.counter > 0
    });

    return (
      <li onContextMenu={this.onContextMenu}>
        <Link to={`/im/${toPeer}`} className={recentClassName} activeClassName="sidebar__list__item--active">

          <AvatarItem image={dialog.peer.avatar}
                      placeholder={dialog.peer.placeholder}
                      size="tiny"
                      title={dialog.peer.title}/>

          <div className="title col-xs" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialog.peer.title)}}/>

          {
            dialog.counter > 0
              ? <span className="counter">{dialog.counter}</span>
              : null
          }

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
        </Link>
      </li>
    );
  }
}

export default RecentItem;
