package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateAlbumException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql.PostgresTransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

class TransactionalServiceWithPostgresIT {
	// Test variables
	private Album ALBUM_PRE, ALBUM_COMM;
	private Coin COIN_COMM_1, COIN_COMM_2, COIN_PRE;
	
	// Tests
	private static EntityManagerFactory emf;
	private EntityManager em;
	private TransactionManagerFactory factory;
	
	private AlbumTransactionalManager albumManager;
	private CoinTransactionalManager coinManager;
	
	@BeforeAll
	static void setUpTestCase() {
		// In "postgres-it" persistence unit the jdbc url's port is defined using the env variable setted in POM for failsafe
		// so is just a matter of using that persistence unit
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}
	
	@BeforeEach
	void setUp() {
		em = emf.createEntityManager();
		factory = new PostgresTransactionManagerFactory(em);
		
		albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
		coinManager = new CoinTransactionalManager(factory.getTransactionManager());
	}
	
	@Nested
	@DisplayName("Tests regarding Album Manager")
	class AlbumManager {
		@Test
		@DisplayName("Test the insertion of an album with success")
		void testTheInsertionOfAnAlbumWithSuccess() {
			initAlbum();
			albumManager.addAlbum(ALBUM_COMM);
			
			em.getTransaction().begin();
			Album fromDB = em.find(Album.class, ALBUM_COMM.getId());
			em.getTransaction().commit();
			
			assertThat(fromDB).isEqualTo(ALBUM_COMM);
		}
		
		@Test
		@DisplayName("Test the insertion of a duplicated album")
		void testTheInsertionOfADuplicatedAlbum() {
			populateDB();
			
			Album duplicate = new Album(ALBUM_COMM.getName(), ALBUM_COMM.getVolume(), "...", 0, 0);
			
			assertThatThrownBy(() -> albumManager.addAlbum(duplicate))
				.isInstanceOf(DuplicateAlbumException.class);
			
			em.getTransaction().begin();
			List<Album> fromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			em.getTransaction().commit();
			
			assertThat(fromDB).containsOnly(ALBUM_COMM, ALBUM_PRE);
			System.out.println("qua");
		}
	}
	
	@Nested
	@DisplayName("Tests regarding Coin Manager")
	class CoinManager {
		
	}
	
	@AfterEach
	void cleanTest() throws Exception {
		// Ensure to start the next test with an empty database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE albums").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE coins").executeUpdate();
		em.getTransaction().commit();
		
		em.clear();
		em.close();
	}
	
	@AfterAll
	static void cleanTestCase() {
		emf.close();
	}

	// private
	private void populateDB() {
		initAlbum();
		
		em.getTransaction().begin();
		em.persist(ALBUM_COMM);
		em.persist(ALBUM_PRE);
		em.getTransaction().commit();
		
		COIN_COMM_1 = new Coin(Grade.AG, "Italy", Year.of(2004), "2€ comm. World Food Programme", "", ALBUM_COMM.getId());
		COIN_COMM_2 = new Coin(Grade.AG, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", ALBUM_COMM.getId());
		COIN_PRE = new Coin(Grade.AG, "Italy", Year.of(1995), "500 Lire", "", ALBUM_PRE.getId());
		
		em.getTransaction().begin();
		em.persist(COIN_COMM_1);
		em.persist(COIN_COMM_2);
		em.persist(COIN_PRE);
		em.getTransaction().commit();
	}
	
	private void initAlbum() {
		ALBUM_PRE = new Album("Europa pre-euro", 1, "Armadio", 50, 0);
		ALBUM_COMM = new Album("Euro commemorativi", 2, "Armadio", 50, 0);
	}
}
