// @flow
import React from 'react';
import PropTypes from 'prop-types';
import { View } from 'react-native';

const defaultHeight = 5;
const defaultBorderRadius = 5;
const minProgress = 0;
const maxProgress = 100;

class ProgressBar extends React.PureComponent {
  static propTypes = {
    borderRadius: PropTypes.number,
    color: PropTypes.string.isRequired,
    height: PropTypes.number,
    progress: function(props, propName, componentName) {
      const value = parseInt(props[propName], 10);
      if (isNaN(value) || props[propName] < minProgress || props[propName] > maxProgress) {
        return new Error('progress should be between 0 and 100');
      }
    },
    style: PropTypes.any,
  };

  render() {
    const { borderRadius, color, height, progress, style } = this.props;
    const currentProgress = Math.ceil(progress);

    let styles = [];
    if (style) {
      if (style.length) {
        styles = styles.concat(style);
      } else {
        styles.push(style);
      }
    }

    styles.push({
      borderRadius: borderRadius || defaultBorderRadius,
      flexDirection: 'row',
      height: height || defaultHeight,
      overflow: 'hidden',
    });

    return (
      <View style={styles}>
        <View
          style={{ backgroundColor: color, borderRadius: borderRadius || defaultBorderRadius, flex: currentProgress }}
        />
        <View style={{ backgroundColor: color, opacity: 0.2, flex: 100 - currentProgress }} />
      </View>
    );
  }
}

export default ProgressBar;
