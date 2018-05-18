import React from 'react';

import { NavigationActions } from 'react-navigation';
import Feather from "react-native-vector-icons/Feather";

class SearchRightHeaderIcon extends React.PureComponent {
  
  clearAndGoBack() {
    const { navigation } = this.props;
    this.props.clearQuery();
    navigation.dispatch(NavigationActions.back())
  }

  render() {
    const { style } = this.props;
    return <Feather name="x" size={24} style={style} onPress={() => this.clearAndGoBack()} />;
  }
}

export default SearchRightHeaderIcon;
