package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

public class AlbumPresenterTestCase {

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
	
	
	
	@AfterEach
	void cleanTestCase() throws Exception {
		closeable.close();
	}
}
