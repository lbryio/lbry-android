import React from 'react';
import PropTypes from 'prop-types';
import { Text, View } from 'react-native';
import { formatCredits, formatFullPrice } from 'lbry-redux';

class CreditAmount extends React.PureComponent {
  static propTypes = {
    amount: PropTypes.number.isRequired,
    precision: PropTypes.number,
    isEstimate: PropTypes.bool,
    label: PropTypes.oneOfType([PropTypes.string, PropTypes.bool]),
    showFree: PropTypes.bool,
    showFullPrice: PropTypes.bool,
    showPlus: PropTypes.bool,
    look: PropTypes.oneOf(['indicator', 'plain', 'fee']),
  };

  static defaultProps = {
    precision: 2,
    label: true,
    showFree: false,
    look: 'indicator',
    showFullPrice: false,
    showPlus: false,
  };

  render() {
    const minimumRenderableAmount = Math.pow(10, -1 * this.props.precision);
    const { amount, precision, showFullPrice, style } = this.props;

    let formattedAmount;
    const fullPrice = formatFullPrice(amount, 2);

    if (showFullPrice) {
      formattedAmount = fullPrice;
    } else {
      formattedAmount =
        amount > 0 && amount < minimumRenderableAmount
          ? `<${minimumRenderableAmount}`
          : formatCredits(amount, precision);
    }

    let amountText;
    if (this.props.showFree && parseFloat(this.props.amount) === 0) {
      amountText = 'FREE';
    } else {
      if (this.props.label) {
        const label =
          typeof this.props.label === 'string'
            ? this.props.label
            : parseFloat(amount) == 1 ? 'credit' : 'credits';

        amountText = `${formattedAmount} ${label}`;
      } else {
        amountText = formattedAmount;
      }
      if (this.props.showPlus && amount > 0) {
        amountText = `+${amountText}`;
      }
    }

    /*{this.props.isEstimate ? (
          <span
            className="credit-amount__estimate"
            title={__('This is an estimate and does not include data fees')}
          >
            *
          </span>
        ) : null}*/
    return (
      <Text style={style}>{amountText}</Text>
    );
  }
}

class FilePrice extends React.PureComponent {
  componentWillMount() {
    this.fetchCost(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.fetchCost(nextProps);
  }

  fetchCost(props) {
    const { costInfo, fetchCostInfo, uri, fetching, claim } = props;

    if (costInfo === undefined && !fetching && claim) {
      fetchCostInfo(uri);
    }
  }

  render() {
    const { costInfo, look = 'indicator', showFullPrice = false, style, textStyle } = this.props;

    const isEstimate = costInfo ? !costInfo.includesData : null;

    if (!costInfo) {
      return (
        <View style={style}>
          <Text style={textStyle}>???</Text>
        </View>
      )
    }

    return (
      <View style={style}>
        <CreditAmount
          style={textStyle}
          label={false}
          amount={costInfo.cost}
          isEstimate={isEstimate}
          showFree
          showFullPrice={showFullPrice}>???</CreditAmount>
      </View>
    );
  }
}

export default FilePrice;