import React from 'react';
import { ActivityIndicator, FlatList, Text, View } from 'react-native';
import { navigateToUri } from 'utils/helper';
import Colors from 'styles/colors';
import FileListItem from 'component/fileListItem';
import fileListStyle from 'styles/fileList';
import relatedContentStyle from 'styles/relatedContent';

export default class RelatedContent extends React.PureComponent<Props> {
  constructor() {
    super();

    this.didSearch = undefined;
  }

  componentDidMount() {
    this.getRecommendedContent();
  }

  componentDidUpdate(prevProps: Props) {
    const { claim, uri } = this.props;

    if (uri !== prevProps.uri) {
      this.didSearch = false;
    }

    if (claim && !this.didSearch) {
      this.getRecommendedContent();
    }
  }

  getRecommendedContent() {
    const { search, title } = this.props;

    if (title) {
      search(title);
      this.didSearch = true;
    }
  }

  didSearch: ?boolean;

  render() {
    const { recommendedContent, isSearching, navigation } = this.props;

    if (!isSearching && (!recommendedContent || recommendedContent.length === 0)) {
      return null;
    }

    return (
      <View style={relatedContentStyle.container}>
        <Text style={relatedContentStyle.title}>Related Content</Text>
        {recommendedContent && recommendedContent.map(recommendedUri => (
          <FileListItem
            style={fileListStyle.item}
            key={recommendedUri}
            uri={recommendedUri}
            navigation={navigation}
            onPress={() => navigateToUri(navigation, recommendedUri, { autoplay: true })} />
        ))}
        {isSearching && <ActivityIndicator size="small" color={Colors.LbryGreen} style={relatedContentStyle.loading} />}
      </View>
    );
  }
}