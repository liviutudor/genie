/*
 * Copyright 2015 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package com.netflix.genie.core.jpa.specifications;

import com.netflix.genie.common.dto.JobStatus;
import com.netflix.genie.core.jpa.entities.JobEntity;
import com.netflix.genie.core.jpa.entities.JobEntity_;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Specifications for JPA queries.
 *
 * @author tgianos
 * @see <a href="http://tinyurl.com/n6nubvm">Docs</a>
 */
public final class JpaJobSpecs {

    private static final Logger LOG = LoggerFactory.getLogger(JpaJobSpecs.class);

    /**
     * Protected constructor for utility class.
     */
    protected JpaJobSpecs() {
    }

    /**
     * Find jobs based on the parameters.
     *
     * @param id              The job id
     * @param jobName         The job name
     * @param userName        The user who created the job
     * @param statuses        The job statuses
     * @param tags            The tags for the jobs to find
     * @param clusterName     The cluster name
     * @param clusterId       The cluster id
     * @param commandName     The command name
     * @param commandId       The command id
     * @return The specification
     */
    public static Specification<JobEntity> find(
            final String id,
            final String jobName,
            final String userName,
            final Set<JobStatus> statuses,
            final Set<String> tags,
            final String clusterName,
            final String clusterId,
            final String commandName,
            final String commandId
    ) {
        return (final Root<JobEntity> root, final CriteriaQuery<?> cq, final CriteriaBuilder cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(id)) {
                predicates.add(cb.like(root.get(JobEntity_.id), id));
            }
            if (StringUtils.isNotBlank(jobName)) {
                predicates.add(cb.like(root.get(JobEntity_.name), jobName));
            }
            if (StringUtils.isNotBlank(userName)) {
                predicates.add(cb.equal(root.get(JobEntity_.user), userName));
            }
            if (statuses != null && !statuses.isEmpty()) {
                final List<Predicate> orPredicates =
                        statuses
                                .stream()
                                .map(status -> cb.equal(root.get(JobEntity_.status), status))
                                .collect(Collectors.toList());
                predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));
            }
            if (tags != null && !tags.isEmpty()) {
                predicates.add(cb.like(root.get(JobEntity_.sortedTags), JpaSpecificationUtils.getTagLikeString(tags)));
            }
            if (StringUtils.isNotBlank(clusterId)) {
                predicates.add(cb.equal(root.get(JobEntity_.cluster), clusterId));
            }
            if (StringUtils.isNotBlank(clusterName)) {
                predicates.add(cb.equal(root.get(JobEntity_.clusterName), clusterName));
            }
            if (StringUtils.isNotBlank(commandId)) {
                predicates.add(cb.equal(root.get(JobEntity_.command), commandId));
            }
            if (StringUtils.isNotBlank(commandName)) {
                predicates.add(cb.equal(root.get(JobEntity_.commandName), commandName));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    /**
     * Find jobs that are zombies.
     *
     * @param currentTime The current time
     * @param zombieTime  The time that zombies should be marked by
     * @return The specification for this query
     */
    public static Specification<JobEntity> findZombies(final long currentTime, final long zombieTime) {
        return (final Root<JobEntity> root, final CriteriaQuery<?> cq, final CriteriaBuilder cb) -> {
            // the equivalent query is as follows:
            // update Job set status='FAILED', finishTime=$max, exitCode=$zombie_code,
            // statusMsg='Job has been marked as a zombie'
            // where updateTime < $min and (status='RUNNING' or status='INIT')"
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.lessThan(root.get(JobEntity_.updated), new Date(currentTime - zombieTime)));
            final Predicate orPredicate1 = cb.equal(root.get(JobEntity_.status), JobStatus.RUNNING);
            final Predicate orPredicate2 = cb.equal(root.get(JobEntity_.status), JobStatus.INIT);
            predicates.add(cb.or(orPredicate1, orPredicate2));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
