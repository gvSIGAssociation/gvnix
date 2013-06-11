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
package org.gvnix.addon.datatables;

import java.beans.Introspector;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.query.JpaQueryMetadata;
import org.gvnix.addon.web.mvc.batch.WebJpaBatchMetadata;
import org.gvnix.support.PhysicalTypeUtils;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.finder.DynamicFinderServicesImpl;
import org.springframework.roo.addon.finder.FieldToken;
import org.springframework.roo.addon.finder.FinderFieldTokenMissingException;
import org.springframework.roo.addon.finder.FinderMetadata;
import org.springframework.roo.addon.finder.InvalidFinderException;
import org.springframework.roo.addon.finder.QueryHolder;
import org.springframework.roo.addon.finder.ReservedToken;
import org.springframework.roo.addon.finder.ReservedTokenHolder;
import org.springframework.roo.addon.finder.Token;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.FinderMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataServiceImpl;
import org.springframework.roo.addon.web.mvc.controller.finder.WebFinderMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link DatatablesMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public final class DatatablesMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private WebMetadataService webMetadataService;

    /**
     * Register itself into metadataDependencyRegister and add metadata trigger
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXDatatables.class.getName()));
    }

    /**
     * Unregister this provider
     * 
     * @param context the component context
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXDatatables.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = DatatablesMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = DatatablesMetadata
                .getPath(metadataIdentificationString);

        final DatatablesAnnotationValues annotationValues = new DatatablesAnnotationValues(
                governorPhysicalTypeMetadata);

        // Get webScaffoldMetadata
        String webScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(
                javaType, path);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataId);
        // register dependency to Roo Web Scaffold
        metadataDependencyRegistry.registerDependency(webScaffoldMetadataId,
                metadataIdentificationString);

        JavaType webScaffoldAspectName = webScaffoldMetadata.getAspectName();

        WebScaffoldAnnotationValues webScaffoldAnnotationValues = webScaffoldMetadata
                .getAnnotationValues();

        // Get formBackingObject and its logical path
        JavaType entity = webScaffoldAnnotationValues.getFormBackingObject();
        LogicalPath entityPath = PhysicalTypeUtils.getPath(entity,
                typeLocationService);

        // Get batch service (if any)
        String webJpaBatchMetadataId = WebJpaBatchMetadata.createIdentifier(
                javaType, path);
        WebJpaBatchMetadata webJpaBatchMetadata = (WebJpaBatchMetadata) metadataService
                .get(webJpaBatchMetadataId);

        String jpaMetadataId = JpaActiveRecordMetadata.createIdentifier(entity,
                entityPath);
        JpaActiveRecordMetadata jpaMetadata = (JpaActiveRecordMetadata) metadataService
                .get(jpaMetadataId);

        if (jpaMetadata == null) {
            // Unsupported type (by now)
            return null;
        }

        // register dependency to JPA Active record
        metadataDependencyRegistry.registerDependency(jpaMetadataId,
                metadataIdentificationString);

        // Get jpa query metadata
        String jpaQueryMetadataId = JpaQueryMetadata.createIdentifier(entity,
                path);
        JpaQueryMetadata jpaQueryMetadata = (JpaQueryMetadata) metadataService
                .get(jpaQueryMetadataId);
        // register dependency to JPA Query
        metadataDependencyRegistry.registerDependency(jpaQueryMetadataId,
                metadataIdentificationString);

        List<FieldMetadata> identifiers = persistenceMemberLocator
                .getIdentifierFields(entity);

        String plural = jpaMetadata.getPlural();

        JavaSymbolName entityManagerMethodName = jpaMetadata
                .getEntityManagerMethod().getMethodName();

        // check if has metadata types
        final MemberDetails entityMemberDetails = getMemberDetails(entity);

        final Map<JavaSymbolName, DateTimeFormatDetails> datePatterns = webMetadataService
                .getDatePatterns(entity, entityMemberDetails,
                        metadataIdentificationString);

        // Identify if controller is annotated with @RooWebFinders
        Map<FinderMetadataDetails, QueryHolderTokens> findersRegistered = null;
        String webFinderMetadataId = WebFinderMetadata.createIdentifier(
                javaType, path);
        WebFinderMetadata webFinderMetadata = (WebFinderMetadata) metadataService
                .get(webFinderMetadataId);
        // register dependency to Roo Web finder
        metadataDependencyRegistry.registerDependency(webFinderMetadataId,
                metadataIdentificationString);

        if (webFinderMetadata != null) {

            // Locate finders details
            findersRegistered = getFindersRegisterd(entity, path,
                    entityMemberDetails, plural, jpaMetadata.getEntityName());

        }

        return new DatatablesMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, annotationValues, entity,
                identifiers, plural, entityManagerMethodName, datePatterns,
                webScaffoldAspectName, webJpaBatchMetadata, jpaQueryMetadata,
                webScaffoldAnnotationValues, findersRegistered);
    }

    /**
     * Locates All {@link FinderMetadataDetails} and its related
     * {@link QueryHolder} for every declared dynamic finder <br>
     * <br>
     * <em>Note:</em> This method is similar to
     * {@link WebMetadataServiceImpl#getDynamicFinderMethodsAndFields(JavaType, MemberDetails, String)}
     * but without register dependency (this dependency produces NPE in
     * {@link #getMetadata(String, JavaType, PhysicalTypeMetadata, String)} when
     * it tries to get JPA information)
     * 
     * @param entity
     * @param path
     * @param entityMemberDetails
     * @param plural
     * @param entityName
     * @return
     * @see WebMetadataServiceImpl#getDynamicFinderMethodsAndFields(JavaType,
     *      MemberDetails, String)
     */
    private Map<FinderMetadataDetails, QueryHolderTokens> getFindersRegisterd(
            JavaType entity, LogicalPath path,
            MemberDetails entityMemberDetails, String plural, String entityName) {

        // Get finder metadata
        final String finderMetadataKey = FinderMetadata.createIdentifier(
                entity, path);
        final FinderMetadata finderMetadata = (FinderMetadata) metadataService
                .get(finderMetadataKey);
        if (finderMetadata == null) {
            return null;
        }

        QueryHolderTokens queryHolder;
        FinderMetadataDetails details;

        Map<FinderMetadataDetails, QueryHolderTokens> findersRegistered = new HashMap<FinderMetadataDetails, QueryHolderTokens>();
        // Iterate over
        for (final MethodMetadata method : finderMetadata
                .getAllDynamicFinders()) {
            final List<JavaSymbolName> parameterNames = method
                    .getParameterNames();
            final List<JavaType> parameterTypes = AnnotatedJavaType
                    .convertFromAnnotatedJavaTypes(method.getParameterTypes());
            final List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
            for (int i = 0; i < parameterTypes.size(); i++) {
                JavaSymbolName fieldName = null;
                if (parameterNames.get(i).getSymbolName().startsWith("max")
                        || parameterNames.get(i).getSymbolName()
                                .startsWith("min")) {
                    fieldName = new JavaSymbolName(
                            Introspector.decapitalize(StringUtils
                                    .capitalize(parameterNames.get(i)
                                            .getSymbolName().substring(3))));
                }
                else {
                    fieldName = parameterNames.get(i);
                }
                final FieldMetadata field = BeanInfoUtils
                        .getFieldForPropertyName(entityMemberDetails, fieldName);
                if (field != null) {
                    final FieldMetadataBuilder fieldMd = new FieldMetadataBuilder(
                            field);
                    fieldMd.setFieldName(parameterNames.get(i));
                    fields.add(fieldMd.build());
                }
            }

            details = new FinderMetadataDetails(method.getMethodName()
                    .getSymbolName(), method, fields);

            // locate QueryHolder instances. This objects contain
            // information about a roo finder (parameters names and types
            // and a "token" list with of find definition

            queryHolder = getQueryHolder(entityMemberDetails,
                    method.getMethodName(), plural, entityName);
            findersRegistered.put(details, queryHolder);

        }
        return findersRegistered;
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXDatatables.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXDatatables";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = DatatablesMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = DatatablesMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return DatatablesMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return DatatablesMetadata.getMetadataIdentiferType();
    }

    /***************************************************/
    /** Methods cloned from DynamicFinderServicesImpl **/
    /***************************************************/

    /**
     * @see DynamicFinderServicesImpl#getQueryHolder(MemberDetails,
     *      JavaSymbolName, String, String)
     */
    public QueryHolderTokens getQueryHolder(final MemberDetails memberDetails,
            final JavaSymbolName finderName, final String plural,
            final String entityName) {
        Validate.notNull(memberDetails, "Member details required");
        Validate.notNull(finderName, "Finder name required");
        Validate.notBlank(plural, "Plural required");

        List<Token> tokens;
        try {
            tokens = tokenize(memberDetails, finderName, plural);
        }
        catch (final FinderFieldTokenMissingException e) {
            return null;
        }
        catch (final InvalidFinderException e) {
            return null;
        }

        final String simpleTypeName = getConcreteJavaType(memberDetails)
                .getSimpleTypeName();
        // final String jpaQuery = getJpaQuery(tokens, simpleTypeName,
        // finderName,
        // plural, entityName);
        final List<JavaType> parameterTypes = getParameterTypes(tokens,
                finderName, plural);
        final List<JavaSymbolName> parameterNames = getParameterNames(tokens,
                finderName, plural);
        return new QueryHolderTokens("", parameterTypes, parameterNames, tokens);
    }

    /**
     * @see DynamicFinderServicesImpl#getConcreteJavaType
     */
    private JavaType getConcreteJavaType(final MemberDetails memberDetails) {
        Validate.notNull(memberDetails, "Member details required");
        JavaType javaType = null;
        for (final MemberHoldingTypeDetails memberHoldingTypeDetails : memberDetails
                .getDetails()) {
            if (Modifier.isAbstract(memberHoldingTypeDetails.getModifier())) {
                continue;
            }
            javaType = memberHoldingTypeDetails.getName();
        }
        return javaType;
    }

    /**
     * @see DynamicFinderServicesImpl#tokenize
     */
    private List<Token> tokenize(final MemberDetails memberDetails,
            final JavaSymbolName finderName, final String plural) {
        final String simpleTypeName = getConcreteJavaType(memberDetails)
                .getSimpleTypeName();
        String finder = finderName.getSymbolName();

        // Just in case it starts with findBy we can remove it here
        final String findBy = "find" + plural + "By";
        if (finder.startsWith(findBy)) {
            finder = finder.substring(findBy.length());
        }

        // If finder still contains the findBy sequence it is most likely a
        // wrong finder (ie someone pasted the finder string accidentally twice
        if (finder.contains(findBy)) {
            throw new InvalidFinderException("Dynamic finder definition for '"
                    + finderName.getSymbolName() + "' in " + simpleTypeName
                    + ".java is invalid");
        }

        final SortedSet<FieldToken> fieldTokens = new TreeSet<FieldToken>();
        for (final MethodMetadata method : getLocatedMutators(memberDetails)) {
            final FieldMetadata field = BeanInfoUtils.getFieldForPropertyName(
                    memberDetails, method.getParameterNames().get(0));

            // If we did find a field matching the first parameter name of the
            // mutator method we can add it to the finder ITD
            if (field != null) {
                fieldTokens.add(new FieldToken(field));
            }
        }

        final List<Token> tokens = new ArrayList<Token>();

        while (finder.length() > 0) {
            final Token token = getFirstToken(fieldTokens, finder,
                    finderName.getSymbolName(), simpleTypeName);
            if (token != null) {
                if (token instanceof FieldToken
                        || token instanceof ReservedToken) {
                    tokens.add(token);
                }
                finder = finder.substring(token.getValue().length());
            }
        }

        return tokens;
    }

    /**
     * @see DynamicFinderServicesImpl#getLocatedMutators
     */
    private List<MethodMetadata> getLocatedMutators(
            final MemberDetails memberDetails) {
        final List<MethodMetadata> locatedMutators = new ArrayList<MethodMetadata>();
        for (final MethodMetadata method : memberDetails.getMethods()) {
            if (isMethodOfInterest(method)) {
                locatedMutators.add(method);
            }
        }
        return locatedMutators;
    }

    /**
     * @see DynamicFinderServicesImpl#getFirstToken
     */
    private Token getFirstToken(final SortedSet<FieldToken> fieldTokens,
            final String finder, final String originalFinder,
            final String simpleTypeName) {
        for (final FieldToken fieldToken : fieldTokens) {
            if (finder.startsWith(fieldToken.getValue())) {
                return fieldToken;
            }
        }
        for (final ReservedToken reservedToken : ReservedTokenHolder.ALL_TOKENS) {
            if (finder.startsWith(reservedToken.getValue())) {
                return reservedToken;
            }
        }
        if (finder.length() > 0) {
            // TODO: Make this a FinderFieldTokenMissingException instead, to
            // make it easier to detect this
            throw new FinderFieldTokenMissingException(
                    "Dynamic finder is unable to match '" + finder
                            + "' token of '" + originalFinder
                            + "' finder definition in " + simpleTypeName
                            + ".java");
        }

        return null; // Finder does not start with reserved or field token
    }

    /**
     * @see DynamicFinderServicesImpl#isMethodOfInterest
     */
    private boolean isMethodOfInterest(final MethodMetadata method) {
        return method.getMethodName().getSymbolName().startsWith("set")
                && method.getModifier() == Modifier.PUBLIC;
    }

    /**
     * @see DynamicFinderServicesImpl#getParameterTypes
     */
    private List<JavaType> getParameterTypes(final List<Token> tokens,
            final JavaSymbolName finderName, final String plural) {
        final List<JavaType> parameterTypes = new ArrayList<JavaType>();

        for (int i = 0; i < tokens.size(); i++) {
            final Token token = tokens.get(i);
            if (token instanceof FieldToken) {
                parameterTypes.add(((FieldToken) token).getField()
                        .getFieldType());
            }
            else {
                if ("Between".equals(token.getValue())) {
                    final Token field = tokens.get(i - 1);
                    if (field instanceof FieldToken) {
                        parameterTypes.add(parameterTypes.get(parameterTypes
                                .size() - 1));
                    }
                }
                else if ("IsNull".equals(token.getValue())
                        || "IsNotNull".equals(token.getValue())) {
                    final Token field = tokens.get(i - 1);
                    if (field instanceof FieldToken) {
                        parameterTypes.remove(parameterTypes.size() - 1);
                    }
                }
            }
        }
        return parameterTypes;
    }

    /**
     * @see DynamicFinderServicesImpl#getParameterNames
     */
    private List<JavaSymbolName> getParameterNames(final List<Token> tokens,
            final JavaSymbolName finderName, final String plural) {
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        for (int i = 0; i < tokens.size(); i++) {
            final Token token = tokens.get(i);
            if (token instanceof FieldToken) {
                final String fieldName = ((FieldToken) token).getField()
                        .getFieldName().getSymbolName();
                parameterNames.add(new JavaSymbolName(fieldName));
            }
            else {
                if ("Between".equals(token.getValue())) {
                    final Token field = tokens.get(i - 1);
                    if (field instanceof FieldToken) {
                        final JavaSymbolName fieldName = parameterNames
                                .get(parameterNames.size() - 1);
                        // Remove the last field token
                        parameterNames.remove(parameterNames.size() - 1);

                        // Replace by a min and a max value
                        parameterNames
                                .add(new JavaSymbolName(
                                        "min"
                                                + fieldName
                                                        .getSymbolNameCapitalisedFirstLetter()));
                        parameterNames
                                .add(new JavaSymbolName(
                                        "max"
                                                + fieldName
                                                        .getSymbolNameCapitalisedFirstLetter()));
                    }
                }
                else if ("IsNull".equals(token.getValue())
                        || "IsNotNull".equals(token.getValue())) {
                    final Token field = tokens.get(i - 1);
                    if (field instanceof FieldToken) {
                        parameterNames.remove(parameterNames.size() - 1);
                    }
                }
            }
        }

        return parameterNames;
    }
}