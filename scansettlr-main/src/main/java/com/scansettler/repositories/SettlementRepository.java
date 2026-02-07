package com.scansettler.repositories;

import com.scansettler.models.Settlement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends MongoRepository<Settlement, String>
{
}
