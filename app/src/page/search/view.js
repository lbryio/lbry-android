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
import SearchResultItem from '../../component/searchResultItem';
import Colors from '../../styles/colors';
import searchStyle from '../../styles/search';

class SearchPage extends React.PureComponent {
  render() {
    const { isSearching, navigation, uris } = this.props;

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
                                                  onPress={() => {navigation.navigate('File', { uri: uri }); }}/>)
              ) : null }
        </ScrollView>
        {isSearching && <ActivityIndicator size="large" color={Colors.LbryGreen} style={searchStyle.loading} /> }
      </View>
    );
  }
}

export default SearchPage;
