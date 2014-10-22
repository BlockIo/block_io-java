package io.block.responses;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurrentPriceInfo extends io.block.APIResponse {

	@Override
	public String getMethodName( ) {
		return "get_current_price";
	}

	public List< Price > getPrices( ) {

		List< Map< String, Object > > jsonPrices = ( List< Map< String, Object > > ) data.get( "balances" );
		List< Price > prices = new ArrayList< Price >( );

		for ( Map< String, Object > jsonPrice: jsonPrices ) {

			Price price = new Price( jsonPrice );
			prices.add( price );

		}

		return prices;
	}

	public class Price {

		Map< String, Object >	data;

		Price( Map< String, Object > data ) {
			this.data = data;
		}

		public BigInteger getPrice( ) {
			return new BigInteger( data.get( "price" ).toString( ) );
		}

		public String getPriceBase( ) {
			return data.get( "price_base" ).toString( );
		}

		public String getExchange( ) {
			return data.get( "exchange" ).toString( );
		}

		public Integer getTime( ) {
			return new Integer( data.get( "time" ).toString( ) );
		}

	}
}
