// Based on https://github.com/react-navigation/react-navigation/blob/master/src/views/Header/Header.js
import React from 'react';
import {
  Animated,
  Platform,
  StyleSheet,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import Feather from 'react-native-vector-icons/Feather';
import pageHeaderStyles from '../../styles/pageHeader';

const APPBAR_HEIGHT = Platform.OS === 'ios' ? 44 : 56;
const AnimatedText = Animated.Text;

class PageHeader extends React.PureComponent {
  render() {
    const { title, onBackPressed } = this.props;
    const containerStyles = [
      pageHeaderStyles.container,
      { height: APPBAR_HEIGHT }
    ];
    
    return (
      <View style={containerStyles}>
        <View style={pageHeaderStyles.flexOne}>
          <View style={pageHeaderStyles.header}>
            <View style={pageHeaderStyles.title}>
              <AnimatedText
                numberOfLines={1}
                style={pageHeaderStyles.titleText}
                accessibilityTraits="header">
                {title}
              </AnimatedText>
            </View>
            <TouchableOpacity style={pageHeaderStyles.left}>
              <Feather name="arrow-left" size={24} onPress={onBackPressed} style={pageHeaderStyles.backIcon} />
            </TouchableOpacity>
          </View>
        </View>
      </View>
    );
  }
}

export default PageHeader;
