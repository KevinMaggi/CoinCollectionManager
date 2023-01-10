package io.github.kevinmaggi.coin_collection_manager.business.transaction.manager;

import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.AlbumTransactionCode;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.CoinAlbumTransactionCode;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.function.CoinTransactionCode;

/**
 * This interface defines methods for executing pieces of code inside a transaction. There's a single method overloaded for all possible
 * type of code to execute.
 * 
 * It is an intermediate between the business logic, anaware of transaction, and the repository level.
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
	 */
	<R> R doInTransaction(CoinTransactionCode<R> code);
	
	/**
	 * Executes a piece of code that involve the {@code AlbumRepository} class and returns the result.
	 * 
	 * @param <R>		returning type of the code
	 * @param code		code to execute
	 * @return			the result of the execution of {@code code}
	 */
	<R> R doInTransaction(AlbumTransactionCode<R> code);
	
	/**
	 * Executes a piece of code that involve the {@code CoinAlbumRepository} class and returns the result.
	 * 
	 * @param <R>		returning type of the code
	 * @param code		code to execute
	 * @return			the result of the execution of {@code code}
	 */
	<R> R doInTransaction(CoinAlbumTransactionCode<R> code);
}