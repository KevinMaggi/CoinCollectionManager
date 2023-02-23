package io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.core.repository.CoinRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
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
			try {
				Coin retrieved = em.find(Coin.class, id);
				if (retrieved != null)
					em.refresh(retrieved);
				return retrieved;
			} catch (EntityNotFoundException e) {
				return null;
			}
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
	 * @param description	{@code Coin}s' (part of) description
	 * @return				a list with the corresponding {@code Coin}s
	 * @throws IllegalArgumentException 	If the {@code description} is null
	 */
	@Override
	public List<Coin> findByDescription(String description) throws IllegalArgumentException {
		if (description == null)
			throw new IllegalArgumentException("Description can't be null");
		else {
			TypedQuery<Coin> q = em.createQuery("SELECT c FROM Coin c WHERE c.description LIKE :description", Coin.class);
			q.setParameter("description", "%" + description + "%");
			return q.getResultList();
		}
	}

	/**
	 * Get all the {@code Coin}s in a specific {@code Album}.
	 *
	 * @param id	the {@code Album}'s id
	 * @return		a list with all the {@code Coin}s
	 * @throws IllegalArgumentException 	If the {@code id} is null
	 */
	@Override
	public List<Coin> findByAlbum(UUID id) throws IllegalArgumentException {
		if (id == null)
			throw new IllegalArgumentException("Album's id can't be null");
		else {
			TypedQuery<Coin> q = em.createQuery("SELECT c FROM Coin c WHERE c.album = :album", Coin.class);
			q.setParameter("album", id);
			return q.getResultList();
		}
	}

	/**
	 * Get a specific {@code Coin}.
	 *
	 * @param grade			the {@code Grade} of the {@code Coin}
	 * @param country		the country of the {@code Coin}
	 * @param year			the minting year of the {@code Coin}
	 * @param description	the description of the {@code Coin}
	 * @param note			the note relative to the {@code Coin}
	 * @return				the {@code Coin}
	 * @throws IllegalArgumentException 	If any argument is null
	 */
	@Override
	public Coin findByGradeCountryYearDescriptionAndNote(Grade grade, String country, Year year, String description, String note)
			throws IllegalArgumentException {
		if (grade == null)
			throw new IllegalArgumentException("Grade can't be null");
		else if (country == null)
			throw new IllegalArgumentException("Country can't be null");
		if (year == null)
			throw new IllegalArgumentException("Year can't be null");
		if (description == null)
			throw new IllegalArgumentException("Description can't be null");
		if (note == null)
			throw new IllegalArgumentException("Note can't be null");
		else {
			try {
				String query = "SELECT c FROM Coin c WHERE "
						+ "c.grade = :grade AND c.country = :country AND c.mintingYear = :year AND c.description = :description AND c.note = :note";
				TypedQuery<Coin> q = em.createQuery(query, Coin.class);
				q.setParameter("grade", grade);
				q.setParameter("country", country);
				q.setParameter("year", year);
				q.setParameter("description", description);
				q.setParameter("note", note);
				return q.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		}
	}

}
