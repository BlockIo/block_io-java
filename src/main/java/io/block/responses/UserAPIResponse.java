package io.block.responses;

import io.block.APIResponse;

import java.math.BigDecimal;

public abstract class UserAPIResponse extends APIResponse {

	public BigDecimal getUserId( ) {
		return _getNumber( "user_id" );
	}

}
