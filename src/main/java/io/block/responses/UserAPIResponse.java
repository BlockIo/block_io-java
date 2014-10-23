package io.block.responses;

import io.block.APIResponse;

public abstract class UserAPIResponse extends APIResponse {

	public Integer getUserId( ) {
		return _getInteger( "user_id" );
	}

}
