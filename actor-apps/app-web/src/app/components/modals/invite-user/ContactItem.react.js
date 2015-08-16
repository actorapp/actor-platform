import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
import classnames from 'classnames';

import AvatarItem from 'components/common/AvatarItem.react';

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class ContactItem extends React.Component {
  static propTypes = {
    contact: React.PropTypes.object,
    onSelect: React.PropTypes.func,
    member: React.PropTypes.bool
  };

  constructor(props) {
    super(props);
  }

  onSelect = () => {
    this.props.onSelect(this.props.contact);
  };

  render() {
    const contact = this.props.contact;
    const contactClassName = classnames('contacts__list__item row', {
      'contacts__list__item--member': this.props.member
    });

    let controls;
    if (!this.props.member) {
      controls = <a className="material-icons" onClick={this.onSelect}>person_add</a>;
    } else {
      controls = <i className="material-icons">check</i>;
    }

    return (
      <li className={contactClassName}>
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
          {controls}
        </div>
      </li>
    );
  }
}

export default ContactItem;
