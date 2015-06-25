import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

const UserProfileContactInfo = React.createClass({
  propTypes: {
    phones: React.PropTypes.array
  },

  mixins: [PureRenderMixin],

  render: function () {
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
});

export default UserProfileContactInfo;
