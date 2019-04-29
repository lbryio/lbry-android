// @flow
import React from 'react';
import {
  ActivityIndicator,
  Dimensions,
  Image,
  ScrollView,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import { TabView, SceneMap } from 'react-native-tab-view';
import { navigateBack } from 'utils/helper';
import Colors from 'styles/colors';
import Button from 'component/button';
import FileList from 'component/fileList';
import PageHeader from 'component/pageHeader';
import SubscribeButton from 'component/subscribeButton';
import UriBar from 'component/uriBar';
import channelPageStyle from 'styles/channelPage';

class ChannelPage extends React.PureComponent {
  state = {
    page: 1,
    showPageButtons: false,
    activeTab: 'content'
  };

  componentDidMount() {
    const { uri, page, claimsInChannel, fetchClaims, fetchClaimCount } = this.props;

    if (!claimsInChannel || !claimsInChannel.length) {
      fetchClaims(uri, page || this.state.page);
    }
  }

  handlePreviousPage = () => {
    const { uri, fetchClaims } = this.props;
    if (this.state.page > 1) {
      this.setState({ page: this.state.page - 1, showPageButtons: false }, () => {
        fetchClaims(uri, this.state.page);
      });
    }
  }

  handleNextPage = () => {
    const { uri, fetchClaims, totalPages } = this.props;
    if (this.state.page < totalPages) {
      this.setState({ page: this.state.page + 1, showPageButtons: false }, () => {
        fetchClaims(uri, this.state.page);
      });
    }
  }

  renderContent = () => {
    const { fetching, claimsInChannel, totalPages, navigation } = this.props;

    let contentList;
    if (fetching) {
      contentList = (
        <View style={channelPageStyle.busyContainer}>
          <ActivityIndicator size="large" color={Colors.LbryGreen} />
          <Text style={channelPageStyle.infoText}>Fetching content...</Text>
        </View>
      );
    } else {
      contentList =
        claimsInChannel && claimsInChannel.length ? (
          <FileList sortByHeight
                    hideFilter
                    fileInfos={claimsInChannel}
                    navigation={navigation}
                    style={channelPageStyle.fileList}
                    contentContainerStyle={channelPageStyle.fileListContent}
                    onEndReached={() => this.setState({ showPageButtons: true })} />
        ) : (
          <View style={channelPageStyle.busyContainer}>
            <Text style={channelPageStyle.infoText}>No content found.</Text>
          </View>
        );
    }

    let pageButtons;
    if (totalPages > 1 && this.state.showPageButtons) {
      pageButtons = (
        <View style={channelPageStyle.pageButtons}>
          <View>
            {(this.state.page > 1) && <Button
                                        style={channelPageStyle.button}
                                        text={"Previous"}
                                        disabled={!!fetching}
                                        onPress={this.handlePreviousPage} />}
          </View>
          {(this.state.page < totalPages) && <Button
                                               style={[channelPageStyle.button, channelPageStyle.nextButton]}
                                               text={"Next"}
                                               disabled={!!fetching}
                                               onPress={this.handleNextPage} />}
        </View>
      );
    }

    return (
      <View style={channelPageStyle.contentTab}>
        {contentList}
        {pageButtons}
      </View>
    );
  }

  renderAbout = () => {
    return (
      <View style={channelPageStyle.aboutTab}>

        <ScrollView style={channelPageStyle.aboutScroll} contentContainerStyle={channelPageStyle.aboutScrollContent}>
          <View style={channelPageStyle.aboutItem}>
            <Text style={channelPageStyle.aboutTitle}>Website</Text>
            <Text style={channelPageStyle.aboutText}>https://www.website.com</Text>
          </View>

          <View style={channelPageStyle.aboutItem}>
            <Text style={channelPageStyle.aboutTitle}>Email</Text>
            <Text style={channelPageStyle.aboutText}>mail@email.com</Text>
          </View>

          <View style={channelPageStyle.aboutItem}>
            <Text style={channelPageStyle.aboutText}>Content description here</Text>
          </View>
        </ScrollView>

      </View>
    );
  }

  render() {
    const {
      fetching,
      claimsInChannel,
      claim,
      navigation,
      totalPages,
      uri,
      drawerStack,
      popDrawerStack
    } = this.props;
    const { name, permanent_url: permanentUrl } = claim;

    // <SubscribeButton style={channelPageStyle.subscribeButton} uri={uri} name={name} />

    return (
      <View style={channelPageStyle.container}>
        <UriBar value={uri} navigation={navigation} />


        <View style={channelPageStyle.viewContainer}>
          <View style={channelPageStyle.cover}>
            <Image
              style={channelPageStyle.coverImage}
              resizeMode={'cover'}
              source={require('../../assets/default_channel_cover.png')} />

            <View style={channelPageStyle.channelHeader}>
              <Text style={channelPageStyle.channelName}>{name}</Text>
            </View>

            <Image
              style={channelPageStyle.avatarImage}
              resizeMode={'stretch'}
              source={require('../../assets/default_avatar.jpg')} />
          </View>

          <View style={channelPageStyle.tabBar}>
            <TouchableOpacity style={channelPageStyle.tab} onPress={() => this.setState({ activeTab: 'content' })}>
              <Text style={channelPageStyle.tabTitle}>CONTENT</Text>
              {'content' === this.state.activeTab && <View style={channelPageStyle.activeTabHint} />}
            </TouchableOpacity>
            <TouchableOpacity style={channelPageStyle.tab} onPress={() => this.setState({ activeTab: 'about' })}>
              <Text style={channelPageStyle.tabTitle}>ABOUT</Text>
              {'about' === this.state.activeTab && <View style={channelPageStyle.activeTabHint} />}
            </TouchableOpacity>
          </View>

          {'content' === this.state.activeTab && this.renderContent()}
          {'about' === this.state.activeTab && this.renderAbout()}
        </View>
      </View>
    )
  }
}

export default ChannelPage;
