/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { FormattedMessage } from 'react-intl';
import PeerUtils from '../../utils/PeerUtils';
import { escapeWithEmoji } from '../../utils/EmojiUtils';
import confirm from '../../utils/confirm';
import { Link } from 'react-router';

import DialogActionCreators from '../../actions/DialogActionCreators';
import DropdownActionCreators from '../../actions/DropdownActionCreators';

import UserStore from '../../stores/UserStore';
import ArchiveStore from '../../stores/ArchiveStore';

import AvatarItem from '../common/AvatarItem.react';
import Stateful from '../common/Stateful';

class RecentItem extends Component {
  static propTypes = {
    dialog: PropTypes.object.isRequired,
    type: PropTypes.string
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [ArchiveStore];
  }

  static calculateState(prevState, nextProps) {
    return {
      archiveChatState: ArchiveStore.getArchiveChatState(nextProps.dialog.peer.peer.id)
    };
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
    const { dialog, type } = this.props;
    const { archiveChatState } = this.state;
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

          <Stateful.Root currentState={archiveChatState}>
            <Stateful.Processing>
              <div className="archive archive--in-progress">
                <i className="icon material-icons spin">autorenew</i>
              </div>
            </Stateful.Processing>
            <Stateful.Success>
              <div className="archive archive--in-progress">
                <i className="icon material-icons">check</i>
              </div>
            </Stateful.Success>
            <Stateful.Failure>
              <div className="archive archive--failure">
                <i className="icon material-icons">warning</i>
              </div>
            </Stateful.Failure>
          </Stateful.Root>

        </Link>
      </li>
    );
  }
}

export default Container.create(RecentItem, {pure: false, withProps: true});
