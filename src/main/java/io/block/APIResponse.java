package io.block;

import java.util.Map;

public abstract class APIResponse {

	protected String				status;
	protected Map< String, Object >	data;

	protected String _getString( String name ) {
		return data.get( name ).toString( );
	}

	protected Float _getFloat( String name ) {
		return new Float( data.get( name ).toString( ) );
	}

	protected Integer _getInteger( String name ) {
		return new Integer( data.get( name ).toString( ) );
	}

	public abstract String getMethodName( );

	public String getStatus( ) {
		return status;
	}

	public String getNetwork( ) {
		return _getString( "network" );
	}

}
