import React from 'react';
import { Lbry, parseURI, normalizeURI, isURIValid } from 'lbry-redux';
import {
  ActivityIndicator,
  Button,
  Text,
  TextInput,
  View,
  ScrollView
} from 'react-native';
import { navigateToUri } from 'utils/helper';
import Colors from 'styles/colors';
import Constants from 'constants';
import PageHeader from 'component/pageHeader';
import FileListItem from 'component/fileListItem';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import UriBar from 'component/uriBar';
import searchStyle from 'styles/search';

class SearchPage extends React.PureComponent {
  state = {
    currentUri: null
  }

  static navigationOptions = {
    title: 'Search Results'
  };

  didFocusListener;

  componentWillMount() {
    const { navigation } = this.props;
    this.didFocusListener = navigation.addListener('didFocus', this.onComponentFocused);
  }

  componentWillUnmount() {
    if (this.didFocusListener) {
      this.didFocusListener.remove();
    }
  }

  onComponentFocused = () => {
    const { pushDrawerStack, setPlayerVisible, search } = this.props;
    pushDrawerStack();
    setPlayerVisible();

    const searchQuery = this.getSearchQuery();
    if (searchQuery && searchQuery.trim().length > 0) {
      this.setState({ currentUri: (isURIValid(searchQuery)) ? normalizeURI(searchQuery) : null })
      search(searchQuery);
    }
  }

  componentDidMount() {
    this.onComponentFocused();
  }

  componentWillReceiveProps(nextProps) {
    const { currentRoute } = nextProps;
    const { currentRoute: prevRoute } = this.props;
    if (Constants.DRAWER_ROUTE_SEARCH === currentRoute && currentRoute !== prevRoute) {
      this.onComponentFocused();
    }
  }

  getSearchQuery() {
    const { navigation } = this.props;
    if (navigation && navigation.state && navigation.state.params) {
      return navigation.state.params.searchQuery;
    }
    return null;
  }

  handleSearchSubmitted = (keywords) => {
    const { search } = this.props;
    this.setState({ currentUri: (isURIValid(keywords)) ? normalizeURI(keywords) : null });
    search(keywords);
  }

  render() {
    const { isSearching, navigation, query, uris, urisByQuery } = this.props;

    return (
      <View style={searchStyle.container}>
        <UriBar value={this.getSearchQuery() || query}
                navigation={navigation}
                onSearchSubmitted={this.handleSearchSubmitted} />
        {isSearching &&
          <View style={searchStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} style={searchStyle.loading} />
          </View>}

        {!isSearching && (!uris || uris.length === 0) &&
            <Text style={searchStyle.noResultsText}>No results to display.</Text>}
        {!isSearching &&
        <ScrollView
          style={searchStyle.scrollContainer}
          contentContainerStyle={searchStyle.scrollPadding}
          keyboardShouldPersistTaps={'handled'}>
          {this.state.currentUri &&
          <FileListItem
            key={this.state.currentUri}
            uri={this.state.currentUri}
            featuredResult={true}
            style={searchStyle.featuredResultItem}
            navigation={navigation}
            onPress={() => navigateToUri(navigation, this.state.currentUri)}
          />}
          {(uris && uris.length) ? (
                uris.map(uri => <FileListItem key={uri}
                                              uri={uri}
                                              style={searchStyle.resultItem}
                                              navigation={navigation}
                                              onPress={() => navigateToUri(navigation, uri)}/>)
              ) : null }
        </ScrollView>}
        <FloatingWalletBalance navigation={navigation} />
      </View>
    );
  }
}

export default SearchPage;
