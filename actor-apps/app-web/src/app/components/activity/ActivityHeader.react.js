import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class ActivityHeader extends React.Component {
  static propTypes = {
    close: React.PropTypes.func,
    title: React.PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const title = this.props.title;
    const close = this.props.close;

    let headerTitle;
    if (typeof title !== 'undefined') {
      headerTitle = <span className="activity__header__title">{title}</span>;
    }

    return (
      <header className="activity__header toolbar">
        <a className="activity__header__close material-icons" onClick={close}>clear</a>
        {headerTitle}
      </header>
    );
  }
}

export default ActivityHeader;
