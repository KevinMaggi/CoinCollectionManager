package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
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

import io.github.kevinmaggi.coin_collection_manager.business.service.exception.CoinNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateCoinException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.FullAlbumException;
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
	private String DUPLICATE_COIN_MSG = "Such coin is already present in the DB";
	private String FULL_ALBUM_MSG = "Can't add such coin to the album because it's already full";
	private String COIN_NOT_FOUND_MSG = "Doesn't exist such coin in the DB";
	
	private UUID UUID_COIN = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	private UUID UUID_ALBUM = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private UUID UUID_NEW_ALBUM = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
	
	private int NUMBER_OF_SLOTS = 50;
	private int OCCUPIED_SLOT = 10;
	private Album ALBUM_FULL = new Album("2€ commemorative", 1, "Armadio", NUMBER_OF_SLOTS, NUMBER_OF_SLOTS);
	private Album ALBUM_NOT_FULL = new Album("2€ commemorative", 2, "Armadio", NUMBER_OF_SLOTS, OCCUPIED_SLOT);
	private Coin COIN_1 = new Coin(Grade.AG, "Italy", Year.of(2004), "2€ comm. World Food Programme", "", UUID_ALBUM);
	private Coin COIN_2 = new Coin(Grade.G, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", UUID_ALBUM);
	
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
		
		// reset test album
		ALBUM_NOT_FULL.setOccupiedSlots(OCCUPIED_SLOT);
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
			
			InOrder inOrder = inOrder(tm, coinRepo);
			
			assertThat(coinManager.findAllCoins()).isEqualTo(fictitiousList);
			
			inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
			inOrder.verify(coinRepo).findAll();
			verifyNoMoreInteractions(tm);
			verifyNoMoreInteractions(coinRepo);
		}
		
		@Nested
		@DisplayName("Test CoinTransactionalManager::FindCoinById when the code is executed")
		class FindCoinById {
			@Test
			@DisplayName("Test that code is executed and exception is thrown if the coin doens't exist")
			void testFindCoinByIdWhenCoinDoesNotExistShouldThrowException() {
				when(coinRepo.findById(any())).thenReturn(null);
				
				InOrder inOrder = inOrder(tm, coinRepo);
				
				assertThatThrownBy(() -> coinManager.findCoinById(UUID_COIN))
					.isInstanceOf(CoinNotFoundException.class)
					.hasMessage(COIN_NOT_FOUND_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(coinRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed without exception")
			void testFindCoinByIdExecutedCode() {
				when(coinRepo.findById(any())).thenReturn(COIN_1);
				
				InOrder inOrder = inOrder(tm, coinRepo);
				
				assertThat(coinManager.findCoinById(UUID_COIN)).isEqualTo(COIN_1);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(coinRepo);
			}
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinsByAlbums when the code is executed")
		void testFindCoinsByAlbumExecutedCode() {
			List<Coin> fictitiousList = Arrays.asList(COIN_1, COIN_2);
			
			when(coinRepo.findByAlbum(any())).thenReturn(fictitiousList);
			
			InOrder inOrder = inOrder(tm, coinRepo);
			
			assertThat(coinManager.findCoinsByAlbum(UUID_ALBUM)).isEqualTo(fictitiousList);
			
			inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
			inOrder.verify(coinRepo).findByAlbum(UUID_ALBUM);
			verifyNoMoreInteractions(tm);
			verifyNoMoreInteractions(coinRepo);
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinsByDescription when the code is executed")
		void testFindCoinByDescriptionExecutedCode() {
			List<Coin> fictitiousList = Arrays.asList(COIN_1, COIN_2);
			
			when(coinRepo.findByDescription(any())).thenReturn(fictitiousList);
			
			InOrder inOrder = inOrder(tm, coinRepo);
			
			assertThat(coinManager.findCoinsByDescription("2€")).isEqualTo(fictitiousList);
			
			inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
			inOrder.verify(coinRepo).findByDescription("2€");
			verifyNoMoreInteractions(tm);
			verifyNoMoreInteractions(coinRepo);
		}
		
		@Nested
		@DisplayName("Tests for CoinTransactionalManager::addCoin")
		class addCoin {
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the coin is already in db")
			void testAddCoinWhenItIsAlreadyPersistedShouldThrowException() {
				when(coinRepo.findByGradeCountryYearDescriptionAndNote(any(), any(), any(), any(), any()))
					.thenReturn(COIN_1);
				
				InOrder inOrder = inOrder(tm, coinRepo);
				
				assertThatThrownBy(() -> coinManager.addCoin(COIN_1))
					.isInstanceOf(DuplicateCoinException.class)
					.hasMessage(DUPLICATE_COIN_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findByGradeCountryYearDescriptionAndNote(
						COIN_1.getGrade(), COIN_1.getCountry(), COIN_1.getMintingYear(), COIN_1.getDescription(), COIN_1.getNote()
						);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(coinRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed and exception is thrown if the coin is not yet in the db but the album is full")
			void testAddCoinWhenItIsNotYetPersistedAndAlbumIsFullShouldExecuteCodeAndThrowException() {
				when(coinRepo.findByGradeCountryYearDescriptionAndNote(any(), any(), any(), any(), any()))
					.thenReturn(null);
				when(albumRepo.findById(any())).thenReturn(ALBUM_FULL);
				
				InOrder inOrder = inOrder(tm, coinRepo, albumRepo);
				
				assertThatThrownBy(() -> coinManager.addCoin(COIN_1))
					.isInstanceOf(FullAlbumException.class)
					.hasMessage(FULL_ALBUM_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findByGradeCountryYearDescriptionAndNote(
						COIN_1.getGrade(), COIN_1.getCountry(), COIN_1.getMintingYear(), COIN_1.getDescription(), COIN_1.getNote()
						);
				inOrder.verify(albumRepo).findById(COIN_1.getAlbum());
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
				verifyNoMoreInteractions(coinRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed if the coin is not yet in the db and the album is not full")
			void testAddCoinWhenItIsNotYetPersistedAndAlbumIsNotFullShouldExecuteCode() {
				Album SPIED_ALBUM = spy(ALBUM_NOT_FULL);		// need to see if updated slots
				
				when(coinRepo.findByGradeCountryYearDescriptionAndNote(any(), any(), any(), any(), any()))
					.thenReturn(null);
				when(coinRepo.save(any())).thenReturn(COIN_1);
				when(albumRepo.findById(any())).thenReturn(SPIED_ALBUM);
				
				InOrder inOrder = inOrder(tm, coinRepo, albumRepo);
				
				assertThat(coinManager.addCoin(COIN_1)).isEqualTo(COIN_1);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findByGradeCountryYearDescriptionAndNote(
						COIN_1.getGrade(), COIN_1.getCountry(), COIN_1.getMintingYear(), COIN_1.getDescription(), COIN_1.getNote()
						);
				inOrder.verify(albumRepo).findById(COIN_1.getAlbum());
				inOrder.verify(albumRepo).save(SPIED_ALBUM);
				verify(SPIED_ALBUM).setOccupiedSlots(OCCUPIED_SLOT+1);
				inOrder.verify(coinRepo).save(COIN_1);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
				verifyNoMoreInteractions(coinRepo);
			}
		}
		
		@Nested
		@DisplayName("Tests for CoinTransactionalManager::deleteCoin")
		class deleteCoin {
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the coin is not yet in db")
			void testUpdateCoinWhenItIsNotYetPersistedShouldThrowException() {
				when(coinRepo.findById(any())).thenThrow(IllegalArgumentException.class);
				
				assertThatThrownBy(() -> coinManager.deleteCoin(COIN_1))
					.isInstanceOf(CoinNotFoundException.class)
					.hasMessage(COIN_NOT_FOUND_MSG);
				
				verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
			}
			
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the coin is not in db anymore")
			void testUpdateCoinWhenItIsNotPersistedAnymoreShouldThrowException() {
				Coin SPIED_COIN = spy(COIN_1);	// need to simulate that COIN_1 has an id (generated)
				doReturn(UUID_COIN).when(SPIED_COIN).getId();
				when(coinRepo.findById(any())).thenReturn(null);
				
				InOrder inOrder = inOrder(tm, coinRepo);
				
				assertThatThrownBy(() -> coinManager.deleteCoin(SPIED_COIN))
					.isInstanceOf(CoinNotFoundException.class)
					.hasMessage(COIN_NOT_FOUND_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(coinRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed if the coin is in the db")
			void testAddCoinWhenItIsNotYetPersistedAndAlbumIsNotFullShouldExecuteCode() {
				Album SPIED_ALBUM = spy(ALBUM_NOT_FULL);		// need to see if updated slots
				Coin SPIED_COIN = spy(COIN_1);	// need to simulate that COIN_1 has an id (generated)
				doReturn(UUID_COIN).when(SPIED_COIN).getId();
				
				when(coinRepo.findById(any())).thenReturn(SPIED_COIN);
				when(albumRepo.findById(any())).thenReturn(SPIED_ALBUM);
				
				InOrder inOrder = inOrder(tm, coinRepo, albumRepo);
				
				coinManager.deleteCoin(SPIED_COIN);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				inOrder.verify(albumRepo).findById(UUID_ALBUM);
				inOrder.verify(albumRepo).save(SPIED_ALBUM);
				verify(SPIED_ALBUM).setOccupiedSlots(OCCUPIED_SLOT-1);
				inOrder.verify(coinRepo).delete(SPIED_COIN);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
				verifyNoMoreInteractions(coinRepo);
			}
		}
		
		@Nested
		@DisplayName("Tests for CoinTransactionalManager::moveCoin")
		class moveCoin {
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the coin is not yet in db")
			void testMoveCoinWhenItIsNotYetPersistedShouldThrowException() {
				when(coinRepo.findById(any())).thenThrow(IllegalArgumentException.class);
				
				assertThatThrownBy(() -> coinManager.moveCoin(COIN_1, UUID_NEW_ALBUM))
					.isInstanceOf(CoinNotFoundException.class)
					.hasMessage(COIN_NOT_FOUND_MSG);
				
				verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
			}
			
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the coin is not in db anymore")
			void testMoveCoinWhenItIsNotPersistedAnymoreShouldThrowException() {
				Coin SPIED_COIN = spy(COIN_1);	// need to simulate that COIN_1 has an id (generated)
				doReturn(UUID_COIN).when(SPIED_COIN).getId();
				when(coinRepo.findById(any())).thenReturn(null);
				
				InOrder inOrder = inOrder(tm, coinRepo);
				
				assertThatThrownBy(() -> coinManager.moveCoin(SPIED_COIN, UUID_NEW_ALBUM))
					.isInstanceOf(CoinNotFoundException.class)
					.hasMessage(COIN_NOT_FOUND_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(coinRepo);
			}
		
			@Test
			@DisplayName("Test that code is executed and exception is thrown if the coin is in the db but the new album is full")
			void testMoveCoinWhenItIsNotYetPersistedAndAlbumIsFullShouldExecuteCodeAndThrowException() {
				Coin SPIED_COIN = spy(COIN_1);	// need to simulate that COIN_1 has an id (generated)
				doReturn(UUID_COIN).when(SPIED_COIN).getId();
				when(coinRepo.findById(any())).thenReturn(SPIED_COIN);
				when(albumRepo.findById(any())).thenReturn(ALBUM_NOT_FULL).thenReturn(ALBUM_FULL);
				
				InOrder inOrder = inOrder(tm, coinRepo, albumRepo);
				
				assertThatThrownBy(() -> coinManager.moveCoin(SPIED_COIN, UUID_NEW_ALBUM))
					.isInstanceOf(FullAlbumException.class)
					.hasMessage(FULL_ALBUM_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				inOrder.verify(albumRepo).findById(UUID_ALBUM);
				inOrder.verify(albumRepo).findById(UUID_NEW_ALBUM);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
				verifyNoMoreInteractions(coinRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed if the coin is in the db and the new album is not full")
			void testMoveCoinWhenItIsNotYetPersistedAndAlbumIsNotFullShouldExecuteCode() {
				Album SPIED_ALBUM_NOT_FULL = spy(ALBUM_NOT_FULL);	// need to see if updated slots
				Album SPIED_ALBUM_FULL = spy(ALBUM_FULL);	// need to see if updated slots
				Coin SPIED_COIN = spy(COIN_1);	// need to simulate that COIN_1 has an id (generated)
				doReturn(UUID_COIN).when(SPIED_COIN).getId();
				when(coinRepo.findById(any())).thenReturn(SPIED_COIN);
				when(albumRepo.findById(any())).thenReturn(SPIED_ALBUM_FULL).thenReturn(SPIED_ALBUM_NOT_FULL);
				when(coinRepo.save(any())).thenReturn(SPIED_COIN);
				
				InOrder inOrder = inOrder(tm, coinRepo, albumRepo, SPIED_COIN);
				
				assertThat(coinManager.moveCoin(SPIED_COIN, UUID_NEW_ALBUM)).isEqualTo(SPIED_COIN);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				inOrder.verify(albumRepo).findById(UUID_ALBUM);
				inOrder.verify(albumRepo).findById(UUID_NEW_ALBUM);
				verify(SPIED_ALBUM_FULL).setOccupiedSlots(NUMBER_OF_SLOTS - 1);
				verify(SPIED_ALBUM_NOT_FULL).setOccupiedSlots(OCCUPIED_SLOT + 1);
				inOrder.verify(albumRepo).save(SPIED_ALBUM_FULL);
				inOrder.verify(albumRepo).save(SPIED_ALBUM_NOT_FULL);
				inOrder.verify(SPIED_COIN).setAlbum(UUID_NEW_ALBUM);
				inOrder.verify(coinRepo).save(SPIED_COIN);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
				verifyNoMoreInteractions(coinRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed if the coin is in the db and the new album is the same")
			void testMoveCoinWhenItIsNotYetPersistedAndAlbumIsTheSameShouldExecuteCode() {
				Album SPIED_ALBUM_NOT_FULL = spy(ALBUM_NOT_FULL);	// need to see if updated slots
				Coin SPIED_COIN = spy(COIN_1);	// need to simulate that COIN_1 has an id (generated)
				doReturn(UUID_COIN).when(SPIED_COIN).getId();
				when(coinRepo.findById(any())).thenReturn(SPIED_COIN);
				when(albumRepo.findById(any())).thenReturn(SPIED_ALBUM_NOT_FULL);
				when(coinRepo.save(any())).thenReturn(SPIED_COIN);
				
				InOrder inOrder = inOrder(tm, coinRepo, albumRepo, SPIED_COIN);
				
				assertThat(coinManager.moveCoin(SPIED_COIN, UUID_ALBUM)).isEqualTo(SPIED_COIN);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(coinRepo).findById(UUID_COIN);
				inOrder.verify(albumRepo, times(2)).findById(UUID_ALBUM);
				inOrder.verify(coinRepo).save(SPIED_COIN);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
				verifyNoMoreInteractions(coinRepo);
			}
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
			assertThatThrownBy(() -> coinManager.findCoinById(UUID_COIN))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::findCoinsByAlbum when exception is thrown")
		void testFindCoinsByAlbumThrownException() {
			assertThatThrownBy(() -> coinManager.findCoinsByAlbum(UUID_ALBUM))
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
		
		@Test
		@DisplayName("Test CoinTransactionalManager::addCoin when exception is thrown")
		void testAddCoinThrownException() {
			assertThatThrownBy(() -> coinManager.addCoin(COIN_1))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::deleteCoin when exception is thrown")
		void testDeleteCoinThrownException() {
			assertThatThrownBy(() -> coinManager.deleteCoin(COIN_1))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test CoinTransactionalManager::moveCoin when exception is thrown")
		void testMoveCoinThrownException() {
			assertThatThrownBy(() -> coinManager.moveCoin(COIN_1, UUID_NEW_ALBUM))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
		}
	}
	
	@AfterEach
	void cleanTestCase() throws Exception {
		closeable.close();
	}
}
