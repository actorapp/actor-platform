/** @jsx React.DOM */

// original https://github.com/joshwnj/react-visibility-sensor

'use strict';

var VisibilitySensor = React.createClass({
  displayName: 'VisibilitySensor',

  propTypes: {
    onChange: React.PropTypes.func.isRequired,
    active: React.PropTypes.bool,
    delay: React.PropTypes.number,
    containment: React.PropTypes.instanceOf(Element),
    className: React.PropTypes.string,
    style: React.PropTypes.object
  },

  getDefaultProps: function () {
    return {
      active: true,
      delay: 1000,
      containment: null
    };
  },

  getInitialState: function () {
    return {
      isVisible: null,
      visibilityRect: {}
    };
  },

  componentDidMount: function () {
    if (this.props.active) {
      this.startWatching();
    }
  },

  componentWillUnmount: function () {
    this.stopWatching();
  },

  componentWillReceiveProps: function (nextProps) {
    if (nextProps.active) {
      this.setState(this.getInitialState());
      this.startWatching();
    } else {
      this.stopWatching();
    }
  },

  startWatching: function () {
    if (this.interval) { return; }
    this.interval = setInterval(this.check, this.props.delay);
    this.check();
  },

  stopWatching: function () {
    this.interval = clearInterval(this.interval);
  },

  /**
   * Check if the element is within the visible viewport
   */
  check: function () {
    var el = this.getDOMNode();
    var rect = el.getBoundingClientRect();
    var containmentRect;

    if (this.props.containment) {
      containmentRect = this.props.containment.getBoundingClientRect();
    } else {
      containmentRect = {
        top: 0,
        left: 0,
        bottom: window.innerHeight || document.documentElement.clientHeight,
        right: window.innerWidth || document.documentElement.clientWidth
      };
    }

    var visibilityRect = {
      top: rect.top >= containmentRect.top,
      left: rect.left >= containmentRect.left,
      bottom: rect.bottom <= containmentRect.bottom,
      right: rect.right <= containmentRect.right
    };

    var isVisible = (
      visibilityRect.top &&
      visibilityRect.left &&
      visibilityRect.bottom &&
      visibilityRect.right
    );

    // notify the parent when the value changes
    if (this.state.isVisible !== isVisible) {
      this.setState({
        isVisible: isVisible,
        visibilityRect: visibilityRect
      });
      this.props.onChange(isVisible, visibilityRect);
    }

    return this.state;
  },

  render: function () {
    return React.createElement('div', {
      className: this.props.className,
      style: this.props.style
    }, [this.props.children]);
  }
});
