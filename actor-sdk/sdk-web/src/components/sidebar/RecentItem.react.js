/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';
import { FormattedMessage } from 'react-intl';
import PeerUtils from '../../utils/PeerUtils';
import { escapeWithEmoji } from '../../utils/EmojiUtils';
import confirm from '../../utils/confirm';

import DialogActionCreators from '../../actions/DialogActionCreators';
import FavoriteActionCreators from '../../actions/FavoriteActionCreators';

import DialogStore from '../../stores/DialogStore';
import UserStore from '../../stores/UserStore';

import AvatarItem from '../common/AvatarItem.react';

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

  onClick = () => DialogActionCreators.selectDialogPeer(this.props.dialog.peer.peer);

  handleHideChat = (event) => {
    event.stopPropagation();
    event.preventDefault();
    const { dialog } = this.props;
    const { intl } = this.context;

    if (UserStore.isContact(dialog.peer.peer.id)) {
      DialogActionCreators.hideChat(dialog.peer.peer);
    } else {
      confirm(intl.messages['modal.confirm.nonContactHide.title'], {
        description: <FormattedMessage id="modal.confirm.nonContactHide.body"
                                       values={{name: dialog.peer.title}}/>,
        abortLabel: intl.messages['button.cancel'],
        confirmLabel: intl.messages['button.ok']
      }).then(
        () => DialogActionCreators.hideChat(dialog.peer.peer),
        () => {}
      );
    }
  };

  handleFavorite = (event) => {
    event.preventDefault();
    event.stopPropagation();
    FavoriteActionCreators.favoriteChat(this.props.dialog.peer.peer);
  };

  handleUnfavorite = (event) => {
    event.preventDefault();
    event.stopPropagation();
    FavoriteActionCreators.unfavoriteChat(this.props.dialog.peer.peer);
  };

  render() {
    const { dialog } = this.props;
    const selectedPeer = DialogStore.getCurrentPeer();

    const isActive = selectedPeer && PeerUtils.equals(dialog.peer.peer, selectedPeer);

    const recentClassName = classnames('sidebar__list__item', 'row', {
      'sidebar__list__item--active': isActive,
      'sidebar__list__item--unread': dialog.counter > 0
    });

    return (
      <li className={recentClassName} onClick={this.onClick}>
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
      </li>
    );
  }
}

export default RecentItem;
