/**
 * This file is part of CloudML [ http://cloudml.org ]
 *
 * Copyright (C) 2012 - SINTEF ICT
 * Contact: Franck Chauvel <franck.chauvel@sintef.no>
 *
 * Module: root
 *
 * CloudML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * CloudML is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with CloudML. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.cloudml.deployer.samples;


import javax.xml.xpath.XPath;
import org.cloudml.connectors.util.JXPath;
import org.cloudml.core.Deployment;
import org.cloudml.core.ExecuteInstance;
import org.cloudml.core.ExternalComponent;
import org.cloudml.core.ExternalComponentInstance;
import org.cloudml.core.InternalComponent;
import org.cloudml.core.InternalComponentInstance;
import org.cloudml.core.Property;
import org.cloudml.core.ProvidedExecutionPlatform;
import org.cloudml.core.ProvidedExecutionPlatformInstance;
import org.cloudml.core.ProvidedPort;
import org.cloudml.core.ProvidedPortInstance;
import org.cloudml.core.Provider;
import org.cloudml.core.Relationship;
import org.cloudml.core.RelationshipInstance;
import org.cloudml.core.RequiredExecutionPlatform;
import org.cloudml.core.RequiredPort;
import org.cloudml.core.RequiredPortInstance;
import org.cloudml.core.Resource;
import static org.cloudml.core.builders.Commons.aProvidedExecutionPlatform;
import static org.cloudml.core.builders.Commons.aProvidedPort;
import static org.cloudml.core.builders.Commons.aProvider;
import static org.cloudml.core.builders.Commons.aRelationship;
import static org.cloudml.core.builders.Commons.aRelationshipInstance;
import static org.cloudml.core.builders.Commons.aRequiredExecutionPlatform;
import static org.cloudml.core.builders.Commons.aRequiredPort;
import static org.cloudml.core.builders.Commons.aResource;
import static org.cloudml.core.builders.Commons.anExternalComponent;
import static org.cloudml.core.builders.Commons.anExternalComponentInstance;
import static org.cloudml.core.builders.Commons.anInternalComponent;
import static org.cloudml.core.builders.Commons.anInternalComponentInstance;
import org.cloudml.core.builders.DeploymentBuilder;
import org.cloudml.core.builders.ExternalComponentBuilder;
import org.cloudml.core.builders.InternalComponentBuilder;
import org.cloudml.core.builders.ProviderBuilder;
import org.cloudml.core.builders.RelationshipBuilder;
import org.cloudml.core.credentials.FileCredentials;
import org.cloudml.deployer.CloudAppDeployer;

/**
 * Sample deployment model for {@link http://osintegrators.com/PaasInstallNotes} on 
 * CloudBees. The sample use mysql database, and therefore requires a slightly 
 * adapted war-file than the one downloaded directly from the website (which
 * utilises HSQL).
 * 
 * Required files before deployment
 *  - war-file: "C:\\temp\\granny-common.war", which can be downloaded from 
 *    {@link https://www.dropbox.com/s/xu0dg4p9dwijght/granny-common.war}
 *  - credential: "c:\\temp\\cloudbees.credential", which you can obtain by 
 *    registering a free CloudBees account, or by asking me for a copy of ours
 *    {@link hui.song@sintef.no}
 * 
 * The deployment model is rather straightforward:
 * 
 * Type-level
 * 
 *     granny-war : InternalComponent  ------------> cbdb : ExternalComponent
 *                |                dbrel : Relationship
 *                | hostedby
 *                V
 *    granny-cloudml : ExternalComponent 
 * 
 * Instance-level
 * 
 *    granny-war-1 : grannywar  -----------> cbdb1 : cbdb
 *                 |
 *                 |  dbreli : dbrel
 *                 V
 *   granny-cloudml : granny-cloudml
 *
 */
public class PaasCloudBees 
{
    public static void main2(String[] args){
        Deployment dm = createCloudBeesDeployment();
        
        System.out.println(dm.getProviders().firstNamed("CloudBees").getProperties().iterator().next().getName());
        
        JXPath xpath = new JXPath("/providers[name='CloudBees']/properties/account");
        System.out.println(xpath.query(dm));
        
        RelationshipInstance ri = dm.getRelationshipInstances().iterator().next();
        ri.getProperties().add(new Property("aa_b","c"));
        //ri.getProvidedEnd().getOwner().get()
        
        //xpath = new JXPath("providedEnd/owner/value/publicAddress");
        //xpath = new JXPath("properties[name='aa::b']/value");
        xpath = new JXPath("properties/aa_b");
        System.out.println(xpath.query(ri));
    }
    
    public static void main(String[] args){
        
        Deployment dm = createCloudBeesDeployment();
        
        CloudAppDeployer deployer = new CloudAppDeployer();
        deployer.deploy(dm);
    }
    
    public static Deployment createCloudBeesDeployment(){         
 
        ExternalComponentBuilder dbb = anExternalComponent()
                .named("cbdb")
                .with(
                        aProvidedPort().named("db")
                )
                .providedBy("CloudBees");
        
        ExternalComponentBuilder wserverb = anExternalComponent()
                .named("granny-cloudml")
                .with(
                        aProvidedExecutionPlatform()
                            .named("TomCat")
                )
                .providedBy("CloudBees");
        
        InternalComponentBuilder grannyb = anInternalComponent()
                .named("granny-war")
                .with(aRequiredExecutionPlatform().named("TomCat"))
                .withProperty("warfile", "C:\\temp\\granny-common.war")
                .withProperty("temp-warfile", "C:\\temp\\granny-common-temp.war")
                .with(aRequiredPort().remote().named("db").mandatory());
        
        RelationshipBuilder relb = aRelationship()
                .from("granny-war", "db")
                .to("cbdb", "db")
                .named("dbrel")
                .withResource(
                        aResource()
                            .named("dbconfig")
                            .withProperty("valet","war-xml")
                            .withProperty("entry_spring", "WEB-INF/classes/META-INF/spring/app-context.xml")
                            .withProperty("path_dburl", "@self{properties/entry_spring}:://bean[@id=\"dataSource\"]/property[@name=\"url\"]/@value")
                            .withProperty("value_dburl", "jdbc:mysql://@instance{providedEnd/owner/value/publicAddress}")
                            .withProperty("path_dbuser", "@self{properties/entry_spring}:://bean[@id=\"dataSource\"]/property[@name=\"username\"]/@value")
                            .withProperty("value_dbuser", "@instance{providedEnd/owner/value/type/login}")
                            .withProperty("path_dbpassword", "@self{properties/entry_spring}:://bean[@id=\"dataSource\"]/property[@name=\"username\"]/@value")
                            .withProperty("value_dbpassword", "@instance{providedEnd/owner/value/type/passwd}")
//                            .withProperty("valet", "war-xml")
//                            .withProperty("path", "WEB-INF/classes/META-INF/spring/app-context.xml")
//                            .withProperty("#getPublicAddress", "//bean[@id=\"dataSource\"]/property[@name=\"url\"]/@value")
//                            .withProperty("@#getPublicAddress-prefix", "jdbc:mysql://")
//                            .withProperty("##getLogin","//bean[@id=\"dataSource\"]/property[@name=\"username\"]/@value")
//                            .withProperty("##getPasswd","//bean[@id=\"dataSource\"]/property[@name=\"password\"]/@value")
                );
                
                
        
        DeploymentBuilder dmb = new DeploymentBuilder()
                .named("cloudbees-deployment")
                .with(aProvider()
                        .named("CloudBees")
                        .withProperty("account", "mod4cloud")
                )
                .with(dbb)
                .with(grannyb)
                .with(wserverb)
                .with(relb);
        dmb.with(anExternalComponentInstance()
                .named("cbdb1")
                .ofType("cbdb")
            ).with(anExternalComponentInstance()
                .named("granny-cloudml")
                .ofType("granny-cloudml")
            ).with(anInternalComponentInstance()
                .named("granny-war-i")
                .ofType("granny-war")
                .hostedBy("granny-cloudml")
            ).with(aRelationshipInstance()
                .named("dbreli")
                .ofType("dbrel")
                .from("granny-war-i", "db")
                .to("cbdb1", "db")
            )
            ;
        Deployment dm = dmb.build();
        dm.getProviders().firstNamed("CloudBees").setCredentials(new FileCredentials("c:\\temp\\cloudbees.credential"));
        ExternalComponent c = dm.getComponents().onlyExternals().firstNamed("cbdb");
        c.setLogin("sintef");
        c.setPasswd("password123");
        System.out.println(dm);
        
        return dm;

    }

}
