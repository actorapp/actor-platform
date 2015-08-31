/* eslint-disable */
import { Mixpanel } from 'constants/ActorAppConstants';

(function (f, b) {
  if (!b.__SV) {
    var a, e, i, g;
    window.mixpanel = b;
    b._i = [];
    b.init = function (a, e, d) {
      function f(b, h) {
        var a = h.split(".");
        2 == a.length && (b = b[a[0]], h = a[1]);
        b[h] = function () {
          b.push([h].concat(Array.prototype.slice.call(arguments, 0)))
        }
      }

      var c = b;
      "undefined" !== typeof d ? c = b[d] = [] : d = "mixpanel";
      c.people = c.people || [];
      c.toString = function (b) {
        var a = "mixpanel";
        "mixpanel" !== d && (a += "." + d);
        b || (a += " (stub)");
        return a
      };
      c.people.toString = function () {
        return c.toString(1) + ".people (stub)"
      };
      i = "disable track track_pageview track_links track_forms register register_once alias unregister identify name_tag set_config people.set people.set_once people.increment people.append people.union people.track_charge people.clear_charges people.delete_user".split(" ");
      for (g = 0; g < i.length; g++)f(c, i[g]);
      b._i.push([a, e, d])
    };
    b.__SV = 1.2;

    require('mixpanel-js');

  }
})(document, window.mixpanel || []);

try {
  mixpanel.init(Mixpanel);
} catch(e) {
  const noop = function() {};

  mixpanel = {
    track: noop,
    alias: noop,
    identify: noop,
    cookie: {
      clear: noop
    },
    people: {
      set: noop,
      set_once: noop
    }
  };
  window.mixpanel = mixpanel;
  console.error('Failed to init mixpanel', e);
}

export default mixpanel;
