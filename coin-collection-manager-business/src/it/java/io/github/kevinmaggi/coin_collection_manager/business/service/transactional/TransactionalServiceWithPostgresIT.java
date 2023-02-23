package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.CoinNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateAlbumException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateCoinException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.FullAlbumException;
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
	private Album ALBUM_PRE, ALBUM_COMM_1, ALBUM_COMM_2;
	private Coin COIN_COMM_1, COIN_COMM_2, COIN_PRE;
	private UUID INVALID_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
	private static final String NEW_LOCATION = "new location";

	// Tests
	private static EntityManagerFactory emf;
	private EntityManager em;
	private TransactionManagerFactory factory;

	private AlbumTransactionalManager albumManager;
	private CoinTransactionalManager coinManager;

	@BeforeAll
	static void setUpTestCase() {
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@BeforeEach
	void setUp() {
		em = emf.createEntityManager();
		factory = new PostgresTransactionManagerFactory(em);

		albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
		coinManager = new CoinTransactionalManager(factory.getTransactionManager());

		// Ensure to start every test with an empty database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE albums").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE coins").executeUpdate();
		em.getTransaction().commit();
	}

	@Nested
	@DisplayName("Tests regarding Album Manager")
	class AlbumManager {
		@Test
		@DisplayName("Test the insertion of an album with success")
		void testTheInsertionOfAnAlbumWithSuccess() {
			initAlbums();

			albumManager.addAlbum(ALBUM_COMM_1);

			em.getTransaction().begin();
			Album fromDB = em.find(Album.class, ALBUM_COMM_1.getId());
			em.getTransaction().commit();

			assertThat(fromDB).isEqualTo(ALBUM_COMM_1);
		}

		@Test
		@DisplayName("Test the insertion of a duplicated album")
		void testTheInsertionOfADuplicatedAlbum() {
			populateDB();

			Album duplicate = new Album(ALBUM_COMM_1.getName(), ALBUM_COMM_1.getVolume(), "...", 0, 0);

			assertThatThrownBy(() -> albumManager.addAlbum(duplicate))
				.isInstanceOf(DuplicateAlbumException.class);

			em.getTransaction().begin();
			List<Album> fromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			em.getTransaction().commit();

			assertThat(fromDB).containsOnly(ALBUM_COMM_1, ALBUM_COMM_2, ALBUM_PRE);
		}

		@Test
		@DisplayName("Test the movement of an album with success")
		void testTheMovementOfAnAlbumWithSuccess() {
			populateDB();

			Album returned = albumManager.moveAlbum(ALBUM_COMM_1, NEW_LOCATION);

			em.getTransaction().begin();
			Album fromDB = em.find(Album.class, ALBUM_COMM_1.getId());
			List<Album> listFromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			em.getTransaction().commit();

			assertThat(returned.getLocation()).isEqualTo(NEW_LOCATION);
			assertThat(fromDB).isEqualTo(ALBUM_COMM_1);
			assertThat(listFromDB).containsOnly(ALBUM_COMM_1, ALBUM_COMM_2, ALBUM_PRE);
		}

		@Test
		@DisplayName("Test the movement of a non-existing album")
		void testTheMovementOfANonExistingAlbum() {
			initAlbums();

			assertThatThrownBy(() -> albumManager.moveAlbum(ALBUM_COMM_1, NEW_LOCATION))
				.isInstanceOf(AlbumNotFoundException.class);

			em.getTransaction().begin();
			List<Album> fromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			em.getTransaction().commit();

			assertThat(fromDB).isEmpty();
		}

		@Test
		@DisplayName("Test the deletion of an album with success")
		void testTheDeletionOfAnAlbumWithSuccess() {
			populateDB();

			albumManager.deleteAlbum(ALBUM_COMM_1);

			em.getTransaction().begin();
			List<Album> fromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			List<Coin> coins = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(fromDB).containsOnly(ALBUM_COMM_2, ALBUM_PRE);
			assertThat(coins).containsOnly(COIN_PRE);
		}

		@Test
		@DisplayName("Test the deletion of a non-existing album")
		void testTheDeletionOfANonExistingAlbum() {
			initAlbums();

			assertThatThrownBy(() -> albumManager.deleteAlbum(ALBUM_COMM_1))
				.isInstanceOf(AlbumNotFoundException.class);

			em.getTransaction().begin();
			List<Album> fromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			em.getTransaction().commit();

			assertThat(fromDB).isEmpty();
		}

		@Test
		@DisplayName("Test the retrieval of all albums in case of no albums")
		void testTheRetrievalOfAllAlbumsInCaseOfNoAlbums() {
			List<Album> retrieved = albumManager.findAllAlbums();

			assertThat(retrieved).isEmpty();
		}

		@Test
		@DisplayName("Test the retrieval of all albums in case of albums")
		void testTheRetrievalOfAllAlbumsInCaseOfAlbums() {
			populateDB();

			List<Album> retrieved = albumManager.findAllAlbums();

			assertThat(retrieved).containsOnly(ALBUM_COMM_1, ALBUM_COMM_2, ALBUM_PRE);
		}

		@Test
		@DisplayName("Test the retrieval of an album by id in case of success")
		void testTheRetrievalOfAnAlbumByIdInCaseOfSuccess() {
			populateDB();

			Album retrieved = albumManager.findAlbumById(ALBUM_COMM_1.getId());

			assertThat(retrieved).isEqualTo(ALBUM_COMM_1);
		}

		@Test
		@DisplayName("Test the retrieval of an album by id in case of unsuccess")
		void testTheRetrievalOfAnAlbumByIdInCaseOfUnsuccess() {
			assertThatThrownBy(() -> albumManager.findAlbumById(INVALID_UUID))
				.isInstanceOf(AlbumNotFoundException.class);
		}

		@Test
		@DisplayName("Test the retrieval of an album by name/volume in case of success")
		void testTheRetrievalOfAnAlbumByNameVolumeInCaseOfSuccess() {
			populateDB();

			Album retrieved = albumManager.findAlbumByNameAndVolume(ALBUM_COMM_1.getName(), ALBUM_COMM_1.getVolume());

			assertThat(retrieved).isEqualTo(ALBUM_COMM_1);
		}

		@Test
		@DisplayName("Test the retrieval of an album by name/volume in case of unsuccess")
		void testTheRetrievalOfAnAlbumByNameVolumeInCaseOfUnsuccess() {
			initAlbums();

			String name = ALBUM_COMM_1.getName();
			int volume = ALBUM_COMM_1.getVolume();

			assertThatThrownBy(() -> albumManager.findAlbumByNameAndVolume(name, volume))
			.isInstanceOf(AlbumNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("Tests regarding Coin Manager")
	class CoinManager {
		@Test
		@DisplayName("Test the insertion of a coin with success")
		void testTheInsertionOfACoinWithSuccess() {
			initAlbums();
			persistAlbums();
			initCoins();

			int occupiedSlot = ALBUM_COMM_1.getOccupiedSlots();

			coinManager.addCoin(COIN_COMM_1);

			em.getTransaction().begin();
			Coin fromDB = em.find(Coin.class, COIN_COMM_1.getId());
			Album album = em.find(Album.class, ALBUM_COMM_1.getId());
			em.getTransaction().commit();

			assertThat(fromDB).isEqualTo(COIN_COMM_1);
			assertThat(album.getOccupiedSlots()).isEqualTo(occupiedSlot + 1);
		}

		@Test
		@DisplayName("Test the insertion of a duplicated coin")
		void testTheInsertionOfADuplicatedCoin() {
			populateDB();

			Coin duplicate = new Coin(COIN_COMM_1.getGrade(), COIN_COMM_1.getCountry(), COIN_COMM_1.getMintingYear(),
					COIN_COMM_1.getDescription(), COIN_COMM_1.getNote(), COIN_COMM_1.getAlbum());

			assertThatThrownBy(() -> coinManager.addCoin(duplicate))
				.isInstanceOf(DuplicateCoinException.class);

			em.getTransaction().begin();
			List<Coin> fromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(fromDB).containsOnly(COIN_COMM_1, COIN_COMM_2, COIN_PRE);
		}

		@Test
		@DisplayName("Test the insertion of a coin in a full album")
		void testTheInsertionOfACoinInAFullAlbum() {
			initAlbums();
			persistAlbums();
			initCoins();

			ALBUM_COMM_1.setOccupiedSlots(ALBUM_COMM_1.getNumberOfSlots());
			em.getTransaction().begin();
			em.merge(ALBUM_COMM_1);
			em.getTransaction().commit();

			assertThatThrownBy(() -> coinManager.addCoin(COIN_COMM_1))
			.isInstanceOf(FullAlbumException.class);

			em.getTransaction().begin();
			List<Coin> fromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(fromDB).isEmpty();
		}

		@Test
		@DisplayName("Test the movement of a coin with success")
		void testTheMovementOfACoinWithSuccess() {
			populateDB();

			Coin returned = coinManager.moveCoin(COIN_COMM_1, ALBUM_COMM_2.getId());

			em.getTransaction().begin();
			Coin fromDB = em.find(Coin.class, COIN_COMM_1.getId());
			List<Coin> listFromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(returned.getAlbum()).isEqualTo(ALBUM_COMM_2.getId());
			assertThat(fromDB).isEqualTo(COIN_COMM_1);
			assertThat(listFromDB).containsOnly(COIN_COMM_1, COIN_COMM_2, COIN_PRE);
		}

		@Test
		@DisplayName("Test the movement of a non-existing coin")
		void testTheMovementOfANonExistingCoin() {
			initAlbums();
			persistAlbums();
			initCoins();

			UUID newAlbum = ALBUM_COMM_2.getId();

			assertThatThrownBy(() -> coinManager.moveCoin(COIN_COMM_1, newAlbum))
				.isInstanceOf(CoinNotFoundException.class);

			em.getTransaction().begin();
			List<Coin> listFromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(listFromDB).isEmpty();
		}

		@Test
		@DisplayName("Test the movement of a coin into a full album")
		void testTheMovementOfACoinIntoAFullAlbum() {
			populateDB();

			ALBUM_COMM_2.setOccupiedSlots(ALBUM_COMM_2.getNumberOfSlots());
			em.getTransaction().begin();
			em.merge(ALBUM_COMM_2);
			em.getTransaction().commit();

			UUID newAlbum = ALBUM_COMM_2.getId();

			assertThatThrownBy(() -> coinManager.moveCoin(COIN_COMM_1, newAlbum))
				.isInstanceOf(FullAlbumException.class);

			em.getTransaction().begin();
			List<Coin> listFromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(listFromDB).containsOnly(COIN_COMM_1, COIN_COMM_2, COIN_PRE);
		}

		@Test
		@DisplayName("Test deletion of a coin with success")
		void testTheDeletionOfACoinWithSuccess() {
			populateDB();

			coinManager.deleteCoin(COIN_COMM_1);

			em.getTransaction().begin();
			List<Coin> listFromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(listFromDB).containsOnly(COIN_COMM_2, COIN_PRE);
		}

		@Test
		@DisplayName("Test the deletion of a non-existing coin")
		void testTheDeletionOfANonExistingCoin() {
			initAlbums();
			persistAlbums();
			initCoins();

			assertThatThrownBy(() -> coinManager.deleteCoin(COIN_COMM_1))
				.isInstanceOf(CoinNotFoundException.class);

			em.getTransaction().begin();
			List<Coin> listFromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();

			assertThat(listFromDB).isEmpty();
		}

		@Test
		@DisplayName("Test the retrieval of all coins in case of no coins")
		void testTheRetrievalOfAllCoinsInCaseOfNoCoins() {
			List<Coin> retrieved = coinManager.findAllCoins();

			assertThat(retrieved).isEmpty();
		}

		@Test
		@DisplayName("Test the retrieval of all coins in case of coins")
		void testTheRetrievalOfAllAlbumsInCaseOfAlbums() {
			populateDB();

			List<Coin> retrieved = coinManager.findAllCoins();

			assertThat(retrieved).containsOnly(COIN_COMM_1, COIN_COMM_2, COIN_PRE);
		}

		@Test
		@DisplayName("Test the retrieval of a coin by id in case of success")
		void testTheRetrievalOfACoinByIdInCaseOfSuccess() {
			populateDB();

			Coin retrieved = coinManager.findCoinById(COIN_COMM_1.getId());

			assertThat(retrieved).isEqualTo(COIN_COMM_1);
		}

		@Test
		@DisplayName("Test the retrieval of a coin by id in case of unsuccess")
		void testTheRetrievalOfACoinByIdInCaseOfUnsuccess() {
			assertThatThrownBy(() -> coinManager.findCoinById(INVALID_UUID))
				.isInstanceOf(CoinNotFoundException.class);
		}

		@Test
		@DisplayName("Test the retrieval of coins by description in case of success")
		void testTheRetrievalOfCoinsByDescriptionInCaseOfSuccess() {
			populateDB();

			List<Coin> retrieved = coinManager.findCoinsByDescription(COIN_COMM_1.getDescription());

			assertThat(retrieved).containsOnly(COIN_COMM_1);
		}

		@Test
		@DisplayName("Test the retrieval of coins by description in case of unsuccess")
		void testTheRetrievalOfCoinsByDescriptionInCaseOfUnsuccess() {
			initAlbums();
			persistAlbums();
			initCoins();

			String description = COIN_COMM_1.getDescription();

			assertThat(coinManager.findCoinsByDescription(description)).isEmpty();
		}

		@Test
		@DisplayName("Test the retrieval of coins by album in case of success")
		void testTheRetrievalOfCoinsByAlbumInCaseOfSuccess() {
			populateDB();

			List<Coin> retrieved = coinManager.findCoinsByAlbum(ALBUM_COMM_1.getId());

			assertThat(retrieved).containsOnly(COIN_COMM_1, COIN_COMM_2);
		}

		@Test
		@DisplayName("Test the retrieval of coins by album in case of unsuccess")
		void testTheRetrievalOfCoinsByAlbumInCaseOfUnsuccess() {
			initAlbums();
			persistAlbums();
			initCoins();

			assertThat(coinManager.findCoinsByAlbum(INVALID_UUID)).isEmpty();
		}
	}

	@AfterEach
	void cleanTest() throws Exception {
		em.clear();
		em.close();
	}

	@AfterAll
	static void cleanTestCase() {
		emf.close();
	}

	// private
	private void populateDB() {
		initAlbums();
		persistAlbums();
		initCoins();
		persistCoins();
	}

	private void persistCoins() {
		em.getTransaction().begin();
		em.persist(COIN_COMM_1);
		em.persist(COIN_COMM_2);
		em.persist(COIN_PRE);
		em.getTransaction().commit();
	}

	private void initCoins() {
		COIN_COMM_1 = new Coin(Grade.AG, "Italy", Year.of(2004), "2€ comm. World Food Programme", "", ALBUM_COMM_1.getId());
		COIN_COMM_2 = new Coin(Grade.AG, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", ALBUM_COMM_1.getId());
		COIN_PRE = new Coin(Grade.AG, "Italy", Year.of(1995), "500 Lire", "", ALBUM_PRE.getId());
	}

	private void persistAlbums() {
		em.getTransaction().begin();
		em.persist(ALBUM_COMM_1);
		em.persist(ALBUM_COMM_2);
		em.persist(ALBUM_PRE);
		em.getTransaction().commit();
	}

	private void initAlbums() {
		ALBUM_PRE = new Album("Europa pre-euro", 1, "Armadio", 50, 0);
		ALBUM_COMM_1 = new Album("Euro commemorativi", 1, "Armadio", 50, 0);
		ALBUM_COMM_2 = new Album("Euro commemorativi", 2, "Armadio", 50, 0);
	}
}
