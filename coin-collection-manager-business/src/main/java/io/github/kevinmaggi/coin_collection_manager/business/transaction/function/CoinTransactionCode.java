package io.github.kevinmaggi.coin_collection_manager.business.transaction.function;

import java.util.function.Function;

import io.github.kevinmaggi.coin_collection_manager.core.repository.CoinRepository;

/**
 * This interface represents a piece of code that must be executed in a transaction and involve the {@code CoinRepository} class.
 *
 * @param <R> returning type of the code
 */
@FunctionalInterface
public interface CoinTransactionCode<R> extends Function<CoinRepository, R> {}
