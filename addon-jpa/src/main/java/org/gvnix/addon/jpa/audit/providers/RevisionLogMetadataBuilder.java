/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.jpa.audit.providers;

import java.util.List;

import org.gvnix.addon.jpa.audit.JpaAuditMetadata;
import org.gvnix.addon.jpa.audit.JpaAuditMetadata.Context;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.JavaSymbolName;

/**
 * Interface provider by {@link RevisionLogProvider} which produces required
 * artifacts for entities annotated with
 * {@link org.gvnix.addon.jpa.audit.GvNIXJpaAudit} to handle revisionLog
 * functionality.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * 
 */
public interface RevisionLogMetadataBuilder {

    /**
     * Initialize builder with common elements
     * 
     * @param context required information to generate artifact
     */
    void initialize(Context context);

    /**
     * Clean instances received on
     * {@link #initialize(ItdTypeDetailsBuilder, Context)}
     */
    void done();

    /**
     * Called by {@link JpaAuditMetadata} to generate provider required
     * artifacts to support revision log functionality
     * <p/>
     * This includes any constructors, properties or initializers required by
     * provider implementation.
     * 
     * @param builder of audit itd
     */
    void addCustomArtifact(ItdTypeDetailsBuilder builder);

    /**
     * Builds method body (implementation) of findAllXXX(atDate) method
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyFindAllFromDate(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of findAllXXX(revisionNumber) method
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyFindAllFromRevision(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of getRevsionNumberForDate(aDate)
     * method
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyGetRevisionNumberForDate(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of findXXX(id,aDate) method
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyFindFromDate(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of findXXX(id,revisionNumber) method
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyFindFromRevision(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of getXXXRevisions(id,
     * fromData,toDate,start,limit)
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyGetRevisions(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of
     * getXXXRevisions(fromData,toDate,start,limit) (instance method)
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyGetRevisionsInstance(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of
     * findXXXRevisonByDates(fromDate,toDate,filter,order,start,limit)
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyFindRevisionByDates(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Builds method body (implementation) of
     * findRevison(fromRevsion,toRevision,filter,order,start,limit)
     * 
     * @param builder
     * @param parameters
     */
    void buildBodyFindRevision(InvocableMemberBodyBuilder builder,
            List<JavaSymbolName> parameters);

    /**
     * Called by {@link JpaAuditMetadata} to generate provider required
     * artifacts in revision instance entity
     * <p/>
     * This includes any constructors, properties or initializers required by
     * provider implementation.
     * 
     * @param classBuilder
     */
    void addCustomArtifactToRevisionItem(
            ClassOrInterfaceTypeDetailsBuilder classBuilder);

    /**
     * Builds method body (implementation) of XXRevision.getItem() method
     * 
     * @param builder
     */
    void buildBodyRevisionItemGetItem(InvocableMemberBodyBuilder builder);

    /**
     * Builds method body (implementation) of XXRevision.getRevisionNumber()
     * method
     * 
     * @param body
     */
    void buildBodyRevisionItemGetRevisionNumber(InvocableMemberBodyBuilder body);

    /**
     * Builds method body (implementation) of XXRevision.getUserName() method
     * 
     * @param body
     */
    void buildBodyRevisionItemGetRevisionUser(InvocableMemberBodyBuilder body);

    /**
     * Builds method body (implementation) of XXRevision.getRevisionDate()
     * method
     * 
     * @param body
     */
    void buildBodyRevisionItemGetRevisionDate(InvocableMemberBodyBuilder body);

    /**
     * Builds method body (implementation) of XXRevision.isCreate() method
     * 
     * @param body
     */
    void buildBodyRevisionItemIsCreate(InvocableMemberBodyBuilder body);

    /**
     * Builds method body (implementation) of XXRevision.isUpdate() method
     * 
     * @param body
     */
    void buildBodyRevisionItemIsUpdate(InvocableMemberBodyBuilder body);

    /**
     * Builds method body (implementation) of XXRevision.isDelete() method
     * 
     * @param body
     */
    void buildBodyRevisionItemIsDelete(InvocableMemberBodyBuilder body);

    /**
     * Builds method body (implementation) of XXRevision.getType() method
     * 
     * @param body
     */
    void buildBodyRevisionItemGetType(InvocableMemberBodyBuilder body);

}
