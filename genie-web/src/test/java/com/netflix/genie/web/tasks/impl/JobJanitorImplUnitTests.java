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
package com.netflix.genie.web.tasks.impl;

import com.netflix.genie.test.categories.UnitTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test code for the job janitor class, which marks un-updated jobs as zombies.
 *
 * @author tgianos
 */
@Category(UnitTest.class)
public class JobJanitorImplUnitTests {

    /**
     * Test whether the janitor cleans up zombie jobs correctly.
     *
     * @throws Exception For any issue
     */
    @Test
    @Ignore
    public void testMarkZombies() throws Exception {
    }
}
