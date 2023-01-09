package io.github.kevinmaggi.coin_collection_manager.core.repository;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;

/**
 * Interface for repository layer for {@code Coin} entity.
 */
public interface CoinRepository extends BaseRepository<Coin> {
	/**
	 * Get {@code Coin}s by their description.
	 * 
	 * @param description	{@code Coin}s' description
	 * @return				a list with the corresponding {@code Coin}s
	 */
	public List<Coin> findByDescription(String description);
	
	/**
	 * Get all the {@code Coin}s in a specific {@code Album}.
	 * 
	 * @param id	the {@code Album}'s id
	 * @return		a list with all the {@code Coin}s
	 */
	public List<Coin> findByAlbum(UUID id);
}
