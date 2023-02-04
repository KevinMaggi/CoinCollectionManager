package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.transactional.AlbumTransactionalManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.transactional.CoinTransactionalManager;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql.PostgresTransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PresenterWithTransactionalServiceAndPostgresRepositoryIT {
	// Test variables
	private Album ALBUM_PRE, ALBUM_COMM_1, ALBUM_COMM_2;
	private Coin COIN_COMM_1, COIN_COMM_2, COIN_PRE;
	private UUID INVALID_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
	private static final String NEW_LOCATION = "new location";
	
	// Tests
	private static EntityManagerFactory emf;
	private EntityManager em;
	private TransactionManagerFactory factory;
	
	private AutoCloseable closeable;
	@Mock
	private View view;
	
	private CoinManager coinManager;
	private AlbumManager albumManager;
	
	private CoinPresenter coinPresenter;
	private AlbumPresenter albumPresenter;
	
	@BeforeAll
	static void setUpTestCase() {
		// In "postgres-it" persistence unit the jdbc url's port is defined using the env variable setted in POM for failsafe
		// so is just a matter of using that persistence unit
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}
	
	@BeforeEach
	void setUp() {
		closeable = MockitoAnnotations.openMocks(this);
		
		em = emf.createEntityManager();
		factory = new PostgresTransactionManagerFactory(em);
		
		coinManager = new CoinTransactionalManager(factory.getTransactionManager());
		albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
		
		coinPresenter = new CoinPresenter(view, coinManager, albumManager);
		albumPresenter = new AlbumPresenter(view, albumManager);
	}
	
	@Nested
	@DisplayName("Tests regarding album presenter")
	class AlbumPresenterIT {
		@Test
		@DisplayName("Test getAllAlbums")
		void testGetAllAlbums() {
			populateDB();
			
			albumPresenter.getAllAlbums();
			
			verify(view).showAllAlbums(argThat(l -> l.containsAll(Arrays.asList(ALBUM_PRE, ALBUM_COMM_1, ALBUM_COMM_2))));
		}
		
		@Test
		@DisplayName("Test getAlbum when the album exists")
		void testGetAlbumWhenAlbumExists() {
			populateDB();
			
			albumPresenter.getAlbum(ALBUM_PRE.getId());
			
			verify(view).showAlbum(ALBUM_PRE);
		}
		
		@Test
		@DisplayName("Test getAlbum when the album doesn't exist")
		void testGetAlbumWhenAlbumDoesNotExist() {
			populateDB();
			
			albumPresenter.getAlbum(INVALID_UUID);
			
			verify(view).showError(any());
			verify(view).showAllAlbums(argThat(l -> l.containsAll(Arrays.asList(ALBUM_PRE, ALBUM_COMM_1, ALBUM_COMM_2))));
		}
		
		@Test
		@DisplayName("Test searchAlbum when the album exists")
		void testSearchAlbumWhenAlbumExists() {
			populateDB();
			
			albumPresenter.searchAlbum(ALBUM_PRE.getName(), ALBUM_PRE.getVolume());
			
			verify(view).showAllAlbums(Arrays.asList(ALBUM_PRE));
		}
		
		@Test
		@DisplayName("Test searchAlbum when the album doesn't exist")
		void testSearchAlbumWhenAlbumDoesNotExist() {
			initAlbums();
			
			albumPresenter.searchAlbum(ALBUM_PRE.getName(), ALBUM_PRE.getVolume());
			
			verify(view).showError(any());
			verify(view).showAllAlbums(Collections.emptyList());
		}
		
		@Test
		@DisplayName("Test addAlbum when the album is not in the DB")
		void testAddAlbumWhenAlbumIsNotInDB() {
			initAlbums();
			
			albumPresenter.addAlbum(ALBUM_PRE);
			
			em.getTransaction().begin();
			Album fromDB = em.find(Album.class, ALBUM_PRE.getId());
			em.getTransaction().commit();
			
			verify(view).albumAdded(ALBUM_PRE);
			verify(view).showSuccess(any());
			assertThat(fromDB).isEqualTo(ALBUM_PRE);
		}
		
		@Test
		@DisplayName("Test addAlbum when the album is already in DB")
		void testAddAlbumWhenAlbumIsAlreadyInDB() {
			populateDB();
			
			albumPresenter.addAlbum(ALBUM_PRE);
			
			verify(view).showError(any());
			verify(view).showAllAlbums(argThat(l -> l.containsAll(Arrays.asList(ALBUM_PRE, ALBUM_COMM_1, ALBUM_COMM_2))));
		}
		
		@Test
		@DisplayName("Test deleteAlbum when the album exists")
		void testDeleteAlbumWhenAlbumExists() {
			populateDB();
			
			albumPresenter.deleteAlbum(ALBUM_PRE);
			
			em.getTransaction().begin();
			Album fromDB = em.find(Album.class, ALBUM_PRE.getId());
			em.getTransaction().commit();
			
			verify(view).albumDeleted(ALBUM_PRE);
			verify(view).showSuccess(any());
			assertThat(fromDB).isNull();
		}
		
		@Test
		@DisplayName("Test deleteAlbum when the album doesn't exist")
		void testDeleteAlbumWhenAlbumDoesNotExist() {
			initAlbums();
			
			albumPresenter.deleteAlbum(ALBUM_PRE);
			
			verify(view).showError(any());
			verify(view).showAllAlbums(Collections.emptyList());
		}
		
		@Test
		@DisplayName("Test moveAlbum when the album exists")
		void testMoveAlbumWhenAlbumExists() {
			populateDB();
			
			albumPresenter.moveAlbum(ALBUM_PRE, NEW_LOCATION);
			
			em.getTransaction().begin();
			Album fromDB = em.find(Album.class, ALBUM_PRE.getId());
			em.getTransaction().commit();
			
			verify(view).albumMoved(ALBUM_PRE);
			verify(view).showSuccess(any());
			assertThat(fromDB.getLocation()).isEqualTo(NEW_LOCATION);
		}
		
		@Test
		@DisplayName("Test moveAlbum when the album doesn't exist")
		void testMoveAlbumWhenAlbumDoesNotExist() {
			initAlbums();
			
			albumPresenter.moveAlbum(ALBUM_PRE, NEW_LOCATION);
			
			verify(view).showError(any());
			verify(view).showAllAlbums(Collections.emptyList());
		}
	}
	
	@Nested
	@DisplayName("Tests regarding coin presenter")
	class CoinPresenterIT {
		@Test
		@DisplayName("Test getAllCoins")
		void testGetAllCoins() {
			populateDB();
			
			coinPresenter.getAllCoins();
			
			verify(view).showAllCoins(argThat(l -> l.containsAll(Arrays.asList(COIN_PRE, COIN_COMM_1, COIN_COMM_2))));
		}
		
		@Test
		@DisplayName("Test getCoinsByAlbum when the album exists")
		void testGetCoinByAlbumWhenAlbumExists() {
			populateDB();
			
			coinPresenter.getCoinsByAlbum(ALBUM_COMM_1);
			
			verify(view).showCoinsInAlbum(argThat(l -> l.containsAll(Arrays.asList(COIN_COMM_1, COIN_COMM_2))), eq(ALBUM_COMM_1));
		}
		
		@Test
		@DisplayName("Test getCoinByAlbums when the album doesn't exist")
		void testGetCoinsByAlbumWhenAlbumDoesNotExist() {
			populateDB();
			
			em.getTransaction().begin();
			em.remove(ALBUM_PRE);
			em.getTransaction().commit();
			
			coinPresenter.getCoinsByAlbum(ALBUM_PRE);
			
			verify(view).showError(any());
			verify(view).showAllAlbums(argThat(l -> l.containsAll(Arrays.asList(ALBUM_COMM_1, ALBUM_COMM_2))));
		}
		
		@Test
		@DisplayName("Test getCoin when the coin exists")
		void testGetCoinWhenCoinExists() {
			populateDB();
			
			coinPresenter.getCoin(COIN_PRE.getId());
			
			verify(view).showCoin(COIN_PRE, ALBUM_PRE);
		}
		
		@Test
		@DisplayName("Test getCoin when the coin doesn't exist")
		void testGetCoinWhenCoinDoesNotExist() {
			populateDB();
			
			coinPresenter.getCoin(INVALID_UUID);
			
			verify(view).showError(any());
			verify(view).showAllCoins(argThat(l -> l.containsAll(Arrays.asList(COIN_PRE, COIN_COMM_1, COIN_COMM_2))));
		}
		
		@Test
		@DisplayName("Test searchCoins")
		void testSearchCoins() {
			populateDB();
			
			coinPresenter.searchCoins(COIN_PRE.getDescription());
			
			verify(view).showSearchedCoins(argThat(l -> l.contains(COIN_PRE)), eq(COIN_PRE.getDescription()));
		}
		
		@Test
		@DisplayName("Test addCoin when the coin is not already in DB")
		void testAddCoinWhenCoinIsNotAlreadyInDB() {
			initAlbums();
			persistAlbums();
			initCoins();
			
			coinPresenter.addCoin(COIN_PRE);
			
			verify(view).coinAdded(COIN_PRE);
			verify(view).showSuccess(any());
		}
		
		@Test
		@DisplayName("Test addCoin when the coin is already in DB")
		void testAddCoinWhenCoinIsAlreadyInDB() {
			populateDB();
			
			coinPresenter.addCoin(COIN_PRE);
			
			em.getTransaction().begin();
			Coin fromDB = em.find(Coin.class, COIN_PRE.getId());
			em.getTransaction().commit();
			
			verify(view).showError(any());
			verify(view).showAllCoins(argThat(l -> l.containsAll(Arrays.asList(COIN_PRE, COIN_COMM_1, COIN_COMM_2))));
			assertThat(fromDB).isEqualTo(COIN_PRE);
		}
		
		@Test
		@DisplayName("Test addCoin when the album is full")
		void testAddCoinWhenTheAlbumIsFull() {
			initAlbums();
			ALBUM_PRE.setOccupiedSlots(ALBUM_PRE.getNumberOfSlots());
			persistAlbums();
			initCoins();
			
			coinPresenter.addCoin(COIN_PRE);
			
			em.getTransaction().begin();
			List<Coin> fromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();
			
			verify(view).showError(any());
			assertThat(fromDB).isEmpty();
		}
		
		@Test
		@DisplayName("Test deleteCoin when the coin is in the DB")
		void testDeleteCoinWhenCoinIsInDB() {
			populateDB();
			
			coinPresenter.deleteCoin(COIN_PRE);
			
			em.getTransaction().begin();
			Coin fromDB = em.find(Coin.class, COIN_PRE.getId());
			em.getTransaction().commit();
			
			verify(view).coinDeleted(COIN_PRE);
			verify(view).showSuccess(any());
			assertThat(fromDB).isNull();
		}
		
		@Test
		@DisplayName("Test deleteCoin when coin is not in DB")
		void testDeleteCoinWhenCoinIsNotInDB() {
			initAlbums();
			persistAlbums();
			initCoins();
			
			coinPresenter.deleteCoin(COIN_PRE);
			
			verify(view).showError(any());
			verify(view).showAllCoins(Collections.emptyList());
		}
		
		@Test
		@DisplayName("Test moveCoin when coin is in DB and new album is not full")
		void testMoveCoinWhenCoinIsInDBAndNewAlbumIsNotFull() {
			populateDB();
			
			coinPresenter.moveCoin(COIN_COMM_1, ALBUM_COMM_2);
			
			em.getTransaction().begin();
			Coin fromDB = em.find(Coin.class, COIN_COMM_1.getId());
			em.getTransaction().commit();
			
			verify(view).coinMoved(COIN_COMM_1, ALBUM_COMM_1, ALBUM_COMM_2);
			verify(view).showSuccess(any());
			assertThat(fromDB.getAlbum()).isEqualTo(ALBUM_COMM_2.getId());
		}
		
		@Test
		@DisplayName("Test moveCoin when coin is in DB but new album is full")
		void testMoveCoinWhenCoinIsInDBButNewAlbumIsFull() {
			initAlbums();
			ALBUM_COMM_2.setOccupiedSlots(ALBUM_COMM_2.getNumberOfSlots());
			persistAlbums();
			initCoins();
			persistCoins();
			
			coinPresenter.moveCoin(COIN_COMM_1, ALBUM_COMM_2);
			
			verify(view).showError(any());
		}
		
		@Test
		@DisplayName("Test moveCoin when coin is not in DB")
		void testMoveCoinWhenCoinIsNotInDB() {
			initAlbums();
			persistAlbums();
			initCoins();
			
			coinPresenter.moveCoin(COIN_COMM_1, ALBUM_COMM_2);
			
			verify(view).showError(any());
			verify(view).showAllCoins(Collections.emptyList());
		}
		
		@Test
		@DisplayName("Test moveCoin when coin is in DB but new album doesn't exist")
		void testMoveCoinWhenCoinIsInDBButNewAlbumDoesNotExist() {
			initAlbums();
			persistAlbums();
			em.getTransaction().begin();
			em.remove(ALBUM_COMM_2);
			em.getTransaction().commit();
			initCoins();
			persistCoins();
			
			coinPresenter.moveCoin(COIN_COMM_1, ALBUM_COMM_2);
			
			verify(view).showError(any());
			verify(view).showAllAlbums(argThat(l -> l.containsAll(Arrays.asList(ALBUM_PRE, ALBUM_COMM_1))));
		}
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
		
		closeable.close();
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
