/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.appservice.webapp;

import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.WebDeploymentSlotBasic;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WebAppDeploymentSlot extends WebAppBase<WebAppDeploymentSlot, WebApp, DeploymentSlot> {

    /**
     * copy constructor
     */
    protected WebAppDeploymentSlot(@Nonnull WebAppDeploymentSlot origin) {
        super(origin);
    }

    protected WebAppDeploymentSlot(@Nonnull String name, @Nonnull WebAppDeploymentSlotModule module) {
        super(name, module);
    }

    protected WebAppDeploymentSlot(@Nonnull WebDeploymentSlotBasic remote, @Nonnull WebAppDeploymentSlotModule module) {
        super(remote.name(), module);
    }

    @Nonnull
    @Override
    public List<AbstractAzResourceModule<?, ?, ?>> getSubModules() {
        return Collections.emptyList();
    }

    @Override
    protected void toggleWebSockets(final boolean enabled) {
        doModify(() -> Objects.requireNonNull(getRemote()).update().withWebSocketsEnabled(enabled).apply(), Status.UPDATING);
    }
}
