import React from 'react';

import classNames from 'classnames';

import AvatarItem from '../common/AvatarItem.react';
import DialogActionCreators from '../../actions/DialogActionCreators';

import DialogStore from '../../stores/DialogStore';

export default React.createClass({
  propTypes: {
    dialog: React.PropTypes.object.isRequired
  },

  render: function() {
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
        <AvatarItem title={dialog.peer.title}
                    image={dialog.peer.avatar}
                    placeholder={dialog.peer.placeholder}
                    size="tiny"/>
        {title}
      </li>
    );
  },

  _onClick: function() {
    DialogActionCreators.selectDialogPeer(this.props.dialog.peer.peer);
  }
});
