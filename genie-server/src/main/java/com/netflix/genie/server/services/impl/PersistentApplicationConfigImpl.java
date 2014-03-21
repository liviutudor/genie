package com.netflix.genie.server.services.impl;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.UUID;

import javax.persistence.EntityExistsException;
import javax.persistence.RollbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.genie.common.exceptions.CloudServiceException;
import com.netflix.genie.common.messages.ApplicationConfigRequest;
import com.netflix.genie.common.messages.ApplicationConfigResponse;
import com.netflix.genie.common.model.ApplicationConfigElement;
import com.netflix.genie.common.model.Types;
import com.netflix.genie.server.persistence.ClauseBuilder;
import com.netflix.genie.server.persistence.PersistenceManager;
import com.netflix.genie.server.persistence.QueryBuilder;
import com.netflix.genie.server.services.ApplicationConfigService;

/**
 * OpenJPA based implementation of the ApplicationConfigService.
 * @author amsharma
 */
public class PersistentApplicationConfigImpl implements
        ApplicationConfigService {

    private static Logger logger = LoggerFactory
            .getLogger(PersistentApplicationConfigImpl.class);
    
    private PersistenceManager<ApplicationConfigElement> pm;
    
    /**
     * Default constructor.
     */
    public PersistentApplicationConfigImpl() {
        // instantiate PersistenceManager
        pm = new PersistenceManager<ApplicationConfigElement>();
    }
    
    /** {@inheritDoc} */
    @Override
    public ApplicationConfigResponse getApplicationConfig(String id) {
        logger.info("called");
        return getApplicationConfig(id, null);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationConfigResponse getApplicationConfig(String id,
            String name) {
    
        logger.info("called");
        ApplicationConfigResponse acr = null;

        try {
            acr = new ApplicationConfigResponse();
            Object[] results;

            if ((id == null) && (name == null)) {
                // return all
                logger.info("GENIE: Returning all applicationConfig elements");

                // Perform a simple query for all the entities
                QueryBuilder builder = new QueryBuilder()
                        .table("ApplicationConfigElement");
                results = pm.query(builder);

                // set up a specific message
                acr.setMessage("Returning all applicationConfig elements");
            } else {
                // do some filtering
                logger.info("GENIE: Returning config for {id, name}: "
                        + "{" + id + ", " + name + "}");

                // construct query
                ClauseBuilder criteria = new ClauseBuilder(ClauseBuilder.AND);
                if (id != null) {
                    criteria.append("id = '" + id + "'");
                }
                if (name != null) {
                    criteria.append("name = '" + name + "'");
                }

                // Get all the results as an array
                QueryBuilder builder = new QueryBuilder().table(
                        "ApplicationConfigElement").clause(criteria.toString());
                results = pm.query(builder);
            }

            if (results.length == 0) {
                acr = new ApplicationConfigResponse(new CloudServiceException(
                        HttpURLConnection.HTTP_NOT_FOUND,
                        "No applicationConfigs found for input parameters"));
                logger.error(acr.getErrorMsg());
                return acr;
            } else {
                acr.setMessage("Returning applicationConfigs for input parameters");
            }

            ApplicationConfigElement[] elements = new ApplicationConfigElement[results.length];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = (ApplicationConfigElement) results[i];
            }
            acr.setApplicationConfigs(elements);
            return acr;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            acr = new ApplicationConfigResponse(new CloudServiceException(
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    "Received exception: " + e.getMessage()));
            return acr;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationConfigResponse createApplicationConfig(
            ApplicationConfigRequest request) {
        logger.info("called");
        return createUpdateConfig(request, Verb.POST);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationConfigResponse updateApplicationConfig(
            ApplicationConfigRequest request) {
        logger.info("called");
        return createUpdateConfig(request, Verb.PUT);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationConfigResponse deleteApplicationConfig(String id) {

        logger.info("called");
        ApplicationConfigResponse acr = null;

        if (id == null) {
            // basic error checking
            acr = new ApplicationConfigResponse(new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "Missing required parameter: id"));
            logger.error(acr.getErrorMsg());
            return acr;
        } else {
            logger.info("GENIE: Deleting applicationConfig for id: " + id);

            try {
                // delete the entity
                ApplicationConfigElement element = pm.deleteEntity(id,
                        ApplicationConfigElement.class);

                if (element == null) {
                    acr = new ApplicationConfigResponse(new CloudServiceException(
                            HttpURLConnection.HTTP_NOT_FOUND,
                            "No applicationConfig exists for id: " + id));
                    logger.error(acr.getErrorMsg());
                    return acr;
                } else {
                    // all good - create a response
                    acr = new ApplicationConfigResponse();
                    acr.setMessage("Successfully deleted applicationConfig for id: "
                            + id);
                    ApplicationConfigElement[] elements = new ApplicationConfigElement[] {element};
                    acr.setApplicationConfigs(elements);
                    return acr;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                acr = new ApplicationConfigResponse(new CloudServiceException(
                        HttpURLConnection.HTTP_INTERNAL_ERROR,
                        "Received exception: " + e.getMessage()));
                return acr;
            }
        }
    }
    
    /**
     * Common private method called by the create and update Can use either
     * method to create/update resource.
     */
    private ApplicationConfigResponse createUpdateConfig(ApplicationConfigRequest request,
            Verb method) {
        logger.info("called");
        ApplicationConfigResponse acr = null;
        ApplicationConfigElement applicationConfigElement = request.getApplicationConfig();
        
        // ensure that the element is not null
        if (applicationConfigElement == null) {
            acr = new ApplicationConfigResponse(new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "Missing applicationConfig object"));
            logger.error(acr.getErrorMsg());
            return acr;
        }

        // generate/validate id for request
        String id = applicationConfigElement.getId();
        if (id == null) {
            if (method.equals(Verb.POST)) {
                id = UUID.randomUUID().toString();
                applicationConfigElement.setId(id);
            } else {
                acr = new ApplicationConfigResponse(new CloudServiceException(
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        "Missing required parameter for PUT: id"));
                logger.error(acr.getErrorMsg());
                return acr;
            }
        }

        // basic error checking
        String user = applicationConfigElement.getUser();
        if (user == null) {
            acr = new ApplicationConfigResponse(new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "Need required param 'user' for create/update"));
            logger.error(acr.getErrorMsg());
            return acr;
        }

        // ensure that the status object is valid
        String status = applicationConfigElement.getStatus();
        if ((status != null) && (Types.ConfigStatus.parse(status) == null)) {
            acr = new ApplicationConfigResponse(new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "Config status can only be ACTIVE, DEPRECATED, INACTIVE"));
            logger.error(acr.getErrorMsg());
            return acr;
        }

        // common error checks done - set update time before proceeding
        applicationConfigElement.setUpdateTime(System.currentTimeMillis());

        // handle POST and PUT differently
        if (method.equals(Verb.POST)) {
            try {
                initAndValidateNewElement(applicationConfigElement);
            } catch (CloudServiceException e) {
                acr = new ApplicationConfigResponse(e);
                logger.error(acr.getErrorMsg(), e);
                return acr;

            }

            logger.info("GENIE: creating config for id: " + id);
            try {
                pm.createEntity(applicationConfigElement);

                // create a response
                acr = new ApplicationConfigResponse();
                acr.setMessage("Successfully created applicationConfig for id: " + id);
                acr.setApplicationConfigs(new ApplicationConfigElement[] {applicationConfigElement});
                return acr;
            } catch (RollbackException e) {
                logger.error(e.getMessage(), e);
                if (e.getCause() instanceof EntityExistsException) {
                    // most likely entity already exists - return useful message
                    acr = new ApplicationConfigResponse(new CloudServiceException(
                            HttpURLConnection.HTTP_CONFLICT,
                            "ApplicationConfig already exists for id: " + id
                                    + ", use PUT to update config"));
                    return acr;
                } else {
                    // unknown exception - send it back
                    acr = new ApplicationConfigResponse(new CloudServiceException(
                            HttpURLConnection.HTTP_INTERNAL_ERROR,
                            "Received exception: " + e.getCause()));
                    logger.error(acr.getErrorMsg());
                    return acr;
                }
            }
        } else {
            // method is PUT
            logger.info("GENIE: updating config for id: " + id);

            try {
                ApplicationConfigElement old = pm.getEntity(id,
                        ApplicationConfigElement.class);
                // check if this is a create or an update
                if (old == null) {
                    try {
                        initAndValidateNewElement(applicationConfigElement);
                    } catch (CloudServiceException e) {
                        acr = new ApplicationConfigResponse(e);
                        logger.error(acr.getErrorMsg(), e);
                        return acr;
                    }
                }
                applicationConfigElement = pm.updateEntity(applicationConfigElement);

                // all good - create a response
                acr = new ApplicationConfigResponse();
                acr.setMessage("Successfully updated applicationConfig for id: " + id);
                acr.setApplicationConfigs(new ApplicationConfigElement[] {applicationConfigElement});
                return acr;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                // unknown exception - send it back
                acr = new ApplicationConfigResponse(new CloudServiceException(
                        HttpURLConnection.HTTP_INTERNAL_ERROR,
                        "Received exception: " + e.getCause()));
                return acr;
            }
        }
    }

    /**
     * Initialize and validate new element.
     *
     * @param applicationConfigElement
     *            the element to initialize
     * @throws CloudServiceException
     *             if some params are missing - else initialize, and set
     *             creation time
     */
    private void initAndValidateNewElement(ApplicationConfigElement applicationConfigElement)
            throws CloudServiceException {

        // basic error checking
        String name = applicationConfigElement.getName();
        //ArrayList<String> configs = applicationConfigElement.getConfigs();
        //ArrayList<String> jars = applicationConfigElement.getJars();
        
        //TODO Should we allow configs and jars to be null?
        if ((name == null)) {
            throw new CloudServiceException(HttpURLConnection.HTTP_BAD_REQUEST,
                    "Need all required params: {name}");
        } else {
            applicationConfigElement.setCreateTime(applicationConfigElement.getUpdateTime());
        }
    }
}