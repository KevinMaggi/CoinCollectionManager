package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

/**
 * Every presenter must be inherited from this class.
 */
public abstract class Presenter {
	
	protected View view;

	protected Presenter(View view) {
		this.view = view;
	}

}
