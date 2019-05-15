import React from 'react';
import { normalizeURI } from 'lbry-redux';
import { NavigationActions } from 'react-navigation';
import { NativeModules, Text, View, TouchableOpacity } from 'react-native';
import { navigateToUri } from 'utils/helper';
import Colors from 'styles/colors';
import DateTime from 'component/dateTime';
import FileItemMedia from 'component/fileItemMedia';
import FilePrice from 'component/filePrice';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Link from 'component/link';
import NsfwOverlay from 'component/nsfwOverlay';
import discoverStyle from 'styles/discover';

class FileItem extends React.PureComponent {
  constructor(props) {
    super(props);
  }

  componentWillMount() {
    this.resolve(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.resolve(nextProps);
  }

  resolve(props) {
    const { isResolvingUri, resolveUri, claim, uri } = props;

    if (!isResolvingUri && claim === undefined && uri) {
      resolveUri(uri);
    }
  }

  navigateToFileUri = () => {
    const { navigation, uri } = this.props;
    const normalizedUri = normalizeURI(uri);
    if (NativeModules.Firebase) {
      NativeModules.Firebase.track('explore_click', { uri: normalizedUri });
    }
    navigateToUri(navigation, normalizedUri);
  }

  render() {
    const {
      claim,
      title,
      thumbnail,
      fileInfo,
      metadata,
      isResolvingUri,
      rewardedContentClaimIds,
      style,
      mediaStyle,
      navigation,
      showDetails,
      compactView,
      titleBeforeThumbnail
    } = this.props;

    const uri = normalizeURI(this.props.uri);
    const obscureNsfw = this.props.obscureNsfw && metadata && metadata.nsfw;
    const isRewardContent = claim && rewardedContentClaimIds.includes(claim.claim_id);
    const channelName = claim ? claim.channel_name : null;
    const channelClaimId = claim && claim.value && claim.value.publisherSignature && claim.value.publisherSignature.certificateId;
    const fullChannelUri = channelClaimId ? `${channelName}#${channelClaimId}` : channelName;
    const height = claim ? claim.height : null;

    return (
      <View style={style}>
        <TouchableOpacity style={discoverStyle.container} onPress={this.navigateToFileUri}>
          {!compactView && titleBeforeThumbnail && <Text numberOfLines={1} style={[discoverStyle.fileItemName, discoverStyle.rewardTitle]}>{title}</Text>}
          <FileItemMedia title={title}
                         thumbnail={thumbnail}
                         blurRadius={obscureNsfw ? 15 : 0}
                         resizeMode="cover"
                         isResolvingUri={isResolvingUri}
                         style={mediaStyle} />

          {(!compactView && fileInfo && fileInfo.completed) && <Icon style={discoverStyle.downloadedIcon} solid={true} color={Colors.BrightGreen} name={"folder"} size={16} />}
          {(!compactView && (!fileInfo || !fileInfo.completed)) && <FilePrice uri={uri} style={discoverStyle.filePriceContainer} textStyle={discoverStyle.filePriceText} />}
          {!compactView && <View style={isRewardContent ? discoverStyle.rewardTitleContainer : null}>
            <Text numberOfLines={1} style={[discoverStyle.fileItemName, discoverStyle.rewardTitle]}>{title}</Text>
            {isRewardContent && <Icon style={discoverStyle.rewardIcon} name="award" size={14} />}
          </View>}
          {(!compactView && showDetails) &&
          <View style={discoverStyle.detailsRow}>
            {channelName &&
              <Link style={discoverStyle.channelName} text={channelName} onPress={() => {
                navigateToUri(navigation, normalizeURI(fullChannelUri));
              }} />}
            <DateTime style={discoverStyle.dateTime} textStyle={discoverStyle.dateTimeText} timeAgo uri={uri} />
          </View>}
        </TouchableOpacity>
        {obscureNsfw && <NsfwOverlay onPress={() => navigation.navigate({ routeName: 'Settings', key: 'settingsPage' })} />}
      </View>
    );
  }
}

export default FileItem;