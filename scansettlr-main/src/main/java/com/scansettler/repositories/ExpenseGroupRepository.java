package com.scansettler.repositories;

import com.scansettler.models.ExpenseGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseGroupRepository extends MongoRepository<ExpenseGroup, String>
{
}
