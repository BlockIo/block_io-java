package io.block.responses;

import io.block.APIResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GreenAddressInfo extends APIResponse {

	@Override
	public String getMethodName( ) {
		return "is_green_address";
	}

	public List< GreenAddress > getGreenAddresses( ) {

		List< Map< String, Object > > jsonGreenAddresses = ( List< Map< String, Object > > ) data.get( "balances" );
		List< GreenAddress > greenAddresses = new ArrayList< GreenAddress >( );

		for ( Map< String, Object > jsonGreenAddress: jsonGreenAddresses ) {

			GreenAddress greenAddress = new GreenAddress( jsonGreenAddress );
			greenAddresses.add( greenAddress );

		}

		return greenAddresses;
	}

	public class GreenAddress {

		Map< String, Object >	data;

		GreenAddress( Map< String, Object > data ) {
			this.data = data;
		}

		public String getAddress( ) {
			return data.get( "address" ).toString( );
		}

		public String getNetwork( ) {
			return data.get( "network" ).toString( );
		}

	}
}
