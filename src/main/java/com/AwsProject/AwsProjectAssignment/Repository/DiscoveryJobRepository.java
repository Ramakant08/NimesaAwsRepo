package com.AwsProject.AwsProjectAssignment.Repository;

import com.AwsProject.AwsProjectAssignment.Entity.DiscoveryJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscoveryJobRepository extends JpaRepository<DiscoveryJob,Long> {

}
