import React from 'react';
import { PureRenderMixin } from 'react/addons';

import AvatarItem from '../../common/AvatarItem.react';

export default React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    contact: React.PropTypes.object,
    onSelect: React.PropTypes.func
  },

  render() {
    var contact = this.props.contact;

    return (
      <li className="contacts__list__item row">
        <AvatarItem title={contact.name}
                    image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"/>

        <div className="col-xs">
          <span className="title">
            {contact.name}
          </span>
        </div>

        <div className="controls">
          <a className="material-icons" onClick={this.onSelect}>add</a>
        </div>
      </li>
    );
  },

  onSelect () {
    this.props.onSelect(this.props.contact);
  }
});
