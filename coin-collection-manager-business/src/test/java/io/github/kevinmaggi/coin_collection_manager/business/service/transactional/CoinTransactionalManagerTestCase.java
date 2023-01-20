package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.exception.DatabaseOperationException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.AlbumTransactionCode;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.CoinAlbumTransactionCode;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.CoinTransactionCode;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManager;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.core.repository.AlbumRepository;
import io.github.kevinmaggi.coin_collection_manager.core.repository.CoinRepository;

class CoinTransactionalManagerTestCase {
	// Tests variable
	private String DB_EXCEPTION_MSG = "Something went wrong during the DB querying";
	
	private UUID UUID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	
	private Album ALBUM = new Album("2€ commemorative", 1, "Armadio", 50, 10);
	private Coin COIN_1 = new Coin(Grade.AG, "Italy", Year.of(2004), "2€ comm. World Food Programme", "", null);
	private Coin COIN_2 = new Coin(Grade.G, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", null);
	
	// Tests
	private AutoCloseable closeable;
	@Mock
	TransactionManager tm;
	@Mock
	CoinRepository coinRepo;
	@Mock
	AlbumRepository albumRepo;
	
	CoinTransactionalManager coinManager;
	
	@BeforeEach
	void setupTestCase() {
		closeable = MockitoAnnotations.openMocks(this);
		
		coinManager = new CoinTransactionalManager(tm);
	}
	
	@Nested
	@DisplayName("Tests when TransactionManager don't throw exception")
	class ExecutionNoException {
		@BeforeEach
		void setupNestedTestCase() {
			// Apparently we need to stub also methods of repositories attributes of TransactionManager, and so to mock them.
			// Effectively we don't need, because stubbing the TransactionManager's methods we "force" them to use our mocked repositories;
			// so we just need to stub our repositories' methods.
			
			when(tm.doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any()))
				.thenAnswer(answer((CoinTransactionCode<?> code) -> code.apply(coinRepo)));
			
			when(tm.doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any()))
				.thenAnswer(answer((AlbumTransactionCode<?> code) -> code.apply(albumRepo)));
			
			when(tm.doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any()))
				.thenAnswer(answer((CoinAlbumTransactionCode<?> code) -> code.apply(coinRepo, albumRepo)));
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findAllCoins when the code is executed")
		void testFindAllCoinExecutedCode() {
			List<Coin> fictitiousList = Arrays.asList(COIN_1, COIN_2);
			
			when(coinRepo.findAll()).thenReturn(fictitiousList);
			
			InOrder inOrder = inOrder(coinRepo);
			
			assertThat(coinManager.findAllCoins()).isEqualTo(fictitiousList);
			
			inOrder.verify(coinRepo).findAll();
			verifyNoMoreInteractions(coinRepo);
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::FindCoinById when the code is executed")
		void testFindCoinByIdExecutedCode() {
			when(coinRepo.findById(any())).thenReturn(COIN_1);
			
			InOrder inOrder = inOrder(coinRepo);
			
			assertThat(coinManager.findCoinById(UUID_1)).isEqualTo(COIN_1);
			
			inOrder.verify(coinRepo).findById(any());
			verifyNoMoreInteractions(coinRepo);
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinsByAlbums when the code is executed")
		void testFindCoinsByAlbumExecutedCode() {
			List<Coin> fictitiousList = Arrays.asList(COIN_1, COIN_2);
			
			when(coinRepo.findByAlbum(any())).thenReturn(fictitiousList);
			
			InOrder inOrder = inOrder(coinRepo);
			
			assertThat(coinManager.findCoinsByAlbum(UUID_1)).isEqualTo(fictitiousList);
			
			inOrder.verify(coinRepo).findByAlbum(any());
			verifyNoMoreInteractions(coinRepo);
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinsByDescription when the code is executed")
		void testFindCoinByDescriptionExecutedCode() {
			List<Coin> fictitiousList = Arrays.asList(COIN_1, COIN_2);
			
			when(coinRepo.findByDescription(any())).thenReturn(fictitiousList);
			
			InOrder inOrder = inOrder(coinRepo);
			
			assertThat(coinManager.findCoinsByDescription("2€")).isEqualTo(fictitiousList);
			
			inOrder.verify(coinRepo).findByDescription(any());
			verifyNoMoreInteractions(coinRepo);
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
	}
	
	@Nested
	@DisplayName("Tests when TransactionManager throws exception")
	class ExceptionNoExecution {
		@BeforeEach
		void setupNestedTestCase() {
			when(tm.doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any()))
				.thenThrow(DatabaseOperationException.class);
			
			when(tm.doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any()))
				.thenThrow(DatabaseOperationException.class);
			
			when(tm.doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any()))
				.thenThrow(DatabaseOperationException.class);
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findAllCoins when exception is thrown")
		void testFindAllCoinThrownException() {
			assertThatThrownBy(() -> coinManager.findAllCoins())
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinById when exception is thrown")
		void testFindCoinByIdThrownException() {
			assertThatThrownBy(() -> coinManager.findCoinById(UUID_1))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinsByAlbum when exception is thrown")
		void testFindCoinsByAlbumThrownException() {
			assertThatThrownBy(() -> coinManager.findCoinsByAlbum(UUID_1))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinsByDescription when exception is thrown")
		void testFindCoinsByDescriptionThrownException() {
			assertThatThrownBy(() -> coinManager.findCoinsByDescription("2€"))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
	}
	
	@AfterEach
	void cleanTestCase() throws Exception {
		closeable.close();
	}
}
