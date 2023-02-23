package io.github.kevinmaggi.coin_collection_manager.core.repository;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;

/**
 * Interface for repository layer for {@code Album} entity.
 */
public interface AlbumRepository extends BaseRepository<Album> {

	/**
	 * Get {@code Album}s by their name.
	 *
	 * @param name		{@code Album}s' name
	 * @param volume 	{@code Album}'s volume
	 * @return			a list with the corresponding {@code Album}s
	 * @throws IllegalArgumentException 	If the {@code name} is null
	 */
	public Album findByNameAndVolume(String name, int volume) throws IllegalArgumentException;
}
