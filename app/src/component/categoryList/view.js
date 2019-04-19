import React from 'react';
import NavigationActions from 'react-navigation';
import { FlatList, Text, View } from 'react-native';
import { normalizeURI } from 'lbry-redux';
import FileItem from '/component/fileItem';
import discoverStyle from 'styles/discover';

class CategoryList extends React.PureComponent {
  render() {
    const { category, categoryMap, navigation } = this.props;

    return (
      <FlatList
        style={discoverStyle.horizontalScrollContainer}
        contentContainerStyle={discoverStyle.horizontalScrollPadding}
        renderItem={ ({item}) => (
          <FileItem
            style={discoverStyle.fileItem}
            mediaStyle={discoverStyle.fileItemMedia}
            key={item}
            uri={normalizeURI(item)}
            navigation={navigation}
            showDetails={true}
            compactView={false} />
          )
        }
        horizontal={true}
        showsHorizontalScrollIndicator={false}
        data={categoryMap[category]}
        keyExtractor={(item, index) => item}
      />
    );
  }
}

export default CategoryList;
