var React = require('react');

var AvatarItem = require('../common/AvatarItem.react');

var UserSection = React.createClass({
  propTypes: {
    messenger: React.PropTypes.object.isRequired
  },

  componentWillMount: function() {
    var messenger = this.props.messenger;
    messenger.bindUser(messenger.getUid(), this._setUser)
  },

  render: function() {
    var user = this.state.user;

    return(
      <div className="sidebar__header__user row">
        <div className="sidebar__header__user__avatar avatar avatar--small">
          <AvatarItem title={user.name} image={user.avatar} placeholder={user.placeholder}/>
        </div>
        <span className="sidebar__header__user__name">{user.name}</span>
        <span className="col-xs"></span>
        <img className="sidebar__header__user__expand" src="assets/img/icons/png/ic_expand_more_2x_white.png" alt=""/>
      </div>
    );
  },

  _setUser: function(user) {
    this.setState({user: user});
  }
});

module.exports = UserSection;
