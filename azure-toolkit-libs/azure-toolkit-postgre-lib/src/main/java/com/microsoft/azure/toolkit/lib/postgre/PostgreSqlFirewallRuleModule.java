/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.postgre;

import com.azure.resourcemanager.postgresql.PostgreSqlManager;
import com.azure.resourcemanager.postgresql.models.FirewallRule;
import com.azure.resourcemanager.postgresql.models.FirewallRules;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.google.common.base.Preconditions;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.database.entity.IFirewallRule;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

public class PostgreSqlFirewallRuleModule extends AbstractAzResourceModule<PostgreSqlFirewallRule, PostgreSqlServer, FirewallRule> {
    public static final String NAME = "firewallRules";

    public PostgreSqlFirewallRuleModule(@Nonnull PostgreSqlServer parent) {
        super(NAME, parent);
    }

    @Override
    protected PostgreSqlFirewallRule newResource(@Nonnull FirewallRule rule) {
        return new PostgreSqlFirewallRule(rule, this);
    }

    @Nonnull
    @Override
    protected Stream<FirewallRule> loadResourcesFromAzure() {
        return this.getClient().listByServer(this.getParent().getResourceGroupName(), this.getParent().getName()).stream();
    }

    @Nullable
    @Override
    protected FirewallRule loadResourceFromAzure(@Nonnull String name, String resourceGroup) {
        return this.getClient().get(this.getParent().getResourceGroupName(), this.getParent().getName(), name);
    }

    @Override
    protected void deleteResourceFromAzure(@Nonnull String id) {
        final ResourceId resourceId = ResourceId.fromString(id);
        final String name = resourceId.name();
        this.getClient().delete(this.getParent().getResourceGroupName(), this.getParent().getName(), name);
    }

    @Override
    protected PostgreSqlFirewallRuleDraft newDraftForCreate(@Nonnull String name, String resourceGroupName) {
        return new PostgreSqlFirewallRuleDraft(name, this);
    }

    @Override
    protected PostgreSqlFirewallRuleDraft newDraftForUpdate(@Nonnull PostgreSqlFirewallRule postgreSqlFirewallRule) {
        return new PostgreSqlFirewallRuleDraft(postgreSqlFirewallRule);
    }

    @Override
    protected FirewallRules getClient() {
        return Optional.ofNullable(this.getParent().getParent().getRemote()).map(PostgreSqlManager::firewallRules).orElse(null);
    }

    void toggleAzureServiceAccess(boolean allowed) {
        final String ruleName = IFirewallRule.AZURE_SERVICES_ACCESS_FIREWALL_RULE_NAME;
        final String rgName = this.getParent().getResourceGroupName();
        final boolean exists = this.exists(ruleName, rgName);
        if (!allowed && exists) {
            this.delete(ruleName, rgName);
        }
        if (allowed && !exists) {
            final PostgreSqlFirewallRuleDraft draft = this.create(ruleName, rgName);
            draft.setStartIpAddress(IFirewallRule.IP_ALLOW_ACCESS_TO_AZURE_SERVICES);
            draft.setEndIpAddress(IFirewallRule.IP_ALLOW_ACCESS_TO_AZURE_SERVICES);
            draft.commit();
        }
    }

    void toggleLocalMachineAccess(boolean allowed) {
        final String ruleName = IFirewallRule.getLocalMachineAccessRuleName();
        final String rgName = this.getParent().getResourceGroupName();
        final boolean exists = this.exists(ruleName, rgName);
        if (!allowed && exists) {
            this.delete(ruleName, rgName);
        }
        if (allowed && !exists) {
            final String publicIp = this.getParent().getLocalMachinePublicIp();
            Preconditions.checkArgument(StringUtils.isNotBlank(publicIp),
                "Cannot enable local machine access to PostgreSql server due to error: cannot get public ip.");
            final PostgreSqlFirewallRuleDraft draft = this.updateOrCreate(ruleName, rgName);
            draft.setStartIpAddress(publicIp);
            draft.setEndIpAddress(publicIp);
            draft.commit();
        }
    }
}
