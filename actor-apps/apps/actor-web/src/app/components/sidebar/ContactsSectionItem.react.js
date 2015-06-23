import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';

import AvatarItem from '../common/AvatarItem.react';

const ContactsSectionItem = React.createClass({
  propTypes: {
    contact: React.PropTypes.object
  },

  mixins: [PureRenderMixin],

  openNewPrivateCoversation() {
    DialogActionCreators.selectDialogPeerUser(this.props.contact.uid);
    ContactActionCreators.hideContactList();
  },

  render() {
    let contact = this.props.contact;

    return (
      <li className="sidebar__list__item row" onClick={this.openNewPrivateCoversation}>
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"
                    title={contact.name}/>

        <div className="col-xs">
          <span className="title">
            {contact.name}
          </span>
        </div>
      </li>
    );
  }
});

export default ContactsSectionItem;
