// @flow
import React from 'react';
import { normalizeURI } from 'lbry-redux';
import { TextInput, View } from 'react-native';
import uriBarStyle from '../../styles/uriBar';

class UriBar extends React.PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      uri: null,
      currentValue: null
    };
  }

  render() {
    const { value, navigation } = this.props;
    if (!this.state.currentValue) {
      this.setState({ currentValue: value });
    }
    
    // TODO: Search and URI suggestions overlay
    return (
      <View style={uriBarStyle.uriContainer}>
        <TextInput style={uriBarStyle.uriText}
                   placeholder={'Enter a LBRY URI or some text'}
                   underlineColorAndroid={'transparent'}
                   numberOfLines={1}
                   value={this.state.currentValue}
                   returnKeyType={'go'}
                   onChangeText={(text) => this.setState({uri: text, currentValue: text})}
                   onSubmitEditing={() => {
                    if (this.state.uri) {
                      let uri = this.state.uri;
                      uri = uri.replace(/ /g, '-');
                      navigation.navigate('File', { uri: normalizeURI(uri) });
                    }
                  }}/>
      </View>
    );
  }
}

export default UriBar;
