package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.CoinNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateCoinException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.FullAlbumException;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

public class CoinPresenterTestCase {
	// Test variables
	private String DB_RETRIEVE_ERR_MSG = "Impossible to retrieve the albums from the database due to an error";
	private String COIN_ADDED_PREFIX = "Coin successfully added: ";
	private String COIN_REMOVED_PREFIX = "Coin successfully deleted: ";
	private String COIN_MOVED_PREFIX = "Coin successfully moved: ";
	private String FULL_ALBUM_MSG = "Impossible to add the coin to this album because it is full";
	private String FULL_NEW_ALBUM_MSG = "Impossible to move the coin to this album because it is full";
	private String DUPLICATED_COIN_MSG = "This coin already exists";
	private String COIN_NOT_FOUND_MSG = "This coin doesn't exist";
	
	private UUID UUID_ALBUM_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private UUID UUID_ALBUM_2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
	
	private Album ALBUM_1 = new Album("2€ commemorative", 1, "Armadio", 50, 25);
	private Album ALBUM_2 = new Album("Pre-euro", 1, "Armadio", 50, 25);
	private Coin COIN_1 = new Coin(Grade.G, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", UUID_ALBUM_1);
	private Coin COIN_2 = new Coin(Grade.AG, "Italy", Year.of(1995), "500 Lire", "", UUID_ALBUM_2);

	// Tests
	private AutoCloseable closeable;
	@Mock
	View view;
	@Mock
	CoinManager manager;
		
	CoinPresenter presenter;
	
	@BeforeEach
	void setupTestCase() {
		closeable = MockitoAnnotations.openMocks(this);
		
		presenter = new CoinPresenter(view, manager);
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::getAllCoins method")
	class GetAllCoins {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testGetAllCoinsCallViewIfManagerDoesNotThrowException() {
			List<Coin> list = Arrays.asList(COIN_1, COIN_2);
			when(manager.findAllCoins()).thenReturn(list);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.getAllCoins();
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws exception")
		void testGetAllCoinsCallViewErrorIfManagerThrowsException() {
			when(manager.findAllCoins()).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.getAllCoins();
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::getCoinsByAlbum method")
	class GetCoinsByAlbum {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testGetCoinsByAlbumCallViewIfManagerDoesNotThrowException() {
			List<Coin> list = Arrays.asList(COIN_1);
			Album spiedAlbum = spy(ALBUM_1);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_1);
			when(manager.findCoinsByAlbum(UUID_ALBUM_1)).thenReturn(list);
			
			InOrder inOrder = inOrder(view, manager, spiedAlbum);
			
			presenter.getCoinsByAlbum(spiedAlbum);
			
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(manager).findCoinsByAlbum(UUID_ALBUM_1);
			inOrder.verify(view).showCoinsInAlbum(list, spiedAlbum);
			verifyNoMoreInteractions(spiedAlbum);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws exception")
		void testGetCoinsByAlbumCallViewErrorIfManagerThrowsException() {
			Album spiedAlbum = spy(ALBUM_1);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_1);
			when(manager.findCoinsByAlbum(UUID_ALBUM_1)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, manager, spiedAlbum);
			
			presenter.getCoinsByAlbum(spiedAlbum);
			
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(manager).findCoinsByAlbum(UUID_ALBUM_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(spiedAlbum);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::searchCoins method")
	class SearchCoins {
		String key = "2€";
		
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testSearchCoinsCallViewIfManagerDoesNotThrowException() {
			List<Coin> list = Arrays.asList(COIN_2);
			when(manager.findCoinsByDescription(key)).thenReturn(list);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.searchCoins(key);
			
			inOrder.verify(manager).findCoinsByDescription(key);
			inOrder.verify(view).showSearchedCoins(list, key);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws exception")
		void testSearchCoinsCallViewErrorIfManagerThrowsException() {
			when(manager.findCoinsByDescription(key)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.searchCoins(key);
			
			inOrder.verify(manager).findCoinsByDescription(key);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::addCoin method")
	class AddCoin {
		@Test
		@DisplayName("Test when manager doesn't throw exceptions")
		void testAddCoinCallViewIfManagerDoesNotThrowExceptions() {
			when(manager.addCoin(COIN_1)).thenReturn(COIN_1);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(manager).addCoin(COIN_1);
			inOrder.verify(view).coinAdded(COIN_1);
			inOrder.verify(view).showSuccess(COIN_ADDED_PREFIX + COIN_1.toString());
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testAddCoinCallViewErrorIfManagerThrowsDBException() {
			when(manager.addCoin(COIN_1)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(manager).addCoin(COIN_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws full album exception")
		void testAddCoinCallViewErrorIfManagerThrowsFullAlbumException() {
			when(manager.addCoin(COIN_1)).thenThrow(FullAlbumException.class);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(manager).addCoin(COIN_1);
			inOrder.verify(view).showError(FULL_ALBUM_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws duplicated coin exception")
		void testAddCoinCallViewErrorIfManagerThrowsDuplicatedCoinException() {
			List<Coin> list = Arrays.asList(COIN_1, COIN_2);
			when(manager.findAllCoins()).thenReturn(list);
			when(manager.addCoin(COIN_1)).thenThrow(DuplicateCoinException.class);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(manager).addCoin(COIN_1);
			inOrder.verify(view).showError(DUPLICATED_COIN_MSG);
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::deleteCoin method")
	class DeleteCoin {
		@Test
		@DisplayName("Test when manager doesn't throw exceptions")
		void testDeleteCoinCallViewIfManagerDoesNotThrowExceptions() {			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.deleteCoin(COIN_1);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(manager).deleteCoin(COIN_1);
			inOrder.verify(view).coinDeleted(COIN_1);
			inOrder.verify(view).showSuccess(COIN_REMOVED_PREFIX + COIN_1.toString());
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testDeleteCoinCallViewErrorIfManagerThrowsDBException() {
			doThrow(DatabaseException.class).when(manager).deleteCoin(COIN_1);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.deleteCoin(COIN_1);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(manager).deleteCoin(COIN_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws coin not found exception")
		void testDeleteCoinCallViewErrorIfManagerThrowsCoinNotFoundException() {
			List<Coin> list = Arrays.asList(COIN_2);
			when(manager.findAllCoins()).thenReturn(list);
			doThrow(CoinNotFoundException.class).when(manager).deleteCoin(COIN_1);
			
			InOrder inOrder = inOrder(view, manager);
			
			presenter.deleteCoin(COIN_1);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(manager).deleteCoin(COIN_1);
			inOrder.verify(view).showError(COIN_NOT_FOUND_MSG);
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::moveCoin method")
	class MoveCoin {
		@Test
		@DisplayName("Test when manager doesn't throw exceptions")
		void testMoveCoinCallViewIfManagerDoesNotThrowExceptions() {
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(manager.moveCoin(COIN_1, UUID_ALBUM_2)).thenReturn(COIN_1);
			
			InOrder inOrder = inOrder(view, manager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(manager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).coinMoved(COIN_1);
			inOrder.verify(view).showSuccess(COIN_MOVED_PREFIX + COIN_1.toString());
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testMoveCoinCallViewErrorIfManagerThrowsDBException() {
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(manager.moveCoin(COIN_1, UUID_ALBUM_2)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, manager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(manager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws full album exception")
		void testAddCoinCallViewErrorIfManagerThrowsFullAlbumException() {
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(manager.moveCoin(COIN_1, UUID_ALBUM_2)).thenThrow(FullAlbumException.class);
			
			InOrder inOrder = inOrder(view, manager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(manager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).showError(FULL_NEW_ALBUM_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws coin not found exception")
		void testDeleteCoinCallViewErrorIfManagerThrowsCoinNotFoundException() {
			List<Coin> list = Arrays.asList(COIN_2);
			when(manager.findAllCoins()).thenReturn(list);
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(manager.moveCoin(COIN_1, UUID_ALBUM_2)).thenThrow(CoinNotFoundException.class);
			
			InOrder inOrder = inOrder(view, manager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(manager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(manager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).showError(COIN_NOT_FOUND_MSG);
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@AfterEach
	void cleanTestCase() throws Exception {
		closeable.close();
	}
}
