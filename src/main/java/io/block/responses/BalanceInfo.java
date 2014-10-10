package io.block.responses;

import io.block.APIResponse;

public class BalanceInfo extends APIResponse {

	@Override
	public String getMethodName( ) {
		return "get_balance";
	}

	public Float getAvailableBalance( ) {
		return _getFloat( "available_balance" );
	}

	public Float getPendingReceivedBalance( ) {
		return _getFloat( "pending_received_balance" );
	}

}
