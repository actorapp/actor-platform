import React from 'react';

import classNames from 'classnames';

import AvatarItem from '../common/AvatarItem.react';
import DialogActionCreators from '../../actions/DialogActionCreators';

import DialogStore from '../../stores/DialogStore';

class RecentSectionItem extends React.Component {
  static propTypes = {
    dialog: React.PropTypes.object.isRequired
  };

  constructor() {
    super();

    this.onClick = this.onClick.bind(this);
  }

  onClick() {
    DialogActionCreators.selectDialogPeer(this.props.dialog.peer.peer);
  }

  render() {
    const dialog = this.props.dialog;
    const selectedDialogPeer = DialogStore.getSelectedDialogPeer();
    let isActive = false;

    if (selectedDialogPeer) {
      isActive = (dialog.peer.peer.id === selectedDialogPeer.id);
    }

    let title;

    if (dialog.counter > 0) {
      const counter = <span className="counter">{dialog.counter}</span>;
      const name = <span className="col-xs title">{dialog.peer.title}</span>;
      title = [name, counter];
    } else {
      title = <span className="col-xs title">{dialog.peer.title}</span>;
    }

    var recentClassName = classNames('sidebar__list__item', 'row', {
      'sidebar__list__item--active': isActive,
      'sidebar__list__item--unread': dialog.counter > 0
    });

    return (
      <li className={recentClassName} onClick={this.onClick}>
        <AvatarItem image={dialog.peer.avatar}
                    placeholder={dialog.peer.placeholder}
                    size="tiny"
                    title={dialog.peer.title}/>
        {title}
      </li>
    );
  }
}

export default RecentSectionItem;
