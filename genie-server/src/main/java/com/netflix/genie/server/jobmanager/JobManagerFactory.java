/*
 *
 *  Copyright 2014 Netflix, Inc.
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
package com.netflix.genie.server.jobmanager;

import com.netflix.config.ConfigurationManager;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.model.Cluster;
import com.netflix.genie.common.model.Job;
import com.netflix.genie.server.services.ClusterConfigService;
import com.netflix.genie.server.services.ClusterLoadBalancer;
import java.net.HttpURLConnection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory class to instantiate individual job managers.
 *
 * @author skrishnan
 * @author tgianos
 * @author amsharma
 */
@Named
public final class JobManagerFactory implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(JobManagerFactory.class);

    private ApplicationContext context;

    /**
     * The service to discover clusters.
     */
    private final ClusterConfigService ccs;

    /**
     * Reference to the cluster load balancer implementation.
     */
    private final ClusterLoadBalancer clb;

    /**
     * Default constructor.
     *
     * @param ccs The cluster config service to use
     * @param clb The clb to use
     * @throws GenieException
     */
    @Inject
    public JobManagerFactory(
            final ClusterConfigService ccs,
            final ClusterLoadBalancer clb) throws GenieException {
        this.ccs = ccs;
        this.clb = clb;
    }

    /**
     * Returns the right job manager for the job type.
     *
     * @param job The job this manager will be managing
     * @return instance of the appropriate job manager
     * @throws GenieException
     */
    public JobManager getJobManager(final Job job) throws GenieException {
        LOG.info("called");

        // Figure out a cluster to run this job. Cluster selection is done based on 
        // ClusterCriteria tags and Command tags specified in the job.
        final Cluster cluster = this.clb.selectCluster(this.ccs.getClusters(job)) ;
        final String className = ConfigurationManager.getConfigInstance()
                .getString("netflix.genie.server." + cluster.getClusterType() + ".JobManagerImpl");

        try {
            final Class jobManagerClass = Class.forName(className);
            final Object instance = this.context.getBean(jobManagerClass);
            if (instance instanceof JobManager) {
                final JobManager jobManager = (JobManager) instance;
                jobManager.setCluster(cluster);
                return jobManager;
            } else {
                final String msg = className + " is not of type JobManager. Unable to continue.";
                LOG.error(msg);
                throw new GenieException(HttpURLConnection.HTTP_BAD_REQUEST, msg);
            }
        } catch (final ClassNotFoundException e) {
            final String msg = "Unable to create job manager for class name " + className;
            LOG.error(msg, e);
            throw new GenieException(HttpURLConnection.HTTP_BAD_REQUEST, msg);
        } catch (final BeansException e) {
            final String msg = "Unable to create job manager for class name " + className;
            LOG.error(msg, e);
            throw new GenieException(HttpURLConnection.HTTP_BAD_REQUEST, msg);
        }
    }

    /**
=======
     * Figure out an appropriate cluster to run this job<br>
     * Cluster selection is done based on tags, command and application.
     *
     * @param job job info for this job
     * @return cluster element to use for running this job
     * @throws GenieException if there is any error finding a cluster for
     * this job
     */
    private Cluster getCluster(final Job job) throws GenieException {
        LOG.info("called");

        if (job == null) {
            final String msg = "No job entered. Unable to continue";
            LOG.error(msg);
            throw new GenieException(HttpURLConnection.HTTP_BAD_REQUEST, msg);
        }

        final List<Cluster> clusters = this.ccs.getClusters(job);

        // return selected instance
        return this.clb.selectCluster(clusters);
    }

    /**
     * Set the spring application context for use creating beans.
     *
     * @param appContext The application context injected by spring
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(
            final ApplicationContext appContext) throws BeansException {
        this.context = appContext;
    }
}