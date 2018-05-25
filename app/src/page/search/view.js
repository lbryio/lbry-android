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
import Colors from '../../styles/colors';
import PageHeader from '../../component/pageHeader';
import SearchResultItem from '../../component/searchResultItem';
import UriBar from '../../component/uriBar';
import searchStyle from '../../styles/search';

class SearchPage extends React.PureComponent {
  static navigationOptions = {
    title: 'Search Results'
  };
  
  componentDidMount() {
    const { navigation, search } = this.props;
    const { searchQuery } = navigation.state.params;
    if (searchQuery && searchQuery.trim().length > 0) {
      search(searchQuery);
    }
  }
  
  render() {
    const { isSearching, navigation, uris, query } = this.props;
    const { searchQuery } = navigation.state.params;
    
    return (
      <View style={searchStyle.container}>
        {!isSearching && (!uris || uris.length === 0) &&
            <Text style={searchStyle.noResultsText}>No results to display.</Text>}
        <ScrollView style={searchStyle.scrollContainer} contentContainerStyle={searchStyle.scrollPadding}>
          {!isSearching && uris && uris.length ? (
                uris.map(uri => <SearchResultItem key={uri}
                                                  uri={uri}
                                                  style={searchStyle.resultItem}
                                                  navigation={navigation}
                                                  onPress={() => navigation.navigate({
                                                    routeName: 'File',
                                                    key: 'filePage',
                                                    params: { uri }})
                                                  }/>)
              ) : null }
        </ScrollView>
        {isSearching && <ActivityIndicator size="large" color={Colors.LbryGreen} style={searchStyle.loading} /> }
        <UriBar value={searchQuery} navigation={navigation} />
      </View>
    );
  }
}

export default SearchPage;
