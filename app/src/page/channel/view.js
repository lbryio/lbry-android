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
import Constants from 'constants';
import Button from 'component/button';
import Link from 'component/link';
import FileList from 'component/fileList';
import PageHeader from 'component/pageHeader';
import SubscribeButton from 'component/subscribeButton';
import UriBar from 'component/uriBar';
import channelPageStyle from 'styles/channelPage';

class ChannelPage extends React.PureComponent {
  state = {
    page: 1,
    showPageButtons: false,
    activeTab: Constants.CONTENT_TAB
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
    const { claim } = this.props;

    if (!claim) {
      return (
        <View style={channelPageStyle.aboutTab}>
          <View style={channelPageStyle.busyContainer}>
            <Text style={channelPageStyle.infoText}>No information to display.</Text>
          </View>
        </View>
      );
    }

    const { cover, description, thumbnail, email, website_url, title } = claim.value;
    return (
      <View style={channelPageStyle.aboutTab}>
        {(!website_url && !email && !description) &&
          <View style={channelPageStyle.busyContainer}>
            <Text style={channelPageStyle.infoText}>Nothing here yet. Please check back later.</Text>
          </View>}

        {(website_url || email || description) &&
        <ScrollView style={channelPageStyle.aboutScroll} contentContainerStyle={channelPageStyle.aboutScrollContent}>
          {(website_url && website_url.trim().length > 0) &&
          <View style={channelPageStyle.aboutItem}>
            <Text style={channelPageStyle.aboutTitle}>Website</Text>
            <Link style={channelPageStyle.aboutText} text={website_url} href={website_url} />
          </View>}

          {(email && email.trim().length > 0) &&
          <View style={channelPageStyle.aboutItem}>
            <Text style={channelPageStyle.aboutTitle}>Email</Text>
            <Link style={channelPageStyle.aboutText} text={email} href={`mailto:${email}`} />
          </View>}

          {(description && description.trim().length > 0) &&
          <View style={channelPageStyle.aboutItem}>
            <Text style={channelPageStyle.aboutText}>{description}</Text>
          </View>}
        </ScrollView>}
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

    let thumbnailUrl, coverUrl, title;
    if (claim && claim.value) {
      title = claim.value.title;
      if (claim.value.cover) {
        coverUrl = claim.value.cover.url;
      }
      if (claim.value.thumbnail) {
        thumbnailUrl = claim.value.thumbnail.url;
      }
    }


    return (
      <View style={channelPageStyle.container}>
        <UriBar value={uri} navigation={navigation} />


        <View style={channelPageStyle.viewContainer}>
          <View style={channelPageStyle.cover}>
            <Image
              style={channelPageStyle.coverImage}
              resizeMode={'cover'}
              source={(coverUrl && coverUrl.trim().length > 0) ? { uri: coverUrl } : require('../../assets/default_channel_cover.png')} />

            <View style={channelPageStyle.channelHeader}>
              <Text style={channelPageStyle.channelName}>{(title && title.trim().length > 0) ? title : name}</Text>
            </View>

            <View style={channelPageStyle.avatarImageContainer}>
              <Image
                style={channelPageStyle.avatarImage}
                resizeMode={'cover'}
                source={(thumbnailUrl && thumbnailUrl.trim().length > 0) ? { uri: thumbnailUrl } : require('../../assets/default_avatar.jpg')} />
            </View>

            <SubscribeButton style={channelPageStyle.subscribeButton} uri={uri} name={name} />
          </View>

          <View style={channelPageStyle.tabBar}>
            <TouchableOpacity style={channelPageStyle.tab} onPress={() => this.setState({ activeTab: Constants.CONTENT_TAB })}>
              <Text style={channelPageStyle.tabTitle}>CONTENT</Text>
              {Constants.CONTENT_TAB === this.state.activeTab && <View style={channelPageStyle.activeTabHint} />}
            </TouchableOpacity>
            <TouchableOpacity style={channelPageStyle.tab} onPress={() => this.setState({ activeTab: Constants.ABOUT_TAB })}>
              <Text style={channelPageStyle.tabTitle}>ABOUT</Text>
              {Constants.ABOUT_TAB === this.state.activeTab && <View style={channelPageStyle.activeTabHint} />}
            </TouchableOpacity>
          </View>

          {Constants.CONTENT_TAB === this.state.activeTab && this.renderContent()}
          {Constants.ABOUT_TAB === this.state.activeTab && this.renderAbout()}
        </View>
      </View>
    )
  }
}

export default ChannelPage;
