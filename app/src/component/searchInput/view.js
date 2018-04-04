import React from 'react';
import { TextInput } from 'react-native';

class SearchInput extends React.PureComponent {
  static INPUT_TIMEOUT = 500;
  
  state = {
    changeTextTimeout: -1
  };
  
  handleChangeText = text => {
    clearTimeout(this.state.changeTextTimeout);
    if (!text || text.trim().length < 2) {
      // only perform a search if 2 or more characters have been input
      return;
    }
    const { search, updateSearchQuery } = this.props;
    updateSearchQuery(text);
    
    let timeout = setTimeout(() => {
      search(text);
    }, SearchInput.INPUT_TIMEOUT);
    this.setState({ changeTextTimeout: timeout });
  }
  
  render() {
    const { style, value } = this.props;
    
    return (
      <TextInput
        style={style}
        placeholder="Search"
        underlineColorAndroid="transparent"
        value={value}
        onChangeText={text => this.handleChangeText(text)} />
    );
  }
}

export default SearchInput;
