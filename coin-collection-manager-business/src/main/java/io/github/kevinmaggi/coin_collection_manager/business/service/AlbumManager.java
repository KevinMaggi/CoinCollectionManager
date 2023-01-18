package io.github.kevinmaggi.coin_collection_manager.business.service;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;

/**
 * This interface declares the methods that every coin manager must implement.
 */
public interface AlbumManager {
	/**
	 * Finds all {@code Album}s in DB.
	 * 
	 * @return	A list of all the {@code Album}s
	 */
	public List<Album> findAllAlbum();
	
	/**
	 * Finds a specific {@code Album}.
	 * 
	 * @param id	Id of the album
	 * @return		The album
	 */
	public Album findAlbumById(UUID id);
	
	/**
	 * Finds an {@code Album} with specific name and volume.
	 * 
	 * @param name		The name
	 * @param volume	The volume
	 * @return			The coin
	 */
	public Album findAlbumByNameAndVolume(String name, int volume);
	
	/**
	 * Adds an {@code Album} to the DB.
	 * 
	 * @param album		The album to add
	 * @return			The added album
	 */
	public Album addAlbum(Album album);
	
	/**
	 * Updates an {@code Album} in the DB.
	 * 
	 * @param album		The album to update
	 * @return			The updated album
	 */
	public Album updateAlbum(Album album);
	
	/**
	 * Removes an {@code Album} from the DB.
	 * 
	 * @param album		The album to remove
	 */
	public void deleteAlbum(Album album);
}
