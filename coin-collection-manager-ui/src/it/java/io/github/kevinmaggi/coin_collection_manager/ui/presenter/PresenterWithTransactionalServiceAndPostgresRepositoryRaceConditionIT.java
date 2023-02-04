package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

public class PresenterWithTransactionalServiceAndPostgresRepositoryRaceConditionIT {
	// Test variables
	private Album ALBUM_PRE, ALBUM_COMM_1, ALBUM_COMM_2;
	private Coin COIN_COMM_1, COIN_COMM_2, COIN_PRE;
	private static final String NEW_LOCATION = "new location";
	
	// Tests
	private static EntityManagerFactory emf;
	private EntityManager em;
	
	private AutoCloseable closeable;
	@Mock
	private View view;
	
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
		
		// Ensure to start every test with an empty database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE albums").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE coins").executeUpdate();
		em.getTransaction().commit();
	}
	
	@Nested
	@DisplayName("Tests regarding album presenter")
	class AlbumPresenterIT {
		@Test
		@DisplayName("Test concurrent calls to addAlbum add only one instance and don't throw exception")
		void testConcurrentCallsToAddAlbumShouldAddOnlyOneInstanceAndDoNotThrowException() {
			initAlbums();
			
			List<Thread> threads = IntStream.range(0, 10)
					.mapToObj(i -> new Thread(
								() -> {
									EntityManager localEm = emf.createEntityManager();
									TransactionManagerFactory factory = new PostgresTransactionManagerFactory(localEm);
									
									AlbumManager albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
									
									new AlbumPresenter(view, albumManager).addAlbum(ALBUM_PRE);
									
									localEm.close();
								}
					))
					.peek(Thread::start)
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(Thread::isAlive));
			
			em.getTransaction().begin();
			List<Album> fromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			em.getTransaction().commit();
			
			assertThat(fromDB).containsExactly(ALBUM_PRE);
		}
		
		@Test
		@DisplayName("Test concurrent calls to deleteAlbum delete the instance and don't throw exception")
		void testConcurrentCallsToDeleteAlbumShouldDeleteTheInstanceAndDoNotThrowException() {
			initAlbums();
			persistAlbums();
			
			List<Thread> threads = IntStream.range(0, 10)
					.mapToObj(i -> new Thread(
								() -> {
									EntityManager localEm = emf.createEntityManager();
									TransactionManagerFactory factory = new PostgresTransactionManagerFactory(localEm);
									
									AlbumManager albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
									
									// Necessary because ALBUM_PRE in localEm is detached and cannot call delete on detached entities
									localEm.getTransaction().begin();
									Album toDelete = localEm.find(Album.class, ALBUM_PRE.getId());
									localEm.getTransaction().commit();
									
									new AlbumPresenter(view, albumManager).deleteAlbum(toDelete);
									
									localEm.close();
								}
					))
					.peek(Thread::start)
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(Thread::isAlive));
			
			em.getTransaction().begin();
			List<Album> fromDB = em.createQuery("SELECT a FROM Album a", Album.class).getResultList();
			em.getTransaction().commit();
			
			assertThat(fromDB).isNotEmpty().doesNotContain(ALBUM_PRE);
		}
		
		@Test
		@DisplayName("Test concurrent calls to moveAlbum move the instance and don't throw exception")
		void testConcurrentCallsToMoveAlbumShouldMoveTheInstanceAndDoNotThrowException() {
			initAlbums();
			persistAlbums();
			
			List<Thread> threads = IntStream.range(0, 10)
					.mapToObj(i -> new Thread(
								() -> {
									EntityManager localEm = emf.createEntityManager();
									TransactionManagerFactory factory = new PostgresTransactionManagerFactory(localEm);
									
									AlbumManager albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
									
									new AlbumPresenter(view, albumManager).moveAlbum(ALBUM_PRE, NEW_LOCATION);
									
									localEm.close();
								}
					))
					.peek(Thread::start)
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(Thread::isAlive));
			
			em.getTransaction().begin();
			Album fromDB = em.find(Album.class, ALBUM_PRE.getId());
			em.getTransaction().commit();
			
			assertThat(fromDB.getLocation()).isEqualTo(NEW_LOCATION);
		}
	}
	
	@Nested
	@DisplayName("Tests regarding coin presenter")
	class CoinPresenterIT {
		@Test
		@DisplayName("Test concurrent calls to addCoin add only one instance and don't throw exception")
		void testConcurrentCallsToAddCoinShouldAddOnlyOneInstanceAndDoNotThrowException() {
			initAlbums();
			persistAlbums();
			initCoins();
			
			List<Thread> threads = IntStream.range(0, 10)
					.mapToObj(i -> new Thread(
								() -> {
									EntityManager localEm = emf.createEntityManager();
									TransactionManagerFactory factory = new PostgresTransactionManagerFactory(localEm);
									
									AlbumManager albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
									CoinManager coinManager = new CoinTransactionalManager(factory.getTransactionManager());
									
									new CoinPresenter(view, coinManager, albumManager).addCoin(COIN_PRE);
									
									localEm.close();
								}
					))
					.peek(Thread::start)
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(Thread::isAlive));
			
			em.getTransaction().begin();
			List<Coin> fromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();
			
			assertThat(fromDB).containsExactly(COIN_PRE);
		}
		
		@Test
		@DisplayName("Test concurrent calls to deleteCoin delete the instance and don't throw exception")
		void testConcurrentCallsToDeleteCoinShouldDeleteTheInstanceAndDoNotThrowException() {
			populateDB();
			
			List<Thread> threads = IntStream.range(0, 10)
					.mapToObj(i -> new Thread(
								() -> {
									EntityManager localEm = emf.createEntityManager();
									TransactionManagerFactory factory = new PostgresTransactionManagerFactory(localEm);
									
									AlbumManager albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
									CoinManager coinManager = new CoinTransactionalManager(factory.getTransactionManager());
									
									// Necessary because COIN_PRE in localEm is detached and cannot call delete on detached entities
									localEm.getTransaction().begin();
									Coin toDelete = localEm.find(Coin.class, COIN_PRE.getId());
									localEm.getTransaction().commit();
									
									new CoinPresenter(view, coinManager, albumManager).deleteCoin(toDelete);
									
									localEm.close();
								}
					))
					.peek(Thread::start)
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(Thread::isAlive));
			
			em.getTransaction().begin();
			List<Coin> fromDB = em.createQuery("SELECT c FROM Coin c", Coin.class).getResultList();
			em.getTransaction().commit();
			
			assertThat(fromDB).isNotEmpty().doesNotContain(COIN_PRE);
		}
		
		@Test
		@DisplayName("Test concurrent calls to moveCoin move the instance and don't throw exception")
		void testConcurrentCallsToMoveCoinShouldMoveTheInstanceAndDoNotThrowException() {
			populateDB();
			
			List<Thread> threads = IntStream.range(0, 10)
					.mapToObj(i -> new Thread(
								() -> {
									EntityManager localEm = emf.createEntityManager();
									TransactionManagerFactory factory = new PostgresTransactionManagerFactory(localEm);
									
									AlbumManager albumManager = new AlbumTransactionalManager(factory.getTransactionManager());
									CoinManager coinManager = new CoinTransactionalManager(factory.getTransactionManager());
									
									new CoinPresenter(view, coinManager, albumManager).moveCoin(COIN_COMM_1, ALBUM_COMM_2);
									
									localEm.close();
								}
					))
					.peek(Thread::start)
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(Thread::isAlive));
			
			em.getTransaction().begin();
			Coin fromDB = em.find(Coin.class, COIN_COMM_1.getId());
			em.getTransaction().commit();
			
			assertThat(fromDB.getAlbum()).isEqualTo(ALBUM_COMM_2.getId());
		}
	}
	
	@AfterEach
	void cleanTest() throws Exception {
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
