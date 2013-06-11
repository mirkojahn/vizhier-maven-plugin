package net.mjahn.tools.vizhier.maven.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.schema.jaxb.Dependency;
import org.apache.maven.schema.jaxb.Model;
import org.apache.maven.schema.jaxb.Model.Modules;

/**
 *
 * @author Mirko Jahn <mirkojahn@gmail.com>
 */
public class PomMeta {
    
    private String artifactId = null;
    private String groupId = null;
    private String version = null;
    private String name = null;
    private PomType pomType = null;
    private Model model;
    private List<ImportDependency> importDeps;
    private final static String ARTIFACT_ID_SEPARATOR = ":";
    List<String> modulePomIds = new ArrayList<String>();
    
    
    // no sync needed in this class, we're immutable and an extra round doesn't hurt
    public PomMeta(Model model) {
        this.model = model;
    }

    public String getArtifactId() {
        if(artifactId == null){
            artifactId = model.getArtifactId();
        }
        return artifactId;
    }

    public String getGroupId() {
        if(groupId == null) {
            groupId = model.getGroupId();
            // the group id might be inherited, so check parent
            if(groupId == null && model.getParent() != null) {
                groupId = model.getParent().getGroupId();
            }
        }
        return groupId;
    }

    public String getVersion() {
        if(version == null) {
            version = model.getVersion();
            // the version might be inherited, so check parent
            if(version == null && model.getParent() != null) {
                version = model.getParent().getVersion();
            }
        }
        return version;
    }

    public Model getModel() {
        return model;
    }
    
    public String getPomName(){
        // well, something like this would be nice, but then we do not have property replacement...
//        if(name == null){
//            name = model.getName();
//            if(name == null){
//                name = getId();
//            }
//        }
//        return name;
        
        // so do it the easy way for now (might not be unique, but shorter)
        return getArtifactId();
    }
    
    public String getId() {
        return getGroupId() + ARTIFACT_ID_SEPARATOR + getArtifactId();
    }
    
    public PomType getPomType() {
        if(pomType == null) {
            pomType = PomType.get(model.getPackaging());
        }
        return pomType;
    }
    
    public boolean hasParent() {
        if(model.getParent() != null && model.getParent().getArtifactId() != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public String getParentId() {
        if(hasParent()) {
            return model.getParent().getGroupId() + ARTIFACT_ID_SEPARATOR + model.getParent().getArtifactId();
        }
        return null;
    }
    
    public void addModulePomId(String folderName, String modulePomId) {
        //if foldername is part of module list... add modulePomId
        Modules modules = model.getModules();
        if(modules != null && modules.getModule() != null && !modules.getModule().isEmpty()){
            Iterator<String> iter = modules.getModule().iterator();
            while(iter.hasNext()) {
                String myModule = iter.next();
                if(myModule.equalsIgnoreCase(folderName)) {
                    // we got a hit! Add the id as a sub module
                    modulePomIds.add(modulePomId);
                    // break here, we got enough
                    return; 
                } else {
                    // FIXME: this looks like a break in the inheritance structure, maybe its good to comment?
                    System.out.println("the pom "+getId()+" is not represented with a module dependency in its parent: "+modulePomId);
                }
            }
        }
    }

    /*
     * 
 <dependencyManagement>
   <dependencies>
     <dependency>
       <groupId>org.test</groupId>
       <artifactId>ThirdParty-BOM</artifactId>
       <version>1.0</version>
       <type>pom</type>
       <scope>import</scope>
     </dependency>
   </dependencies>
 </dependencyManagement>
     * 
     */
    public List<ImportDependency> getImportDependencies(){
        if(importDeps == null || importDeps.isEmpty()) {
            importDeps = new ArrayList<ImportDependency>();
            try {
                // prevent NPE's
                List<Dependency>  deps = model.getDependencyManagement().getDependencies().getDependency();
                if(deps != null && !deps.isEmpty()){
                    // ok we at least have such dependencies, so check them out
                    Iterator<Dependency> iter = deps.iterator();
                    while(iter.hasNext()){
                        Dependency dep = iter.next();
                        if(dep.getScope() != null && "import".equals(dep.getScope())) {
                            // ok, we got one... now add it to the list
                            importDeps.add(new ImportDependency(dep));
                        }
                    }
                }

            } catch (Exception e){
                // do nothing here - not relevant for now
            }
        }
        return importDeps;
    }
    
    public List<String> getModulePomIds() {
        return modulePomIds;
    }
    
}
