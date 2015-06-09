import React from 'react';
import { PureRenderMixin } from 'react/addons';

import AvatarItem from '../../common/AvatarItem.react';

export default React.createClass({
  propTypes: {
    contact: React.PropTypes.object,
    onSelect: React.PropTypes.func
  },

  mixins: [PureRenderMixin],

  _onSelect () {
    this.props.onSelect(this.props.contact);
  },

  render() {
    var contact = this.props.contact;

    return (
      <li className="contacts__list__item row">
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"
                    title={contact.name}/>

        <div className="col-xs">
          <span className="title">
            {contact.name}
          </span>
        </div>

        <div className="controls">
          <a className="material-icons" onClick={this._onSelect}>add</a>
        </div>
      </li>
    );
  }
});
