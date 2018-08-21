// @flow
import React from 'react';
import { SEARCH_TYPES, isNameValid, isURIValid, normalizeURI } from 'lbry-redux';
import { FlatList, Keyboard, TextInput, View } from 'react-native';
import UriBarItem from './internal/uri-bar-item';
import uriBarStyle from '../../styles/uriBar';

class UriBar extends React.PureComponent {
  static INPUT_TIMEOUT = 500;

  textInput = null;

  keyboardDidHideListener = null;

  componentDidMount () {
    this.keyboardDidHideListener = Keyboard.addListener('keyboardDidHide', this._keyboardDidHide);
    this.setSelection();
  }

  componentWillUnmount() {
    if (this.keyboardDidHideListener) {
      this.keyboardDidHideListener.remove();
    }
  }

  constructor(props) {
    super(props);
    this.state = {
      changeTextTimeout: null,
      currentValue: null,
      inputText: null,
      focused: false
    };
  }

  handleChangeText = text => {
    const newValue = text ? text : '';
    clearTimeout(this.state.changeTextTimeout);
    const { updateSearchQuery } = this.props;

    let timeout = setTimeout(() => {
      updateSearchQuery(text);
    }, UriBar.INPUT_TIMEOUT);
    this.setState({ inputText: newValue, currentValue: newValue, changeTextTimeout: timeout });
  }

  handleItemPress = (item) => {
    const { navigation, updateSearchQuery } = this.props;
    const { type, value } = item;

    Keyboard.dismiss();

    if (SEARCH_TYPES.SEARCH === type) {
      navigation.navigate({ routeName: 'Search', key: 'searchPage', params: { searchQuery: value }});
    } else {
      const uri = normalizeURI(value);
      navigation.navigate({ routeName: 'File', key: uri, params: { uri }});
    }
  }

  _keyboardDidHide = () => {
    if (this.textInput) {
      this.textInput.blur();
    }
    this.setState({ focused: false });
  }

  setSelection() {
    if (this.textInput) {
      this.textInput.setNativeProps({ selection: { start: 0, end: 0 }});
    }
  }

  render() {
    const { navigation, suggestions, updateSearchQuery, value } = this.props;
    if (this.state.currentValue === null) {
      this.setState({ currentValue: value });
    }

    let style = [uriBarStyle.overlay];
    if (this.state.focused) {
      style.push(uriBarStyle.inFocus);
    }

    return (
      <View style={style}>
        {this.state.focused && (
        <View style={uriBarStyle.suggestions}>
          <FlatList style={uriBarStyle.suggestionList}
                    data={suggestions}
                    keyboardShouldPersistTaps={'handled'}
                    keyExtractor={(item, value) => item.value}
                    renderItem={({item}) => <UriBarItem item={item}
                                                        navigation={navigation}
                                                        onPress={() => this.handleItemPress(item)} />} />
        </View>)}
        <View style={uriBarStyle.uriContainer}>
          <TextInput ref={(ref) => { this.textInput = ref }}
                     style={uriBarStyle.uriText}
                     onLayout={() => { this.setSelection(); }}
                     selectTextOnFocus={true}
                     placeholder={'Search for videos, music, games and more'}
                     underlineColorAndroid={'transparent'}
                     numberOfLines={1}
                     clearButtonMode={'while-editing'}
                     value={this.state.currentValue}
                     returnKeyType={'go'}
                     inlineImageLeft={'baseline_search_black_24'}
                     inlineImagePadding={16}
                     onFocus={() => this.setState({ focused: true })}
                     onBlur={() => {
                       this.setState({ focused: false });
                       this.setSelection();
                     }}
                     onChangeText={this.handleChangeText}
                     onSubmitEditing={() => {
                      if (this.state.inputText) {
                        let inputText = this.state.inputText;
                        if (isNameValid(inputText) || isURIValid(inputText)) {
                          const uri = normalizeURI(inputText);
                          navigation.navigate({ routeName: 'File', key: uri, params: { uri }});
                        } else {
                          // Open the search page with the query populated
                          navigation.navigate({ routeName: 'Search', key: 'searchPage', params: { searchQuery: inputText }});
                        }
                      }
                    }}/>
        </View>
      </View>
    );
  }
}

export default UriBar;
