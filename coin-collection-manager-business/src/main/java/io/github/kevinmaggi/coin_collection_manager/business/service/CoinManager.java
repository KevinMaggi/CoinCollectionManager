package io.github.kevinmaggi.coin_collection_manager.business.service;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.business.service.exception.CoinNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateCoinException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.FullAlbumException;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;

/**
 * This interface declares the methods that every coin manager must implement.
 */
public interface CoinManager {
	/**
	 * Finds all {@code Coin}s in DB.
	 * 
	 * @return	A list of all the {@code Coin}s
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	public List<Coin> findAllCoins() throws DatabaseException;
	
	/**
	 * Finds a specific {@code Coin}.
	 * 
	 * @param id	Id of the coin
	 * @return		The coin
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws CoinNotFoundException	if no coin corresponds to the id
	 */
	public Coin findCoinById(UUID id) throws DatabaseException, CoinNotFoundException;
	
	/**
	 * Finds {@code Coin}s contained in an {@code Album}.
	 * 
	 * @param album	The album
	 * @return		A list of all the coins
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	public List<Coin> findCoinsByAlbum(UUID album) throws DatabaseException;
	
	/**
	 * Finds a {@code Coin} with a specific description.
	 * 
	 * @param description	The description
	 * @return				A list with {@code Coin}s with that description
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	public List<Coin> findCoinsByDescription(String description) throws DatabaseException;
	
	/**
	 * Adds a {@code Coin} to the DB.
	 * 
	 * @param coin	The coin to add
	 * @return		The coin added
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws FullAlbumException		if try to add a coin to a full album
	 * @throws DuplicateCoinException	if try to add a coin already present in DB
	 */
	public Coin addCoin(Coin coin) throws DatabaseException, FullAlbumException, DuplicateCoinException;
	
	/**
	 * Removes a {@code Coin} from the DB.
	 * 
	 * @param coin	The coin to remove
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws CoinNotFoundException	if try to update a coin not yet/anymore in DB
	 */
	public void deleteCoin(Coin coin) throws DatabaseException, CoinNotFoundException;
	
	/**
	 * Updates a {@code Coin} in the DB changing the album where it is located.
	 * 
	 * @param coin			The coin to update
	 * @param newAlbumId	The album where to move it
	 * @return				The updated coin
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws FullAlbumException		if try to add a coin to a full album
	 * @throws CoinNotFoundException	if try to update a coin not yet/anymore in DB
	 */
	public Coin moveCoin(Coin coin, UUID newAlbumId) throws DatabaseException, FullAlbumException, CoinNotFoundException;
}
