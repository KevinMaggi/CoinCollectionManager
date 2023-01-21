package io.github.kevinmaggi.coin_collection_manager.business.service;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateAlbumException;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;

/**
 * This interface declares the methods that every coin manager must implement.
 */
public interface AlbumManager {
	/**
	 * Finds all {@code Album}s in DB.
	 * 
	 * @return	A list of all the {@code Album}s
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	public List<Album> findAllAlbums() throws DatabaseException ;
	
	/**
	 * Finds a specific {@code Album}.
	 * 
	 * @param id	Id of the album
	 * @return		The album
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	public Album findAlbumById(UUID id) throws DatabaseException;
	
	/**
	 * Finds an {@code Album} with specific name and volume.
	 * 
	 * @param name		The name
	 * @param volume	The volume
	 * @return			The coin
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	public Album findAlbumByNameAndVolume(String name, int volume) throws DatabaseException;
	
	/**
	 * Adds an {@code Album} to the DB.
	 * 
	 * @param album		The album to add
	 * @return			The added album
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws DuplicateAlbumException	if try to add an album already present in DB
	 */
	public Album addAlbum(Album album) throws DatabaseException, DuplicateAlbumException;
	
	/**
	 * Updates an {@code Album} in the DB.
	 * 
	 * @param album		The album to update
	 * @return			The updated album
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws AlbumNotFoundException	if try to update an album not yet/anymore in DB
	 */
	public Album updateAlbum(Album album) throws DatabaseException, AlbumNotFoundException;
	
	/**
	 * Removes an {@code Album} from the DB.
	 * 
	 * @param album		The album to remove
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws AlbumNotFoundException	if try to update an album not yet/anymore in DB
	 */
	public void deleteAlbum(Album album) throws DatabaseException, AlbumNotFoundException;
}
