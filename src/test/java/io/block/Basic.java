package io.block;

import io.block.responses.BalanceInfo;
import io.block.responses.NewAddressInfo;

import java.util.HashMap;
import java.util.Map;

public class Basic {

	public static void main( String[] args ) throws Exception {

		BlockIO blockIO = new BlockIO( "0000-0000-0000-0001" );

		System.out.println( "Getting account balance:" );

		try {
			BalanceInfo balanceInfo = blockIO.getBalance( );

			System.out.println( "Using Network: " + balanceInfo.getNetwork( ) );
			System.out.println( "Available Amount: " + balanceInfo.getAvailableBalance( ) + " " + balanceInfo.getNetwork( ) );

		} catch ( Exception e ) {
			e.printStackTrace( );
		}

		System.out.println( "" );
		System.out.println( "" );

		try {

			Map< String, String > parameters = new HashMap< String, String >( );
			parameters.put( "label", "such_java" );
			NewAddressInfo newAddressInfo = blockIO.getNewAddress( parameters );

			System.out.println( "New Address: " + newAddressInfo.getAddress( ) );
			System.out.println( "Label: " + newAddressInfo.getLabel( ) );

		} catch ( Exception e ) {
			e.printStackTrace( );
		}

		System.out.println( "" );
		System.out.println( "" );

	}

}
