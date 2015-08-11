import React from 'react';

import Lightbox from 'jsonlylightbox';

// lightbox init
const lightbox = new Lightbox();
const lightboxOptions = {
  animation: false,
  controlClose: '<i class="material-icons">close</i>'
};
lightbox.load(lightboxOptions);

class Image extends React.Component {
  static propTypes = {
    content: React.PropTypes.object.isRequired,
    loadingClassName: React.PropTypes.string,
    loadedClassName: React.PropTypes.string
  };

  constructor(props) {
    super(props);

    this.state = {
      isImageLoaded: false
    };
  }

  openLightBox() {
    lightbox.open(this.props.content.fileUrl, "message");
  }

  render() {
    const content = this.props.content;

    const preview = <img className="photo photo--preview" src={content.preview}/>;
    const k = content.w / 300;
    const styles = {
      width: Math.round(content.w / k),
      height: Math.round(content.h / k)
    };

    let original,
      preloader;

    if (content.fileUrl) {
      original = (
        <img className="photo photo--original"
             height={content.h}
             onClick={this.openLightBox.bind(this)}
             onLoad={() => this.setState({isImageLoaded: true})}
             src={content.fileUrl}
             width={content.w}/>
      );
    }

    if (content.isUploading === true || this.state.isImageLoaded === false) {
      preloader =
        <div className="preloader"><div/><div/><div/><div/><div/></div>;
    }

    const className = this.state.isImageLoaded ? this.props.loadedClassName : this.props.loadingClassName;

    return (
      <div className={className} style={styles}>
        {preview}
        {original}
        {preloader}
        <svg dangerouslySetInnerHTML={{__html: '<filter id="blur-effect"><feGaussianBlur stdDeviation="3"/></filter>'}}></svg>
      </div>
    );
  }
}

export default Image;
