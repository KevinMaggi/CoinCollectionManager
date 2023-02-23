package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManager;

/**
 * Every transactional manager must inherit from this class.
 */
public abstract class TransactionalManager {
	protected TransactionManager tm;

	protected TransactionalManager(TransactionManager tm) {
		this.tm = tm;
	}
}
