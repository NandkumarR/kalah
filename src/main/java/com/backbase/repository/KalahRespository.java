package com.backbase.repository;

import com.backbase.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nandk on 06/03/2021.
 * Mongo Repository defined for CRUD operations.
 */
@Repository
public interface KalahRespository extends MongoRepository<Game,String> {
}
