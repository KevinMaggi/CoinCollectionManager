package io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.repository.AlbumRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class PostgresAlbumRepository implements AlbumRepository {

	private EntityManager em;

	public PostgresAlbumRepository(EntityManager em) {
		this.em = em;
	}

	@Override
	public List<Album> findAll() {
		return em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
	}

	@Override
	public Album findById(UUID id) throws IllegalArgumentException {
		if (id == null)
			throw new IllegalArgumentException("ID can't be null");
		else
			return em.find(Album.class, id);
	}

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

	@Override
	public void delete(Album album) throws IllegalArgumentException {
		if (album == null)
			throw new IllegalArgumentException("Album to delete can't be null");
		else
			em.remove(album);
	}

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
