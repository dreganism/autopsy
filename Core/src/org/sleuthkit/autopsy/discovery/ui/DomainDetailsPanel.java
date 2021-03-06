/*
 * Autopsy
 *
 * Copyright 2020 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
package org.sleuthkit.autopsy.discovery.ui;

import com.google.common.eventbus.Subscribe;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang.StringUtils;
import org.sleuthkit.autopsy.coreutils.ThreadConfined;
import org.sleuthkit.autopsy.discovery.search.DiscoveryEventUtils;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.autopsy.discovery.search.SearchData;

/**
 * Panel to display details area for domain discovery results.
 *
 */
final class DomainDetailsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ArtifactsWorker detailsWorker;
    private String domain;
    private String selectedTabName;

    /**
     * Creates new form ArtifactDetailsPanel.
     *
     * @param selectedTabName The name of the tab to select initially.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    DomainDetailsPanel(String selectedTabName) {
        initComponents();
        addArtifactTabs(selectedTabName);
    }

    /**
     * Add the tabs for each of the artifact types which we will be displaying.
     *
     * @param tabName The name of the tab to select initially.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private void addArtifactTabs(String tabName) {
        for (BlackboardArtifact.ARTIFACT_TYPE type : SearchData.Type.DOMAIN.getArtifactTypes()) {
            jTabbedPane1.add(type.getDisplayName(), new DomainArtifactsTabPanel(type));
        }
        selectedTabName = tabName;
        selectTab();
        jTabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (jTabbedPane1.getSelectedIndex() >= 0) {
                    String newTabTitle = jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex());
                    if (selectedTabName == null || !selectedTabName.equals(newTabTitle)) {
                        selectedTabName = newTabTitle;
                        runDomainWorker();
                    }
                }
            }
        });
    }

    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    /**
     * Set the selected tab index to be the previously selected tab if a
     * previously selected tab exists.
     */
    private void selectTab() {
        for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
            if (!StringUtils.isBlank(selectedTabName) && selectedTabName.equals(jTabbedPane1.getTitleAt(i))) {
                jTabbedPane1.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Run the worker which retrieves the list of artifacts for the domain to
     * populate the details area.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private void runDomainWorker() {
        Component selectedComponent = jTabbedPane1.getSelectedComponent();
        if (selectedComponent instanceof DomainArtifactsTabPanel) {
            if (detailsWorker != null && !detailsWorker.isDone()) {
                detailsWorker.cancel(true);
            }
            DomainArtifactsTabPanel selectedTab = (DomainArtifactsTabPanel) selectedComponent;
            if (selectedTab.getStatus() == DomainArtifactsTabPanel.ArtifactRetrievalStatus.UNPOPULATED) {
                DiscoveryEventUtils.getDiscoveryEventBus().register(selectedTab);
                selectedTab.setStatus(DomainArtifactsTabPanel.ArtifactRetrievalStatus.POPULATING);
                detailsWorker = new ArtifactsWorker(selectedTab.getArtifactType(), domain);
                detailsWorker.execute();
            }
        }
    }

    /**
     * Populate the the details tabs.
     *
     * @param populateEvent The PopulateDomainTabsEvent which indicates which
     *                      domain the details tabs should be populated for.
     */
    @Subscribe
    void handlePopulateDomainTabsEvent(DiscoveryEventUtils.PopulateDomainTabsEvent populateEvent) {
        domain = populateEvent.getDomain();
        SwingUtilities.invokeLater(() -> {
            resetTabsStatus();
            selectTab();
            runDomainWorker();
            if (StringUtils.isBlank(domain)) {
                //send fade out event
                DiscoveryEventUtils.getDiscoveryEventBus().post(new DiscoveryEventUtils.DetailsVisibleEvent(false));
            } else {
                //send fade in event
                DiscoveryEventUtils.getDiscoveryEventBus().post(new DiscoveryEventUtils.DetailsVisibleEvent(true));
            }
        });
    }

    /**
     * Private helper method to ensure tabs will re-populate after a new domain
     * is selected.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private void resetTabsStatus() {
        for (Component comp : jTabbedPane1.getComponents()) {
            if (comp instanceof DomainArtifactsTabPanel) {
                ((DomainArtifactsTabPanel) comp).setStatus(DomainArtifactsTabPanel.ArtifactRetrievalStatus.UNPOPULATED);
            }
        }
    }

    /**
     * Get the name of the tab that was most recently selected.
     *
     * @return The name of the tab that was most recently selected.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    String getSelectedTabName() {
        return selectedTabName;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();

        setEnabled(false);
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(0, 0));
        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
