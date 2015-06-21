import React from 'react';

import classNames from 'classnames';

import AvatarItem from '../common/AvatarItem.react';
import DialogActionCreators from '../../actions/DialogActionCreators';

import DialogStore from '../../stores/DialogStore';

class RecentSectionItem extends React.Component {
  constructor() {
    super();

    this._onClick = this._onClick.bind(this);
  }

  _onClick() {
    DialogActionCreators.selectDialogPeer(this.props.dialog.peer.peer);
  }

  render() {
    var dialog = this.props.dialog;
    var selectedDialogPeer = DialogStore.getSelectedDialogPeer();
    var isActive = false;

    if (selectedDialogPeer) {
      isActive = (dialog.peer.peer.id === selectedDialogPeer.id);
    }

    var title;

    if (dialog.counter > 0) {
      var counter = <span className="counter">{dialog.counter}</span>;
      var name = <span className="col-xs title">{dialog.peer.title}</span>;
      title = [name, counter];
    } else {
      title = <span className="col-xs title">{dialog.peer.title}</span>;
    }

    var recentClassName = classNames('sidebar__list__item', 'row', {
      'sidebar__list__item--active': isActive,
      'sidebar__list__item--unread': dialog.counter > 0
    });

    return (
      <li className={recentClassName} onClick={this._onClick}>
        <AvatarItem image={dialog.peer.avatar}
                    placeholder={dialog.peer.placeholder}
                    size="tiny"
                    title={dialog.peer.title}/>
        {title}
      </li>
    );
  }
}

RecentSectionItem.propTypes = {
  dialog: React.PropTypes.object.isRequired
};

export default RecentSectionItem;
