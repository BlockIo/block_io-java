package io.block.responses;

public class NewAddressInfo extends UserAPIResponse {

	@Override
	public String getMethodName( ) {
		return "get_new_address";
	}

	public String getAddress( ) {
		return _getString( "address" );
	}

	public String getLabel( ) {
		return _getString( "label" );
	}

}
