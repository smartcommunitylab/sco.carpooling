package it.smartcommunitylab.carpooling.mongo.repos;

import it.smartcommunitylab.carpooling.model.Discussion;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DiscussionRepository extends MongoRepository<Discussion, String> {

}
