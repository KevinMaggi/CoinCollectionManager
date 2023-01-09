package io.github.kevinmaggi.coin_collection_manager.core.repository;

import java.util.List;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;

/**
 * Interface for repository layer for {@code Album} entity.
 */
public interface AlbumRepository extends BaseRepository<Album> {
	
	/**
	 * Get {@code Album}s by their name.
	 * 
	 * @param name	{@code Album}s' name
	 * @return		a list with the corresponding {@code Album}s
	 */
	public List<Album> findByName(String name);
}
