import React from 'react';
import { Text, TextInput, TouchableOpacity, View } from 'react-native';
import Tag from 'component/tag';
import tagStyle from 'styles/tag';
import Colors from 'styles/colors';
import Icon from 'react-native-vector-icons/FontAwesome5';

export default class TagSearch extends React.PureComponent {
  state = {
    tag: null,
    tagResults: [],
  };

  componentDidMount() {
    const { selectedTags = [] } = this.props;
    this.updateTagResults(this.state.tag, selectedTags);
  }

  componentWillReceiveProps(nextProps) {
    const { selectedTags: prevSelectedTags = [] } = this.props;
    const { selectedTags = [] } = nextProps;

    if (selectedTags.length !== prevSelectedTags.length) {
      this.updateTagResults(this.state.tag, selectedTags);
    }
  }

  onAddTagPress = tag => {
    const { handleAddTag } = this.props;
    if (handleAddTag) {
      handleAddTag(tag);
    }
  };

  handleTagChange = tag => {
    const { selectedTags = [] } = this.props;
    this.setState({ tag });
    this.updateTagResults(tag, selectedTags);
  };

  updateTagResults = (tag, selectedTags = []) => {
    const { unfollowedTags } = this.props;

    // the search term should always be the first result
    let results = [];
    const tagNotSelected = name => selectedTags.indexOf(name.toLowerCase()) === -1;
    const suggestedTagsSet = new Set(unfollowedTags.map(tag => tag.name));
    const suggestedTags = Array.from(suggestedTagsSet).filter(tagNotSelected);
    if (tag && tag.trim().length > 0) {
      results.push(tag.toLowerCase());
      const doesTagMatch = name => name.toLowerCase().includes(tag.toLowerCase());
      results = results.concat(suggestedTags.filter(doesTagMatch).slice(0, 5));
    } else {
      results = results.concat(suggestedTags.slice(0, 5));
    }

    this.setState({ tagResults: results });
  };

  render() {
    const { name, style, type, selectedTags = [] } = this.props;

    return (
      <View>
        <TextInput
          style={tagStyle.searchInput}
          placeholder={'Search for more tags'}
          underlineColorAndroid={Colors.NextLbryGreen}
          value={this.state.tag}
          numberOfLines={1}
          onChangeText={this.handleTagChange}
        />
        <View style={tagStyle.tagResultsList}>
          {this.state.tagResults.map(tag => (
            <Tag key={tag} name={tag} style={tagStyle.tag} type="add" onAddPress={name => this.onAddTagPress(name)} />
          ))}
        </View>
      </View>
    );
  }
}
