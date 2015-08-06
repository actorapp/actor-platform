export default {
  show: () => {
    document.body.classList.add('banner--onscreen');
  },

  hide: () => {
    document.body.classList.remove('banner--onscreen');
  },

  jump: (os) => {
    window.localStorage.setItem('banner_jump', os);
  }
};
