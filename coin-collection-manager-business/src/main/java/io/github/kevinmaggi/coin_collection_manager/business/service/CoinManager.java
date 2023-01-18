package io.github.kevinmaggi.coin_collection_manager.business.service;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;

/**
 * This interface declares the methods that every coin manager must implement.
 */
public interface CoinManager {
	/**
	 * Finds all {@code Coin}s in DB.
	 * 
	 * @return	A list of all the {@code Coin}s
	 */
	public List<Coin> findAllCoin();
	
	/**
	 * Finds a specific {@code Coin}.
	 * 
	 * @param id	Id of the coin
	 * @return		The coin
	 */
	public Coin findCoinById(UUID id);
	
	/**
	 * Finds {@code Coin}s contained in an {@code Album}.
	 * 
	 * @param album	The album
	 * @return		A list of all the coins
	 */
	public List<Coin> findCoinByAlbum(UUID album);
	
	/**
	 * Finds a {@code Coin} with a specific description.
	 * 
	 * @param description	The description
	 * @return				The coin
	 */
	public Coin findCoinByDescription(String description);
	
	/**
	 * Adds a {@code Coin} to the DB.
	 * 
	 * @param coin	The coin to add
	 * @return		The coin added
	 */
	public Coin addCoin(Coin coin);
	
	/**
	 * Updates a {@code Coin} in the DB.
	 * 
	 * @param coin	The coin to update
	 * @return		The updated coin
	 */
	public Coin updateCoin(Coin coin);
	
	/**
	 * Removes a {@code Coin} from the DB.
	 * 
	 * @param coin	The coin to remove
	 */
	public void deleteCoin(Coin coin);
}
