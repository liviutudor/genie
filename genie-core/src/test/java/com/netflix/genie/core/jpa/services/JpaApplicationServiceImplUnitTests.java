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
package com.netflix.genie.core.jpa.services;

import com.google.common.collect.Lists;
import com.netflix.genie.test.categories.UnitTest;
import com.netflix.genie.common.dto.Application;
import com.netflix.genie.common.dto.ApplicationStatus;
import com.netflix.genie.common.exceptions.GenieBadRequestException;
import com.netflix.genie.common.exceptions.GenieConflictException;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GenieNotFoundException;
import com.netflix.genie.common.exceptions.GeniePreconditionException;
import com.netflix.genie.core.jpa.entities.ApplicationEntity;
import com.netflix.genie.core.jpa.entities.CommandEntity;
import com.netflix.genie.core.jpa.repositories.JpaApplicationRepository;
import com.netflix.genie.core.jpa.repositories.JpaCommandRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import java.util.HashSet;
import java.util.UUID;

/**
 * Tests for the ApplicationServiceJPAImpl.
 *
 * @author tgianos
 */
@Category(UnitTest.class)
public class JpaApplicationServiceImplUnitTests {

    private static final String APP_1_ID = "app1";
    private static final String APP_1_NAME = "tez";
    private static final String APP_1_USER = "tgianos";
    private static final String APP_1_VERSION = "1.2.3";

    private JpaApplicationRepository jpaApplicationRepository;
    private JpaApplicationServiceImpl appService;

    /**
     * Setup the tests.
     */
    @Before
    public void setup() {
        this.jpaApplicationRepository = Mockito.mock(JpaApplicationRepository.class);
        final JpaCommandRepository jpaCommandRepository = Mockito.mock(JpaCommandRepository.class);
        this.appService = new JpaApplicationServiceImpl(this.jpaApplicationRepository, jpaCommandRepository);
    }

    /**
     * Test the get application method.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testGetApplication() throws GenieException {
    }

    /**
     * Test the get application method.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testGetApplicationNotExists() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(Mockito.eq(id))).thenReturn(null);
        this.appService.getApplication(UUID.randomUUID().toString());
    }

    /**
     * Test the create method.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testCreateApplication() throws GenieException {
    }

    /**
     * Test the create method when no id is entered.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testCreateApplicationNoId() throws GenieException {
    }

    /**
     * Test to make sure an exception is thrown when application already exists.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieConflictException.class)
    public void testCreateApplicationAlreadyExists() throws GenieException {
        final Application app = new Application.Builder(
                APP_1_NAME, APP_1_USER, APP_1_VERSION, ApplicationStatus.ACTIVE
        )
                .withId(APP_1_ID)
                .build();
        Mockito.when(this.jpaApplicationRepository.exists(Mockito.eq(APP_1_ID))).thenReturn(true);
        this.appService.createApplication(app);
    }

    /**
     * Test to update an application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testUpdateApplication() throws GenieException {
    }

    /**
     * Test to update an application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testUpdateApplicationNoAppExists() throws GenieException {
        final Application app = new Application.Builder(
                APP_1_NAME, APP_1_USER, APP_1_VERSION, ApplicationStatus.ACTIVE
        )
                .withId(APP_1_ID)
                .build();
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(Mockito.eq(id))).thenReturn(null);
        this.appService.updateApplication(id, app);
    }

    /**
     * Test to update an application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieBadRequestException.class)
    public void testUpdateApplicationIdsDontMatch() throws GenieException {
        final String id = UUID.randomUUID().toString();
        final Application app = new Application.Builder(
                APP_1_NAME, APP_1_USER, APP_1_VERSION, ApplicationStatus.ACTIVE
        )
                .withId(UUID.randomUUID().toString())
                .build();
        Mockito.when(this.jpaApplicationRepository.exists(id)).thenReturn(true);
        this.appService.updateApplication(id, app);
    }

    /**
     * Test delete all.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testDeleteAll() throws GenieException {
    }

    /**
     * Test delete all when still in a relationship with a command.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GeniePreconditionException.class)
    public void testDeleteAllBlocked() throws GenieException {
        final ApplicationEntity applicationEntity
                = Mockito.mock(ApplicationEntity.class);
        final CommandEntity commandEntity = Mockito.mock(CommandEntity.class);
        Mockito.when(this.jpaApplicationRepository.findAll()).thenReturn(Lists.newArrayList(applicationEntity));
        Mockito.when(applicationEntity.getCommands()).thenReturn(Sets.newSet(commandEntity));
        this.appService.deleteAllApplications();
    }

    /**
     * Test delete.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testDelete() throws GenieException {
    }

    /**
     * Test to make sure delete is blocked if the application is still linked to a command.
     *
     * @throws GenieException Due to the application still being linked to a command in the database
     */
    @Test(expected = GeniePreconditionException.class)
    @Ignore
    public void testDeleteBlocked() throws GenieException {
        final ApplicationEntity applicationEntity
                = Mockito.mock(ApplicationEntity.class);
        final String id = UUID.randomUUID().toString();
        final CommandEntity commandEntity = Mockito.mock(CommandEntity.class);
        Mockito.when(this.jpaApplicationRepository.findOne(Mockito.eq(id))).thenReturn(applicationEntity);
        Mockito.when(applicationEntity.getCommands()).thenReturn(Sets.newSet(commandEntity));
        this.appService.deleteApplication(id);
    }

    /**
     * Test delete.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieException.class)
    public void testDeleteNoAppToDelete() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(Mockito.eq(id))).thenReturn(null);
        this.appService.deleteApplication(id);
    }

    /**
     * Test add configurations to application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testAddConfigsToApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.getOne(Mockito.eq(id))).thenReturn(null);
        this.appService.addConfigsToApplication(id, new HashSet<>());
    }

    /**
     * Test update configurations for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testUpdateConfigsForApplication() throws GenieException {
    }

    /**
     * Test update configurations for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testUpdateConfigsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(Mockito.eq(id))).thenReturn(null);
        this.appService.updateConfigsForApplication(id, new HashSet<>());
    }

    /**
     * Test get configurations for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testGetConfigsForApplication() throws GenieException {
    }

    /**
     * Test get configurations to application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testGetConfigsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.getConfigsForApplication(id);
    }

    /**
     * Test remove all configurations for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testRemoveAllConfigsForApplication() throws GenieException {
    }

    /**
     * Test remove all configurations for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testRemoveAllConfigsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.removeAllConfigsForApplication(id);
    }

    /**
     * Test remove configuration for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testRemoveConfigForApplication() throws GenieException {
    }

    /**
     * Test remove configuration for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testRemoveConfigForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.removeConfigForApplication(id, "something");
    }

    /**
     * Test add jars to application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testAddJarsToApplication() throws GenieException {
    }

    /**
     * Test add jars to application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testAddJarsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.addDependenciesForApplication(id, new HashSet<>());
    }

    /**
     * Test update jars for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testUpdateJarsForApplication() throws GenieException {
    }

    /**
     * Test update jars for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testUpdateJarsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.updateDependenciesForApplication(id, new HashSet<>());
    }

    /**
     * Test get jars for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testGetJarsForApplication() throws GenieException {
    }

    /**
     * Test get jars to application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testGetJarsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.getDependenciesForApplication(id);
    }

    /**
     * Test remove all jars for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testRemoveAllJarsForApplication() throws GenieException {
    }

    /**
     * Test remove all jars for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testRemoveAllJarsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.removeAllDependenciesForApplication(id);
    }

    /**
     * Test remove configuration for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testRemoveJarForApplication() throws GenieException {
    }

    /**
     * Test remove configuration for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testRemoveJarForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.removeDependencyForApplication(id, "something");
    }

    /**
     * Test add tags to application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testAddTagsToApplication() throws GenieException {
    }

    /**
     * Test add tags to application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testAddTagsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.addTagsForApplication(id, new HashSet<>());
    }

    /**
     * Test update tags for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testUpdateTagsForApplication() throws GenieException {
    }

    /**
     * Test update tags for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testUpdateTagsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.updateTagsForApplication(id, new HashSet<>());
    }

    /**
     * Test get tags for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testGetTagsForApplication() throws GenieException {
    }

    /**
     * Test get tags to application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testGetTagsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.getTagsForApplication(id);
    }

    /**
     * Test remove all tags for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testRemoveAllTagsForApplication() throws GenieException {
    }

    /**
     * Test remove all tags for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testRemoveAllTagsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.removeAllTagsForApplication(id);
    }

    /**
     * Test remove tag for application.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testRemoveTagForApplication() throws GenieException {
    }

    /**
     * Test remove configuration for application.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testRemoveTagForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.removeTagForApplication(id, "something");
    }

    /**
     * Test the Get commands for application method.
     *
     * @throws GenieException For any problem
     */
    @Test
    @Ignore
    public void testGetCommandsForApplication() throws GenieException {
    }

    /**
     * Test the Get commands for application method.
     *
     * @throws GenieException For any problem
     */
    @Test(expected = GenieNotFoundException.class)
    public void testGetCommandsForApplicationNoApp() throws GenieException {
        final String id = UUID.randomUUID().toString();
        Mockito.when(this.jpaApplicationRepository.findOne(id)).thenReturn(null);
        this.appService.getCommandsForApplication(id, null);
    }
}
