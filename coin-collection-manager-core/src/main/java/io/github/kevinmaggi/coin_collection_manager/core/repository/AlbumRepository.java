package io.github.kevinmaggi.coin_collection_manager.core.repository;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;

/**
 * Interface for repository layer for Album entity
 */
public interface AlbumRepository {
	/**
	 * Get all the Albums contained in the database
	 * 
	 * @return 		A list with all the Albums
	 */
	public List<Album> findAll();
	
	/**
	 * Get an Album by its id
	 * 
	 * @param id 	Album's id
	 * @return 		the Album
	 */
	public Album findById(UUID id);
	
	/**
	 * Get an Album by its name
	 * 
	 * @param name	Album's name
	 * @return		the Album
	 */
	public Album findByName(String name);
	
	/**
	 * Persist (add or update) an Album in the database
	 * 
	 * @param album	the album to save
	 * @return		the Album
	 */
	public Album save(Album album);
	
	/**
	 * Remove an Album from the database
	 * 
	 * @param album	the album to delete
	 */
	public void delete(Album album);
}
