package io.block.responses;

import java.math.BigDecimal;

public class AddressByLabelInfo extends UserAPIResponse {

	@Override
	public String getMethodName( ) {
		return "get_address_by_label";
	}

	public String getAddress( ) {
		return _getString( "address" );
	}

	public String getLabel( ) {
		return _getString( "label" );
	}

	public BigDecimal getAvailableBalance( ) {
		return _getNumber( "available_balance" );
	}

	public BigDecimal getPendingReceivedBalance( ) {
		return _getNumber( "pending_received_balance" );
	}
}
