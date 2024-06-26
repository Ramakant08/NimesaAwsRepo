package com.AwsProject.AwsProjectAssignment.Repository;

import com.AwsProject.AwsProjectAssignment.Entity.S3BucketObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface S3BucketObjectRepository extends JpaRepository<S3BucketObject, Long> {
    List<S3BucketObject> findByBucketName(String bucketName);
    List<S3BucketObject> findByBucketNameAndObjectNameLike(String bucketName, String pattern);
    long countByBucketName(String bucketName);

    // Alternatively, using @Query to define the query explicitly
    @Query("SELECT COUNT(s) FROM S3BucketObject s WHERE s.bucketName = :bucketName")
    long countByBucketNameQuery(@Param("bucketName") String bucketName);
}
