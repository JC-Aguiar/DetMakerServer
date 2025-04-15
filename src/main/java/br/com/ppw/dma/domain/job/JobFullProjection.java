package br.com.ppw.dma.domain.job;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;

public interface JobFullProjection {

    Job getJob();
    List<JobParameter> getParametros();
    List<JobResource> getCargas();
    List<JobResource> getLogs();
    List<JobResource> getRemessas();

//    Job getJob();
//    JobResource getResource();
//    JobParameter getParameter();


//    public static Set<Job> mapearPares(List<JobFullProjection> projectionList) {
//        var jobs = projectionList.stream().map(JobFullProjection::getJob).collect(toSet());
//        System.out.println("Mapeando parâmetros.");
//        var mapJobParameters = projectionList.stream()
//            .collect(groupingBy(
//                JobFullProjection::getJob,
//                mapping(JobFullProjection::getParameter, toList())
//            ));
//        System.out.println(mapJobParameters.values());
//        System.out.println();
//
//        System.out.println("Mapeando recursos.");
//        var mapJobResources = projectionList.stream().collect(groupingBy(
//            JobFullProjection::getJob,
//            mapping(JobFullProjection::getResource, toList())
//        ));
//        System.out.println(mapJobResources.values());
//
//        jobs.forEach(job -> {
//            job.getParametros().addAll(mapJobParameters.get(job));
//            mapJobResources.get(job).forEach(resource -> {
//                switch (resource.getTipo()) {
//                    case CARGA ->   job.getCargas().add(resource);
//                    case LOG ->     job.getLogs().add(resource);
//                    case REMESSA -> job.getRemessas().add(resource);
//                }
//            });
//        });
//        return jobs;
//    }

}
