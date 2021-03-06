/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.core.elasticsearch.documents;

import com.netflix.genie.common.dto.Job;
import com.netflix.genie.common.dto.JobStatus;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Representation of the Genie 3.1 Job Document in elasticsearch.
 *
 * @author tgianos
 * @since 3.1.0
 */
@Document(indexName = "genie", type = "job")
public class JobDocument extends CommonDocument {

    private Set<String> tags = new HashSet<>();
    private String clusterName;
    private String commandName;
    private JobStatus status;
    private String statusMsg;
    private Date started = new Date(0);
    private Date finished = new Date(0);
    private String archiveLocation;

    /**
     * Gets the id of the cluster on which this job was run.
     *
     * @return executionClusterName
     */
    public String getClusterName() {
        return this.clusterName;
    }

    /**
     * Sets the id of the cluster on which this job is run.
     *
     * @param clusterName Name of the cluster on which job is executed.
     */
    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Gets the command id for this job.
     *
     * @return commandName
     */
    public String getCommandName() {
        return this.commandName;
    }

    /**
     * Set command Name with which this job is run.
     *
     * @param commandName Name of the command if specified on which the job is run
     */
    public void setCommandName(final String commandName) {
        this.commandName = commandName;
    }

    /**
     * Gets the status for this job.
     *
     * @return status
     * @see JobStatus
     */
    public JobStatus getStatus() {
        return this.status;
    }

    /**
     * Set the status for the job.
     *
     * @param status The new status
     */
    public void setStatus(final JobStatus status) {
        this.status = status;
    }

    /**
     * Gets the status message or this job.
     *
     * @return statusMsg
     */
    public String getStatusMsg() {
        return this.statusMsg;
    }

    /**
     * Set the status message for the job.
     *
     * @param statusMsg The status message.
     */
    public void setStatusMsg(final String statusMsg) {
        this.statusMsg = statusMsg;
    }

    /**
     * Gets the start time for this job.
     *
     * @return startTime : start time in ms
     */
    public Date getStarted() {
        return new Date(this.started.getTime());
    }

    /**
     * Set the startTime for the job.
     *
     * @param started epoch time in ms
     */
    public void setStarted(final Date started) {
        this.started = new Date(started.getTime());
    }

    /**
     * Gets the finish time for this job.
     *
     * @return finished. The job finish timestamp.
     */
    public Date getFinished() {
        return new Date(this.finished.getTime());
    }

    /**
     * Set the finishTime for the job.
     *
     * @param finished The finished time.
     */
    public void setFinished(final Date finished) {
        this.finished = new Date(finished.getTime());
    }

    /**
     * Get location where logs are archived.
     *
     * @return s3/hdfs location where logs are archived
     */
    public String getArchiveLocation() {
        return this.archiveLocation;
    }

    /**
     * Set location where logs are archived.
     *
     * @param archiveLocation s3/hdfs location where logs are archived
     */
    public void setArchiveLocation(final String archiveLocation) {
        this.archiveLocation = archiveLocation;
    }

    /**
     * Set job status, and update start/update/finish times, if needed.
     *
     * @param jobStatus status for job
     */
    public void setJobStatus(final JobStatus jobStatus) {
        this.status = jobStatus;

        if (jobStatus == JobStatus.INIT) {
            this.setStarted(new Date());
        } else if (jobStatus == JobStatus.SUCCEEDED
                || jobStatus == JobStatus.KILLED
                || jobStatus == JobStatus.FAILED) {
            setFinished(new Date());
        }
    }

    /**
     * Sets job status and human-readable message.
     *
     * @param newStatus predefined status
     * @param msg       human-readable message
     */
    public void setJobStatus(final JobStatus newStatus, final String msg) {
        setJobStatus(newStatus);
        setStatusMsg(msg);
    }

    /**
     * Gets the tags allocated to this job.
     *
     * @return the tags
     */
    public Set<String> getTags() {
        return this.tags;
    }

    /**
     * Sets the tags allocated to this job.
     *
     * @param tags the tags to set. Not Null.
     */
    public void setTags(final Set<String> tags) {
        this.tags = tags;
    }

    /**
     * Get a DTO representing this job.
     *
     * @return The read-only DTO.
     */
    public Job getDTO() {
        return new Job.Builder(
                this.getName(),
                this.getUser(),
                this.getVersion()
        )
                .withId(this.getId())
                .withCreated(this.getCreated())
                .withDescription(this.getDescription())
                .withTags(this.tags)
                .withUpdated(this.getUpdated())
                .withArchiveLocation(this.archiveLocation)
                .withCommandName(this.commandName)
                .withClusterName(this.clusterName)
                .withFinished(this.finished)
                .withStarted(this.started)
                .withStatus(this.status)
                .withStatusMsg(this.statusMsg)
                .build();
    }
}
