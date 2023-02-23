package io.github.kevinmaggi.coin_collection_manager.business.transaction.function;

import java.util.function.BiFunction;

import io.github.kevinmaggi.coin_collection_manager.core.repository.AlbumRepository;
import io.github.kevinmaggi.coin_collection_manager.core.repository.CoinRepository;

/**
 * This interface represents a piece of code that must be executed in a transaction and involve both the {@code CoinRepository}
 * and the {@code AlbumRepository} classes.
 *
 * @param <R> returning type of the code
 */
@FunctionalInterface
public interface CoinAlbumTransactionCode<R> extends BiFunction<CoinRepository, AlbumRepository, R> {}