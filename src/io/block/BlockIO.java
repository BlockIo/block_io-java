package io.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public class BlockIO {

	protected String	apiKey;

	public BlockIO( String apiKey ) {
		this.apiKey = apiKey;
	}

	protected APIResponse apiCall( String method, Map< String, String > parameters ) throws Exception {

		CloseableHttpClient client = HttpClients.createDefault( );
		HttpGet get = new HttpGet( "https://block.io/api/v1/" + method + "/" );

		if ( parameters == null ) {
			parameters = new HashMap< String, String >( );
		}

		parameters.put( "api_key", this.apiKey );

		URIBuilder uri = new URIBuilder( get.getURI( ) );

		for ( Entry< String, String > entry: parameters.entrySet( ) ) {
			uri.addParameter( entry.getKey( ), entry.getValue( ) );
		}

		get.setURI( uri.build( ) );

		CloseableHttpResponse response = client.execute( get );

		Gson gson = new Gson( );
		String jsonText = EntityUtils.toString( response.getEntity( ) );
		APIResponse apiResponse = gson.fromJson( jsonText, APIResponse.class );

		response.close( );

		if ( apiResponse.status.equals( "success" ) ) {

			if ( apiResponse.data.containsKey( "user_id" ) ) {
				// Convert user_id from double to integer
				apiResponse.data.put( "user_id", ( ( Double ) apiResponse.data.get( "user_id" ) ).intValue( ) );
			}

		} else {

			throw new Exception( "Block.io API Error: " + apiResponse.data.get( "error_message" ) );

		}

		return apiResponse;

	}

	public APIResponse getNewAddress( String label ) throws Exception {

		Map< String, String > params = new HashMap< String, String >( );
		if ( label != null ) {
			params.put( "label", label );
		}

		return apiCall( "get_new_address", params );

	}

	public APIResponse getBalance( ) throws Exception {

		return apiCall( "get_balance", null );

	}

	public APIResponse getAddressReceived( Map< String, String > addressOrLabel ) throws Exception {

		Map< String, String > params = new HashMap< String, String >( );
		if ( addressOrLabel.containsKey( "address" ) ) {
			params.put( "address", addressOrLabel.get( "address" ) );
		} else if ( addressOrLabel.containsKey( "label" ) ) {
			params.put( "label", addressOrLabel.get( "label" ) );
		} else {
			throw new Exception( "Missing value for key: address or label" );
		}

		return apiCall( "get_address_received", params );

	}

}
