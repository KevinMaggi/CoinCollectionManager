package io.github.kevinmaggi.coin_collection_manager.core.repository;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;

/**
 * Interface for repository layer for Coin entity
 */
public interface CoinRepository {
	/**
	 * Get all the Coins contained in the database
	 * 
	 * @return		A list with all the Albums
	 */
	public List<Coin> findAll();
	
	/**
	 * Get a Coin by its id
	 * 
	 * @param id 	Coin id
	 * @return 		the Coin
	 */
	public Coin findById(UUID id);
	
	/**
	 * Get a Coin by its name
	 * 
	 * @param name	Coin's name
	 * @return		the Coin
	 */
	public Coin findByName(String name);
	
	/**
	 * Get all the Coins in a specific Album
	 * 
	 * @param album	the Album
	 * @return		a list with all the Coins
	 */
	public List<Coin> findByAlbum(Album album); //TODO or UUID?
	
	/**
	 * Persist (add or update) a Coin in the database
	 * 
	 * @param coin	the Coin to save
	 * @return		the Coin
	 */
	public Coin save(Coin coin);
	
	/**
	 * Remove a Coin from the database
	 * 
	 * @param coin	the Coin to delete
	 */
	public void delete(Coin coin);
}
