package org.goldenroute.portfolio.repository;

import org.goldenroute.portfolio.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long>
{
}