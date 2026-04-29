package client.main;


// parameters that get parsed at start of the game 

public class ClientConfig {
	private final String serverBaseUrl; 
	private final String gameId; 
	private final String firstName; 
	private final String lastName; 
	private final String uAccountUsername; 
	private final String mode; 
	
	public ClientConfig(String mode, String serverBaseUrl, String gameId, String firstName, String lastName,
			String uAccountUsername) {
		this.mode = mode;
		this.serverBaseUrl = serverBaseUrl;
		this.gameId = gameId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.uAccountUsername = uAccountUsername; 
		
	}	
	
	public String getMode() {
		return mode;
	}

	public String getServerBaseUrl() {
		return serverBaseUrl;
	}

	public String getGameId() {
		return gameId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getuAccountUsername() {
		return uAccountUsername;
	}


	
}

