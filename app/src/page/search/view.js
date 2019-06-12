import React from 'react';
import { Lbry, parseURI, normalizeURI, isURIValid } from 'lbry-redux';
import { ActivityIndicator, Button, Text, TextInput, View, ScrollView } from 'react-native';
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
    currentQuery: null,
    currentUri: null,
  };

  static navigationOptions = {
    title: 'Search Results',
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
    const { pushDrawerStack, setPlayerVisible, query, search } = this.props;
    pushDrawerStack();
    setPlayerVisible();

    const searchQuery = query || this.getSearchQuery();
    if (searchQuery && searchQuery.trim().length > 0) {
      this.setState({
        currentQuery: searchQuery,
        currentUri: isURIValid(searchQuery) ? normalizeURI(searchQuery) : null,
      });
      search(searchQuery);
    }
  };

  componentDidMount() {
    this.onComponentFocused();
  }

  componentWillReceiveProps(nextProps) {
    const { currentRoute, query } = nextProps;
    const { currentRoute: prevRoute, search } = this.props;

    if (Constants.DRAWER_ROUTE_SEARCH === currentRoute && currentRoute !== prevRoute) {
      this.onComponentFocused();
    }

    if (query && query.trim().length > 0 && query !== this.state.currentQuery) {
      this.setState({
        currentQuery: query,
        currentUri: isURIValid(query) ? normalizeURI(query) : null,
      });
      search(query);
    }
  }

  getSearchQuery() {
    const { navigation } = this.props;
    if (navigation && navigation.state && navigation.state.params) {
      return navigation.state.params.searchQuery;
    }
    return null;
  }

  handleSearchSubmitted = keywords => {
    const { search } = this.props;
    this.setState({ currentUri: isURIValid(keywords) ? normalizeURI(keywords) : null });
    search(keywords);
  };

  render() {
    const { isSearching, navigation, query, uris, urisByQuery } = this.props;

    return (
      <View style={searchStyle.container}>
        <UriBar value={query} navigation={navigation} onSearchSubmitted={this.handleSearchSubmitted} />
        {isSearching && (
          <View style={searchStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} style={searchStyle.loading} />
          </View>
        )}

        {!isSearching && (
          <ScrollView
            style={searchStyle.scrollContainer}
            contentContainerStyle={searchStyle.scrollPadding}
            keyboardShouldPersistTaps={'handled'}
          >
            {this.state.currentUri && (
              <FileListItem
                key={this.state.currentUri}
                uri={this.state.currentUri}
                featuredResult={true}
                style={searchStyle.featuredResultItem}
                navigation={navigation}
                onPress={() => navigateToUri(navigation, this.state.currentUri)}
              />
            )}
            {uris && uris.length
              ? uris.map(uri => (
                  <FileListItem
                    key={uri}
                    uri={uri}
                    style={searchStyle.resultItem}
                    navigation={navigation}
                    onPress={() => navigateToUri(navigation, uri)}
                  />
                ))
              : null}
            {(!uris || uris.length === 0) && (
              <View style={searchStyle.noResults}>
                <Text style={searchStyle.noResultsText}>
                  There are no results to display for <Text style={searchStyle.boldText}>{query}</Text>. Please try a
                  different search term.
                </Text>
              </View>
            )}
          </ScrollView>
        )}
        <FloatingWalletBalance navigation={navigation} />
      </View>
    );
  }
}

export default SearchPage;
