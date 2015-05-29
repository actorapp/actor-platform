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
    var selectedDialog = DialogStore.getSelectedDialog();
    var isActive  = false;

    if (selectedDialog) {
      isActive = (dialog.peer.peer.id == selectedDialog.peer.peer.id)
    }

    var title;

    if (dialog.counter > 0) {
      title = <span><b>{dialog.peer.title}</b></span>
    } else {
      title = <span>{dialog.peer.title}</span>
    }

    var recentClassName = classNames('sidebar__list__item', {
      'sidebar__list__item--active': isActive
    });

    return(
      <li className={recentClassName} onClick={this._onClick}>
        <AvatarItem title={dialog.peer.title}
                    image={dialog.peer.avatar}
                    placeholder={dialog.peer.placeholder}
                    size="tiny"/>
          <span>
            {title}
          </span>
      </li>
    )
  },

  _onClick: function() {
    DialogActionCreators.selectDialog(this.props.dialog);
  }
});

module.exports = RecentSectionItem;
