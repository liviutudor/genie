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
package com.netflix.genie.core.jpa.entities;

import com.netflix.genie.test.categories.UnitTest;
import com.netflix.genie.common.dto.ClusterStatus;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GeniePreconditionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test the Cluster class.
 *
 * @author tgianos
 */
@Category(UnitTest.class)
public class ClusterEntityUnitTests extends EntityTestsBase {

    private static final String NAME = "h2prod";
    private static final String USER = "tgianos";
    private static final String CONFIG = "s3://netflix/clusters/configs/config1";
    private static final String CLUSTER_TYPE = "Hadoop";
    private static final String VERSION = "1.2.3";

    private ClusterEntity c;
    private Set<String> configs;

    /**
     * Setup the tests.
     */
    @Before
    public void setup() {
        this.c = new ClusterEntity();
        this.configs = new HashSet<>();
        this.configs.add(CONFIG);
    }

    /**
     * Test the default Constructor.
     */
    @Test
    public void testDefaultConstructor() {
        Assert.assertNull(this.c.getClusterType());
        Assert.assertNull(this.c.getName());
        Assert.assertNull(this.c.getStatus());
        Assert.assertNull(this.c.getUser());
        Assert.assertNull(this.c.getVersion());
        Assert.assertNotNull(this.c.getConfigs());
        Assert.assertTrue(this.c.getConfigs().isEmpty());
        Assert.assertNotNull(this.c.getTags());
        Assert.assertTrue(this.c.getTags().isEmpty());
        Assert.assertNotNull(this.c.getCommands());
        Assert.assertTrue(this.c.getCommands().isEmpty());
    }

    /**
     * Test the argument Constructor.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test
    public void testConstructor() throws GeniePreconditionException {
        this.c = new ClusterEntity(NAME, USER, VERSION, ClusterStatus.UP, CLUSTER_TYPE);
        Assert.assertEquals(CLUSTER_TYPE, this.c.getClusterType());
        Assert.assertEquals(NAME, this.c.getName());
        Assert.assertEquals(ClusterStatus.UP, this.c.getStatus());
        Assert.assertEquals(USER, this.c.getUser());
        Assert.assertEquals(VERSION, this.c.getVersion());
        Assert.assertNotNull(this.c.getConfigs());
        Assert.assertTrue(this.c.getConfigs().isEmpty());
        Assert.assertNotNull(this.c.getTags());
        Assert.assertTrue(this.c.getTags().isEmpty());
        Assert.assertNotNull(this.c.getCommands());
        Assert.assertTrue(this.c.getCommands().isEmpty());
    }

    /**
     * Test to make sure validation works.
     *
     * @throws GenieException If any precondition isn't met.
     */
    @Test
    public void testOnCreateOrUpdateCluster() throws GenieException {
        this.c = new ClusterEntity(NAME, USER, VERSION, ClusterStatus.UP, CLUSTER_TYPE);
        Assert.assertNotNull(this.c.getTags());
        Assert.assertTrue(this.c.getTags().isEmpty());
        this.c.onCreateOrUpdateCluster();
        Assert.assertEquals(2, this.c.getTags().size());
    }

    /**
     * Make sure validation works on valid apps.
     */
    @Test
    public void testValidate() {
        this.c = new ClusterEntity(NAME, USER, VERSION, ClusterStatus.UP, CLUSTER_TYPE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from super class.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoName() {
        this.c = new ClusterEntity(null, USER, VERSION, ClusterStatus.UP, CLUSTER_TYPE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from super class.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoUser() {
        this.c = new ClusterEntity(NAME, " ", VERSION, ClusterStatus.UP, CLUSTER_TYPE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from super class.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoVersion() {
        this.c = new ClusterEntity(NAME, USER, "", ClusterStatus.UP, CLUSTER_TYPE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from cluster.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoStatus() {
        this.c = new ClusterEntity(NAME, USER, VERSION, null, CLUSTER_TYPE);
        this.validate(this.c);
    }

    /**
     * Make sure validation works on with failure from cluster.
     */
    @Test(expected = ConstraintViolationException.class)
    public void testValidateNoClusterType() {
        this.c = new ClusterEntity(NAME, USER, VERSION, ClusterStatus.UP, null);
        this.validate(this.c);
    }

    /**
     * Test setting the status.
     */
    @Test
    public void testSetStatus() {
        Assert.assertNull(this.c.getStatus());
        this.c.setStatus(ClusterStatus.TERMINATED);
        Assert.assertEquals(ClusterStatus.TERMINATED, this.c.getStatus());
    }

    /**
     * Test setting the cluster type.
     */
    @Test
    public void testSetClusterType() {
        Assert.assertNull(this.c.getClusterType());
        this.c.setClusterType(CLUSTER_TYPE);
        Assert.assertEquals(CLUSTER_TYPE, this.c.getClusterType());
    }

    /**
     * Test setting the tags.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test
    public void testSetTags() throws GeniePreconditionException {
        Assert.assertNotNull(this.c.getTags());
        final Set<String> tags = new HashSet<>();
        tags.add("prod");
        tags.add("sla");
        this.c.setTags(tags);
        Assert.assertEquals(tags, this.c.getTags());
    }

    /**
     * Test setting the configs.
     */
    @Test
    public void testSetConfigs() {
        Assert.assertNotNull(this.c.getConfigs());
        this.c.setConfigs(this.configs);
        Assert.assertEquals(this.configs, this.c.getConfigs());
    }

    /**
     * Test setting the commands.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test
    public void testSetCommands() throws GeniePreconditionException {
        Assert.assertNotNull(this.c.getCommands());
        Assert.assertTrue(this.c.getCommands().isEmpty());
        final CommandEntity one = new CommandEntity();
        one.setId("one");
        final CommandEntity two = new CommandEntity();
        two.setId("two");
        final List<CommandEntity> commands = new ArrayList<>();
        commands.add(one);
        commands.add(two);
        this.c.setCommands(commands);
        Assert.assertEquals(commands, this.c.getCommands());
        Assert.assertTrue(one.getClusters().contains(this.c));
        Assert.assertTrue(two.getClusters().contains(this.c));
        this.c.setCommands(null);
        Assert.assertNull(this.c.getCommands());
        Assert.assertFalse(one.getClusters().contains(this.c));
        Assert.assertFalse(two.getClusters().contains(this.c));
    }

    /**
     * Test adding a command.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test
    public void testAddCommand() throws GeniePreconditionException {
        final CommandEntity commandEntity = new CommandEntity();
        commandEntity.setId("commandId");
        Assert.assertNotNull(this.c.getCommands());
        Assert.assertTrue(this.c.getCommands().isEmpty());
        this.c.addCommand(commandEntity);
        Assert.assertTrue(this.c.getCommands().contains(commandEntity));
        Assert.assertTrue(commandEntity.getClusters().contains(this.c));
    }

    /**
     * Test to make sure you can't add a null command.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test(expected = GeniePreconditionException.class)
    public void testAddNullCommand() throws GeniePreconditionException {
        this.c.addCommand(null);
    }

    /**
     * Test removing a command.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test
    public void testRemoveCommand() throws GeniePreconditionException {
        final CommandEntity one = new CommandEntity();
        one.setId("one");
        final CommandEntity two = new CommandEntity();
        two.setId("two");
        Assert.assertNotNull(this.c.getCommands());
        Assert.assertTrue(this.c.getCommands().isEmpty());
        this.c.addCommand(one);
        Assert.assertTrue(this.c.getCommands().contains(one));
        Assert.assertFalse(this.c.getCommands().contains(two));
        Assert.assertTrue(one.getClusters().contains(this.c));
        Assert.assertNotNull(two.getClusters());
        Assert.assertTrue(two.getClusters().isEmpty());
        this.c.addCommand(two);
        Assert.assertTrue(this.c.getCommands().contains(one));
        Assert.assertTrue(this.c.getCommands().contains(two));
        Assert.assertTrue(one.getClusters().contains(this.c));
        Assert.assertTrue(two.getClusters().contains(this.c));

        this.c.removeCommand(one);
        Assert.assertFalse(this.c.getCommands().contains(one));
        Assert.assertTrue(this.c.getCommands().contains(two));
        Assert.assertFalse(one.getClusters().contains(this.c));
        Assert.assertTrue(two.getClusters().contains(this.c));
    }

    /**
     * Make sure you can't remove a null command.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test(expected = GeniePreconditionException.class)
    public void testRemoveNullCommand() throws GeniePreconditionException {
        this.c.removeCommand(null);
    }

    /**
     * Test removing all the commands.
     *
     * @throws GeniePreconditionException If any precondition isn't met.
     */
    @Test
    public void testRemoveAllCommands() throws GeniePreconditionException {
        Assert.assertNotNull(this.c.getCommands());
        Assert.assertTrue(this.c.getCommands().isEmpty());
        final CommandEntity one = new CommandEntity();
        one.setId("one");
        final CommandEntity two = new CommandEntity();
        two.setId("two");
        final List<CommandEntity> commands = new ArrayList<>();
        commands.add(one);
        commands.add(two);
        this.c.setCommands(commands);
        Assert.assertEquals(commands, this.c.getCommands());
        Assert.assertTrue(one.getClusters().contains(this.c));
        Assert.assertTrue(two.getClusters().contains(this.c));

        this.c.removeAllCommands();
        Assert.assertEquals(0, this.c.getCommands().size());
        Assert.assertTrue(this.c.getCommands().isEmpty());
        Assert.assertFalse(one.getClusters().contains(this.c));
        Assert.assertFalse(two.getClusters().contains(this.c));
    }
}
