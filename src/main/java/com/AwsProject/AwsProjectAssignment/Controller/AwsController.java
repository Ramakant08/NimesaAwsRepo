package com.AwsProject.AwsProjectAssignment.Controller;

import com.AwsProject.AwsProjectAssignment.Entity.DiscoveryJob;
import com.AwsProject.AwsProjectAssignment.Entity.S3BucketObject;
import com.AwsProject.AwsProjectAssignment.Repository.DiscoveryJobRepository;
import com.AwsProject.AwsProjectAssignment.Repository.S3BucketObjectRepository;
import com.AwsProject.AwsProjectAssignment.Service.AwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AwsController {

    @Autowired
    private AwsService awsService;
    @Autowired
    private DiscoveryJobRepository discoveryJobRepository;
    @Autowired
    private S3BucketObjectRepository s3BucketObjectRepository;

    @PostMapping("/discoverServices")
    public ResponseEntity<Long> discoverServices(@RequestBody List<String> services) {
        DiscoveryJob job = new DiscoveryJob();
        job.setStatus("In Progress");
        job = discoveryJobRepository.save(job);
        final DiscoveryJob[] jobReference = {job};

        DiscoveryJob finalJob = job;

        CompletableFuture.runAsync(() -> {
            if (services.contains("EC2")) {
                List<String> instances = awsService.discoverEC2Instances();
                finalJob.setStatus("Success");
            }
            if (services.contains("S3")) {
                List<String> buckets = awsService.listS3Buckets();
                saveBucketsToDatabase(buckets);
                finalJob.setStatus("Success");
            }
            finalJob.setStatus("");
            discoveryJobRepository.save(finalJob);
        });

        return ResponseEntity.ok(job.getId());
    }

    @GetMapping("/getJobResult/{jobId}")
    public ResponseEntity<String> getJobResult(@PathVariable Long jobId) {
        return discoveryJobRepository.findById(jobId)
                .map(job -> ResponseEntity.ok(job.getStatus()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found"));
    }

    @GetMapping("/getDiscoveryResult/{service}")
    public ResponseEntity<?> getDiscoveryResult(@PathVariable String service) {
        if (service.equalsIgnoreCase("S3")) {
            return ResponseEntity.ok(s3BucketObjectRepository.findAll());
        } else if (service.equalsIgnoreCase("EC2")) {
            // Fetch EC2 instances from DB and return
        }
        return ResponseEntity.badRequest().body("Invalid service");
    }

    @PostMapping("/getS3BucketObjects")
    public ResponseEntity<Long> getS3BucketObjects(@RequestParam String bucketName) {
        DiscoveryJob job = new DiscoveryJob();
        job.setStatus("In Progress");
        job = discoveryJobRepository.save(job);
        Long jobId = job.getId();

        DiscoveryJob finalJob = job;
        CompletableFuture.runAsync(() -> {
            List<String> objects = awsService.listS3BucketObjects(bucketName);
            for (String objectName : objects) {
                S3BucketObject s3Object = new S3BucketObject();
                s3Object.setBucketName(bucketName);
                s3Object.setObjectName(objectName);
                s3BucketObjectRepository.save(s3Object);
            }
            finalJob.setStatus("Success");
            discoveryJobRepository.save(finalJob);
        });

        return ResponseEntity.ok(jobId);
    }

    @GetMapping("/getS3BucketObjectCount/{bucketName}")
    public ResponseEntity<Long> getS3BucketObjectCount(@PathVariable String bucketName) {
        long count = s3BucketObjectRepository.countByBucketName(bucketName);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/getS3BucketObjectLike")
    public ResponseEntity<List<String>> getS3BucketObjectLike(@RequestParam String bucketName, @RequestParam String pattern) {
        List<S3BucketObject> objects = s3BucketObjectRepository.findByBucketNameAndObjectNameLike(bucketName, "%" + pattern + "%");
        List<String> objectNames = objects.stream()
                .map(S3BucketObject::getObjectName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(objectNames);
    }

    private void saveBucketsToDatabase(List<String> buckets) {
        for (String bucketName : buckets) {
            List<String> objects = awsService.listS3BucketObjects(bucketName);
            for (String objectName : objects) {
                S3BucketObject s3BucketObject = new S3BucketObject();
                s3BucketObject.setBucketName(bucketName);
                s3BucketObject.setObjectName(objectName);
                s3BucketObjectRepository.save(s3BucketObject);
            }
        }
    }
}
