package io.block.responses;

import io.block.APIResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GreenTransactionInfo extends APIResponse {

	@Override
	public String getMethodName( ) {
		return "is_green_transaction";
	}

	public List< GreenTransaction > getGreenTransactions( ) {

		List< Map< String, Object > > jsonGreenTransactions = ( List< Map< String, Object > > ) data.get( "balances" );
		List< GreenTransaction > greenTransactions = new ArrayList< GreenTransaction >( );

		for ( Map< String, Object > jsonGreenTransaction: jsonGreenTransactions ) {

			GreenTransaction greenAddress = new GreenTransaction( jsonGreenTransaction );
			greenTransactions.add( greenAddress );

		}

		return greenTransactions;
	}

	public class GreenTransaction {

		Map< String, Object >	data;

		GreenTransaction( Map< String, Object > data ) {
			this.data = data;
		}

		public String getTxId( ) {
			return data.get( "txid" ).toString( );
		}

		public String getNetwork( ) {
			return data.get( "network" ).toString( );
		}

	}
}
