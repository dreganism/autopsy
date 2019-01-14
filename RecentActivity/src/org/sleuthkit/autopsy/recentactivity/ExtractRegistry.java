/*
 *
 * Autopsy Forensic Browser
 *
 * Copyright 2012-2019 Basis Technology Corp.
 *
 * Copyright 2012 42six Solutions.
 * Contact: aebadirad <at> 42six <dot> com
 * Project Contact/Architect: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.recentactivity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.collections4.CollectionUtils;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coreutils.ExecUtil;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProcessTerminator;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestModule.IngestModuleException;
import org.sleuthkit.autopsy.keywordsearchservice.KeywordSearchService;
import org.sleuthkit.autopsy.recentactivity.UsbDeviceIdMapper.USBInfo;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_DEVICE_ATTACHED;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_OS_ACCOUNT;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_OS_INFO;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_RECENT_OBJECT;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_REMOTE_DRIVE;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_WIFI_NETWORK;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_ACCESSED;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DEVICE_ID;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DEVICE_MAKE;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DEVICE_MODEL;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DOMAIN;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_LOCAL_PATH;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ORGANIZATION;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_OWNER;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PATH;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PROCESSOR_ARCHITECTURE;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PRODUCT_ID;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PROG_NAME;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_REMOTE_PATH;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_SSID;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_TEMP_DIR;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_USER_ID;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_USER_NAME;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_VALUE;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_VERSION;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.ReadContentInputStream.ReadContentInputStreamException;
import org.sleuthkit.datamodel.Report;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Extract windows registry data using regripper. Runs two versions of
 * regripper. One is the generally available set of plug-ins and the second is a
 * set that were customized for Autopsy to produce a more structured output of
 * XML so that we can parse and turn into blackboard artifacts.
 */
@NbBundle.Messages({
    "RegRipperNotFound=Autopsy RegRipper executable not found.",
    "RegRipperFullNotFound=Full version RegRipper executable not found."
})
class ExtractRegistry extends Extract {

    private static final Logger logger = Logger.getLogger(ExtractRegistry.class.getName());
    private static final String MODULE_NAME = NbBundle.getMessage(ExtractIE.class, "ExtractRegistry.moduleName.text");
    private final static String PARENT_MODULE_NAME = NbBundle.getMessage(ExtractRegistry.class, "ExtractRegistry.parentModuleName.noSpace");
    final private static UsbDeviceIdMapper USB_MAPPER = new UsbDeviceIdMapper();
    final private static String RIP_EXE = "rip.exe";
    final private static String RIP_PL = "rip.pl";
    final private static ImmutableList<String> REG_FILE_NAMES = ImmutableList.of("system", "software", "security", "sam"); //NON-NLS
    private final Path rrHome;  // Path to the Autopsy version of RegRipper
    private final Path rrFullHome; // Path to the full version of RegRipper
    private Content dataSource;
    private IngestJobContext context;

    private final List<String> rrCmd = new ArrayList<>();
    private final List<String> rrFullCmd = new ArrayList<>();

    ExtractRegistry() throws IngestModuleException {
        InstalledFileLocator installedFileLocator = InstalledFileLocator.getDefault();

        final File rrRoot = installedFileLocator.locate("rr", ExtractRegistry.class.getPackage().getName(), false); //NON-NLS
        if (rrRoot == null) {
            throw new IngestModuleException(Bundle.RegRipperNotFound());
        }
        final File rrFullRoot = installedFileLocator.locate("rr-full", ExtractRegistry.class.getPackage().getName(), false); //NON-NLS
        if (rrFullRoot == null) {
            throw new IngestModuleException(Bundle.RegRipperFullNotFound());
        }

        String executableToRun = PlatformUtil.isWindowsOS() ? RIP_EXE : RIP_PL;

        rrHome = rrRoot.toPath();
        String rrPath = rrHome.resolve(executableToRun).toString();
        if (!(new File(rrPath).exists())) {
            throw new IngestModuleException(Bundle.RegRipperNotFound());
        }

        rrFullHome = rrFullRoot.toPath();
        String rrFullPath = rrFullHome.resolve(executableToRun).toString();
        if (!(new File(rrFullPath).exists())) {
            throw new IngestModuleException(Bundle.RegRipperFullNotFound());
        }

        if (PlatformUtil.isWindowsOS()) {
            rrCmd.add(rrPath);
            rrFullCmd.add(rrFullPath);
        } else {
            String perl;
            File usrBin = new File("/usr/bin/perl");
            File usrLocalBin = new File("/usr/local/bin/perl");
            if (usrBin.canExecute() && usrBin.exists() && !usrBin.isDirectory()) {
                perl = "/usr/bin/perl";
            } else if (usrLocalBin.canExecute() && usrLocalBin.exists() && !usrLocalBin.isDirectory()) {
                perl = "/usr/local/bin/perl";
            } else {
                throw new IngestModuleException("perl not found in your system");
            }
            rrCmd.add(perl);
            rrCmd.add(rrPath);
            rrFullCmd.add(perl);
            rrFullCmd.add(rrFullPath);
        }
    }

    /**
     * Search for the registry hives on the system.
     */
    private List<AbstractFile> findRegistryFiles() {
        List<AbstractFile> allRegistryFiles = new ArrayList<>();

        // find the user-specific ntuser-dat files
        try {
            allRegistryFiles.addAll(fileManager.findFiles(dataSource, "ntuser.dat")); //NON-NLS
        } catch (TskCoreException ex) {
            logger.log(Level.WARNING, "Error fetching 'ntuser.dat' file.", ex); //NON-NLS
        }

        // find the system hives'
        for (String regFileName : REG_FILE_NAMES) {
            try {
                allRegistryFiles.addAll(fileManager.findFiles(dataSource, regFileName, "/system32/config")); //NON-NLS
            } catch (TskCoreException ex) {
                String msg = NbBundle.getMessage(ExtractRegistry.class,
                        "ExtractRegistry.findRegFiles.errMsg.errReadingFile", regFileName);
                logger.log(Level.WARNING, msg, ex);
                this.addErrorMessage(this.getModuleName() + ": " + msg);
            }
        }
        return allRegistryFiles;
    }

    /**
     * Identifies registry files in the database by mtimeItem, runs regripper on
     * them, and parses the output.
     */
    private void analyzeRegistryFiles() {
        List<AbstractFile> allRegistryFiles = findRegistryFiles();

        // open the log file
        FileWriter logFile = null;
        try {
            logFile = new FileWriter(RAImageIngestModule.getRAOutputPath(currentCase, "reg") + File.separator + "regripper-info.txt"); //NON-NLS
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        for (AbstractFile regFile : allRegistryFiles) {
            String regFileName = regFile.getName();
            long regFileId = regFile.getId();
            String regFileNameLocal = RAImageIngestModule.getRATempPath(currentCase, "reg") + File.separator + regFileName;
            String outputPathBase = RAImageIngestModule.getRAOutputPath(currentCase, "reg") + File.separator + regFileName + "-regripper-" + Long.toString(regFileId); //NON-NLS
            File regFileNameLocalFile = new File(regFileNameLocal);
            try {
                ContentUtils.writeToFile(regFile, regFileNameLocalFile, context::dataSourceIngestIsCancelled);
            } catch (ReadContentInputStreamException ex) {
                logger.log(Level.WARNING, String.format("Error reading registry file '%s' (id=%d).",
                        regFile.getName(), regFileId), ex); //NON-NLS
                this.addErrorMessage(
                        NbBundle.getMessage(this.getClass(), "ExtractRegistry.analyzeRegFiles.errMsg.errWritingTemp",
                                this.getModuleName(), regFileName));
                continue;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, String.format("Error writing temp registry file '%s' for registry file '%s' (id=%d).",
                        regFileNameLocal, regFile.getName(), regFileId), ex); //NON-NLS
                this.addErrorMessage(
                        NbBundle.getMessage(this.getClass(), "ExtractRegistry.analyzeRegFiles.errMsg.errWritingTemp",
                                this.getModuleName(), regFileName));
                continue;
            }

            if (context.dataSourceIngestIsCancelled()) {
                break;
            }

            try {
                if (logFile != null) {
                    logFile.write(Long.toString(regFileId) + "\t" + regFile.getUniquePath() + "\n");
                }
            } catch (TskCoreException | IOException ex) {
                logger.log(Level.SEVERE, "Error writing to Regisistry parsing module log file.", ex);
            }

            logger.log(Level.INFO, "{0}- Now getting registry information from {1}", new Object[]{getModuleName(), regFileNameLocal}); //NON-NLS
            RegOutputFiles regOutputFiles = ripRegistryFile(regFileNameLocal, outputPathBase);
            if (context.dataSourceIngestIsCancelled()) {
                break;
            }

            // parse the autopsy-specific output
            if (isNotEmpty(regOutputFiles.autopsyPlugins)) {
                if (parseAutopsyPluginOutput(regOutputFiles.autopsyPlugins, regFile) == false) {
                    this.addErrorMessage(
                            NbBundle.getMessage(this.getClass(), "ExtractRegistry.analyzeRegFiles.failedParsingResults",
                                    this.getModuleName(), regFileName));
                }
            }

            // create a report for the full output
            if (isNotEmpty(regOutputFiles.fullPlugins)) {
                try {
                    Report report = currentCase.addReport(regOutputFiles.fullPlugins,
                            NbBundle.getMessage(this.getClass(), "ExtractRegistry.parentModuleName.noSpace"),
                            "RegRipper " + regFile.getUniquePath(), regFile); //NON-NLS

                    // Index the report content so that it will be available for keyword search.
                    KeywordSearchService searchService = Lookup.getDefault().lookup(KeywordSearchService.class);
                    if (null == searchService) {
                        logger.log(Level.WARNING, "Keyword search service not found. Report will not be indexed");
                    } else {
                        searchService.index(report);
                    }
                } catch (TskCoreException e) {
                    this.addErrorMessage("Error adding regripper output as Autopsy report: " + e.getLocalizedMessage()); //NON-NLS
                }
            }

            // delete the hive
            regFileNameLocalFile.delete();
        }

        try {
            if (logFile != null) {
                logFile.close();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String getModuleName() {
        return MODULE_NAME;
    }

    private class RegOutputFiles {

        public String autopsyPlugins = "";
        public String fullPlugins = "";
    }

    /**
     * Execute regripper on the given registry.
     *
     * @param regFilePath     Path to local copy of registry
     * @param outFilePathBase Path to location to save output file to. Base
     *                        mtimeItem that will be extended on
     */
    private RegOutputFiles ripRegistryFile(String regFilePath, String outFilePathBase) {
        String autopsyType = "";    // Type argument for rr for autopsy-specific modules
        String fullType;   // Type argument for rr for full set of modules

        RegOutputFiles regOutputFiles = new RegOutputFiles();

        if (regFilePath.toLowerCase().contains("system")) { //NON-NLS
            autopsyType = "autopsysystem"; //NON-NLS
            fullType = "system"; //NON-NLS
        } else if (regFilePath.toLowerCase().contains("software")) { //NON-NLS
            autopsyType = "autopsysoftware"; //NON-NLS
            fullType = "software"; //NON-NLS
        } else if (regFilePath.toLowerCase().contains("ntuser")) { //NON-NLS
            autopsyType = "autopsyntuser"; //NON-NLS
            fullType = "ntuser"; //NON-NLS
        } else if (regFilePath.toLowerCase().contains("sam")) { //NON-NLS
            fullType = "sam"; //NON-NLS
        } else if (regFilePath.toLowerCase().contains("security")) { //NON-NLS
            fullType = "security"; //NON-NLS
        } else {
            return regOutputFiles;
        }

        // run the autopsy-specific set of modules
        if (!autopsyType.isEmpty()) {
            regOutputFiles.autopsyPlugins = outFilePathBase + "-autopsy.txt"; //NON-NLS
            String errFilePath = outFilePathBase + "-autopsy.err.txt"; //NON-NLS
            logger.log(Level.INFO, "Writing RegRipper results to: {0}", regOutputFiles.autopsyPlugins); //NON-NLS
            executeRegRipper(rrCmd, rrHome, regFilePath, autopsyType, regOutputFiles.autopsyPlugins, errFilePath);
        }
        if (context.dataSourceIngestIsCancelled()) {
            return regOutputFiles;
        }

        // run the full set of rr modules
        if (!fullType.isEmpty()) {
            regOutputFiles.fullPlugins = outFilePathBase + "-full.txt"; //NON-NLS
            String errFilePath = outFilePathBase + "-full.err.txt"; //NON-NLS
            logger.log(Level.INFO, "Writing Full RegRipper results to: {0}", regOutputFiles.fullPlugins); //NON-NLS
            executeRegRipper(rrFullCmd, rrFullHome, regFilePath, fullType, regOutputFiles.fullPlugins, errFilePath);
        }
        return regOutputFiles;
    }

    private void executeRegRipper(List<String> regRipperPath, Path regRipperHomeDir, String hiveFilePath, String hiveFileType, String outputFile, String errFile) {
        try {
            List<String> commandLine = new ArrayList<>(regRipperPath);
            commandLine.add("-r"); //NON-NLS
            commandLine.add(hiveFilePath);
            commandLine.add("-f"); //NON-NLS
            commandLine.add(hiveFileType);

            ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            processBuilder.directory(regRipperHomeDir.toFile()); // RegRipper 2.8 has to be run from its own directory
            processBuilder.redirectOutput(new File(outputFile));
            processBuilder.redirectError(new File(errFile));
            ExecUtil.execute(processBuilder, new DataSourceIngestModuleProcessTerminator(context));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to run RegRipper", ex); //NON-NLS
            this.addErrorMessage(NbBundle.getMessage(this.getClass(), "ExtractRegistry.execRegRip.errMsg.failedAnalyzeRegFile", this.getModuleName()));
        }
    }

    // @@@ VERIFY that we are doing the right thing when we parse multiple NTUSER.DAT
    /**
     *
     * @param regFilePath     Path to the output file produced by RegRipper.
     * @param regAbstractFile File object for registry that we are parsing (to
     *                        make blackboard artifacts with)
     *
     * @return
     */
    private boolean parseAutopsyPluginOutput(String regFilePath, AbstractFile regAbstractFile) {
        // Read the file in and create a Document and elements
        Document doc = parseRegistryDoc(regFilePath);

        if (doc == null) {
            return false;
        }

        SleuthkitCase caseDB = currentCase.getSleuthkitCase();

        //collection of BlackboardArtifacts  that we will post to blackboard for additional processing.
        Collection<BlackboardArtifact> artifacts = new HashSet<>();
        // cycle through the elements in the doc
        NodeList children = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Element tempnode = (Element) children.item(i);

            NodeList artroots = tempnode.getElementsByTagName("artifacts"); //NON-NLS
            if (artroots.getLength() == 0) {
                // If there isn't an artifact node, skip this entry
                continue;
            }
            NodeList myartlist = artroots.item(0).getChildNodes();

            String dataType = tempnode.getNodeName();
            // If all artifact nodes should really go under one Blackboard artifact, need to process it differently
            switch (dataType) {
                case "WinVersion":
                    artifacts.addAll(processWinVersion(myartlist, caseDB, regAbstractFile));
                    break;
                case "Profiler":
                    artifacts.addAll(processProfiler(myartlist, caseDB, regAbstractFile));
                    break;
                case "CompName":
                    artifacts.addAll(processCompName(myartlist, caseDB, regAbstractFile));
                    break;
                default:
                    NodeList timenodes = tempnode.getElementsByTagName("mtime"); //NON-NLS
                    Long mtime = null;
                    if (timenodes.getLength() > 0) {
                        String etime = timenodes.item(0).getTextContent();
                        try {
                            mtime = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy").parse(etime).getTime() / 1000;
                        } catch (ParseException ex) {
                            logger.log(Level.WARNING, "Failed to parse epoch time when parsing the registry."); //NON-NLS
                        }
                    }
                    artifacts.addAll(
                            processOtherDataType(myartlist, dataType, regAbstractFile, mtime));
                    break;
            }
        } // for

        if (CollectionUtils.isNotEmpty(artifacts)) {
            try {
                blackboard.postArtifacts(artifacts, PARENT_MODULE_NAME);
            } catch (Blackboard.BlackboardException ex) {
                logger.log(Level.SEVERE, "Error while trying to post usb device artifact.", ex); //NON-NLS
                this.addErrorMessage(Bundle.Extractor_errPostingArtifacts(getModuleName()));
            }
        }
        return true;
    }

    private Document parseRegistryDoc(final String regFilePath) {
        try (FileInputStream fstream = new FileInputStream(regFilePath);) {
            String regString;
            regString = new Scanner(fstream, "UTF-8").useDelimiter("\\Z").next();//NON-NLS
            String result = regString.replaceAll("----------------------------------------", "") // NON-NLS
                    .replaceAll("\\n", "") // NON-NLS
                    .replaceAll("\\r", "") // NON-NLS
                    .replaceAll("'", "&apos;") // NON-NLS
                    .replaceAll("&", "&amp;") // NON-NLS
                    .replace('\0', ' ');  // NON-NLS
            String stringdoc = "<?xml version=\"1.0\"?><document>" + result + "</document>";  // NON-NLS
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(stringdoc)));
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "Error configuring the registry parser: {0}", ex); //NON-NLS
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, "Error parsing the registry XML: {0}", ex); //NON-NLS
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "Error finding the registry file."); //NON-NLS     
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error building the document parser: {0}", ex); //NON-NLS
        }
        return null;
    }

    private Collection<BlackboardArtifact> processOtherDataType(NodeList myartlist, String dataType, AbstractFile regAbstractFile, Long mtime) throws IllegalArgumentException, DOMException {
        Collection<BlackboardArtifact> artifacts = new HashSet<>();
        for (int j = 0; j < myartlist.getLength(); j++) {
            Node artchild = myartlist.item(j);

            if (artchild.hasAttributes()) { // If it has attributes, then it is an Element (based off API)
                Element artnode = (Element) artchild;
                String value = artnode.getTextContent().trim();

                Optional<BlackboardArtifact> artifact = Optional.empty();
                switch (dataType) {
                    case "recentdocs": //NON-NLS
                        // BlackboardArtifact bbart = tempDb.getContentById(orgId).newArtifact(ARTIFACT_TYPE.TSK_RECENT_OBJECT);
                        // bbattributes.add(new BlackboardAttribute(ATTRIBUTE_TYPE.TSK_LAST_ACCESSED.getTypeID(), "RecentActivity", dataType, mtime));
                        // bbattributes.add(new BlackboardAttribute(ATTRIBUTE_TYPE.TSK_NAME.getTypeID(), "RecentActivity", dataType, mtimeItem));
                        // bbattributes.add(new BlackboardAttribute(ATTRIBUTE_TYPE.TSK_VALUE.getTypeID(), "RecentActivity", dataType, value));
                        // bbart.addAttributes(bbattributes);
                        // @@@ BC: Why are we ignoring this...
                        break;
                    case "usb"://NON-NLS
                        artifact = processUSB(artnode, regAbstractFile, value);
                        break;
                    case "uninstall"://NON-NLS
                        artifact = processUninstall(artnode, value, regAbstractFile);
                        break;
                    case "office"://NON-NLS
                        artifact = processOffice(artnode, regAbstractFile, mtime, value);
                        break;
                    case "ProcessorArchitecture": //NON-NLS
                        // Architecture is now included under Profiler
                        break;
                    case "ProfileList"://NON-NLS
                        artifact = processProfileList(value, artnode, regAbstractFile);
                        break;
                    case "NtuserNetwork"://NON-NLS
                        artifact = processNtuserNetwork(artnode, value, regAbstractFile);
                        break;
                    case "SSID":
                        artifact = processSSID(artnode, value, regAbstractFile);
                        break;
                    case "shellfolders": // NON-NLS
                        // The User Shell Folders subkey stores the paths to Windows Explorer folders for the current user of the computer
                        // (https://technet.microsoft.com/en-us/library/Cc962613.aspx).
                        // No useful information. Skip.
                        break;
                    default:
                        logger.log(Level.WARNING, "Unrecognized node name: {0}", dataType); //NON-NLS
                        break;
                }
                artifact.ifPresent(artifacts::add);
            }
        }
        return artifacts;
    }

    private Optional<BlackboardArtifact> processSSID(Element artnode, String SSID, AbstractFile regAbstractFile) {

        try {

            List<BlackboardAttribute> bbattributes = Arrays.asList(
                    new BlackboardAttribute(
                            TSK_SSID, PARENT_MODULE_NAME,
                            SSID),
                    new BlackboardAttribute(
                            TSK_DATETIME, PARENT_MODULE_NAME,
                            Long.parseLong(artnode.getAttribute("writeTime"))), //NON-NLS
                    new BlackboardAttribute(
                            TSK_DEVICE_ID, PARENT_MODULE_NAME,
                            artnode.getAttribute("adapter")));  //NON-NLS

            BlackboardArtifact bbart = regAbstractFile.newArtifact(TSK_WIFI_NETWORK);
            bbart.addAttributes(bbattributes);
            return Optional.of(bbart);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding SSID artifact to blackboard."); //NON-NLS
        }
        return Optional.empty();
    }

    private Optional<BlackboardArtifact> processNtuserNetwork(Element artnode, String remoteName, AbstractFile regAbstractFile) throws IllegalArgumentException {

        try {
            List<BlackboardAttribute> bbattributes = Arrays.asList(
                    new BlackboardAttribute(
                            TSK_LOCAL_PATH, PARENT_MODULE_NAME,
                            artnode.getAttribute("localPath")), //NON-NLS
                    new BlackboardAttribute(
                            TSK_REMOTE_PATH, PARENT_MODULE_NAME,
                            remoteName));

            BlackboardArtifact bbart = regAbstractFile.newArtifact(TSK_REMOTE_DRIVE);
            bbart.addAttributes(bbattributes);
            return Optional.of(bbart);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding network drive artifact to blackboard."); //NON-NLS
        }
        return Optional.empty();
    }

    private Optional<BlackboardArtifact> processProfileList(String homeDir, Element artnode, AbstractFile regFile) throws IllegalArgumentException {
        String sid = artnode.getAttribute("sid"); //NON-NLS
        String username = artnode.getAttribute("username"); //NON-NLS
        BlackboardArtifact bbart = null;

        try {
            BlackboardAttribute.Type userAttrType = new BlackboardAttribute.Type(TSK_USER_ID);

            //check if any of the existing artifacts match this username
            ArrayList<BlackboardArtifact> existingArtifacts = currentCase.getSleuthkitCase().getBlackboardArtifacts(TSK_OS_ACCOUNT);

            for (BlackboardArtifact artifact : existingArtifacts) {
                if (artifact.getDataSource().getId() == regFile.getDataSourceObjectId()) {
                    BlackboardAttribute attribute = artifact.getAttribute(userAttrType);
                    if (attribute != null && attribute.getValueString().equals(sid)) {
                        bbart = artifact;
                        break;
                    }
                }
            }
        } catch (TskCoreException ex) {
            logger.log(Level.WARNING, "Error getting existing os account artifact", ex);
        }
        try {
            Collection<BlackboardAttribute> bbattributes = new ArrayList<>();
            if (bbart == null) {
                //create new artifact
                bbart = regFile.newArtifact(TSK_OS_ACCOUNT);
                bbattributes.addAll(Arrays.asList(
                        new BlackboardAttribute(
                                TSK_USER_NAME, MODULE_NAME,
                                username),
                        new BlackboardAttribute(
                                TSK_USER_ID, MODULE_NAME,
                                sid),
                        new BlackboardAttribute(
                                TSK_PATH, MODULE_NAME,
                                homeDir)
                ));
            } else {
                //add attributes to existing artifact
                if (bbart.getAttribute(new BlackboardAttribute.Type(TSK_USER_NAME)) == null) {
                    bbattributes.add(new BlackboardAttribute(
                            TSK_USER_NAME, MODULE_NAME,
                            username));
                }
                if (bbart.getAttribute(new BlackboardAttribute.Type(TSK_PATH)) == null) {
                    bbattributes.add(new BlackboardAttribute(
                            TSK_PATH, MODULE_NAME,
                            homeDir));
                }
            }
            bbart.addAttributes(bbattributes);
            // index the artifact for keyword search
            blackboard.postArtifact(bbart, MODULE_NAME);
            return Optional.of(bbart);
        } catch (TskCoreException | Blackboard.BlackboardException ex2) {
            logger.log(Level.SEVERE, "Error adding account artifact to blackboard.", ex2); //NON-NLS
        }
        //NON-NLS
        return Optional.empty();
    }

    private Optional<BlackboardArtifact> processOffice(Element artnode, AbstractFile regAbstractFile, Long mtime, String value) throws IllegalArgumentException {
        try {
            List<BlackboardAttribute> bbattributes = Lists.newArrayList(
                    new BlackboardAttribute(
                            TSK_NAME, PARENT_MODULE_NAME,
                            artnode.getAttribute("name")), //NON-NLS
                    new BlackboardAttribute(
                            TSK_VALUE, PARENT_MODULE_NAME,
                            value),
                    new BlackboardAttribute(
                            TSK_PROG_NAME, PARENT_MODULE_NAME,
                            artnode.getNodeName()));

            // @@@ BC: Consider removing this after some more testing. It looks like an Mtime associated with the root key and not the individual item
            if (mtime != null) {
                bbattributes.add(new BlackboardAttribute(TSK_DATETIME_ACCESSED, PARENT_MODULE_NAME, mtime));
            }
            BlackboardArtifact bbart = regAbstractFile.newArtifact(TSK_RECENT_OBJECT);
            bbart.addAttributes(bbattributes);
            return Optional.of(bbart);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding recent object artifact to blackboard."); //NON-NLS
        }
        return Optional.empty();
    }

    private Optional<BlackboardArtifact> processUninstall(Element artnode, String progName, AbstractFile regAbstractFile) throws IllegalArgumentException {
        Long itemMtime = null;
        try {
            Long epochtime = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy").parse(artnode.getAttribute("mtime")).getTime(); //NON-NLS
            itemMtime = epochtime / 1000;
        } catch (ParseException e) {
            logger.log(Level.WARNING, "Failed to parse epoch time for installed program artifact."); //NON-NLS
        }

        try {
            List<BlackboardAttribute> bbattributes = Lists.newArrayList(
                    new BlackboardAttribute(
                            TSK_PROG_NAME, PARENT_MODULE_NAME,
                            progName),
                    new BlackboardAttribute(
                            TSK_DATETIME, PARENT_MODULE_NAME,
                            itemMtime));
            BlackboardArtifact bbart = regAbstractFile.newArtifact(ARTIFACT_TYPE.TSK_INSTALLED_PROG);
            bbart.addAttributes(bbattributes);
            return Optional.of(bbart);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding installed program artifact to blackboard."); //NON-NLS
        }
        return Optional.empty();
    }

    private Optional<BlackboardArtifact> processUSB(Element artnode, AbstractFile regAbstractFile, String deviceID) throws IllegalArgumentException {

        try {
            String model = artnode.getAttribute("dev"); //NON-NLS
            String make = "";
            if (model.toLowerCase().contains("vid")) { //NON-NLS
                USBInfo info = USB_MAPPER.parseAndLookup(model);
                if (info.getVendor() != null) {
                    make = info.getVendor();
                }
                if (info.getProduct() != null) {
                    model = info.getProduct();
                }
            }

            List<BlackboardAttribute> bbattributes = Lists.newArrayList(
                    new BlackboardAttribute(
                            TSK_DATETIME, PARENT_MODULE_NAME,
                            Long.parseLong(artnode.getAttribute("mtime"))),
                    new BlackboardAttribute(
                            TSK_DEVICE_MAKE, PARENT_MODULE_NAME,
                            make),
                    new BlackboardAttribute(
                            TSK_DEVICE_MODEL, PARENT_MODULE_NAME,
                            model),
                    new BlackboardAttribute(
                            TSK_DEVICE_ID, PARENT_MODULE_NAME,
                            deviceID));
            BlackboardArtifact bbart = regAbstractFile.newArtifact(TSK_DEVICE_ATTACHED);
            bbart.addAttributes(bbattributes);
            return Optional.of(bbart);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding device attached artifact to blackboard."); //NON-NLS
        }
        return Optional.empty();
    }

    private Collection<BlackboardArtifact> processCompName(NodeList myartlist, SleuthkitCase caseDB, AbstractFile regAbstractFile) throws DOMException, IllegalArgumentException {
        String compName = "";
        String domain = "";
        for (int j = 0; j < myartlist.getLength(); j++) {
            Node artchild = myartlist.item(j);
            // If it has attributes, then it is an Element (based off API)
            if (artchild.hasAttributes()) {
                Element artnode = (Element) artchild;

                String value = artnode.getTextContent().trim();
                String name = artnode.getAttribute("name"); //NON-NLS

                if (name.equals("ComputerName")) { // NON-NLS
                    compName = value;
                } else if (name.equals("Domain")) { // NON-NLS
                    domain = value;
                }

            }
        }
        try {
            List<BlackboardAttribute> bbattributes = Lists.newArrayList(
                    new BlackboardAttribute(
                            TSK_NAME, PARENT_MODULE_NAME,
                            compName),
                    new BlackboardAttribute(
                            TSK_DOMAIN, PARENT_MODULE_NAME,
                            domain));

            // Check if there is already an OS_INFO artifact for this file and add to that if possible
            ArrayList<BlackboardArtifact> results = caseDB.getBlackboardArtifacts(TSK_OS_INFO, regAbstractFile.getId());

            BlackboardArtifact bbart = results.isEmpty()
                    ? regAbstractFile.newArtifact(TSK_OS_INFO)
                    : results.get(0);
            bbart.addAttributes(bbattributes);
            return singleton(bbart); //todo: this may cause it to be re-indexed? is that okay?
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding os info artifact to blackboard."); //NON-NLS
        }
        return emptySet();
    }

    private Collection<BlackboardArtifact> processProfiler(NodeList myartlist, SleuthkitCase caseDB, AbstractFile regAbstractFile) throws IllegalArgumentException, DOMException {
        //map from node attribute names to tsk ATTRIBUTE_TYPE.
        Map<String, ATTRIBUTE_TYPE> keys = ImmutableMap.of(
                "PROCESSOR_IDENTIFIER", null,// TODO: should this go into an attribute?         //NON-NLS
                "OS", TSK_VERSION, //NON-NLS
                "PROCESSOR_ARCHITECTURE", TSK_PROCESSOR_ARCHITECTURE, //NON-NLS
                "TEMP", TSK_TEMP_DIR);        //NON-NLS

        List<BlackboardAttribute> bbattributes = new ArrayList<>();

        //parse the NodeList into black board attributes
        for (int j = 0; j < myartlist.getLength(); j++) {
            Node artchild = myartlist.item(j);
            // If it has attributes, then it is an Element (based off API)
            if (artchild.hasAttributes()) {
                Element artnode = (Element) artchild;
                ATTRIBUTE_TYPE attributeType = keys.get(artnode.getAttribute("name")); //NON-NLS
                if (attributeType != null) {
                    bbattributes.add(new BlackboardAttribute(attributeType, PARENT_MODULE_NAME, artnode.getTextContent().trim()));
                }
            }
        }
        try {
            // Check if there is already an OS_INFO artifact for this file and add to that if possible
            ArrayList<BlackboardArtifact> results = caseDB.getBlackboardArtifacts(TSK_OS_INFO, regAbstractFile.getId());
            BlackboardArtifact bbart = results.isEmpty()
                    ? regAbstractFile.newArtifact(TSK_OS_INFO)
                    : results.get(0);
            bbart.addAttributes(bbattributes);
            return singleton(bbart);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding os info artifact to blackboard."); //NON-NLS
        }
        return emptySet();
    }

    private Collection< BlackboardArtifact> processWinVersion(NodeList myartlist, SleuthkitCase caseDB, AbstractFile regAbstractFile) throws NumberFormatException, IllegalArgumentException, DOMException {
        String version = "";
        String systemRoot = "";
        String productId = "";
        String regOwner = "";
        String regOrg = "";
        Long installtime = null;
        for (int j = 0; j < myartlist.getLength(); j++) {
            Node artchild = myartlist.item(j);
            // If it has attributes, then it is an Element (based off API)
            if (artchild.hasAttributes()) {
                Element artnode = (Element) artchild;

                String value = artnode.getTextContent().trim();
                String name = artnode.getAttribute("name"); //NON-NLS
                switch (name) {
                    case "ProductName": // NON-NLS
                        version = value;
                        break;
                    case "CSDVersion": // NON-NLS
                        // This is dependant on the fact that ProductName shows up first in the module output
                        version = version + " " + value;
                        break;
                    case "SystemRoot": //NON-NLS
                        systemRoot = value;
                        break;
                    case "ProductId": //NON-NLS
                        productId = value;
                        break;
                    case "RegisteredOwner": //NON-NLS
                        regOwner = value;
                        break;
                    case "RegisteredOrganization": //NON-NLS
                        regOrg = value;
                        break;
                    case "InstallDate": //NON-NLS
                        try {
                            installtime = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy").parse(value).getTime() / 1000;
                        } catch (ParseException e) {
                            logger.log(Level.SEVERE, "RegRipper::Conversion on DateTime -> ", e); //NON-NLS
                        }
                        break;
                }
            }
        }//for 

        try {
            List<BlackboardAttribute> bbattributes = Lists.newArrayList(
                    new BlackboardAttribute(
                            TSK_PROG_NAME, PARENT_MODULE_NAME,
                            version),
                    new BlackboardAttribute(
                            TSK_PATH, PARENT_MODULE_NAME,
                            systemRoot),
                    new BlackboardAttribute(
                            TSK_PRODUCT_ID, PARENT_MODULE_NAME,
                            productId),
                    new BlackboardAttribute(
                            TSK_OWNER, PARENT_MODULE_NAME,
                            regOwner),
                    new BlackboardAttribute(
                            TSK_ORGANIZATION, PARENT_MODULE_NAME,
                            regOrg
                    ));
            if (installtime != null) {
                bbattributes.add(new BlackboardAttribute(
                        TSK_DATETIME, PARENT_MODULE_NAME,
                        installtime));
            }
            // Check if there is already an OS_INFO artifact for this file, and add to that if possible.
            ArrayList<BlackboardArtifact> results = caseDB.getBlackboardArtifacts(TSK_OS_INFO, regAbstractFile.getId());
            BlackboardArtifact bbart = results.isEmpty()
                    ? regAbstractFile.newArtifact(TSK_OS_INFO)
                    : results.get(0);
            bbart.addAttributes(bbattributes);
            return Collections.singleton(bbart);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error adding installed program artifact to blackboard."); //NON-NLS
        }
        return Collections.emptySet();
    }

    @Override
    public void process(Content dataSource, IngestJobContext context) {
        this.dataSource = dataSource;
        this.context = context;
        analyzeRegistryFiles();
    }
}
