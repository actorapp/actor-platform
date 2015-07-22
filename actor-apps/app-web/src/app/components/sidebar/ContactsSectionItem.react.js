import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

import DialogActionCreators from 'actions/DialogActionCreators';

import AvatarItem from 'components/common/AvatarItem.react';

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class ContactsSectionItem extends React.Component {
  static propTypes = {
    contact: React.PropTypes.object
  };

  constructor(props) {
    super(props);
  }

  openNewPrivateCoversation = () => {
    DialogActionCreators.selectDialogPeerUser(this.props.contact.uid);
  }

  render() {
    const contact = this.props.contact;

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
}

export default ContactsSectionItem;
