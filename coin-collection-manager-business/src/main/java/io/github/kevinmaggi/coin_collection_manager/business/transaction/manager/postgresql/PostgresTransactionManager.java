package io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql;

import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.*;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManager;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.exception.DatabaseOperationException;
import io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql.PostgresAlbumRepository;
import io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql.PostgresCoinRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

/**
 * This class offers methods for executing pieces of code inside a transaction for Postgres DBs.
 * There's a single method overloaded for all possible type of code to execute.
 *
 * It is an intermediate between the business logic, unaware of transaction, and the repository level.
 *
 * @see io.github.kevinmaggi.coin_collection_manager.business.transaction.function
 */
public class PostgresTransactionManager implements TransactionManager {
	private static final String EXCEPTION_MSG_ILLEGAL_ARGUMENT = "An illegal argument has been passed, transaction not committed";
	private static final String EXCEPTION_MSG_GENERIC = "Something went wrong committing to DB, rollback done";

	private EntityManager em;

	private PostgresAlbumRepository albumRepo;
	private PostgresCoinRepository coinRepo;

	/**
	 * Constructor.
	 *
	 * @param em			{@code EntityManager} for interfacing with db
	 * @param coinRepo		{@code CoinRepository} to use
	 * @param albumRepo		{@code AlbumRepository} to use
	 */
	public PostgresTransactionManager(EntityManager em, PostgresCoinRepository coinRepo, PostgresAlbumRepository albumRepo) {
		this.em = em;
		this.coinRepo = coinRepo;
		this.albumRepo = albumRepo;
	}

	/**
	 * Executes a piece of code that involve the {@code CoinRepository} class and returns the result.
	 *
	 * @param <R>		returning type of the code
	 * @param code		code to execute
	 * @return			the result of the execution of {@code code}
	 * @throws DatabaseOperationException	when the code execution fails because of some problem related to DB operation
	 * @throws RuntimeException				when the code execution throws exception other than IllegalArgumentException
	 * 										and PersistenceException, they will be re-thrown
	 */
	@Override
	public <R> R doInTransaction(CoinTransactionCode<R> code) throws DatabaseOperationException, RuntimeException {
		try {
			em.getTransaction().begin();
			R result = code.apply(coinRepo);
			em.getTransaction().commit();
			return result;
		} catch (IllegalArgumentException e) {
			em.getTransaction().rollback();
			throw new DatabaseOperationException(EXCEPTION_MSG_ILLEGAL_ARGUMENT, e);
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw new DatabaseOperationException(EXCEPTION_MSG_GENERIC, e);
		} catch (RuntimeException e) {
			em.getTransaction().rollback();
			throw e;
		}
	}

	/**
	 * Executes a piece of code that involve the {@code AlbumRepository} class and returns the result.
	 *
	 * @param <R>		returning type of the code
	 * @param code		code to execute
	 * @return			the result of the execution of {@code code}
	 * @throws DatabaseOperationException	when the code execution fails because of some problem related to DB operation
	 * @throws RuntimeException				when the code execution throws exception other than IllegalArgumentException
	 * 										and PersistenceException, they will be re-thrown
	 */
	@Override
	public <R> R doInTransaction(AlbumTransactionCode<R> code) throws DatabaseOperationException, RuntimeException {
		try {
			em.getTransaction().begin();
			R result = code.apply(albumRepo);
			em.getTransaction().commit();
			return result;
		} catch (IllegalArgumentException e) {
			em.getTransaction().rollback();
			throw new DatabaseOperationException(EXCEPTION_MSG_ILLEGAL_ARGUMENT, e);
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw new DatabaseOperationException(EXCEPTION_MSG_GENERIC, e);
		} catch (RuntimeException e) {
			em.getTransaction().rollback();
			throw e;
		}
	}

	/**
	 * Executes a piece of code that involve the {@code CoinRepository} and {@code AlbumRepository} classes and returns the result.
	 *
	 * @param <R>		returning type of the code
	 * @param code		code to execute
	 * @return			the result of the execution of {@code code}
	 * @throws DatabaseOperationException	when the code execution fails because of some problem related to DB operation
	 * @throws RuntimeException				when the code execution throws exception other than IllegalArgumentException
	 * 										and PersistenceException, they will be re-thrown
	 */
	@Override
	public <R> R doInTransaction(CoinAlbumTransactionCode<R> code) throws DatabaseOperationException, RuntimeException {
		try {
			em.getTransaction().begin();
			R result = code.apply(coinRepo, albumRepo);
			em.getTransaction().commit();
			return result;
		} catch (IllegalArgumentException e) {
			em.getTransaction().rollback();
			throw new DatabaseOperationException(EXCEPTION_MSG_ILLEGAL_ARGUMENT, e);
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw new DatabaseOperationException(EXCEPTION_MSG_GENERIC, e);
		} catch (RuntimeException e) {
			em.getTransaction().rollback();
			throw e;
		}
	}

}
