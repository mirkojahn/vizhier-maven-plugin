package net.mjahn.tools.vizhier.maven.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
                }
            }
        }
    }

    public List<String> getModulePomIds() {
        return modulePomIds;
    }
    
}
