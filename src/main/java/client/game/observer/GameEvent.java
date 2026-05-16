package client.game.observer;

import java.util.ArrayList;
import java.util.List;

public class GameEvent<T> {
	// TODO: add, notify , remove hinzufuegen
	private final List<GameObserver<T>> observers = new ArrayList<>();
	
	public void addObserver(GameObserver<T> observer) {
		observers.add(observer);
	}

	
	public void removeObservers(GameObserver<T> observer) {
		observers.remove(observer); 
	}
	
	public void notifyObservers(T event) {
		for (GameObserver<T> obs : observers) {
			obs.update(event);
		}
	}
	
}
