package net.mjahn.tools.vizhier.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import net.mjahn.tools.vizhier.maven.plugin.model.PomMeta;
import org.apache.commons.io.FileUtils;
import org.apache.maven.schema.jaxb.Model;

/**
 * Goal which creates a graphical representation of the Maven hierarchy.
 *
 */
@Mojo(name = "viz", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresProject = false, threadSafe = true)
public class PomReaderMojo extends AbstractMojo {

    /**
     * Location of the pom file.
     */
    @Parameter(defaultValue = "${basedir}", property = "sourceDir", required = true)
    private File sourceDirectory;
    
    /**
     * Location of the output file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;
    
    /**
     * The class used to create the output.
     */
    // FIXME: implement dynamic interface for loading different output formatter
    @Parameter(defaultValue = "net.mjahn.tools.vizhier.maven.plugin.DotNotationFormatter", property = "outputFormatterClass", required = true)
    private String outputFormatterClass;
    
    // FIXME: allow a variety of different fillter mechanisms
    @Parameter(property = "dependencyInclusionPattern", required = false)
    private String dependencyInclusionPattern = null;
    
    private List<PomMeta> pomList;
    private static final FileFilter folderPassFilter = new FileFilter() {

        public boolean accept(File file) {
            if(file.isDirectory()) {
                // ok, we got a folder - no ignore some things (no hidden folders like .svn or .git, no target folder)
                if(!file.getName().startsWith("\\.") || !file.getName().endsWith("target")){
                    return true;
                }
            }
            return false;
        }
        
    };
    private static final FileFilter pomPassFilter = new FileFilter() {

        public boolean accept(File file) {
            if(!file.isDirectory()) {
                // ok, we got a file
                if(file.getName().equals("pom.xml")){
                    // hey! It's a pom
                    return true;
                }
            }
            return false;
        }
        
    };
    
    public void execute() throws MojoExecutionException {
        File pom = new File(sourceDirectory, "pom.xml");
        if (!pom.exists()) {
            getLog().error("No >>pom.xml<< file found in "+sourceDirectory+"! Aborting plug-in run.");
            throw new MojoExecutionException("Couldn't find pom.xml file for analysis!");
        }
        // ok, we got it, now load the pom and extract all relevant data
        pomList = new ArrayList<PomMeta>();
        try {
            getPomsFromSubFolders(sourceDirectory, pomList, null);
            // now prepare for the output
            if(!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            File outputFile = new File(outputDirectory, "graph.dot");
            outputFile.createNewFile();
            
            StringBuilder builder = new StringBuilder();
            DotNotationFormatter formatter = new DotNotationFormatter(pom,builder);
            
            Iterator<PomMeta> iter = pomList.iterator();
            while(iter.hasNext()) {
                formatter.printNodesForPom(iter.next());
            }
            formatter.closeFormatter();
            
            FileUtils.writeStringToFile(new File(outputDirectory, "graph.dot"), builder.toString());
        } catch (Exception e){
            throw new MojoExecutionException("Failed processing the pom hierarchy!", e);
        }
    }
    
    void getPomsFromSubFolders(File folder, List<PomMeta> poms, PomMeta parentPom) throws Exception {
        // first make sure, we really are in a folder
        if(folder.isDirectory()) {
            // now, get the pom in this directory (be more robust new File(folder,"pom.xml"); would have worked as well.
            File[] myPom = folder.listFiles(pomPassFilter);
            if(myPom != null && myPom.length == 1) {
                // we have one pom file in the folder, so let's go on
                PomMeta thisPom = getMetaDataFromPom(myPom[0]);
                // so this pom might be a module of the parent pom
                if(parentPom != null){
                    // the folder name is equal to the module. If they match, add the unique id of the module
                    parentPom.addModulePomId(folder.getName(), thisPom.getId());
                }
                poms.add(thisPom);
                // only with a pom in this hierarchy, go deeper into the rabbit hole
                File[] myFolders = folder.listFiles(folderPassFilter);
                if(myFolders != null && myFolders.length > 0) {
                    for(int i=0;i<myFolders.length;i++){
                        getPomsFromSubFolders(myFolders[i], poms, thisPom);
                    }
                }
            } else {
                System.out.println("no pom found in folder " + folder.getName());
                
            }
        }
    }
    
    PomMeta getMetaDataFromPom(File pomFile) throws Exception {
        Model model = getXMLFromFile(pomFile);
        PomMeta pm = new PomMeta(model);
        return pm;
    }
    
    private Model getXMLFromFile(File file) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Model.class.getPackage().getName(), this.getClass().getClassLoader());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement element = (JAXBElement) unmarshaller.unmarshal(file);
        return (Model) element.getValue();

    }
}
