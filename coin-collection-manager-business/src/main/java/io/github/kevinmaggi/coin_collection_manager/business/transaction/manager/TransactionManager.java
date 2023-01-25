package io.github.kevinmaggi.coin_collection_manager.business.transaction.manager;

import io.github.kevinmaggi.coin_collection_manager.business.transaction.exception.DatabaseOperationException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.AlbumTransactionCode;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.CoinAlbumTransactionCode;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.CoinTransactionCode;

/**
 * This interface defines methods for executing pieces of code inside a transaction. There's a single method overloaded for all possible
 * type of code to execute.
 * 
 * It is an intermediate between the business logic, unaware of transaction, and the repository level.
 * 
 * @see io.github.kevinmaggi.coin_collection_manager.business.transaction.function
 */
public interface TransactionManager {
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
	<R> R doInTransaction(CoinTransactionCode<R> code) throws DatabaseOperationException, RuntimeException;
	
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
	<R> R doInTransaction(AlbumTransactionCode<R> code) throws DatabaseOperationException, RuntimeException;
	
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
	<R> R doInTransaction(CoinAlbumTransactionCode<R> code) throws DatabaseOperationException, RuntimeException;
}