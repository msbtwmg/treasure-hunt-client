package client.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import client.main.ClientConfig;
import messagesbase.ResponseEnvelope;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ERequestState;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerRegistration;
import reactor.core.publisher.Mono;

public class ClientNetwork {
	private static Logger logger = LoggerFactory.getLogger(ClientNetwork.class);


	private final ClientConfig startup;
	private final WebClient baseWebClient;
	private final String serverBaseUrl; 
	private final String gameId;
	private String playerId;

	
	public ClientNetwork(ClientConfig startup) {
		
		this.startup = startup; 
		this.serverBaseUrl = startup.getServerBaseUrl();
		this.gameId = startup.getGameId();

		
		//  WebClient configuration, will be reused/customized for each
		// individual endpoint
		
		this.baseWebClient = WebClient.builder().baseUrl(serverBaseUrl + "/games")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE) // we send XML (cf. network protocol)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE) // we receive XML (cf. network protocol)
				.build();
		
		logger.debug("baseWebClient: {}", baseWebClient);
		
		
		logger.info("Initializing network done.");
	
	}
	
	
	public UniquePlayerIdentifier registerPlayer() {

	    final PlayerRegistration playerReg = new PlayerRegistration(
	            startup.getFirstName(),
	            startup.getLastName(),
	            startup.getuAccountUsername()
	    );

	    Mono<ResponseEnvelope<UniquePlayerIdentifier>> webAccess = baseWebClient
	            .method(HttpMethod.POST)
	            .uri("/" + gameId + "/players")
	            .body(BodyInserters.fromValue(playerReg))
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {});

	    ResponseEnvelope<UniquePlayerIdentifier> resultReg = webAccess.block();

	    if (resultReg.getState() == ERequestState.Error) {
	        logger.error("🚨 Client Registration error: {}", resultReg.getExceptionMessage());
	        throw new IllegalStateException("CLIENT REGISTER FAILED");
	    }

	    UniquePlayerIdentifier playerID = resultReg.getData().get();

	    logger.info("Player {} successfully registered for game {}", playerID, gameId);

	    return playerID;
	}
	
	public void sendHalfMap(PlayerHalfMap playerHalfMap) {
		
		Mono<ResponseEnvelope<UniquePlayerIdentifier>> webAccess = baseWebClient
				.method(HttpMethod.POST)
				.uri("/" + gameId + "/halfmaps")
				.body(BodyInserters.fromValue(playerHalfMap)) // specify the data which is sent to the server
				.retrieve()
				// expected object type to be returned by the server
				.bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {}); 		
		
	
		
		ResponseEnvelope<UniquePlayerIdentifier> resultHalfMap = webAccess.block();
		
		if (resultHalfMap.getState() == ERequestState.Error) {
			// typically happens if you forgot to create a new game before the client
			// execution or forgot to adapt the run configuration so that it supplies
			// the id of the new game to the client
			// open http://REDACTED:18235/games in your browser to create a new
			// game and obtain its game id
			logger.error("🚨 Half Map rejected: " + resultHalfMap.getExceptionMessage());
			
	        throw new IllegalStateException("HALF MAP EXCEPTION");
		} else {
			logger.info("HalfMap successfully sent");
		}
	}
	
	
}
