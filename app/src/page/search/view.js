import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Button,
  Text,
  TextInput,
  View,
  ScrollView
} from 'react-native';
import { navigateToUri } from '../../utils/helper';
import Colors from '../../styles/colors';
import PageHeader from '../../component/pageHeader';
import FileListItem from '../../component/fileListItem';
import FloatingWalletBalance from '../../component/floatingWalletBalance';
import UriBar from '../../component/uriBar';
import searchStyle from '../../styles/search';

class SearchPage extends React.PureComponent {
  static navigationOptions = {
    title: 'Search Results'
  };

  componentWillMount() {
    this.props.pushDrawerStack();
  }

  componentDidMount() {
    const { navigation, search } = this.props;
    const { searchQuery } = navigation.state.params;
    if (searchQuery && searchQuery.trim().length > 0) {
      search(searchQuery);
    }
  }

  render() {
    const { isSearching, navigation, query, search, uris } = this.props;
    const { searchQuery } = navigation.state.params;

    return (
      <View style={searchStyle.container}>
        <UriBar value={searchQuery}
                navigation={navigation}
                onSearchSubmitted={(keywords) => search(keywords)} />
        {!isSearching && (!uris || uris.length === 0) &&
            <Text style={searchStyle.noResultsText}>No results to display.</Text>}
        <ScrollView style={searchStyle.scrollContainer} contentContainerStyle={searchStyle.scrollPadding}>
          {!isSearching && uris && uris.length ? (
                uris.map(uri => <FileListItem key={uri}
                                              uri={uri}
                                              style={searchStyle.resultItem}
                                              navigation={navigation}
                                              onPress={() => navigateToUri(navigation, uri)}/>)
              ) : null }
        </ScrollView>
        {isSearching && <ActivityIndicator size="large" color={Colors.LbryGreen} style={searchStyle.loading} /> }
        <FloatingWalletBalance navigation={navigation} />
      </View>
    );
  }
}

export default SearchPage;
