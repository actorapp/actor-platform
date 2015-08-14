import React from 'react';
import classnames from 'classnames';

import Lightbox from 'jsonlylightbox';

// lightbox init
const lightbox = new Lightbox();
const lightboxOptions = {
  animation: false,
  controlClose: '<i class="material-icons">close</i>'
};
lightbox.load(lightboxOptions);

let cache = {};

class Image extends React.Component {
  static propTypes = {
    content: React.PropTypes.object.isRequired,
    className: React.PropTypes.string,
    loadedClassName: React.PropTypes.string
  };

  constructor(props) {
    super(props);

    this.state = {
      isImageLoaded: this.isCached()
    };
  }

  openLightBox() {
    lightbox.open(this.props.content.fileUrl, 'message');
  }

  onLoad() {
    this.setCached();
    if (!this.state.isImageLoaded) {
      this.setState({isImageLoaded: true});
    }
  }

  isCached() {
    return (cache[this.props.content.fileUrl] === true);
  }

  setCached() {
    cache[this.props.content.fileUrl] = true;
  }

  render() {
    const { content, className, loadedClassName } = this.props;
    const { isImageLoaded } = this.state;

    const k = content.w / 300;
    const styles = {
      width: Math.round(content.w / k),
      height: Math.round(content.h / k)
    };

    let original = null,
        preview = null,
        preloader = null;


    if (content.fileUrl) {
      original = (
        <img className="photo photo--original"
             height={content.h}
             onClick={this.openLightBox.bind(this)}
             onLoad={this.onLoad.bind(this)}
             src={content.fileUrl}
             width={content.w}/>
      );
    }

    if (!this.isCached()) {
      preview = <img className="photo photo--preview" src={content.preview}/>;

      if (content.isUploading === true || isImageLoaded === false) {
        preloader = <div className="preloader"><div/><div/><div/><div/><div/></div>;
      }
    }

    const imageClassName = isImageLoaded ? classnames(className, loadedClassName) : className;

    return (
      <div className={imageClassName} style={styles}>
        {preview}
        {original}
        {preloader}
        <svg dangerouslySetInnerHTML={{__html: '<filter id="blur-effect"><feGaussianBlur stdDeviation="3"/></filter>'}}></svg>
      </div>
    );
  }
}

export default Image;
