package io.block.examples;

import io.block.APIResponse;
import io.block.BlockIO;

import java.util.Map.Entry;

public class Basic {

	public static void main( String[] args ) throws Exception {
		
		BlockIO blockIO = new BlockIO( "0000-0000-0000-0000" );
		
		APIResponse api = blockIO.getBalance( );
		
		System.out.println("Response Data:");
		for(  Entry< String, Object > entry : api.data.entrySet( ) ) {
			
			System.out.println( entry.getKey( ) + " = " + entry.getValue( ) );
			
		}
		
	}
	
}
