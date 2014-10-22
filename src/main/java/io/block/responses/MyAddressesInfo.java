package io.block.responses;

import io.block.APIResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyAddressesInfo extends APIResponse {

	@Override
	public String getMethodName( ) {
		return "get_my_addresses";
	}

	public List< Address > getAddresses( ) {

		List< Map< String, Object > > jsonAddresses = ( List< Map< String, Object > > ) data.get( "addresses" );
		List< Address > addresses = new ArrayList< Address >( );

		for ( Map< String, Object > jsonBalance: jsonAddresses ) {

			Address address = new Address( jsonBalance );
			addresses.add( address );

		}

		return addresses;

	}

	public class Address {

		Map< String, Object >	data;

		Address( Map< String, Object > jsonData ) {
			this.data = jsonData;
		}

		public Integer getUserId( ) {
			return ( new BigDecimal( data.get( "user_id" ).toString( ) ) ).intValue( );
		}

		public String getAddress( ) {
			return data.get( "address" ).toString( );
		}

		public String getLabel( ) {
			return data.get( "label" ).toString( );
		}

		public BigDecimal getAvailableBalance( ) {
			return new BigDecimal( data.get( "available_balance" ).toString( ) );
		}

		public BigDecimal getPendingReceivedBalance( ) {
			return new BigDecimal( data.get( "pending_received_balance" ).toString( ) );
		}

	}

}
