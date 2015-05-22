var React = require('react');

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
    var name = user.name;
    var avatar;

    if (user.avatar !== null) {
      avatar = <img className="avatar__image" src={user.avatar}/>
    } else {
      avatar = <span className="avatar__placeholder avatar__placeholder--yellow">O</span>
    }

    return(
      <div className="sidebar__header__user row">
        <div className="sidebar__header__user__avatar avatar avatar--small">
          {avatar}
        </div>
        <span className="sidebar__header__user__name">{name}</span>
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
