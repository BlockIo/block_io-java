package io.block.responses;

import io.block.APIResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddressBalanceInfo extends APIResponse {

	@Override
	public String getMethodName( ) {
		return "get_address_balance";
	}

	public BigDecimal getAvailableBalance( ) {
		return _getNumber( "available_balance" );
	}

	public BigDecimal getPendingReceivedBalance( ) {
		return _getNumber( "pending_received_balance" );
	}

	public List< Balance > getBalances( ) {

		List< Map< String, Object > > jsonBalances = ( List< Map< String, Object > > ) data.get( "balances" );
		List< Balance > balances = new ArrayList< Balance >( );

		for ( Map< String, Object > jsonBalance: jsonBalances ) {

			Balance balance = new Balance( jsonBalance );
			balances.add( balance );

		}

		return balances;
	}

	public class Balance {

		protected Map< String, Object >	data;

		public Balance( Map< String, Object > data ) {
			this.data = data;
		}

		public Integer getUserId( ) {
			return ( new BigInteger( this.data.get( "user_id" ).toString( ) ) ).intValue( );
		}

		public String getLabel( ) {
			return this.data.get( "label" ).toString( );
		}

		public String getAddress( ) {
			return this.data.get( "address" ).toString( );
		}

		public BigDecimal getAvailableBalance( ) {
			return new BigDecimal( data.get( "available_balance" ).toString( ) );
		}

		public BigDecimal getPendingReceivedBalance( ) {
			return new BigDecimal( data.get( "pending_received_balance" ).toString( ) );
		}
	}

}
