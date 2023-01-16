package io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.repository.CoinRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * Implementation of repository layer for {@code Coin} entity for Postgres DBs.
 */
public class PostgresCoinRepository extends PostgresRepository implements CoinRepository {
	
	/**
	 * Simple constructor.
	 * 
	 * @param em {@code EntityManager} to use for database operation
	 */
	public PostgresCoinRepository(EntityManager em) {
		super(em);
	}

	/**
	 * Get all the {@code Coin}s contained in the database.
	 * 
	 * @return		A list with all the {@code Coin}s
	 */
	@Override
	public List<Coin> findAll() {
		return em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
	}

	/**
	 * Get a {@code Coin} by its id.
	 * 
	 * @param id 	{@code Coin} id
	 * @return 		the {@code Coin}
	 * @throws IllegalArgumentException 	If the {@code id} is null
	 */
	@Override
	public Coin findById(UUID id) throws IllegalArgumentException {
		if (id == null)
			throw new IllegalArgumentException("ID can't be null");
		else
			return em.find(Coin.class, id);
	}

	/**
	 * Persist (add or update) a {@code Coin} in the database.
	 * 
	 * @param coin	the {@code Coin} to save
	 * @return		the {@code Coin}
	 * @throws IllegalArgumentException 	If the {@code Coin} is null
	 */
	@Override
	public Coin save(Coin coin) throws IllegalArgumentException {
		if (coin == null)
			throw new IllegalArgumentException("Coin to save can't be null");
		else {
			if (coin.getId() == null)
				em.persist(coin);
			else
				coin = em.merge(coin);
			return coin;
		}
	}

	/**
	 * Remove a {@code Coin} from the database.
	 * 
	 * @param coin	the {@code Coin} to delete
	 * @throws IllegalArgumentException 	If the {@code Coin} is null
	 */
	@Override
	public void delete(Coin coin) throws IllegalArgumentException {
		if (coin == null)
			throw new IllegalArgumentException("Coin to delete can't be null");
		else
			em.remove(coin);
	}

	/**
	 * Get {@code Coin}s by their description.
	 * 
	 * @param description	{@code Coin}s' description
	 * @return				a list with the corresponding {@code Coin}s
	 */
	@Override
	public List<Coin> findByDescription(String description) {
		if (description == null)
			throw new IllegalArgumentException("Description can't be null");
		else {
			TypedQuery<Coin> q = em.createQuery("SELECT c FROM Coin c WHERE c.description = :description", Coin.class);
			q.setParameter("description", description);
			return q.getResultList();
		}
	}

	/**
	 * Get all the {@code Coin}s in a specific {@code Album}.
	 * 
	 * @param id	the {@code Album}'s id
	 * @return		a list with all the {@code Coin}s
	 */
	@Override
	public List<Coin> findByAlbum(UUID id) {
		if (id == null)
			throw new IllegalArgumentException("Album's id can't be null");
		else {
			TypedQuery<Coin> q = em.createQuery("SELECT c FROM Coin c WHERE c.album = :album", Coin.class);
			q.setParameter("album", id);
			return q.getResultList();
		}
	}

}
