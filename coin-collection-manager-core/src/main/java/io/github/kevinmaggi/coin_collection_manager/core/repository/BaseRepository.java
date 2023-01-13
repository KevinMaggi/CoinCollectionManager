package io.github.kevinmaggi.coin_collection_manager.core.repository;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.BaseEntity;

/**
 * Interface for repository layer for a {@code BaseEntity}.
 *
 * @param <T> Specific entity
 */
public interface BaseRepository<T extends BaseEntity> {

	/**
	 * Get all the {@code T entity} contained in the database.
	 * 
	 * @return		A list with all the {@code T entities}
	 */
	List<T> findAll();

	/**
	 * Get a {@code T entity} by its id.
	 * 
	 * @param id 	{@code T entity} id
	 * @return 		the {@code T entity}
	 * @throws IllegalArgumentException 	If the {@code id} is null
	 */
	T findById(UUID id) throws IllegalArgumentException;

	/**
	 * Persist (add or update) a {@code T entity} in the database.
	 * 
	 * @param t	the {@code T entity} to save
	 * @return		the {@code T entity}
	 * @throws IllegalArgumentException 	If the {@code T entity} is null
	 */
	T save(T t) throws IllegalArgumentException;

	/**
	 * Remove a {@code T entity} from the database.
	 * 
	 * @param t	the {@code T entity} to delete
	 * @throws IllegalArgumentException 	If the {@code T entity} is null
	 */
	void delete(T t) throws IllegalArgumentException;

}