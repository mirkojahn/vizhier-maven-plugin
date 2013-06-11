/*
 * 
 */
package net.mjahn.tools.vizhier.maven.plugin.model;

import org.apache.maven.schema.jaxb.Dependency;

/**
 *
 * @author Mirko Jahn <mirkojahn@gmail.com>
 */
public class ImportDependency {
    
    private final String id;
    private final String label;
    private final Dependency dep;
    
    public ImportDependency(Dependency dep) {
        this.dep = dep;
        StringBuilder idBuilder = new StringBuilder();
        StringBuilder labelBuilder = new StringBuilder();
        // groupid
        if(dep.getGroupId() != null && !dep.getGroupId().isEmpty()){
            idBuilder.append(dep.getGroupId());
        }
        // artifactid (always have to be present)
        idBuilder.append(dep.getArtifactId());
        labelBuilder.append(dep.getArtifactId());
        
        // set the 
        id = idBuilder.toString();
        label = labelBuilder.toString();
    }
    
    public String getId(){
        return id;
    }
    
    public String getLabel() {
        return label;
    }
}
