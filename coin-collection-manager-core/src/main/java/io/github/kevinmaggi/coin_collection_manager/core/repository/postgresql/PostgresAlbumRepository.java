package io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.repository.AlbumRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Implementation of repository layer for {@code Album} entity for Postgres DBs.
 */
public class PostgresAlbumRepository extends PostgresRepository implements AlbumRepository {

	/**
	 * Simple constructor.
	 * 
	 * @param em {@code EntityManager} to use for database operation
	 */
	public PostgresAlbumRepository(EntityManager em) {
		super(em);
	}

	/**
	 * Get all the {@code Album}s contained in the database.
	 * 
	 * @return		A list with all the {@code Album}s
	 */
	@Override
	public List<Album> findAll() {
		return em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
	}

	/**
	 * Get a {@code Album} by its id.
	 * 
	 * @param id 	{@code Album} id
	 * @return 		the {@code Album}
	 * @throws IllegalArgumentException 	If the {@code id} is null
	 */
	@Override
	public Album findById(UUID id) throws IllegalArgumentException {
		if (id == null)
			throw new IllegalArgumentException("ID can't be null");
		else
			try {
				Album retrieved = em.find(Album.class, id);
				if (retrieved != null)
					em.refresh(retrieved);
				return retrieved;
			} catch (EntityNotFoundException e) {
				return null;
			}
	}

	/**
	 * Persist (add or update) a {@code Album} in the database.
	 * 
	 * @param album	the {@code Album} to save
	 * @return		the {@code Album}
	 * @throws IllegalArgumentException 	If the {@code Album} is null
	 */
	@Override
	public Album save(Album album) throws IllegalArgumentException {
		if (album == null)
			throw new IllegalArgumentException("Album to save can't be null");
		else {
			if (album.getId() == null)
				em.persist(album);
			else
				album = em.merge(album);
			return album;
		}
	}

	/**
	 * Remove a {@code Album} from the database.
	 * 
	 * @param album	the {@code Album} to delete
	 * @throws IllegalArgumentException 	If the {@code Album} is null
	 */
	@Override
	public void delete(Album album) throws IllegalArgumentException {
		if (album == null)
			throw new IllegalArgumentException("Album to delete can't be null");
		else
			em.remove(album);
	}

	/**
	 * Get {@code Album}s by their name.
	 * 
	 * @param name		{@code Album}s' name
	 * @param volume 	{@code Album}'s volume
	 * @return			a list with the corresponding {@code Album}s
	 * @throws IllegalArgumentException 	If the {@code name} is null
	 */
	@Override
	public Album findByNameAndVolume(String name, int volume) throws IllegalArgumentException {
		if (name == null)
			throw new IllegalArgumentException("Name can't be null");
		else {
			try {
				TypedQuery<Album> q = em.createQuery("SELECT a FROM Album a WHERE a.name = :name AND a.volume = :volume", Album.class);
				q.setParameter("name", name);
				q.setParameter("volume", volume);
				return q.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		}
	}

}
