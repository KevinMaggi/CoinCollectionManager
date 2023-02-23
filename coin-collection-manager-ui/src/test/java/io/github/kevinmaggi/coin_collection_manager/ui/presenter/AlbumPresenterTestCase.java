package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateAlbumException;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

public class AlbumPresenterTestCase {
	// Test variables
	private String DB_RETRIEVE_ERR_MSG = "Impossible to retrieve the albums from the database due to an error";
	private String ALBUM_ADDED_PREFIX = "Album successfully added: ";
	private String ALBUM_REMOVED_PREFIX = "Album successfully deleted: ";
	private String ALBUM_MOVED_PREFIX = "Album successfully moved: ";
	private String DUPLICATED_ALBUM_MSG = "This album already exists";
	private String ALBUM_NOT_FOUND_MSG = "This album doesn't exist";

	private UUID UUID_ALBUM_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

	private Album ALBUM_1 = new Album("2€ commemorative", 1, "Armadio", 50, 25);
	private Album ALBUM_2 = new Album("Pre-euro", 1, "Armadio", 50, 25);

	// Tests
	private AutoCloseable closeable;
	@Mock
	View view;
	@Mock
	AlbumManager manager;

	AlbumPresenter presenter;

	@BeforeEach
	void setupTestCase() {
		closeable = MockitoAnnotations.openMocks(this);

		presenter = new AlbumPresenter(view, manager);
	}

	@Nested
	@DisplayName("Tests for AlbumPresenter::getAllAlbums method")
	class GetAllAlbums {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testGetAllAlbumsCallViewIfManagerDoesNotThrowException() {
			List<Album> list = Arrays.asList(ALBUM_1, ALBUM_2);
			when(manager.findAllAlbums()).thenReturn(list);

			InOrder inOrder = inOrder(view, manager);

			presenter.getAllAlbums();

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws exception")
		void testGetAllAlbumsCallViewErrorIfManagerThrowsException() {
			when(manager.findAllAlbums()).thenThrow(DatabaseException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.getAllAlbums();

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}

	@Nested
	@DisplayName("Tests for AlbumPresenter::getAlbum method")
	class GetAlbum {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testGetAlbumCallViewIfManagerDoesNotThrowException() {
			when(manager.findAlbumById(UUID_ALBUM_1)).thenReturn(ALBUM_1);

			InOrder inOrder = inOrder(view, manager);

			presenter.getAlbum(UUID_ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(view).showAlbum(ALBUM_1);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws DB exception")
		void testGetAlbumCallViewErrorIfManagerThrowsDbException() {
			when(manager.findAlbumById(UUID_ALBUM_1)).thenThrow(DatabaseException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.getAlbum(UUID_ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws Album not found exception")
		void testGetAlbumCallViewErrorIfManagerThrowsAlbumNotFoundException() {
			List<Album> list = Arrays.asList(ALBUM_2);
			when(manager.findAllAlbums()).thenReturn(list);
			when(manager.findAlbumById(UUID_ALBUM_1)).thenThrow(AlbumNotFoundException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.getAlbum(UUID_ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(view).showError(ALBUM_NOT_FOUND_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}

	@Nested
	@DisplayName("Tests for AlbumPresenter::searchAlbum method")
	class SearchAlbum {
		String keyName = "2€ commemorative";
		int keyVol = 1;

		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testSearchAlbumCallViewIfManagerDoesNotThrowException() {
			when(manager.findAlbumByNameAndVolume(keyName, keyVol)).thenReturn(ALBUM_1);

			InOrder inOrder = inOrder(view, manager);

			presenter.searchAlbum(keyName, keyVol);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).findAlbumByNameAndVolume(keyName, keyVol);
			inOrder.verify(view).showSearchedAlbum(ALBUM_1, ALBUM_1.getName() + " vol." + ALBUM_1.getVolume());
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws DB exception")
		void testSearchAlbumCallViewErrorIfManagerThrowsDbException() {
			when(manager.findAlbumByNameAndVolume(keyName, keyVol)).thenThrow(DatabaseException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.searchAlbum(keyName, keyVol);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).findAlbumByNameAndVolume(keyName, keyVol);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws Album not found exception")
		void testSearchAlbumCallViewErrorIfManagerThrowsAlbumNotFoundException() {
			List<Album> list = Arrays.asList(ALBUM_2);
			when(manager.findAllAlbums()).thenReturn(list);
			when(manager.findAlbumByNameAndVolume(keyName, keyVol)).thenThrow(AlbumNotFoundException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.searchAlbum(keyName, keyVol);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).findAlbumByNameAndVolume(keyName, keyVol);
			inOrder.verify(view).showError(ALBUM_NOT_FOUND_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}

	@Nested
	@DisplayName("Tests for AlbumPresenter::addAlbum method")
	class AddAlbum {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testAddAlbumCallViewIfManagerDoesNotThrowException() {
			when(manager.addAlbum(ALBUM_1)).thenReturn(ALBUM_1);

			InOrder inOrder = inOrder(view, manager);

			presenter.addAlbum(ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).addAlbum(ALBUM_1);
			inOrder.verify(view).albumAdded(ALBUM_1);
			inOrder.verify(view).showSuccess(ALBUM_ADDED_PREFIX + ALBUM_1.toString());
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws DB exception")
		void testAddAlbumCallViewErrorIfManagerThrowsException() {
			when(manager.addAlbum(ALBUM_1)).thenThrow(DatabaseException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.addAlbum(ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).addAlbum(ALBUM_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws duplicated album exception")
		void testAddCoinCallViewErrorIfManagerThrowsDuplicatedCoinException() {
			List<Album> list = Arrays.asList(ALBUM_1, ALBUM_2);
			when(manager.findAllAlbums()).thenReturn(list);
			when(manager.addAlbum(ALBUM_1)).thenThrow(DuplicateAlbumException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.addAlbum(ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).addAlbum(ALBUM_1);
			inOrder.verify(view).showError(DUPLICATED_ALBUM_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}

	@Nested
	@DisplayName("Tests for AlbumPresenter::deleteAlbum method")
	class DeleteAlbum {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testDeleteAlbumCallViewIfManagerDoesNotThrowException() {
			InOrder inOrder = inOrder(view, manager);

			presenter.deleteAlbum(ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).deleteAlbum(ALBUM_1);
			inOrder.verify(view).albumDeleted(ALBUM_1);
			inOrder.verify(view).showSuccess(ALBUM_REMOVED_PREFIX + ALBUM_1.toString());
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws DB exception")
		void testDeleteAlbumCallViewErrorIfManagerThrowsException() {
			doThrow(DatabaseException.class).when(manager).deleteAlbum(ALBUM_1);

			InOrder inOrder = inOrder(view, manager);

			presenter.deleteAlbum(ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).deleteAlbum(ALBUM_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws album not found exception")
		void testDeleteCoinCallViewErrorIfManagerThrowsDuplicatedCoinException() {
			List<Album> list = Arrays.asList(ALBUM_2);
			when(manager.findAllAlbums()).thenReturn(list);
			doThrow(AlbumNotFoundException.class).when(manager).deleteAlbum(ALBUM_1);

			InOrder inOrder = inOrder(view, manager);

			presenter.deleteAlbum(ALBUM_1);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).deleteAlbum(ALBUM_1);
			inOrder.verify(view).showError(ALBUM_NOT_FOUND_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}

	@Nested
	@DisplayName("Tests for AlbumPresenter::moveAlbum method")
	class MoveAlbum {
		String newLocation = "cassaforte";

		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testMoveAlbumCallViewIfManagerDoesNotThrowException() {
			when(manager.moveAlbum(ALBUM_1, newLocation)).thenReturn(ALBUM_1);

			InOrder inOrder = inOrder(view, manager);

			presenter.moveAlbum(ALBUM_1, newLocation);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).moveAlbum(ALBUM_1, newLocation);
			inOrder.verify(view).albumMoved(ALBUM_1);
			inOrder.verify(view).showSuccess(ALBUM_MOVED_PREFIX + ALBUM_1.toString());
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws DB exception")
		void testMoveAlbumCallViewErrorIfManagerThrowsException() {
			when(manager.moveAlbum(ALBUM_1, newLocation)).thenThrow(DatabaseException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.moveAlbum(ALBUM_1, newLocation);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).moveAlbum(ALBUM_1, newLocation);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}

		@Test
		@DisplayName("Test when manager throws album not found exception")
		void testMoveCoinCallViewErrorIfManagerThrowsDuplicatedCoinException() {
			List<Album> list = Arrays.asList(ALBUM_2);
			when(manager.findAllAlbums()).thenReturn(list);
			when(manager.moveAlbum(ALBUM_1, newLocation)).thenThrow(AlbumNotFoundException.class);

			InOrder inOrder = inOrder(view, manager);

			presenter.moveAlbum(ALBUM_1, newLocation);

			inOrder.verify(manager).findAllAlbums();
			inOrder.verify(manager).moveAlbum(ALBUM_1, newLocation);
			inOrder.verify(view).showError(ALBUM_NOT_FOUND_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(manager);
			verifyNoMoreInteractions(view);
		}
	}

	@AfterEach
	void cleanTestCase() throws Exception {
		closeable.close();
	}
}
