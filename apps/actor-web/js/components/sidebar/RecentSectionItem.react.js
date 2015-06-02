var React = require('react');
var classNames = require('classnames');

var AvatarItem = require('../common/AvatarItem.react');
var DialogActionCreators = require('../../actions/DialogActionCreators');

var DialogStore = require('../../stores/DialogStore');

var RecentSectionItem = React.createClass({
  propTypes: {
    dialog: React.PropTypes.object.isRequired
  },

  render: function() {
    var dialog = this.props.dialog;
    var selectedDialogPeer = DialogStore.getSelectedDialogPeer();
    var isActive  = false;

    if (selectedDialogPeer) {
      isActive = (dialog.peer.peer.id == selectedDialogPeer.id)
    }

    var title;

    if (dialog.counter > 0) {
      title = <span className="col-xs title">{dialog.peer.title} [{dialog.counter}]</span>
    } else {
      title = <span className="col-xs title">{dialog.peer.title}</span>
    }

    var recentClassName = classNames('sidebar__list__item', 'row', {
      'sidebar__list__item--active': isActive,
      'sidebar__list__item--unread': dialog.counter > 0
    });

    return(
      <li className={recentClassName} onClick={this._onClick}>
        <AvatarItem title={dialog.peer.title}
                    image={dialog.peer.avatar}
                    placeholder={dialog.peer.placeholder}
                    size="tiny"/>
        {title}
      </li>
    )
  },

  _onClick: function() {
    DialogActionCreators.selectDialogPeer(this.props.dialog.peer.peer);
  }
});

module.exports = RecentSectionItem;
