// @flow
import * as React from 'react';
import { buildURI } from 'lbry-redux';
import { FlatList } from 'react-native';
import FileItem from '../fileItem';
import discoverStyle from '../../styles/discover';

// In the future, all Flow types need to be specified in a common source (lbry-redux, perhaps?)
type FileInfo = {
  name: string,
  channelName: ?string,
  pending?: boolean,
  channel_claim_id: string,
  value?: {
    publisherSignature: {
      certificateId: string,
    },
  },
  metadata: {
    publisherSignature: {
      certificateId: string,
    },
  },
};

type Props = {
  hideFilter: boolean,
  sortByHeight?: boolean,
  claimsById: Array<{}>,
  fileInfos: Array<FileInfo>,
  checkPending?: boolean,
};

type State = {
  sortBy: string,
};

class FileList extends React.PureComponent<Props, State> {
  static defaultProps = {
    hideFilter: false,
  };

  constructor(props: Props) {
    super(props);

    this.state = {
      sortBy: 'dateNew',
    };

    (this: any).handleSortChanged = this.handleSortChanged.bind(this);

    this.sortFunctions = {
      dateNew: fileInfos =>
        this.props.sortByHeight
          ? fileInfos.slice().sort((fileInfo1, fileInfo2) => {
              if (fileInfo1.pending) {
                return -1;
              }
              const height1 = this.props.claimsById[fileInfo1.claim_id]
                ? this.props.claimsById[fileInfo1.claim_id].height
                : 0;
              const height2 = this.props.claimsById[fileInfo2.claim_id]
                ? this.props.claimsById[fileInfo2.claim_id].height
                : 0;
              if (height1 > height2) {
                return -1;
              } else if (height1 < height2) {
                return 1;
              }
              return 0;
            })
          : [...fileInfos].reverse(),
      dateOld: fileInfos =>
        this.props.sortByHeight
          ? fileInfos.slice().sort((fileInfo1, fileInfo2) => {
              const height1 = this.props.claimsById[fileInfo1.claim_id]
                ? this.props.claimsById[fileInfo1.claim_id].height
                : 999999;
              const height2 = this.props.claimsById[fileInfo2.claim_id]
                ? this.props.claimsById[fileInfo2.claim_id].height
                : 999999;
              if (height1 < height2) {
                return -1;
              } else if (height1 > height2) {
                return 1;
              }
              return 0;
            })
          : fileInfos,
      title: fileInfos =>
        fileInfos.slice().sort((fileInfo1, fileInfo2) => {
          const getFileTitle = fileInfo => {
            const { value, metadata, name, claim_name: claimName } = fileInfo;
            if (metadata) {
              // downloaded claim
              return metadata.title || claimName;
            } else if (value) {
              // published claim
              const { title } = value.stream.metadata;
              return title || name;
            }
            // Invalid claim
            return '';
          };
          const title1 = getFileTitle(fileInfo1).toLowerCase();
          const title2 = getFileTitle(fileInfo2).toLowerCase();
          if (title1 < title2) {
            return -1;
          } else if (title1 > title2) {
            return 1;
          }
          return 0;
        }),
      filename: fileInfos =>
        fileInfos.slice().sort(({ file_name: fileName1 }, { file_name: fileName2 }) => {
          const fileName1Lower = fileName1.toLowerCase();
          const fileName2Lower = fileName2.toLowerCase();
          if (fileName1Lower < fileName2Lower) {
            return -1;
          } else if (fileName2Lower > fileName1Lower) {
            return 1;
          }
          return 0;
        }),
    };
  }

  getChannelSignature = (fileInfo: FileInfo) => {
    if (fileInfo.pending) {
      return undefined;
    }

    if (fileInfo.value) {
      return fileInfo.value.publisherSignature.certificateId;
    }
    return fileInfo.channel_claim_id;
  };

  handleSortChanged(event: SyntheticInputEvent<*>) {
    this.setState({
      sortBy: event.target.value,
    });
  }

  sortFunctions: {};

  render() {
    const { fileInfos, hideFilter, checkPending, navigation, style } = this.props;
    const { sortBy } = this.state;
    const items = [];

    if (!fileInfos) {
      return null;
    }

    this.sortFunctions[sortBy](fileInfos).forEach(fileInfo => {
      const { name: claimName, claim_name: claimNameDownloaded, claim_id: claimId } = fileInfo;
      const uriParams = {};

      // This is unfortunate
      // https://github.com/lbryio/lbry/issues/1159
      const name = claimName || claimNameDownloaded;
      uriParams.contentName = name;
      uriParams.claimId = claimId;
      const uri = buildURI(uriParams);

      items.push(uri);
    });
    
    return (
      <FlatList
        style={style}
        data={items}
        keyExtractor={(item, index) => item}
        renderItem={({item}) => (
          <FileItem style={discoverStyle.fileItem}
                    uri={item}
                    navigation={navigation} />
        )} />  
    );
  }
}

export default FileList;
