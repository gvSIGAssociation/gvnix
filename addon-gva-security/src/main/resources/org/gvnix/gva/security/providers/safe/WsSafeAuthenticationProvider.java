/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.springsource.petclinic.security.authentication.wssafe;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.ws.soap.SOAPFaultException;

import org.springframework.roo.project.Path;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSRequest;
import es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSResponse;
import es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSRequest;
import es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSResponse;
import es.gva.dgm.ayf.war.definitions.v2u00.Permisoapp;
import es.gva.dgm.ayf.war.definitions.v2u00.RetornaAutorizacionWSRequest;
import es.gva.dgm.ayf.war.definitions.v2u00.RetornaAutorizacionWSResponse;
import es.gva.dgm.ayf.war.definitions.v2u00.RetornaTodasAutorizacionesWSRequest;
import es.gva.dgm.ayf.war.definitions.v2u00.RetornaTodasAutorizacionesWSResponse;
import es.gva.dgm.ayf.war.definitions.v2u00.Permiso;

import org.abego.treelayout.Configuration;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.resource.PropertiesResolver;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.log4j.Logger;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * This class implements the security authentication of C.I.T. It uses S.A.F.E.
 * WsAuth service.
 * 
 * @author jmvivo <a href="mailto:jmvivo@disid.com">Jose Manuel Vivó Arnal</a>
 * @author miborra <a href="mailto:miborra@disid.com">Manuel Iborra</a>
 * @author jcgarcia <a href="mailto:jcgarcia@disid.com">Juan Carlos García del
 *         Canto</a>
 * 
 */
public class WsSafeAuthenticationProvider extends
        AbstractUserDetailsAuthenticationProvider {

    private static Logger logger = Logger
            .getLogger(WsSafeAuthenticationProvider.class.getName());

    // Path and key to get password property value
    private static final String PROPERTIES_PATH = "safe_client_sign.properties";

    // Password encoder for User instances
    private PasswordEncoder passwordEncoder = new PlaintextPasswordEncoder();

    // Salt source to use
    private SaltSource saltSource;

    // End point to use
    private String endpoint = null;

    // End point to authorization
    private String endpointAutoriza = null;

    // ApplicationId
    private String applicationId = null;

    // Define el entorno a usar apara identificar los roles
    private String environment = null;

    //private ResourceBundle configuration = null;
    
    Properties prop = null;
    ClassLoader loader = null;           
    InputStream stream = null;

    private boolean mapRoles = true;

    /**
     * <p>
     * Compares the password of the request with the stored in the
     * {@link UserDetails}. The {@link UserDetails#getPassword()} is encoded by
     * {@link #passwordEncoder} .
     * </p>
     * 
     * <p>
     * It can't be a duplicate check but is needed because
     * <code>userDetail</code> would be come from user cache.
     * </p>
     * 
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        Object salt = null;

        if (this.saltSource != null) {
            salt = this.saltSource.getSalt(userDetails);
        }

        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException("Bad credentials: "
                    + userDetails.getUsername());
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.isPasswordValid(userDetails.getPassword(),
                presentedPassword, salt)) {
            throw new BadCredentialsException("Bad credentials: "
                    + userDetails.getUsername() + " password chekc");
        }

    }

    /**
     * <p>
     * Performs request to CIT'a WSAthr service to get the user and fill a
     * {@link WscitUser} instance.
     * </p>
     * 
     * <p>
     * The {@link WscitUser#getPassword()} field will be encoded by
     * {@link #passwordEncoder} mixed by {@link #saltSource}.
     * </p>
     */
    @Override
    protected UserDetails retrieveUser(String username,
            UsernamePasswordAuthenticationToken authentication) {

        // Get user data
        String presentedPassword = authentication.getCredentials().toString();
        AutenticaUsuarioLDAPWSRequest aut = new AutenticaUsuarioLDAPWSRequest();
        aut.setUsuarioLDAP(username);
        aut.setPwdLDAP(presentedPassword);

        try {

            es.gva.dgm.ayf.war.definitions.v2u00.AutenticacionArangiService autService = new es.gva.dgm.ayf.war.definitions.v2u00.AutenticacionArangiService(
                    new URL(getEndpoint()));
            es.gva.dgm.ayf.war.definitions.v2u00.AutenticacionArangiPortType port = autService
                    .getAutenticacionArangiPortTypeSoap11();

            // Applying security on webservice
            Client client = ClientProxy.getClient(port);
            setSecurity(client);

            AutenticaUsuarioLDAPWSResponse response1 = port
                    .autenticaUsuarioLDAPWS(aut);
            GetInformacionWSRequest getInformacionWSRequest = new GetInformacionWSRequest();
            getInformacionWSRequest.setToken(response1.getToken());
            GetInformacionWSResponse getInformacionWSResponse = port
                    .getInformacionWS(getInformacionWSRequest);

            RetornaAutorizacionWSRequest retornaUsuApliAut = new RetornaAutorizacionWSRequest();
            // Get application id
            String applicationId = getApplicationId();
            retornaUsuApliAut.setIdAplicacion(applicationId);
            retornaUsuApliAut.setUsuarioHDFI(getInformacionWSResponse
                    .getIdHDFI());
            es.gva.dgm.ayf.war.definitions.v2u00.AutorizacionService autorizaService = new es.gva.dgm.ayf.war.definitions.v2u00.AutorizacionService(
                    new URL(getEndpointAutoriza()));
            es.gva.dgm.ayf.war.definitions.v2u00.AutorizacionPortType autorizaPort = autorizaService
                    .getAutorizacionPortTypeSoap11();
            // Applying security on webservice
            Client autorizaClient = ClientProxy.getClient(autorizaPort);
            setSecurity(autorizaClient);
            RetornaAutorizacionWSResponse autorizaResponse = autorizaPort
                    .retornaAutorizacionWS(retornaUsuApliAut);
            List<Permisoapp> listaPermisos = autorizaResponse.getPermisoapp();
            // Transforms the data
            WsSafeUser user = convertWSInfoToUser(getInformacionWSResponse,
                    username, listaPermisos);

            Object salt = null;

            if (this.saltSource != null) {
                salt = this.saltSource.getSalt(user);
            }
            // Stores the user password in encoded for
            user.setPassword(passwordEncoder.encodePassword(presentedPassword,
                    salt));

            return user;

        }
        catch (Exception e) {// MalformedURLException e1) {
            logger.warn("Solicitud de login denegada (usuario='" + username
                    + "'): " + e.getLocalizedMessage());

            if (e instanceof SOAPFaultException) {
                String message = e.getMessage();
                if (message.indexOf("autenticaUsuarioLDAPWS") > 0) {
                    message = "Por favor, compruebe que el usuario y password son correctos";
                }
                throw new AuthenticationServiceException("Acceso denegado. "
                        + message, e);
            }
            else {
                throw new AuthenticationServiceException(
                        "Error en servicio web de login al validar al usuario."
                                + e.getMessage(), e);
            }

        }
    }

    /**
     * s Metodo que setea la cabecera de seguridad a los Web Services. Se le
     * pasa la clase Handler y los datos del certificado de aplicacion. Tambien
     * se le pasa el fichero con los datos del keystore.
     * 
     * @param client
     * @throws SystemException
     */
    public static void setSecurity(Client client) {

        Properties configuration = getSafeProperties();
        org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE);
        outProps.put(
                WSHandlerConstants.USER,
                configuration
                        .getProperty("org.apache.ws.security.crypto.merlin.keystore.alias"));
        outProps.put(
                WSHandlerConstants.SIGNATURE_USER,
                configuration
                        .getProperty("org.apache.ws.security.crypto.merlin.keystore.alias"));
        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS,
                "com.springsource.petclinic.security.authentication.wssafe.WsPasswordHandler");
        outProps.put(WSHandlerConstants.SIG_PROP_FILE, PROPERTIES_PATH);
        outProps.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference");

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        cxfEndpoint.getOutInterceptors().add(wssOut);
    }

    private WsSafeUser convertWSInfoToUser(GetInformacionWSResponse userFromWS,
            String username, List<Permisoapp> listPermisos) {
        WsSafeUser user = new WsSafeUser();
        user.setNombre(userFromWS.getNombre());
        user.setEmail(userFromWS.getEmail());
        user.setApellido1(userFromWS.getApellido1());
        user.setApellido2(userFromWS.getApellido2());
        user.setCif(userFromWS.getCif());
        user.setHabilitado(userFromWS.getHabilitado());
        user.setIdHDFI(userFromWS.getIdHDFI());
        user.setIusserDN(userFromWS.getIssuerDN());
        user.setNif(userFromWS.getNif());
        user.setOid(userFromWS.getOid());
        user.setRazonSocial(userFromWS.getRazonSocial());
        user.setRepresentante(userFromWS.getRepresentante());
        user.setSerialNumber(userFromWS.getSerialNumber());
        user.setSubjectDN(userFromWS.getSubjectDN());
        user.setTipoAut(userFromWS.getTipoAut());
        user.setTipoCertificado(userFromWS.getTipoCertificado());

        // Sprint Security User info
        user.setUsername(username);
        user.setAccountNonExpired(true); // Status info
        user.setAccountNonLocked(true);// Status info
        user.setCredentialsNonExpired(true); // Status info
        user.setEnabled(true);// Status info

        // Roles
        if (listPermisos == null) {
            throw new BadCredentialsException(
                    " El usuario proporcionado no tiene módulos asignados");
        }
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        Iterator<Permisoapp> iter = listPermisos.iterator();
        while (iter.hasNext()) {
            Permisoapp permisoApp = iter.next();
            // Obtenemos a partir del grupo el rol del usuario
            String rolUsu = convertToApplicationRol(permisoApp.getIdgrupo());
            // Si no existe ningún grupo definido no añadimos los permisos
            if (rolUsu != null) {
                String[] roles = rolUsu.split(",");
                for(int i = 0;i < roles.length;i++){
                    WsSafeUserAuthority usAuth = new WsSafeUserAuthority();
                    usAuth.setAuthority(roles[i]);
                    usAuth.setIdgrupo(permisoApp.getIdgrupo());
                    usAuth.setIdaplicacion(permisoApp.getIdaplicacion());
                    usAuth.setNif(permisoApp.getNif());
                    usAuth.setUsrtipo(permisoApp.getUsrtipo());
                    usAuth.setIdrol(permisoApp.getIdrol());
                    authorities.add(usAuth);
                }
            }
        }
        user.setAuthorities(authorities);

        return user;
    }

    /**
     * Este método se encarga de encontrar, a partir de un grupo facilitado el
     * nombre del Rol para la aplicación
     * 
     * @param idgrupo Grupo del usuario que devuelve el webservice
     * @return
     */
    private String convertToApplicationRol(String idgrupo) {
        /*
         * Se obtienen los ids posibles para cada grupo
         * definidos en el pom.xml dependiendo del entorno de ejecución.
         */
        if (!mapRoles) {
            return idgrupo;
        }
        
        if (prop == null){          
            prop = new Properties();
            loader = Thread.currentThread().getContextClassLoader();           
            stream = loader.getResourceAsStream("safe_client.properties");
            try {
                prop.load(stream);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        StringBuilder sbuilder = new StringBuilder("SAFE.");
        if (getEnvironment() != null && !getEnvironment().isEmpty()) {
            sbuilder.append('.');
            sbuilder.append(getEnvironment());
            sbuilder.append('.');
        }
        sbuilder.append("role.");
        sbuilder.append(idgrupo);
        try {
            return prop.getProperty(sbuilder.toString());
        }
        catch (MissingResourceException e) {
            // TODO log warning!!!
            return idgrupo;
        }
    }

    public static Properties getSafeProperties() {

        Properties configuration = new Properties();
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream(PROPERTIES_PATH);
        configuration = new Properties();
        try {
            configuration.load(in);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    /**
     * The source of salts to use when decoding passwords. <code>null</code> is
     * a valid value, meaning the <code>DaoAuthenticationProvider</code> will
     * present <code>null</code> to the relevant <code>PasswordEncoder</code>.
     * 
     * @param saltSource to use when attempting to decode passwords via the
     *        <code>PasswordEncoder</code>
     */
    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    protected SaltSource getSaltSource() {
        return saltSource;
    }

    /**
     * Sets the PasswordEncoder instance to be used to encode and validate
     * passwords. If not set, {@link PlaintextPasswordEncoder} will be used by
     * default.
     * 
     * @param passwordEncoder The passwordEncoder to use
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    protected PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * Sets the end point to connect with web service
     * 
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    protected String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the end point to connect with web service authorization
     * 
     */
    public void setEndpointAutoriza(String endpointAutoriza) {
        this.endpointAutoriza = endpointAutoriza;
    }

    protected String getEndpointAutoriza() {
        return endpointAutoriza;
    }

    /**
     * Sets the applicationId of current profile
     */

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * @param environment
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

}
