// @flow
import React from 'react';
import moment from 'moment';
import { View, Text } from 'react-native';

type Props = {
  date?: number,
  timeAgo?: boolean,
  formatOptions: {},
  show?: string,
};

class DateTime extends React.PureComponent<Props> {
  static SHOW_DATE = 'date';
  static SHOW_TIME = 'time';
  static SHOW_BOTH = 'both';

  static defaultProps = {
    formatOptions: {
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    },
  };

  render() {
    const { date, formatOptions, timeAgo, style, textStyle } = this.props;
    const show = this.props.show || DateTime.SHOW_BOTH;
    const locale = 'en-US'; // default to en-US until we get a working i18n module for RN

    if (timeAgo) {
      return date ? <View style={style}><Text style={textStyle}>{moment(date).from(moment())}</Text></View> : null;
    }

    // TODO: formatOptions not working as expected in RN
    // date.toLocaleDateString([locale, 'en-US'], formatOptions)}

    return (
      <View style={style}>
        <Text style={textStyle}>
          {date &&
            (show === DateTime.SHOW_BOTH || show === DateTime.SHOW_DATE) &&
            moment(date).format('MMMM D, YYYY')}
          {show === DateTime.SHOW_BOTH && ' '}
          {date &&
            (show === DateTime.SHOW_BOTH || show === DateTime.SHOW_TIME) &&
            date.toLocaleTimeString()}
          {!date && '...'}
        </Text>
      </View>
    );
  }
}

export default DateTime;
