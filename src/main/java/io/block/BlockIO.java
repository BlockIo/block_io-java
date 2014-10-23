package io.block;

import io.block.responses.BalanceInfo;
import io.block.responses.MyAddressesInfo;
import io.block.responses.NewAddressInfo;
import io.block.responses.TransactionsInfo;

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

	protected < T extends APIResponse > T apiCall( T apiResponse, Map< String, String > parameters ) throws Exception {

		CloseableHttpClient client = HttpClients.createDefault( );
		HttpGet get = new HttpGet( "https://block.io/api/v2/" + apiResponse.getMethodName( ) + "/" );

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

		int httpStatus = response.getStatusLine( ).getStatusCode( );

		String responseText = EntityUtils.toString( response.getEntity( ) );

		if ( httpStatus == 500 ) {
			throw new Exception( "[API ERROR] HTTP Error " + httpStatus + " Message:" + responseText );
		}
		if ( httpStatus == 404 ) {
			throw new Exception( "[API ERROR] API Error " + httpStatus + " Message:" + responseText );
		}

		Gson gson = new Gson( );
		try {
			apiResponse = ( T ) gson.fromJson( responseText, apiResponse.getClass( ) );
		} catch ( Exception e ) {
			e.printStackTrace( );
			throw e;
		}

		response.close( );

		return ( T ) apiResponse;

	}

	public BalanceInfo getBalance( ) throws Exception {

		return apiCall( new BalanceInfo( ), null );

	}

	public MyAddressesInfo getMyAddresses( ) throws Exception {
		return apiCall( new MyAddressesInfo( ), null );
	}

	public NewAddressInfo getNewAddress( ) throws Exception {

		return getNewAddress( null );

	}

	public NewAddressInfo getNewAddress( Map< String, String > parameters ) throws Exception {

		return apiCall( new NewAddressInfo( ), parameters );

	}

	public TransactionsInfo getTransactions( ) throws Exception {
		return getTransactions( null );
	}

	public TransactionsInfo getTransactions( Map< String, String > parameters ) throws Exception {

		return apiCall( new TransactionsInfo( ), parameters );
	}

}
