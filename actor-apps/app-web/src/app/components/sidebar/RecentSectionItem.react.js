/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import classNames from 'classnames';

import PeerUtils from 'utils/PeerUtils';
import { escapeWithEmoji } from 'utils/EmojiUtils';

import DialogActionCreators from 'actions/DialogActionCreators';

import DialogStore from 'stores/DialogStore';

import AvatarItem from 'components/common/AvatarItem.react';

class RecentSectionItem extends React.Component {
  static propTypes = {
    dialog: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);
  }

  onClick = () => {
    const { dialog } = this.props;

    DialogActionCreators.selectDialogPeer(dialog.peer.peer);
  };

  //onDelete = (event) => {
  //  event.stopPropagation();
  //  const { dialog } = this.props;
  //  DialogActionCreators.deleteChat(dialog.peer.peer);
  //};

  render() {
    const { dialog } = this.props;
    const selectedPeer = DialogStore.getSelectedDialogPeer();

    const isActive = selectedPeer && PeerUtils.equals(dialog.peer.peer, selectedPeer);

    const recentClassName = classNames('sidebar__list__item', 'row', {
      'sidebar__list__item--active': isActive,
      'sidebar__list__item--unread': dialog.counter > 0
    });
    const counter = dialog.counter > 0 ? <span className="counter">{dialog.counter}</span> : null;

    return (
      <li className={recentClassName} onClick={this.onClick}>
        <AvatarItem image={dialog.peer.avatar}
                    placeholder={dialog.peer.placeholder}
                    size="tiny"
                    title={dialog.peer.title}/>
        <div className="title col-xs" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialog.peer.title)}}/>
        {counter}
        {/* <i className="material-icons delete" onClick={this.onDelete}>clear</i> */}
      </li>
    );
  }
}

export default RecentSectionItem;
