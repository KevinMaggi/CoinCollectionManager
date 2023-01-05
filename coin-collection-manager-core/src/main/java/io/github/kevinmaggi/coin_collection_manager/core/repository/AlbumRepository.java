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
	 * @return 		a list with all the Albums
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
	 * Get Albums by their name
	 * 
	 * @param name	Albums' name
	 * @return		a list with the corresponding Albums
	 */
	public List<Album> findByName(String name);
	
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
	 * @param album	the Album to delete
	 */
	public void delete(Album album);
}
