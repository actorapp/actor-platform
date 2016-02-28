/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { FormattedMessage } from 'react-intl';
import PeerUtils from '../../utils/PeerUtils';
import { escapeWithEmoji } from '../../utils/EmojiUtils';
import confirm from '../../utils/confirm';
import { Link } from 'react-router';

import DialogActionCreators from '../../actions/DialogActionCreators';
import FavoriteActionCreators from '../../actions/FavoriteActionCreators';
import ArchiveActionCreators from '../../actions/ArchiveActionCreators';

import UserStore from '../../stores/UserStore';
import ArchiveStore from '../../stores/ArchiveStore';

import AvatarItem from '../common/AvatarItem.react';
import Stateful from '../common/Stateful';

class RecentItem extends Component {
  constructor(props){
    super(props);
  }

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

  onClick = () => DialogActionCreators.selectDialogPeer(this.props.dialog.peer.peer);

  // handleHideChat = (event) => {
  //   event.stopPropagation();
  //   event.preventDefault();
  //   const { dialog } = this.props;
  //   const { intl } = this.context;
  //
  //   if (UserStore.isContact(dialog.peer.peer.id)) {
  //     DialogActionCreators.hideChat(dialog.peer.peer);
  //   } else {
  //     confirm(intl.messages['modal.confirm.nonContactHide.title'], {
  //       description: <FormattedMessage id="modal.confirm.nonContactHide.body"
  //                                      values={{name: dialog.peer.title}}/>
  //     }).then(
  //       () => DialogActionCreators.hideChat(dialog.peer.peer),
  //       () => {}
  //     );
  //   }
  // };

  handleAddToArchive = (event) => {
    event.preventDefault();
    event.stopPropagation();
    const { peer } = this.props.dialog.peer;
    ArchiveActionCreators.archiveChat(peer);
  };

  render() {
    const { dialog, type } = this.props;
    const { archiveChatState } = this.state;
    const toPeer = PeerUtils.peerToString(dialog.peer.peer);

    const recentClassName = classnames('sidebar__list__item', 'row', {
      'sidebar__list__item--unread': dialog.counter > 0
    });

    return (
      <li>
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
            <Stateful.Pending>
              <div className="archive" onClick={this.handleAddToArchive}>
                <i className="icon material-icons">archive</i>
              </div>
            </Stateful.Pending>
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
