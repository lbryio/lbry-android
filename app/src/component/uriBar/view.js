// @flow
import React from 'react';
import { SEARCH_TYPES, isNameValid, isURIValid, normalizeURI } from 'lbry-redux';
import { FlatList, Keyboard, TextInput, View } from 'react-native';
import { navigateToUri } from 'utils/helper';
import UriBarItem from './internal/uri-bar-item';
import NavigationButton from 'component/navigationButton';
import discoverStyle from 'styles/discover';
import uriBarStyle from 'styles/uriBar';

class UriBar extends React.PureComponent {
  static INPUT_TIMEOUT = 1000; // 1 second

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
      focused: false,
      // TODO: Add a setting to enable / disable direct search?
      directSearch: true
    };
  }

  handleChangeText = text => {
    const newValue = text ? text : '';
    clearTimeout(this.state.changeTextTimeout);
    const { updateSearchQuery, onSearchSubmitted, navigation } = this.props;

    let timeout = setTimeout(() => {
      if (text.trim().length === 0) {
        // don't do anything if the text is empty
        return;
      }

      updateSearchQuery(text);

      if (!text.startsWith('lbry://')) {
        // not a URI input, so this is a search, perform a direct search
        if (onSearchSubmitted) {
          onSearchSubmitted(text);
        } else {
          navigation.navigate({ routeName: 'Search', key: 'searchPage', params: { searchQuery: text }});
        }
      }

    }, UriBar.INPUT_TIMEOUT);
    this.setState({ inputText: newValue, currentValue: newValue, changeTextTimeout: timeout });
  }

  handleItemPress = (item) => {
    const { navigation, onSearchSubmitted, updateSearchQuery } = this.props;
    const { type, value } = item;

    Keyboard.dismiss();

    if (SEARCH_TYPES.SEARCH === type) {
      this.setState({ currentValue: value });
      updateSearchQuery(value);

      if (onSearchSubmitted) {
        onSearchSubmitted(value);
        return;
      }

      navigation.navigate({ routeName: 'Search', key: 'searchPage', params: { searchQuery: value }});
    } else {
      const uri = normalizeURI(value);
      navigateToUri(navigation, uri);
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
    const { navigation, onSearchSubmitted, suggestions, updateSearchQuery, value } = this.props;
    if (this.state.currentValue === null) {
      this.setState({ currentValue: value });
    }

    let style = [uriBarStyle.overlay];

    // TODO: Add optional setting to enable URI / search bar suggestions
    /*if (this.state.focused) { style.push(uriBarStyle.inFocus); }*/

    return (
      <View style={style}>
        <View style={uriBarStyle.uriContainer}>
          <NavigationButton
            name="bars"
            size={24}
            style={uriBarStyle.drawerMenuButton}
            iconStyle={discoverStyle.drawerHamburger}
            onPress={() => navigation.openDrawer() } />
          <TextInput ref={(ref) => { this.textInput = ref }}
                     style={uriBarStyle.uriText}
                     onLayout={() => { this.setSelection(); }}
                     selectTextOnFocus={true}
                     placeholder={'Search movies, music, and more'}
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
                        if (inputText.startsWith('lbry://') && isURIValid(inputText)) {
                          // if it's a URI (lbry://...), open the file page
                          const uri = normalizeURI(inputText);
                          navigateToUri(navigation, uri);
                        } else {
                          // Not a URI, default to a search request
                          if (onSearchSubmitted) {
                            // Only the search page sets the onSearchSubmitted prop, so call this prop if set
                            onSearchSubmitted(inputText);
                            return;
                          }

                          // Open the search page with the query populated
                          navigation.navigate({ routeName: 'Search', key: 'searchPage', params: { searchQuery: inputText }});
                        }
                      }
                    }}/>
          {(this.state.focused && !this.state.directSearch) && (
          <View style={uriBarStyle.suggestions}>
            <FlatList style={uriBarStyle.suggestionList}
                      data={suggestions}
                      keyboardShouldPersistTaps={'handled'}
                      keyExtractor={(item, value) => item.value}
                      renderItem={({item}) => (
                        <UriBarItem
                          item={item}
                          navigation={navigation}
                          onPress={() => this.handleItemPress(item)}
                        />)} />
          </View>)}
        </View>
      </View>
    );
  }
}

export default UriBar;
