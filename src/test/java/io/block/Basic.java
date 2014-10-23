package io.block;

import io.block.responses.BalanceInfo;
import io.block.responses.MyAddressesInfo;
import io.block.responses.MyAddressesInfo.Address;
import io.block.responses.NewAddressInfo;
import io.block.responses.TransactionsInfo;
import io.block.responses.TransactionsInfo.Tx;
import io.block.responses.TransactionsInfo.Tx.AmountsReceived;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Basic {

	private static final String	JAVA_CLIENT_EXAMPLE_LABEL	= "Block_io_Java_Client";

	public static void main( String[] args ) throws Exception {

		String APIKey = "";

		if ( "".equals( APIKey ) ) {
			System.out.print( "No API Key defined. Please enter your API Key: " );
			BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
			APIKey = br.readLine( );
			br.close( );
			System.out.println( "Thanks! Using API Key: " + APIKey );

		}

		BlockIO blockIO = new BlockIO( APIKey );

		System.out.println( "Getting account balance:" );

		try {
			BalanceInfo balanceInfo = blockIO.getBalance( );

			System.out.println( "Using Network: " + balanceInfo.getNetwork( ) );
			System.out.println( "Available Amount: " + balanceInfo.getAvailableBalance( ) + " " + balanceInfo.getNetwork( ) );

			System.out.println( );

		} catch ( Exception e ) {
			e.printStackTrace( );
		}

		Boolean foundExampleLabel = false;

		MyAddressesInfo myAddresses = blockIO.getMyAddresses( );

		System.out.println( "Showing addresses for network: " + myAddresses.getNetwork( ) );

		for ( Address address: myAddresses.getAddresses( ) ) {

			System.out.println( "Label: " + address.getLabel( ) );
			System.out.println( "-- Address: " + address.getAddress( ) );
			System.out.println( "-- Available Balance: " + address.getAvailableBalance( ).floatValue( ) );
			System.out.println( "-- Pending Received: " + address.getPendingReceivedBalance( ).floatValue( ) );
			System.out.println( "-- User Id: " + address.getUserId( ) );

			if ( JAVA_CLIENT_EXAMPLE_LABEL.equals( address.getLabel( ) ) ) {
				foundExampleLabel = true;
			}

		}

		if ( !foundExampleLabel ) {

			try {

				Map< String, String > parameters = new HashMap< String, String >( );
				parameters.put( "label", JAVA_CLIENT_EXAMPLE_LABEL );
				NewAddressInfo newAddressInfo = blockIO.getNewAddress( parameters );

				System.out.println( "New Address: " + newAddressInfo.getAddress( ) );
				System.out.println( "Label: " + newAddressInfo.getLabel( ) );

			} catch ( Exception e ) {
				e.printStackTrace( );
			}

			System.out.println( "" );
			System.out.println( "" );

		}

		System.out.println( "Searching for transactions from the example label:" );

		HashMap< String, String > params = new HashMap< String, String >( );
		params.put( "type", "received" );
		params.put( "label", JAVA_CLIENT_EXAMPLE_LABEL );
		TransactionsInfo txInfo = blockIO.getTransactions( params );

		List< Tx > txs = txInfo.getTxs( );

		System.out.println( "Found " + txs.size( ) + " transactions for label: " + JAVA_CLIENT_EXAMPLE_LABEL );

		for ( Tx tx: txs ) {

			System.out.println( "Transaction: " + tx.getTxId( ) );
			System.out.println( "-- Confirmations: " + tx.getConfirmations( ) );
			System.out.println( "-- Time: " + tx.getTime( ) );
			System.out.println( "-- FromGreenAddress: " + tx.getFromGreenAddress( ) );
			System.out.println( "-- Outputs: " );
			for ( AmountsReceived a: tx.getAmountsReceiveds( ) ) {

				System.out.println( "---- " + a.getRecipient( ) + " received " + a.getAmount( ) );

			}

			System.out.println( "-- Senders: " + tx.getSenders( ) );

			System.out.println( );
		}

	}
}
