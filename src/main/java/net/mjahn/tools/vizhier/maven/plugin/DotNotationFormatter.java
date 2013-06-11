package net.mjahn.tools.vizhier.maven.plugin;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.mjahn.tools.vizhier.maven.plugin.model.ImportDependency;
import net.mjahn.tools.vizhier.maven.plugin.model.PomMeta;
import net.mjahn.tools.vizhier.maven.plugin.model.PomType;

/**
 *
 * @author Mirko Jahn <mirkojahn@gmail.com>
 */
public class DotNotationFormatter {
    
    private File pomFile;
    private StringBuilder builder;
    private String filterPattern;

    public DotNotationFormatter(File pomLocation, StringBuilder builder) {
        pomFile = pomLocation;
        this.builder = builder;
        addPreamble(this.pomFile.getPath());
        
    }
    
    public void printNodesForPom(PomMeta pom) {
        addNode(pom);
        addParentDependency(pom);
        addModuleDependency(pom);
        addDependencies(pom);
        addImports(pom);
    }
    
    public void closeFormatter(){
        builder.append("\n}");
    }
    
    private void addPreamble(String pomLocation) {
        builder.append("/* \n");
        builder.append(" * DOT File (use a tool like GraphViz to visualize)\n");
        builder.append(" * \n");
        builder.append(" * Created at ");
        builder.append(new Date());
        builder.append(" by the vizhier-maven-plugin \n");
        builder.append(" * on POM: ");
        builder.append(pomLocation);
        builder.append("\n");
        builder.append(" * \n");
        builder.append(" */\n");
        builder.append("digraph pomHierarchy { \n");
    }
    
    public void addNode(PomMeta pom){
        // write the node description
        builder.append("\t");
        builder.append(escape(pom.getId()));
        builder.append(" [shape=box,color=");
        builder.append(getColorForPomType(pom.getPomType()));
        builder.append(",style=bold,label=\"");
        builder.append(pom.getPomName());
        builder.append("\"];\n");
        
    }
    
    public void addParentDependency(PomMeta pom) {
        if(pom.hasParent()) {
            builder.append("\t");
            builder.append(escape(pom.getId()));
            builder.append(" -> ");
            builder.append(escape(pom.getParentId()));
            builder.append(" [label=\"parent\",color=violet,weight=0.9];\n");
        }
    }
    
    public void addModuleDependency(PomMeta pom) {
        // FIXME: implement this!
        List<String> pomIds = pom.getModulePomIds();
        if(pomIds != null && !pomIds.isEmpty()){
            Iterator<String> iter = pomIds.iterator();
            while(iter.hasNext()){
                // FIXME: in case the node is not know, it will not have a nice label 
                // this usecase is very likely, so potentially create a node for this?!
                
                builder.append("\t");
                builder.append(escape(pom.getId()));
                builder.append(" -> ");
                builder.append(escape(iter.next()));
                builder.append(" [label=\"hasModule\",color=green,weight=0.9];\n");
            }
        }
    }
    
    public void addDependencies(PomMeta pom) {
        // FIXME: implement this!
        // x -> y [label="dependsOn|with:(scope:compile|runtime|test)",color=black,weight=0.2]
    }
    
    public void addImports(PomMeta pom) {
        // FIXME: implement this!
        List<ImportDependency> deps = pom.getImportDependencies();
        if( deps != null && !deps.isEmpty()) {
            Iterator<ImportDependency> iter = deps.iterator();
            while(iter.hasNext()){
                ImportDependency dep = iter.next();
                builder.append("\t");
                builder.append(escape(pom.getId()));
                builder.append(" -> ");
                builder.append(escape(dep.getId()));
                builder.append(" [label=\"imports config\",color=orange,weight=0.5,style=dotted];\n");
            }
        }
        // x -> y [label="imports config",color=orange,weight=0.5,style=dotted]
    }
    
    private String getColorForPomType(PomType type) {
        if(type == PomType.JAR || type == PomType.BUNDLE) {
            return "blue";
        } else if(type == PomType.WAR) {
            return "green";
        } else if(type == PomType.PLUGIN) {
            return "violet";
        } else {
            // most likely a pom, but this is also a catch all
            return "black";
        }
    }
    
    private String escape(String st) {
        return st.replace(".", "").replace(":", "");
    }
}
