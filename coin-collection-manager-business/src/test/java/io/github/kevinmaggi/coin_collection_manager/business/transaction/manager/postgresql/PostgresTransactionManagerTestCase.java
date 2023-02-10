package io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.kevinmaggi.coin_collection_manager.business.transaction.exception.*;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.*;
import io.github.kevinmaggi.coin_collection_manager.core.model.*;
import io.github.kevinmaggi.coin_collection_manager.core.repository.*;
import io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql.*;
import jakarta.persistence.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
public class PostgresTransactionManagerTestCase {
	// Tests variables
	private String MSG_ILLEGAL_ARGUMENT = "An illegal argument has been passed, transaction not committed";
	private String MSG_GENERIC = "Something went wrong committing to DB, rollback done";
	
	private UUID ALBUM_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	private Coin COIN = new Coin(Grade.AG, "Italy", Year.of(2004), "2€ comm. World Food Programme", "", ALBUM_UUID);
	private Album ALBUM = new Album("2€ commemorative", 1, "Armadio", 50, 50);
	
	// Tests
	@Container
	private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.1")
																		.withDatabaseName("databasename")
																		.withUsername("postgres-test")
																		.withPassword("postgres-test");
	
	private static EntityManagerFactory emf;
	private EntityManager em;
	
	PostgresTransactionManager tm;
	
	@Mock
	PostgresCoinRepository coinRepo;
	@Mock
	PostgresAlbumRepository albumRepo;
	
	@BeforeAll
	public static void setUpTestCase() {
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}
	
	@BeforeEach
	public void setUpTest() {
		em = emf.createEntityManager();
		
		tm = new PostgresTransactionManager(em, coinRepo, albumRepo);
	}
	
	@Nested
	@DisplayName("Tests for method PostgresTransactionManager::doInTransaction(CoinTransactionCode)")
	class DoInTransactionCoin {
		@Test
		@DisplayName("Test that should commit and return the returned if code succeds")
		void testDoInTransactionWhenCodeSuccedsShouldCommitAndReturn() {
			List<Coin> list = new ArrayList<Coin>();
			list.add(COIN);
			when(coinRepo.findAll()).thenReturn(list);
			
			CoinTransactionCode<List<Coin>> code = (CoinRepository repo) -> {return repo.findAll();};
			
			List<Coin> result = tm.doInTransaction(code);
			
			verify(coinRepo).findAll();
			assertThat(result).isSameAs(list);
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and throw exception if the code throws IllegalArgumentException")
		void testDoInTransactionWhenCodeThrowIAEShouldThrowException() {
			when(coinRepo.findById(any())).thenThrow(IllegalArgumentException.class);
			
			CoinTransactionCode<Coin> code = (CoinRepository repo) -> {return repo.findById(null);};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isInstanceOf(DatabaseOperationException.class)
				.hasMessage(MSG_ILLEGAL_ARGUMENT)
				.hasCauseInstanceOf(IllegalArgumentException.class);
			verify(coinRepo).findById(any());
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and throw exception if the code throws PersistenceException")
		void testDoInTransactionWhenCodeThrowPEShouldThrowException() {
			when(coinRepo.findAll()).thenThrow(PersistenceException.class);
			
			CoinTransactionCode<List<Coin>> code = (CoinRepository repo) -> {return repo.findAll();};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isInstanceOf(DatabaseOperationException.class)
				.hasMessage(MSG_GENERIC)
				.hasCauseInstanceOf(PersistenceException.class);
			verify(coinRepo).findAll();
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and re-throw exception if code throws other exception")
		void testDoInTransactionWhenCodeThrowsOtherExceptionShouldRethrowIt() {
			RuntimeException ex = new RuntimeException("ex msg");
			when(coinRepo.findAll()).thenThrow(ex);
			
			CoinTransactionCode<?> code = (CoinRepository repo) -> {return repo.findAll();};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isSameAs(ex);
			verify(coinRepo).findAll();
			assertThat(em.getTransaction().isActive()).isFalse();
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresTransactionManager::doInTransaction(AlbumTransactionCode)")
	class DoInTransactionAlbum {
		@Test
		@DisplayName("Test that should commit and return the returned if code succeds")
		void testDoInTransactionWhenCodeSuccedsShouldCommitAndReturn() {
			List<Album> list = new ArrayList<Album>();
			list.add(ALBUM);
			when(albumRepo.findAll()).thenReturn(list);
			
			AlbumTransactionCode<List<Album>> code = (AlbumRepository repo) -> {return repo.findAll();};
			
			List<Album> result = tm.doInTransaction(code);
			
			verify(albumRepo).findAll();
			assertThat(result).isSameAs(list);
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and throw exception if the code throws IllegalArgumentException")
		void testDoInTransactionWhenCodeThrowIAEShouldThrowException() {
			when(albumRepo.findById(any())).thenThrow(IllegalArgumentException.class);
			
			AlbumTransactionCode<Album> code = (AlbumRepository repo) -> {return repo.findById(null);};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isInstanceOf(DatabaseOperationException.class)
				.hasMessage(MSG_ILLEGAL_ARGUMENT)
				.hasCauseInstanceOf(IllegalArgumentException.class);
			verify(albumRepo).findById(any());
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and throw exception if the code throws PersistenceException")
		void testDoInTransactionWhenCodeThrowPEShouldThrowException() {
			when(albumRepo.findAll()).thenThrow(PersistenceException.class);
			
			AlbumTransactionCode<List<Album>> code = (AlbumRepository repo) -> {return repo.findAll();};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isInstanceOf(DatabaseOperationException.class)
				.hasMessage(MSG_GENERIC)
				.hasCauseInstanceOf(PersistenceException.class);
			verify(albumRepo).findAll();
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and re-throw exception if code throws other exception")
		void testDoInTransactionWhenCodeThrowsOtherExceptionShouldRethrowIt() {
			RuntimeException ex = new RuntimeException("ex msg");
			when(albumRepo.findAll()).thenThrow(ex);
			
			AlbumTransactionCode<?> code = (AlbumRepository repo) -> {return repo.findAll();};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isSameAs(ex);
			verify(albumRepo).findAll();
			assertThat(em.getTransaction().isActive()).isFalse();
		}
	}
	
	@Nested
	@DisplayName("Tests for method PostgresTransactionManager::doInTransaction(CoinAlbumTransactionCode)")
	class DoInTransactionCoinAlbum {
		@Test
		@DisplayName("Test that should commit and return the returned if code succeds")
		void testDoInTransactionWhenCodeSuccedsShouldCommitAndReturn() {
			List<Coin> list = new ArrayList<Coin>();
			list.add(COIN);
			
			Supplier<List<Coin>> func = () -> {coinRepo.findAll(); albumRepo.findAll(); return list;};
			
			CoinAlbumTransactionCode<List<Coin>> code = (CoinRepository cRepo, AlbumRepository aRepo) -> {return func.get();};
			
			List<Coin> result = tm.doInTransaction(code);
			
			verify(coinRepo).findAll();
			verify(albumRepo).findAll();
			assertThat(result).isSameAs(list);
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and throw exception if the code throws IllegalArgumentException")
		void testDoInTransactionWhenCodeThrowIAEShouldThrowException() {
			Supplier<List<Coin>> func = () -> {coinRepo.findAll(); albumRepo.findAll(); throw new IllegalArgumentException();};
			
			CoinAlbumTransactionCode<List<Coin>> code = (CoinRepository cRepo, AlbumRepository aRepo) -> {return func.get();};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isInstanceOf(DatabaseOperationException.class)
				.hasMessage(MSG_ILLEGAL_ARGUMENT)
				.hasCauseInstanceOf(IllegalArgumentException.class);
			verify(coinRepo).findAll();
			verify(albumRepo).findAll();
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and throw exception if the code throws PersistenceException")
		void testDoInTransactionWhenCodeThrowPEShouldThrowException() {
			Supplier<List<Coin>> func = () -> {coinRepo.findAll(); albumRepo.findAll(); throw new PersistenceException();};
			
			CoinAlbumTransactionCode<List<Coin>> code = (CoinRepository cRepo, AlbumRepository aRepo) -> {return func.get();};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isInstanceOf(DatabaseOperationException.class)
				.hasMessage(MSG_GENERIC)
				.hasCauseInstanceOf(PersistenceException.class);
			verify(coinRepo).findAll();
			verify(albumRepo).findAll();
			assertThat(em.getTransaction().isActive()).isFalse();
		}
		
		@Test
		@DisplayName("Test that should rollback and re-throw exception if code throws other exception")
		void testDoInTransactionWhenCodeThrowsOtherExceptionShouldRethrowIt() {
			RuntimeException ex = new RuntimeException("ex msg");
			Supplier<?> func = () -> {coinRepo.findAll(); albumRepo.findAll(); throw ex;};
			
			CoinAlbumTransactionCode<?> code = (CoinRepository cRepo, AlbumRepository aRepo) -> {return func.get();};
			
			assertThatThrownBy(() -> tm.doInTransaction(code))
				.isSameAs(ex);
			verify(coinRepo).findAll();
			verify(albumRepo).findAll();
			assertThat(em.getTransaction().isActive()).isFalse();
		}
	}
	
	@AfterEach
	public void cleanTest() throws Exception {
		em.clear();
		em.close();
	}
	
	@AfterAll
	public static void cleanTestCase() {
		emf.close();
	}
}
