(function (root, factory) {
  if (typeof define === 'function' && define.amd) {
    define(['react'], factory);
  } else if (typeof exports !== 'undefined') {
    module.exports = factory(require('react'));
  } else {
    root.ReactList = factory(root.React);
  }
})(this, function (React) {
  'use strict';

  var requestAnimationFrame =
    (typeof window !== 'undefined' && window.requestAnimationFrame) ||
      function (cb) { return setTimeout(cb, 16); };

  var cancelAnimationFrame =
    (typeof window !== 'undefined' && window.cancelAnimationFrame) ||
      clearTimeout;

  var isEqualSubset = function (a, b) {
    for (var key in a) if (b[key] !== a[key]) return false;
    return true;
  };

  var isEqual = function (a, b) {
    return isEqualSubset(a, b) && isEqualSubset(b, a);
  };

  return React.createClass({
    getDefaultProps: function () {
      return {
        items: [],
        isLoading: false,
        error: null,
        renderPageSize: 10,
        threshold: 500,
        uniform: false,
        component: 'div',
        renderItems: function (children) {
          return React.createElement('div', {ref: 'items'}, children);
        },
        renderItem: function (item, i) {
          return React.createElement('div', {key: i}, item);
        },
        renderLoading: function () {
          return React.createElement('div', null, 'Loading...');
        },
        renderError: function (er) {
          return React.createElement('div', null, '' + er);
        },
        renderEmpty: function () {
          return React.createElement('div', null, 'Nothing to show.');
        }
      };
    },

    getInitialState: function () {
      return {
        isLoaded: !this.props.fetch,
        isLoading: this.props.isLoading,
        error: this.props.error,
        index: 0,
        length: this.props.renderPageSize,
        itemHeight: 0,
        columns: 0
      };
    },

    componentDidMount: function () {
      if (this.props.fetchInitially) this.fetch();
      this.update();
    },

    componentWillUnmount: function () {
      cancelAnimationFrame(this.afid);
    },

    shouldComponentUpdate: function (props, state) {
      return !isEqual(this.props, props) || !isEqual(this.state, state);
    },

    getScrollParent: function () {
      if (this._scrollParent) return this._scrollParent;
      for (var el = this.getDOMNode(); el; el = el.parentElement) {
        var overflowY = window.getComputedStyle(el).overflowY;
        if (overflowY === 'auto' || overflowY === 'scroll') return el;
      }
      return window;
    },

    // Get scroll position relative to the top of the list.
    getScroll: function () {
      var scrollParent = this.getScrollParent();
      var el = this.getDOMNode();
      if (scrollParent === el) {
        return el.scrollTop;
      } else if (scrollParent === window) {
        return -el.getBoundingClientRect().top;
      } else {
        return scrollParent.scrollTop - el.offsetTop;
      }
    },

    setScroll: function (y) {
      var scrollParent = this.getScrollParent();
      if (scrollParent === window) return window.scrollTo(0, y);
      scrollParent.scrollTop = y;
    },

    getViewportHeight: function () {
      var scrollParent = this.getScrollParent();
      return scrollParent === window ?
        scrollParent.innerHeight :
        scrollParent.clientHeight;
    },

    scrollTo: function (item) {
      var items = this.props.items;
      var targetIndex = items.indexOf(item);
      if (targetIndex === -1) return;
      var itemHeight = this.state.itemHeight;
      var current = this.getScroll();
      var max = Math.floor(targetIndex / this.state.columns) * itemHeight;
      var min = max - this.getViewportHeight() + itemHeight;
      if (current > max) return this.setScroll(max);
      if (current < min) this.setScroll(min);
    },

    fetch: function () {
      if (this.state.isLoaded || this.isFetching || this.state.error) return;
      this.setState({isLoading: true, error: null});
      this.isFetching = true;
      this.props.fetch(this.handleFetch);
    },

    handleFetch: function (er, isDone) {
      this.isFetching = false;
      if (!this.isMounted()) return;
      this.setState({
        isLoaded: !er && !!isDone,
        isLoading: false,
        error: er
      });
    },

    // REFACTOR
    update: function () {
      this.afid = requestAnimationFrame(this.update);
      var items = this.props.items;
      var threshold = this.props.threshold;
      var renderPageSize = this.props.renderPageSize;
      var itemHeight = this.state.itemHeight;
      var columns = this.state.columns;
      var index = this.state.index;
      var length = this.state.length;
      var elBottom = this.getDOMNode().scrollHeight;
      var viewTop = this.getScroll();
      var viewBottom = viewTop + this.getViewportHeight();
      if (this.props.uniform) {

        // Grab the item elements.
        var itemEls = this.refs.items.getDOMNode().children;

        // Set `itemHeight` based on the first item. If the first item has not
        // been rendered yet, defer this branch until the next tick.
        if (itemEls.length) {
          var firstRect = itemEls[0].getBoundingClientRect();
          var firstRectHeight = Math.floor(firstRect.height);
          if (firstRectHeight) {
            itemHeight = firstRectHeight;
            var firstRowBottom = firstRect.top + itemHeight;
            columns = 1;
            for (var i = 1, l = itemEls.length; i < l; ++i) {
              if (itemEls[i].getBoundingClientRect().top >= firstRowBottom) {
                break;
              }
              ++columns;
            }
            if (viewBottom > -threshold && viewTop < elBottom + threshold) {
              var rowThreshold = Math.ceil(threshold / itemHeight);
              index =
                (Math.floor(viewTop / itemHeight) - rowThreshold) * columns;
              var rows = Math.ceil(this.getViewportHeight() / itemHeight);
              length = columns * (rows + rowThreshold * 2);
            }
          }
        }

        // Ensure at least `renderPageSize` elements are rendered at any given
        // time. This will ensure `columns` can be calculated correctly. It's
        // also important to always supply a `renderPageSize` that is >= the
        // most columns of items that will ever be visible, but this is the
        // responsibility of the component consumer.
        index = Math.max(0, Math.min(index, items.length - renderPageSize));
        length = Math.max(length, renderPageSize);
      } else if (length <= items.length && viewBottom > elBottom - threshold) {
        length += renderPageSize;
      }

      // Fetch if the models in memory have been exhausted.
      if (index + length > items.length) this.fetch();

      // Finally, set the new state.
      this.setState({
        itemHeight: itemHeight,
        columns: columns,
        index: index,
        length: length
      });
    },

    renderSpace: function (n) {
      if (!this.props.uniform || !this.state.columns) return;
      var height = (n / this.state.columns) * this.state.itemHeight;
      if (!height) return;
      return React.createElement('div', {style: {height: height}});
    },

    renderSpaceAbove: function () {
      return this.renderSpace(this.state.index);
    },

    renderSpaceBelow: function () {
      var n = this.props.items.length - this.state.index - this.state.length;
      return this.renderSpace(Math.max(0, n));
    },

    renderItems: function () {
      return this.props.renderItems(
        this.props.items
          .slice(this.state.index, this.state.index + this.state.length)
          .map(this.props.renderItem)
      );
    },

    renderStatusMessage: function () {
      var info = this.props.fetch ? this.state : this.props;
      if (info.isLoading) return this.props.renderLoading();
      if (info.error) return this.props.renderError(info.error);
      if (!this.props.items.length) return this.props.renderEmpty();
    },

    render: function () {
      return React.createElement(this.props.component, this.props,
        this.renderSpaceAbove(),
        this.renderItems(),
        this.renderSpaceBelow(),
        this.renderStatusMessage()
      );
    }
  });
});
