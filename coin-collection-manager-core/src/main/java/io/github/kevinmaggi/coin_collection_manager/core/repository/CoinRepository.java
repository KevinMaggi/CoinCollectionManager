package io.github.kevinmaggi.coin_collection_manager.core.repository;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;

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
	
	/**
	 * Get a specific {@code Coin}.
	 * 
	 * @param grade			the {@code Grade} of the {@code Coin}
	 * @param country		the country of the {@code Coin}
	 * @param year			the minting year of the {@code Coin}
	 * @param description	the description of the {@code Coin}
	 * @param note			the note relative to the {@code Coin}
	 * @return				the {@code Coin}
	 */
	public Coin findByGradeCountryYearDescriptionAndNote(Grade grade, String country, Year year, String description, String note);
}
