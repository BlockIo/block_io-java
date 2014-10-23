package io.block.responses;

import io.block.APIResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionsInfo extends APIResponse {

	@Override
	public String getMethodName( ) {
		return "get_transactions";
	}

	public List< Tx > getTxs( ) {

		List< Map< String, Object > > jsonTransactions = ( List< Map< String, Object > > ) data.get( "txs" );
		List< Tx > transactions = new ArrayList< Tx >( );

		for ( Map< String, Object > jsonTransaction: jsonTransactions ) {

			Tx tx = new Tx( jsonTransaction );
			transactions.add( tx );

		}

		return transactions;

	}

	public class Tx {

		Map< String, Object >	data;

		Tx( Map< String, Object > jsonData ) {
			this.data = jsonData;
		}

		public String getTxId( ) {
			return data.get( "txid" ).toString( );
		}

		public Boolean getFromGreenAddress( ) {
			return "true".equals( data.get( "from_green_address" ).toString( ) );
		}

		public Integer getTime( ) {
			return ( new BigDecimal( data.get( "time" ).toString( ) ) ).intValue( );
		}

		public Integer getConfirmations( ) {
			return ( new BigDecimal( data.get( "confirmations" ).toString( ) ) ).intValue( );
		}

		public List< AmountsReceived > getAmountsReceiveds( ) {

			List< Map< String, Object > > jsonAmountsReceiveds = ( List< Map< String, Object > > ) data.get( "amounts_received" );
			List< AmountsReceived > amountsReceiveds = new ArrayList< AmountsReceived >( );

			for ( Map< String, Object > jsonAmountsReceived: jsonAmountsReceiveds ) {

				AmountsReceived amountsReceived = new AmountsReceived( jsonAmountsReceived );
				amountsReceiveds.add( amountsReceived );

			}

			return amountsReceiveds;

		}

		public List< String > getSenders( ) {
			return ( List< String > ) data.get( "senders" );
		}

		public class AmountsReceived {

			Map< String, Object >	data;

			AmountsReceived( Map< String, Object > jsonData ) {
				this.data = jsonData;
			}

			public String getRecipient( ) {
				return data.get( "recipient" ).toString( );
			}

			public BigDecimal getAmount( ) {
				return new BigDecimal( data.get( "amount" ).toString( ) );
			}

		}

	}

}
