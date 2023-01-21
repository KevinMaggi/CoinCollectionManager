package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateAlbumException;
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

class AlbumTransactionalManagerTestCase {
	// Test variable
	private String DB_EXCEPTION_MSG = "Something went wrong during the DB querying";
	private String DUPLICATE_ALBUM_MSG = "Such album is already present in the DB";
	private String ALBUM_NOT_FOUND_MSG = "Doesn't exist such album in the DB";

	private UUID UUID_ALBUM = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	
	private int NUMBER_OF_SLOTS = 50;
	private int OCCUPIED_SLOT = 10;
	private String NAME = "2€ commemorative";
	private Album ALBUM_1 = new Album(NAME, 1, "Armadio", NUMBER_OF_SLOTS, NUMBER_OF_SLOTS);
	private Album ALBUM_2 = new Album(NAME, 2, "Armadio", NUMBER_OF_SLOTS, OCCUPIED_SLOT);
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
	
	AlbumTransactionalManager albumManager;
	
	@BeforeEach
	void setupTestCase() {
		closeable = MockitoAnnotations.openMocks(this);
		
		albumManager = new AlbumTransactionalManager(tm);
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
		@DisplayName("Test AlbumTransactionalManager::findAllAlbums when the code is executed")
		void testFindAllAlbumsExecutedCode() {
			List<Album> fictitiousList = Arrays.asList(ALBUM_1, ALBUM_2);
			
			when(albumRepo.findAll()).thenReturn(fictitiousList);
			
			InOrder inOrder = inOrder(tm, albumRepo);
			
			assertThat(albumManager.findAllAlbums()).isEqualTo(fictitiousList);
			
			inOrder.verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
			inOrder.verify(albumRepo).findAll();
			verifyNoMoreInteractions(tm);
			verifyNoMoreInteractions(albumRepo);
		}
		
		@Test
		@DisplayName("Test AlbumTransactionalManager::findAlbumById when the code is executed")
		void testFindAlbumByIdExecutedCode() {
			when(albumRepo.findById(any())).thenReturn(ALBUM_1);
			
			InOrder inOrder = inOrder(tm, albumRepo);
			
			assertThat(albumManager.findAlbumById(UUID_ALBUM)).isEqualTo(ALBUM_1);
			
			inOrder.verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
			inOrder.verify(albumRepo).findById(UUID_ALBUM);
			verifyNoMoreInteractions(tm);
			verifyNoMoreInteractions(albumRepo);
		}
		
		@Test
		@DisplayName("Test AlbumTransactionalManager::findAlbumByNameAndVolume when the code is executed")
		void testFindAlbumByNameAndVolumeExecutedCode() {
			when(albumRepo.findByNameAndVolume(any(), anyInt())).thenReturn(ALBUM_1);
			
			InOrder inOrder = inOrder(tm, albumRepo);
			
			assertThat(albumManager.findAlbumByNameAndVolume(NAME, 1)).isEqualTo(ALBUM_1);
			
			inOrder.verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
			inOrder.verify(albumRepo).findByNameAndVolume(NAME, 1);
			verifyNoMoreInteractions(tm);
			verifyNoMoreInteractions(albumRepo);
		}
		
		@Nested
		@DisplayName("Tests for method AlbumTransactionalManager::addAlbum when the code is executed")
		class addAlbum {
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the album is already in db")
			void testAddAlbumWhenItIsAlreadyPersistedShoulThrownException() {
			when(albumRepo.findByNameAndVolume(any(), anyInt())).thenReturn(ALBUM_1);
			
			InOrder inOrder = inOrder(tm, albumRepo);
			
			assertThatThrownBy(() -> albumManager.addAlbum(ALBUM_1))
				.isInstanceOf(DuplicateAlbumException.class)
				.hasMessage(DUPLICATE_ALBUM_MSG);
			
			inOrder.verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
			inOrder.verify(albumRepo).findByNameAndVolume(ALBUM_1.getName(), ALBUM_1.getVolume());
			verifyNoMoreInteractions(tm);
			verifyNoMoreInteractions(albumRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed without exception")
			void testAddAlbumWhenNoExceptionIsThrown() {
				when(albumRepo.save(any())).thenReturn(ALBUM_1);
				
				InOrder inOrder = inOrder(tm, albumRepo);
				
				assertThat(albumManager.addAlbum(ALBUM_1)).isEqualTo(ALBUM_1);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
				inOrder.verify(albumRepo).findByNameAndVolume(ALBUM_1.getName(), ALBUM_1.getVolume());
				inOrder.verify(albumRepo).save(ALBUM_1);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
			}
		}
		
		@Nested
		@DisplayName("Tests for method AlbumTransactionalManager::updateAlbum when the code is executed")
		class updateAlbum {
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the album is not yet in db")
			void testUpdateAlbumWhenItIsNotYetPersistedShouldThrowException() {
				when(albumRepo.findById(any())).thenThrow(IllegalArgumentException.class);
				
				assertThatThrownBy(() -> albumManager.updateAlbum(ALBUM_1))
					.isInstanceOf(AlbumNotFoundException.class)
					.hasMessage(ALBUM_NOT_FOUND_MSG);
				
				verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
			}
			
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the album is not in db anymore")
			void testUpdateAlbumWhenItIsNotAnymorePersistedShouldThrowException() {
				Album SPIED_ALBUM = spy(ALBUM_1);	// need to simulate that ALBUM_1 has an id (generated)
				doReturn(UUID_ALBUM).when(SPIED_ALBUM).getId();
				when(albumRepo.findById(any())).thenReturn(null);
				
				InOrder inOrder = inOrder(tm, albumRepo);
				
				assertThatThrownBy(() -> albumManager.updateAlbum(SPIED_ALBUM))
					.isInstanceOf(AlbumNotFoundException.class)
					.hasMessage(ALBUM_NOT_FOUND_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
				inOrder.verify(albumRepo).findById(UUID_ALBUM);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed without exception")
			void testUpdateAlbumWhenNoExceptionIsThrown() {
				Album SPIED_ALBUM = spy(ALBUM_1);	// need to simulate that ALBUM_1 has an id (generated)
				doReturn(UUID_ALBUM).when(SPIED_ALBUM).getId();
				when(albumRepo.findById(any())).thenReturn(SPIED_ALBUM);
				when(albumRepo.save(SPIED_ALBUM)).thenReturn(SPIED_ALBUM);
				
				InOrder inOrder = inOrder(tm, albumRepo);
				
				assertThat(albumManager.updateAlbum(SPIED_ALBUM)).isEqualTo(SPIED_ALBUM);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
				inOrder.verify(albumRepo).findById(UUID_ALBUM);
				inOrder.verify(albumRepo).save(SPIED_ALBUM);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
			}
		}
		
		@Nested
		@DisplayName("Tests for method AlbumTransactionalManager::deleteAlbum when the code is executed")
		class deleteAlbum {
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the album is not yet in db")
			void testDeleteAlbumWhenItIsNotYetPersistedShouldThrowException() {
				when(albumRepo.findById(any())).thenThrow(IllegalArgumentException.class);
				
				assertThatThrownBy(() -> albumManager.deleteAlbum(ALBUM_1))
					.isInstanceOf(AlbumNotFoundException.class)
					.hasMessage(ALBUM_NOT_FOUND_MSG);
				
				verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
			}
			
			@Test
			@DisplayName("Test that code is executed and an exception is thrown if the album is not in db anymore")
			void testDeleteAlbumWhenItIsNotAnymorePersistedShouldThrowException() {
				Album SPIED_ALBUM = spy(ALBUM_1);	// need to simulate that ALBUM_1 has an id (generated)
				doReturn(UUID_ALBUM).when(SPIED_ALBUM).getId();
				when(albumRepo.findById(any())).thenReturn(null);
				
				InOrder inOrder = inOrder(tm, albumRepo);
				
				assertThatThrownBy(() -> albumManager.deleteAlbum(SPIED_ALBUM))
					.isInstanceOf(AlbumNotFoundException.class)
					.hasMessage(ALBUM_NOT_FOUND_MSG);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(albumRepo).findById(UUID_ALBUM);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
			}
			
			@Test
			@DisplayName("Test that code is executed without exception")
			void testUpdateAlbumWhenNoExceptionIsThrown() {
				List<Coin> fictitiousList = Arrays.asList(COIN_1, COIN_2);
				Album SPIED_ALBUM = spy(ALBUM_1);	// need to simulate that ALBUM_1 has an id (generated)
				doReturn(UUID_ALBUM).when(SPIED_ALBUM).getId();
				when(albumRepo.findById(any())).thenReturn(SPIED_ALBUM);
				when(albumRepo.save(SPIED_ALBUM)).thenReturn(SPIED_ALBUM);
				when(coinRepo.findByAlbum(UUID_ALBUM)).thenReturn(fictitiousList);
				
				InOrder inOrder = inOrder(tm, albumRepo, coinRepo);
				
				albumManager.deleteAlbum(SPIED_ALBUM);
				
				inOrder.verify(tm).doInTransaction(ArgumentMatchers.<CoinAlbumTransactionCode<?>>any());
				inOrder.verify(albumRepo).findById(UUID_ALBUM);
				inOrder.verify(coinRepo).findByAlbum(UUID_ALBUM);
				inOrder.verify(coinRepo).delete(COIN_1);
				inOrder.verify(coinRepo).delete(COIN_2);
				inOrder.verify(albumRepo).delete(SPIED_ALBUM);
				verifyNoMoreInteractions(tm);
				verifyNoMoreInteractions(albumRepo);
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
		@DisplayName("Test AlbumTransactionalManager::findAllAlbums when exception is thrown")
		void testFindAllAlbumsThrownException() {
			assertThatThrownBy(() -> albumManager.findAllAlbums())
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test AlbumTransactionalManager::findAlbumById when exception is thrown")
		void testFindAlbumByIdThrownException() {
			assertThatThrownBy(() -> albumManager.findAlbumById(UUID_ALBUM))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test AlbumTransactionalManager::findAlbumByNameAndVolume when exception is thrown")
		void testFindAlbumByNameAndVolumeThrownException() {
			assertThatThrownBy(() -> albumManager.findAlbumByNameAndVolume(NAME, 1))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test AlbumTransactionalManager::addAlbum when exception is thrown")
		void testAddAlbumThrownException() {
			assertThatThrownBy(() -> albumManager.addAlbum(ALBUM_1))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test AlbumTransactionalManager::updateAlbum when exception is thrown")
		void testUpdateAlbumThrownException() {
			assertThatThrownBy(() -> albumManager.updateAlbum(ALBUM_1))
				.isInstanceOf(DatabaseException.class)
				.hasMessage(DB_EXCEPTION_MSG)
				.hasCauseInstanceOf(DatabaseOperationException.class);
			
			verify(tm, times(1)).doInTransaction(ArgumentMatchers.<AlbumTransactionCode<?>>any());
		}
		
		@Test
		@DisplayName("Test AlbumTransactionalManager::deleteAlbum when exception is thrown")
		void testDeleteAlbumThrownException() {
			assertThatThrownBy(() -> albumManager.deleteAlbum(ALBUM_1))
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
