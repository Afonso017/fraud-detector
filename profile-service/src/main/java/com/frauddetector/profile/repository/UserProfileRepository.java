package com.frauddetector.profile.repository;

import com.frauddetector.profile.entity.UserProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends CrudRepository<UserProfile, String> {}