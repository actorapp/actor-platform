import _ from 'lodash';

import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class UserProfileContactInfo extends React.Component {
  static propTypes = {
    phones: React.PropTypes.array
  };

  constructor(props) {
    super(props);
  }

  render() {
    let phones = this.props.phones;

    let contactPhones = _.map(phones, (phone, i) => {
      return (
        <li className="profile__list__item row" key={i}>
          <i className="material-icons">call</i>
          <div className="col-xs">
            <span className="contact">+{phone.number}</span>
            <span className="title">{phone.title}</span>
          </div>
        </li>
      );
    });

    return (
      <ul className="profile__list profile__list--contacts">
        {contactPhones}
      </ul>
    );
  }
}

export default UserProfileContactInfo;
