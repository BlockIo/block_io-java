package io.block.responses;

import java.math.BigDecimal;

import io.block.APIResponse;

public class BalanceInfo extends APIResponse {

	@Override
	public String getMethodName( ) {
		return "get_balance";
	}

	public BigDecimal getAvailableBalance( ) {
		return _getNumber( "available_balance" );
	}

	public BigDecimal getPendingReceivedBalance( ) {
		return _getNumber( "pending_received_balance" );
	}

}
