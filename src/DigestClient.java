import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DigestClient {

    public static final String LOGIN_BASE_URI = "http://data.crea.ca";
	public static final String LOGIN_SUB_URI  = "/Login.svc/Login";
	public static final String METADATA_URI   = "http://data.crea.ca/Metadata.svc/GetMetadata";
	public static final String SEARCH_URI     = "http://data.crea.ca/Search.svc/Search";

	public static String sessionId 			  = "";
	public static String authorizationHeader  = "";
	
	public static final String USER 		  = "CEzDgz3HUKR7ZRaspngkAOqV"; 
	public static final String PASSWORD		  = "qBryouTAAdEfWVHsO8rCCXU2"; 
	
	public static void main( String[] args ) throws JSONException, IOException, org.json.JSONException {

		JSONObject obj = new JSONObject();
		//insertData( obj );
		login();
		//getMetadata();
		searchTransaction();
	}

	@SuppressWarnings("deprecation")
	public static void getMetadata() throws JSONException, org.json.JSONException {

		System.out.println( "---------------------------------------------------------------" );
		System.out.println( "" );
		System.out.println( "GET METADATA: " + METADATA_URI );
		System.out.println( "---------------------------------------------------------------" );
		
		System.out.println( "SessionID: " + sessionId.split( ";" )[ 0 ] );

		ClientConfig cc = new DefaultClientConfig();
		Client client = Client.create(cc);

		WebResource webResource = client.resource( METADATA_URI );	
		
		String sid = sessionId.split( ";" )[ 0 ];
		
		ClientResponse response = webResource.get( ClientResponse.class );
		
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		queryParams.add( "Type",   URLEncoder.encode( "METADATA-TABLE" ) );
		queryParams.add( "Format", URLEncoder.encode( "COMPACT" ) );
		queryParams.add( "ID",     URLEncoder.encode( "0" ) );
		
		response = webResource.queryParams( queryParams )
							  .header( "Authorization", authorizationHeader )
							  .header( "Cookie", sid )
							  .type( MediaType.APPLICATION_XML )
							  .accept( "*" )
							  .get( ClientResponse.class );
		
		System.out.println( "Metadata Response Code: " + response.getStatus() );
		
		if ( response.getStatus() != 200 ) {
            
			System.out.println( "MetaDataError: " + response.getStatus() );
        
		}
		
		String output = response.getEntity( String.class );

		System.out.println( output );
		System.out.println( "Data Generated Successfully!" );
	    
	}
	
	@SuppressWarnings("deprecation")
	public static void searchTransaction() throws JSONException, org.json.JSONException, FileNotFoundException {

		System.out.println( "---------------------------------------------------------------" );
		System.out.println( "" );
		System.out.println( "SEARCH TRANSACTION: " + SEARCH_URI );
		System.out.println( "---------------------------------------------------------------" );
		
		System.out.println( "SessionID: " + sessionId.split( ";" )[ 0 ] );

		ClientConfig cc = new DefaultClientConfig();
		Client client = Client.create(cc);

		WebResource webResource = client.resource( SEARCH_URI );	
		
		String sid = sessionId.split( ";" )[ 0 ];
		
		ClientResponse response = webResource.get( ClientResponse.class );
		
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		queryParams.add( "Format", 	     URLEncoder.encode( "STANDARD-XML" ) );
		queryParams.add( "SearchType",   URLEncoder.encode( "Property" ) );
		queryParams.add( "Class",  		 URLEncoder.encode( "Property" ) );
		queryParams.add( "QueryType",  	 URLEncoder.encode( "DMQL2" ) );
		queryParams.add( "Query",  		 URLEncoder.encode( "(LastUpdated=2021-04-08T22:00:17Z)" ) );
		//queryParams.add( "Select",  		 URLEncoder.encode( "SELECT=(ID)" ) );
		//queryParams.add( "Count",  		 1 );
		//queryParams.add( "Limit",  		 URLEncoder.encode( "None" ) );
		//queryParams.add( "Offset",  		 URLEncoder.encode( "1" ) );
		queryParams.add( "Culture",  		 URLEncoder.encode( "en-CA" ) );
		
		response = webResource.queryParams( queryParams )
							  .header( "Authorization", authorizationHeader )
							  .header( "Cookie", sid )
							  .type( MediaType.APPLICATION_XML )
							  .accept( "*" )
							  .get( ClientResponse.class );
		
		System.out.println( "Search Transaction Response Code: " + response.getStatus() );
		
		if ( response.getStatus() != 200 ) {
            
			System.out.println( "Search Transaction Error: " + response.getStatus() );
        
		}
		
		String output = response.getEntity( String.class );

		org.json.JSONObject xmlJSONObj = XML.toJSONObject( output.toString() );
        
		String jsonPrettyPrintString = xmlJSONObj.toString( 4 );
        
		PrintWriter writer = new PrintWriter(new File("result-json.txt"));
		
		writer.write( jsonPrettyPrintString );
		writer.close();
		
		System.out.println( "Data Generated <Search Transaction> Successfully!" );
		
		JSONObject propertyObj = getPropertyObject( xmlJSONObj );
		
	}
	
	public static JSONObject getPropertyObject ( JSONObject obj ) {
		
		JSONObject finalObj = new JSONObject();
		
		try {
			
			JSONObject RETS = new JSONObject( obj.get( "RETS" ).toString() );
			JSONObject RETSRESPONSE = new JSONObject( RETS.get( "RETS-RESPONSE" ).toString() );
			
			JSONArray propertiesArray = RETSRESPONSE.optJSONArray( "PropertyDetails" );
			
			for ( int i=0; i<propertiesArray.length(); i++ ) {
				
				JSONObject mainObj = new JSONObject( propertiesArray.get( i ).toString() );
				JSONObject buildingObj = mainObj.getJSONObject( "Building" );

				System.out.println( buildingObj.toString() );
				
			    insertData( buildingObj );
				
			}
			
		}
		catch ( Exception e ) {
			System.out.println( "Exception in getPropertyObject: " + e );
		}
		
		return finalObj;
	}
	
	public static void insertData( JSONObject obj ) throws JSONException {
		
		Connection conn = null;
	     
		Statement stmt = null;
		
		String ArchitecturalStyle = "";
		String AssociationFee = "";
		String AssociationFeeFrequency = "";  
		String AttachedGarageYN = ""; 
		String BathroomsHalf = ""; 
		String BathroomsTotal = ""; 
		String BedroomsTotal = ""; 
		String BuildingAreaTotal = ""; 
		String BuildingAreaUnits = ""; 
		String CarportSpaces = ""; 
		String CarportYN = ""; 
		String City = ""; 
		String CoListAgentCellPhone = ""; 
		String CoListAgentDesignation = ""; 
		String CoListAgentDirectPhone = ""; 
		String CoListAgentFax = ""; 
		String CoListAgentFullName = ""; 
		String CoListAgentKey = ""; 
		String CoListAgentOfficePhone = ""; 
		String CoListAgentOfficePhoneExt = ""; 
		String CoListAgentPager = ""; 
		String CoListAgentTollFreePhone = ""; 
		String CoListAgentURL = ""; 
		String CoListOfficeFax = ""; 
		String CoListOfficeKey = ""; 
		String CoListOfficeName = ""; 
		String CoListOfficePhone = ""; 
		String CoListOfficePhoneExt = ""; 
		String CoListOfficeURL = ""; 
		String CommunityFeatures = ""; 
		String ConstructionMaterials = ""; 
		String Cooling = ""; 
		String CoolingYN = ""; 
		String Country = ""; 
		String CoveredSpaces = ""; 
		String Fencing = ""; 
		String FireplaceFeatures = ""; 
		String FireplaceFuel = ""; 
		String FireplacesTotal = ""; 
		String Flooring = ""; 
		String FrontageLength = ""; 
		String FrontageType = ""; 
		String GarageSpaces = ""; 
		String GarageYN = ""; 
		String GreenBuildingCertification = ""; 
		String GreenCertificationRating = ""; 
		String Heating = ""; 
		String HeatingFuel = ""; 
		String Lease = ""; 
		String LeaseFrequency = ""; 
		String LeaseTerm = ""; 
		String Levels = ""; 
		String ListAOR = ""; 
		String ListAgentCellPhone = ""; 
		String ListAgentDesignation = ""; 
		String ListAgentFax = ""; 
		String ListAgentFullName = ""; 
		String ListAgentKey = ""; 
		String ListAgentOfficePhone = ""; 
		String ListAgentOfficePhoneExt = ""; 
		String ListAgentPager = ""; 
		String ListAgentURL = ""; 
		String ListingID = ""; 
		String ListingContractDate = ""; 
		String ListingKey = ""; 
		String ListOfficeFax = ""; 
		String ListOfficeKey = ""; 
		String ListOfficeName = ""; 
		String ListOfficePhone = ""; 
		String ListOfficePhoneExt = ""; 
		String ListOfficeURL = ""; 
		String ListPrice = ""; 
		String LotFeatures = ""; 
		String LotSizeArea = ""; 
		String LotSizeUnits = ""; 
		String ModificationTimestamp = ""; 
		String NumberOfUnitsTotal = ""; 
		String OpenParkingSpaces = ""; 
		String OpenParkingYN = ""; 
		String OriginatingSystemKey = ""; 
		String OriginatingSystemName = ""; 
		String ParkingTotal = ""; 
		String PhotosChangeTimestamp = ""; 
		String PhotosCount = ""; 
		String PoolFeatures = ""; 
		String PoolYN = ""; 
		String PostalCode = ""; 
		String PropertyType = ""; 
		String PublicRemarks = ""; 
		String Roof = ""; 
		String Sewer = ""; 
		String StateOrProvince = ""; 
		String Stories = ""; 
		String StreetAdditionalInfo = ""; 
		String StreetDirPrefix = ""; 
		String StreetDirSuffix = ""; 
		String StreetName = ""; 
		String StreetNumber = ""; 
		String StreetSuffix = ""; 
		String SubdivisionName = ""; 
		String UnitNumber = ""; 
		String UnparsedAddress = ""; 
		String Vieww = ""; 
		String ViewYN = ""; 
		String WaterBodyName = ""; 
		String ViewwWaterfrontYN = ""; 
		String YearBuilt = ""; 
		String Zoning = ""; 
		String AnalyticsView = ""; 
		String AnalyticsClick = "";
		
		if ( obj.has( "ArchitecturalStyle" ) )
			ArchitecturalStyle = obj.getString( "ArchitecturalStyle" );  
		
		if ( obj.has( "AssociationFeeFrequency" ) )
			AssociationFeeFrequency = obj.getString( "AssociationFeeFrequency" );  
		
		if ( obj.has( "AttachedGarageYN" ) )
			AttachedGarageYN = obj.getString( "AttachedGarageYN" );  
		
		if ( obj.has( "BathroomsHalf" ) )
			BathroomsHalf = obj.getString( "BathroomsHalf" );  
		
		if ( obj.has( "BathroomsTotal" ) )
			BathroomsTotal = obj.getString( "BathroomsTotal" );  
		
		if ( obj.has( "BedroomsTotal" ) )
			BedroomsTotal = obj.getString( "BedroomsTotal" );  
		
		if ( obj.has( "BuildingAreaUnits" ) )
			BuildingAreaUnits = obj.getString( "BuildingAreaUnits" );  
	  
		if ( obj.has( "CarportSpaces" ) )
			CarportSpaces = obj.getString( "CarportSpaces" );  
		
		if ( obj.has( "CarportYN" ) )
			CarportYN = obj.getString( "CarportYN" );  
		
		if ( obj.has( "City" ) )
			City = obj.getString( "City" ); 
		
		if ( obj.has( "CoListAgentCellPhone" ) )
			CoListAgentCellPhone = obj.getString( "CoListAgentCellPhone" );  
		
		if ( obj.has( "CoListAgentDesignation" ) )
			CoListAgentDesignation = obj.getString( "CoListAgentDesignation" );  
		
		if ( obj.has( "CoListAgentDirectPhone" ) )
			CoListAgentDirectPhone = obj.getString( "CoListAgentDirectPhone" );  
		
		if ( obj.has( "CoListAgentFax" ) )
			CoListAgentFax = obj.getString( "CoListAgentFax" );  
		
		if ( obj.has( "CoListAgentFullName" ) )
			CoListAgentFullName = obj.getString( "CoListAgentFullName" );  
		
		if ( obj.has( "CoListAgentKey" ) )
			CoListAgentKey = obj.getString( "CoListAgentKey" );  
		 
		if ( obj.has( "CoListAgentOfficePhone" ) )
			CoListAgentOfficePhone = obj.getString( "CoListAgentOfficePhone" );  
		
		if ( obj.has( "CoListAgentOfficePhoneExt" ) )
			CoListAgentOfficePhoneExt = obj.getString( "CoListAgentOfficePhoneExt" );  
		
		if ( obj.has( "CoListAgentPager" ) )
			CoListAgentPager = obj.getString( "CoListAgentPager" );  
		
		if ( obj.has( "CoListAgentTollFreePhone" ) )
			CoListAgentTollFreePhone = obj.getString( "CoListAgentTollFreePhone" );  
		
		if ( obj.has( "CoListAgentURL" ) )
			CoListAgentURL = obj.getString( "CoListAgentURL" );  
		
		if ( obj.has( "CoListOfficeFax" ) )
			CoListOfficeFax = obj.getString( "CoListOfficeFax" );  
		
		if ( obj.has( "CoListOfficeKey" ) )
			CoListOfficeKey = obj.getString( "CoListOfficeKey" );  
		
		if ( obj.has( "CoListOfficeName" ) )
			CoListOfficeName = obj.getString( "CoListOfficeName" ); 
		
		if ( obj.has( "CoListOfficePhone" ) )
			CoListOfficePhone = obj.getString( "CoListOfficePhone" );  
		
		if ( obj.has( "CoListOfficePhoneExt" ) )
			CoListOfficePhoneExt = obj.getString( "CoListOfficePhoneExt" );  
		
		if ( obj.has( "CoListOfficeURL" ) )
			CoListOfficeURL = obj.getString( "CoListOfficeURL" );  
				
		if ( obj.has( "CommunityFeatures" ) )
			CommunityFeatures = obj.getString( "CommunityFeatures" );  
		
		if ( obj.has( "ConstructionMaterials" ) )
			ConstructionMaterials = obj.getString( "ConstructionMaterials" );  
		
		if ( obj.has( "Cooling" ) )
			Cooling = obj.getString( "Cooling" );  
		
		if ( obj.has( "CoolingYN" ) )
			CoolingYN = obj.getString( "CoolingYN" );  
		
		if ( obj.has( "Country" ) )
			Country = obj.getString( "Country" );  
		
		if ( obj.has( "CoveredSpaces" ) )
			CoveredSpaces = obj.getString( "CoveredSpaces" );  
		
		if ( obj.has( "Fencing" ) )
			Fencing = obj.getString( "Fencing" );  
		
		if ( obj.has( "FireplaceFeatures" ) )
			FireplaceFeatures = obj.getString( "FireplaceFeatures" );  
		
		if ( obj.has( "FireplaceFuel" ) )
			FireplaceFuel = obj.getString( "FireplaceFuel" );  
		
		if ( obj.has( "FireplacesTotal" ) )
			FireplacesTotal = obj.getString( "FireplacesTotal" ); 
		
		if ( obj.has( "Flooring" ) )
			Flooring = obj.getString( "Flooring" );  
		
		if ( obj.has( "FrontageLength" ) )
			FrontageLength = obj.getString( "FrontageLength" );  
		
		if ( obj.has( "FrontageType" ) )
			FrontageType = obj.getString( "FrontageType" );  
				
		if ( obj.has( "GarageSpaces" ) )
			GarageSpaces = obj.getString( "GarageSpaces" );  
	
		if ( obj.has( "GarageYN" ) )
			GarageYN = obj.getString( "GarageYN" );  
		
		if ( obj.has( "GreenBuildingCertification" ) )
			GreenBuildingCertification = obj.getString( "GreenBuildingCertification" );  
		
		if ( obj.has( "GreenCertificationRating" ) )
			GreenCertificationRating = obj.getString( "GreenCertificationRating" );  
		
		if ( obj.has( "Heating" ) )
			Heating = obj.getString( "Heating" );  
		
		if ( obj.has( "HeatingFuel" ) )
			HeatingFuel = obj.getString( "HeatingFuel" );  
		
		if ( obj.has( "Lease" ) )
			Lease = obj.getString( "Lease" );  
		
		if ( obj.has( "LeaseFrequency" ) )
			LeaseFrequency = obj.getString( "LeaseFrequency" );  
		
		if ( obj.has( "LeaseTerm" ) )
			LeaseTerm = obj.getString( "LeaseTerm" );  
		
		if ( obj.has( "Levels" ) )
			Levels = obj.getString( "Levels" );  
		
		if ( obj.has( "ListAOR" ) )
			ListAOR = obj.getString( "ListAOR" );  

		if ( obj.has( "ListAgentCellPhone" ) )
			ListAgentCellPhone = obj.getString( "ListAgentCellPhone" );  

		if ( obj.has( "ListAgentDesignation" ) )
			ListAgentDesignation = obj.getString( "ListAgentDesignation" );  

		if ( obj.has( "ListAgentFax" ) )
			ListAgentFax = obj.getString( "ListAgentFax" );  

		if ( obj.has( "ListAgentFullName" ) )
			ListAgentFullName = obj.getString( "ListAgentFullName" );  

		if ( obj.has( "ListAgentKey" ) )
			ListAgentKey = obj.getString( "ListAgentKey" );  

		if ( obj.has( "ListAgentOfficePhone" ) )
			ListAgentOfficePhone = obj.getString( "ListAgentOfficePhone" );  

		if ( obj.has( "ListAgentOfficePhoneExt" ) )
			ListAgentOfficePhoneExt = obj.getString( "ListAgentOfficePhoneExt" );  

		if ( obj.has( "ListAgentPager" ) )
			ListAgentPager = obj.getString( "ListAgentPager" );  

		if ( obj.has( "ListAgentURL" ) )
			ListAgentURL = obj.getString( "ListAgentURL" );  

		if ( obj.has( "ListingID" ) )
			ListingID = obj.getString( "ListingID" );  

		if ( obj.has( "ListingContractDate" ) )
			ListingContractDate = obj.getString( "ListingContractDate" );  
		
		if ( obj.has( "ListingKey" ) )
			ListingKey = obj.getString( "ListingKey" );  

		if ( obj.has( "ListOfficeFax" ) )
			ListOfficeFax = obj.getString( "ListOfficeFax" );  

		if ( obj.has( "ListOfficeKey" ) )
			ListOfficeKey = obj.getString( "ListOfficeKey" );  

		if ( obj.has( "ListOfficeName" ) )
			ListOfficeName = obj.getString( "ListOfficeName" );  

		if ( obj.has( "ListOfficePhone" ) )
			ListOfficePhone = obj.getString( "ListOfficePhone" );  

		if ( obj.has( "ListOfficePhoneExt" ) )
			ListOfficePhoneExt = obj.getString( "ListOfficePhoneExt" );  

		if ( obj.has( "ListOfficeURL" ) )
			ListOfficeURL = obj.getString( "ListOfficeURL" );  

		if ( obj.has( "ListPrice" ) )
			ListPrice = obj.getString( "ListPrice" );  

		if ( obj.has( "LotFeatures" ) )
			LotFeatures = obj.getString( "LotFeatures" );  

		if ( obj.has( "LotSizeArea" ) )
			LotSizeArea = obj.getString( "LotSizeArea" );  

		if ( obj.has( "LotSizeUnits" ) )
			LotSizeUnits = obj.getString( "LotSizeUnits" );   
		
		if ( obj.has( "ModificationTimestamp" ) )
			ModificationTimestamp = obj.getString( "ModificationTimestamp" );  

		if ( obj.has( "NumberOfUnitsTotal" ) )
			NumberOfUnitsTotal = obj.getString( "NumberOfUnitsTotal" );  

		if ( obj.has( "OpenParkingSpaces" ) )
			OpenParkingSpaces = obj.getString( "OpenParkingSpaces" );  

		if ( obj.has( "OpenParkingYN" ) )
			OpenParkingYN = obj.getString( "OpenParkingYN" );  

		if ( obj.has( "OriginatingSystemKey" ) )
			OriginatingSystemKey = obj.getString( "OriginatingSystemKey" );  

		if ( obj.has( "OriginatingSystemName" ) )
			OriginatingSystemName = obj.getString( "OriginatingSystemName" );  

		if ( obj.has( "ParkingTotal" ) )
			ParkingTotal = obj.getString( "ParkingTotal" );  

		if ( obj.has( "PhotosChangeTimestamp" ) )
			PhotosChangeTimestamp = obj.getString( "PhotosChangeTimestamp" );  

		if ( obj.has( "PhotosCount" ) )
			PhotosCount = obj.getString( "PhotosCount" );  
		
		if ( obj.has( "PoolFeatures" ) )
			PoolFeatures = obj.getString( "PoolFeatures" );  

		if ( obj.has( "PoolYN" ) )
			PoolYN = obj.getString( "PoolYN" );  

		if ( obj.has( "PostalCode" ) )
			PostalCode = obj.getString( "PostalCode" );  

		if ( obj.has( "PropertyType" ) )
			PropertyType = obj.getString( "PropertyType" );  

		if ( obj.has( "PublicRemarks" ) )
			PublicRemarks = obj.getString( "PublicRemarks" );  

		if ( obj.has( "Roof" ) )
			Roof = obj.getString( "Roof" );  

		if ( obj.has( "Sewer" ) )
			Sewer = obj.getString( "Sewer" );  

		if ( obj.has( "StateOrProvince" ) )
			StateOrProvince = obj.getString( "StateOrProvince" );  

		if ( obj.has( "StreetAdditionalInfo" ) )
			StreetAdditionalInfo = obj.getString( "StreetAdditionalInfo" );  

		if ( obj.has( "StreetDirPrefix" ) )
			StreetDirPrefix = obj.getString( "StreetDirPrefix" );  

		if ( obj.has( "StreetDirSuffix" ) )
			StreetDirSuffix = obj.getString( "StreetDirSuffix" );  

		if ( obj.has( "StreetName" ) )
			StreetName = obj.getString( "StreetName" );  

		if ( obj.has( "StreetNumber" ) )
			StreetNumber = obj.getString( "StreetNumber" );  

		if ( obj.has( "StreetSuffix" ) )
			StreetSuffix = obj.getString( "StreetSuffix" );  

		if ( obj.has( "SubdivisionName" ) )
			SubdivisionName = obj.getString( "SubdivisionName" );  

		if ( obj.has( "UnitNumber" ) )
			UnitNumber = obj.getString( "UnitNumber" );  
		
		if ( obj.has( "View" ) )
			Vieww = obj.getString( "View" );  

		if ( obj.has( "ViewYN" ) )
			ViewYN = obj.getString( "ViewYN" );  

		if ( obj.has( "WaterBodyName" ) )
			WaterBodyName = obj.getString( "WaterBodyName" );  

		if ( obj.has( "ViewwWaterfrontYN" ) )
			ViewwWaterfrontYN = obj.getString( "ViewwWaterfrontYN" );  

		if ( obj.has( "YearBuilt" ) )
			YearBuilt = obj.getString( "YearBuilt" );  

		if ( obj.has( "Zoning" ) )
			Zoning = obj.getString( "Zoning" );  

		if ( obj.has( "AnalyticsView" ) )
			AnalyticsView = obj.getString( "AnalyticsView" );  

		if ( obj.has( "AnalyticsClick" ) )
			AnalyticsClick = obj.getString( "AnalyticsClick" );  
		
 		try {
	    
			try {
	        
				Class.forName( "com.mysql.jdbc.Driver" );
	         
			} 
			catch (Exception e) {
	        
				System.out.println(e);
	      
			}
	      
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://104.155.177.189:3306/crea?cloudSqlInstance=crea-database&socketFactory=com.google.cloud.sql.mysql.SocketFactory&ipTypes=PUBLIC&user=root&password=");
	     
			System.out.println("Connection is created successfully:");
	     
			stmt = (Statement) conn.createStatement();
	      
			String query1 = "INSERT INTO crea ( ArchitecturalStyle ," + 
					"					AssociationFee ," + 
					"                    AssociationFeeFrequency ," + 
					"                    AttachedGarageYN ," + 
					"                    BathroomsHalf ," + 
					"                    BathroomsTotal ," + 
					"                    BedroomsTotal ," + 
					"                    BuildingAreaTotal ," + 
					"                    BuildingAreaUnits ," + 
					"                    CarportSpaces ," + 
					"                    CarportYN ," + 
					"                    City ," + 
					"                    CoListAgentCellPhone ," + 
					"                    CoListAgentDesignation ," + 
					"                    CoListAgentDirectPhone ," + 
					"                    CoListAgentFax ," + 
					"                    CoListAgentFullName ," + 
					"                    CoListAgentKey ," + 
					"                    CoListAgentOfficePhone ," + 
					"                    CoListAgentOfficePhoneExt ," + 
					"                    CoListAgentPager ," + 
					"                    CoListAgentTollFreePhone ," + 
					"                    CoListAgentURL ," + 
					"                    CoListOfficeFax ," + 
					"                    CoListOfficeKey ," + 
					"                    CoListOfficeName ," + 
					"                    CoListOfficePhone ," + 
					"                    CoListOfficePhoneExt ," + 
					"                    CoListOfficeURL ," + 
					"                    CommunityFeatures ," + 
					"                    ConstructionMaterials ," + 
					"                    Cooling ," + 
					"                    CoolingYN ," + 
					"                    Country ," + 
					"                    CoveredSpaces ," + 
					"                    Fencing ," + 
					"                    FireplaceFeatures ," + 
					"                    FireplaceFuel ," + 
					"                    FireplacesTotal ," + 
					"                    Flooring ," + 
					"                    FrontageLength ," + 
					"                    FrontageType ," + 
					"                    GarageSpaces ," + 
					"                    GarageYN ," + 
					"                    GreenBuildingCertification ," + 
					"                    GreenCertificationRating ," + 
					"                    Heating ," + 
					"                    HeatingFuel ," + 
					"                    Lease ," + 
					"                    LeaseFrequency ," + 
					"                    LeaseTerm , " + 
					"                    Levels ," + 
					"                    ListAOR ," + 
					"                    ListAgentCellPhone ," + 
					"                    ListAgentDesignation ," + 
					"                    ListAgentFax ," + 
					"                    ListAgentFullName ," + 
					"                    ListAgentKey ," + 
					"                    ListAgentOfficePhone , " + 
					"                    ListAgentOfficePhoneExt ," + 
					"                    ListAgentPager ," + 
					"                    ListAgentURL ," + 
					"                    ListingID ," + 
					"                    ListingContractDate ," + 
					"                    ListingKey ," + 
					"                    ListOfficeFax ," + 
					"                    ListOfficeKey ," + 
					"                    ListOfficeName ," + 
					"                    ListOfficePhone ," + 
					"                    ListOfficePhoneExt ," + 
					"                    ListOfficeURL ," + 
					"                    ListPrice ," + 
					"                    LotFeatures ," + 
					"                    LotSizeArea ," + 
					"                    LotSizeUnits ," + 
					"                    ModificationTimestamp ," + 
					"                    NumberOfUnitsTotal ," + 
					"                    OpenParkingSpaces ," + 
					"                    OpenParkingYN ," + 
					"                    OriginatingSystemKey ," + 
					"                    OriginatingSystemName ," + 
					"                    ParkingTotal ," + 
					"                    PhotosChangeTimestamp ," + 
					"                    PhotosCount ," + 
					"                    PoolFeatures ," + 
					"                    PoolYN ," + 
					"                    PostalCode ," + 
					"                    PropertyType ," + 
					"                    PublicRemarks ," + 
					"                    Roof ," + 
					"                    Sewer ," + 
					"                    StateOrProvince ," + 
					"                    Stories ," + 
					"                    StreetAdditionalInfo ," + 
					"                    StreetDirPrefix ," + 
					"                    StreetDirSuffix ," + 
					"                    StreetName ," + 
					"                    StreetNumber ," + 
					"                    StreetSuffix ," + 
					"                    SubdivisionName ," + 
					"                    UnitNumber ," + 
					"                    UnparsedAddress ," + 
					"                    Vieww ," + 
					"                    ViewYN ," + 
					"                    WaterBodyName ," + 
					"                    ViewwWaterfrontYN ," + 
					"                    YearBuilt ," + 
					"                    Zoning ," + 
					"                    AnalyticsView ," + 
					"                    AnalyticsClick  ) VALUES "
					+ "("
					
					+ "'" + ArchitecturalStyle + "', "
					+ "'" + AssociationFee + "', "
					+ "'" + AssociationFeeFrequency + "', "
					+ "'" + AttachedGarageYN + "', "
					+ "'" + BathroomsHalf + "', "
					+ "'" + BathroomsTotal + "', "
					+ "'" + BedroomsTotal + "', "
					+ "'" + BuildingAreaTotal + "', "
					+ "'" + BuildingAreaUnits + "', "
					+ "'" + CarportSpaces + "', "
					+ "'" + CarportYN + "', "
					+ "'" + City + "', "
					+ "'" + CoListAgentCellPhone + "', "
					+ "'" + CoListAgentDesignation + "', "
					+ "'" + CoListAgentDirectPhone + "', "
					+ "'" + CoListAgentFax + "', "
					+ "'" + CoListAgentFullName + "', "
					+ "'" + CoListAgentKey + "', "
					+ "'" + CoListAgentOfficePhone + "', "
					+ "'" + CoListAgentOfficePhoneExt + "', "
					+ "'" + CoListAgentPager + "', "
					+ "'" + CoListAgentTollFreePhone + "', "
					+ "'" + CoListAgentURL + "', "
					+ "'" + CoListOfficeFax + "', "
					+ "'" + CoListOfficeKey + "', "
					+ "'" + CoListOfficeName + "', "
					+ "'" + CoListOfficePhone + "', "
					+ "'" + CoListOfficePhoneExt + "', "
					+ "'" + CoListOfficeURL + "', "
					+ "'" + CommunityFeatures + "', "
					+ "'" + ConstructionMaterials + "', "
					+ "'" + Cooling + "', "
					+ "'" + CoolingYN + "', "
					+ "'" + Country + "', "
					+ "'" + CoveredSpaces + "', "
					+ "'" + Fencing + "', "
					+ "'" + FireplaceFeatures + "', "
					+ "'" + FireplaceFuel + "', "
					+ "'" + FireplacesTotal + "', "
					+ "'" + Flooring + "', "
					+ "'" + FrontageLength + "', "
					+ "'" + FrontageType + "', "
					+ "'" + GarageSpaces + "', "
					+ "'" + GarageYN + "', "
					+ "'" + GreenBuildingCertification + "', "
					+ "'" + GreenCertificationRating + "', "
					+ "'" + Heating + "', "
					+ "'" + HeatingFuel + "', "
					+ "'" + Lease + "', "
					+ "'" + LeaseFrequency + "', "
					+ "'" + LeaseTerm + "', "
					+ "'" + Levels + "', "
					+ "'" + ListAOR + "', "
					+ "'" + ListAgentCellPhone + "', "
					+ "'" + ListAgentDesignation + "', "
					+ "'" + ListAgentFax + "', "
					+ "'" + ListAgentFullName + "', "
					+ "'" + ListAgentKey + "', "
					+ "'" + ListAgentOfficePhone + "', "
					+ "'" + ListAgentOfficePhoneExt + "', "
					+ "'" + ListAgentPager + "', "
					+ "'" + ListAgentURL + "', "
					+ "'" + ListingID + "', "
					+ "'" + ListingContractDate + "', "
					+ "'" + ListingKey + "', "
					+ "'" + ListOfficeFax + "', "
					+ "'" + ListOfficeKey + "', "
					+ "'" + ListOfficeName + "', "
					+ "'" + ListOfficePhone + "', "
					+ "'" + ListOfficePhoneExt + "', "
					+ "'" + ListOfficeURL + "', "
					+ "'" + ListPrice + "', "
					+ "'" + LotFeatures + "', "
					+ "'" + LotSizeArea + "', "
					+ "'" + LotSizeUnits + "', "
					+ "'" + ModificationTimestamp + "', "
					+ "'" + NumberOfUnitsTotal + "', "
					+ "'" + OpenParkingSpaces + "', "
					+ "'" + OpenParkingYN + "', "
					+ "'" + OriginatingSystemKey + "', "
					+ "'" + OriginatingSystemName + "', "
					+ "'" + ParkingTotal + "', "
					+ "'" + PhotosChangeTimestamp + "', "
					+ "'" + PhotosCount + "', "
					+ "'" + PoolFeatures + "', "
					+ "'" + PoolYN + "', "
					+ "'" + PostalCode + "', "
					+ "'" + PropertyType + "', "
					+ "'" + PublicRemarks + "', "
					+ "'" + Roof + "', "
					+ "'" + Sewer + "', "
					+ "'" + StateOrProvince + "', "
					+ "'" + Stories + "', "
					+ "'" + StreetAdditionalInfo + "', "
					+ "'" + StreetDirPrefix + "', "
					+ "'" + StreetDirSuffix + "', "
					+ "'" + StreetName + "', "
					+ "'" + StreetNumber + "', "
					+ "'" + StreetSuffix + "', "
					+ "'" + SubdivisionName + "', "
					+ "'" + UnitNumber + "', "
					+ "'" + UnparsedAddress + "', "
					+ "'" + Vieww + "', "
					+ "'" + ViewYN + "', "
					+ "'" + WaterBodyName + "', "
					+ "'" + ViewwWaterfrontYN + "', "
					+ "'" + YearBuilt + "', "
					+ "'" + Zoning + "', "
					+ "'" + AnalyticsView + "', "
					+ "'" + AnalyticsClick + "'"
					
					+ ")";
	      	      
			stmt.executeUpdate(query1);
	    
			System.out.println("Record is inserted in the table successfully..................");
	      } 
		
		catch (SQLException excep) {
	    
			excep.printStackTrace();
	      
		} 
		catch (Exception excep) {
	    
			excep.printStackTrace();
	      
		} 
		finally {
	    
			try {
	        
				if (stmt != null)
	            
					conn.close();
	         } 
			catch (SQLException se) {}
	        
			try {
	        
				if (conn != null)
	            
					conn.close();
	         } 
			catch (SQLException se) {
	        
				se.printStackTrace();
	      
			}  
	      
		}
	    
		System.out.println("Please check it in the MySQL Table......... ……..");
	   
	}
	
	public static void login() throws JSONException {

		System.out.println( "LOGIN TO: " + LOGIN_BASE_URI + LOGIN_SUB_URI );
		System.out.println( "---------------------------------------------------------------" );
		
		ClientConfig cc = new DefaultClientConfig();
		Client client = Client.create(cc);

		WebResource webResource = client.resource( LOGIN_BASE_URI + LOGIN_SUB_URI );
		ClientResponse response = webResource.get( ClientResponse.class );

		System.out.println( "Invoking First Call..." );
		System.out.println( "First Call Status: " + response.getStatus() );

		String noAuthResp = response.getHeaders().get( "www-Authenticate" ).toString();

		noAuthResp = noAuthResp.replace("Digest ", "");
		noAuthResp = noAuthResp.replace('[', '{');
		noAuthResp = noAuthResp.replace(']', '}');

		JSONObject resp = new JSONObject( noAuthResp );
		
		System.out.println( "" );
		System.out.println( "Invoking Second Call..." );
		
		String realm        = resp.getString( "realm" );
		String qop          = resp.getString("qop"); 
		String nonce        = resp.getString("nonce"); 
		String opaque       = ""; 
		int nonceCount      = 678; 
		String clientNonce  = "afdjas0"; 

		String method       = "GET"; // HTTP method 

		String ha1          = new DigestClient().formHA1( USER, realm, PASSWORD );
		String ha2 		    = new DigestClient().formHA2( method, LOGIN_SUB_URI );
		String responseCode = new DigestClient().generateResponse( ha1, nonce, nonceCount, clientNonce, qop, ha2 );

		String DigestHeader = "Digest username=\"" + USER + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\""
				+ LOGIN_SUB_URI + "\", qop=" + qop + ", nc=" + nonceCount + ", cnonce=\"" + clientNonce + "\", response=\""
				+ responseCode + "\", opaque=\"" + opaque + "\"";

		response = webResource.header( "authorization", DigestHeader ).type( MediaType.TEXT_PLAIN ).accept( "*" ).get( ClientResponse.class );
		
		System.out.println("Cookie: " +  response.getCookies().get( 1 ).toString());
	
		sessionId = response.getCookies().get(1).toString();
		authorizationHeader = DigestHeader;
		
	}
	
	// For generating HA1 value
	public String formHA1( String userName, String realm, String password ) {
		
		String ha1 = DigestUtils.md5Hex( userName + ":" + realm + ":" + password );
		
		return ha1;
	}

	// For generating HA2 value
	public String formHA2( String method, String uri ) {
	
		String ha2 = DigestUtils.md5Hex( method + ":" + uri );
		
		return ha2;
	}

	// For generating response at client side
	public String generateResponse( String ha1, String nonce, int nonceCount, String clientNonce, String qop, String ha2 ) {
		
		String response = DigestUtils.md5Hex( ha1 + ":" + nonce + ":" + nonceCount + ":" + clientNonce + ":" + qop + ":" + ha2 );
		
		return response;

	}
	
}  