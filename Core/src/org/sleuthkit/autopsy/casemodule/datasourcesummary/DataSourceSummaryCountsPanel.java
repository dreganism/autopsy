/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.casemodule.datasourcesummary;

import java.util.Map;
import org.sleuthkit.autopsy.coreutils.Logger;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.coreutils.FileTypeUtils;
import org.sleuthkit.datamodel.DataSource;

/**
 * Panel for displaying summary information on the known files present in the
 * specified DataSource
 */
@Messages({"DataSourceSummaryCountsPanel.FilesByMimeTypeTableModel.type.header=File Type",
    "DataSourceSummaryCountsPanel.FilesByMimeTypeTableModel.count.header=Count",
    "DataSourceSummaryCountsPanel.ArtifactCountsTableModel.type.header=Result Type",
    "DataSourceSummaryCountsPanel.ArtifactCountsTableModel.count.header=Count",
    "DataSourceSummaryCountsPanel.FilesByCategoryTableModel.type.header=File Type",
    "DataSourceSummaryCountsPanel.FilesByCategoryTableModel.count.header=Count"
})
class DataSourceSummaryCountsPanel extends javax.swing.JPanel {
    private static final Object[] MIME_TYPE_COLUMN_HEADERS = new Object[]{
        Bundle.DataSourceSummaryCountsPanel_FilesByMimeTypeTableModel_type_header(),
        Bundle.DataSourceSummaryCountsPanel_FilesByMimeTypeTableModel_count_header()
    };
    
    private static final Object[] FILE_BY_CATEGORY_COLUMN_HEADERS = new Object[]{
        Bundle.DataSourceSummaryCountsPanel_FilesByCategoryTableModel_type_header(),
        Bundle.DataSourceSummaryCountsPanel_FilesByCategoryTableModel_count_header()
    };
    
    private static final Object[] ARTIFACT_COUNTS_COLUMN_HEADERS = new Object[]{
        Bundle.DataSourceSummaryCountsPanel_ArtifactCountsTableModel_type_header(),
        Bundle.DataSourceSummaryCountsPanel_ArtifactCountsTableModel_count_header()
    };
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(DataSourceSummaryCountsPanel.class.getName());
    private final DefaultTableCellRenderer rightAlignedRenderer = new DefaultTableCellRenderer();
    
    private DataSource dataSource;

    /**
     * Creates new form DataSourceSummaryCountsPanel
     */
    DataSourceSummaryCountsPanel() {
        rightAlignedRenderer.setHorizontalAlignment(JLabel.RIGHT);
        initComponents();
        fileCountsByMimeTypeTable.getTableHeader().setReorderingAllowed(false);
        fileCountsByCategoryTable.getTableHeader().setReorderingAllowed(false);
        setDataSource(null);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        updateCountsTableData(dataSource);
    }

    /**
     * Specify the DataSource to display file information for
     *
     * @param selectedDataSource the DataSource to display file information for
     */
    private void updateCountsTableData(DataSource selectedDataSource) {
        fileCountsByMimeTypeTable.setModel(new DefaultTableModel(getMimeTypeModel(selectedDataSource), MIME_TYPE_COLUMN_HEADERS));
        fileCountsByMimeTypeTable.getColumnModel().getColumn(1).setCellRenderer(rightAlignedRenderer);
        fileCountsByMimeTypeTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        
        fileCountsByCategoryTable.setModel(new DefaultTableModel(getFileCategoryModel(selectedDataSource), FILE_BY_CATEGORY_COLUMN_HEADERS));
        fileCountsByCategoryTable.getColumnModel().getColumn(1).setCellRenderer(rightAlignedRenderer);
        fileCountsByCategoryTable.getColumnModel().getColumn(0).setPreferredWidth(130);
 
        artifactCountsTable.setModel(new DefaultTableModel(getArtifactCountsModel(selectedDataSource), ARTIFACT_COUNTS_COLUMN_HEADERS));
        artifactCountsTable.getColumnModel().getColumn(0).setPreferredWidth(230);
        artifactCountsTable.getColumnModel().getColumn(1).setCellRenderer(rightAlignedRenderer);
        
        this.repaint();
    }
    
    
    private static Object[] pair(String key, Object val) {
        return new Object[]{key, val};
    }
    
    @Messages({
        "DataSourceSummaryCountsPanel.FilesByMimeTypeTableModel.images.row=Images",
        "DataSourceSummaryCountsPanel.FilesByMimeTypeTableModel.videos.row=Videos",
        "DataSourceSummaryCountsPanel.FilesByMimeTypeTableModel.audio.row=Audio",
        "DataSourceSummaryCountsPanel.FilesByMimeTypeTableModel.documents.row=Documents",
        "DataSourceSummaryCountsPanel.FilesByMimeTypeTableModel.executables.row=Executables"
    })
    private static Object[][] getMimeTypeModel(DataSource dataSource) {
        return new Object[][]{
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByMimeTypeTableModel_images_row(), 
                    getCount(dataSource, FileTypeUtils.FileTypeCategory.IMAGE)),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByMimeTypeTableModel_videos_row(), 
                    getCount(dataSource, FileTypeUtils.FileTypeCategory.VIDEO)),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByMimeTypeTableModel_audio_row(), 
                    getCount(dataSource, FileTypeUtils.FileTypeCategory.AUDIO)),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByMimeTypeTableModel_documents_row(), 
                    getCount(dataSource, FileTypeUtils.FileTypeCategory.DOCUMENTS)),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByMimeTypeTableModel_executables_row(), 
                    getCount(dataSource, FileTypeUtils.FileTypeCategory.EXECUTABLE))
        };
    }

    private static Long getCount(DataSource dataSource, FileTypeUtils.FileTypeCategory category) {
        return DataSourceInfoUtilities.getCountOfFilesForMimeTypes(dataSource, category.getMediaTypes());
    }
    
    
    @Messages({
        "DataSourceSummaryCountsPanel.FilesByCategoryTableModel.all.row=All",
        "DataSourceSummaryCountsPanel.FilesByCategoryTableModel.allocated.row=Allocated",
        "DataSourceSummaryCountsPanel.FilesByCategoryTableModel.unallocated.row=Unallocated",
        "DataSourceSummaryCountsPanel.FilesByCategoryTableModel.slack.row=Slack",
        "DataSourceSummaryCountsPanel.FilesByCategoryTableModel.directory.row=Directory"
    })
    private static Object[][] getFileCategoryModel(DataSource selectedDataSource) {
        Long dataSourceId = selectedDataSource == null ? null : selectedDataSource.getId();
        // or 0 if not found
        Long fileCount = zeroIfNull(DataSourceInfoUtilities.getCountsOfFiles().get(dataSourceId));
        Long unallocatedFiles = zeroIfNull(DataSourceInfoUtilities.getCountsOfUnallocatedFiles().get(dataSourceId));
        Long allocatedFiles = zeroIfNull(getAllocatedCount(fileCount, unallocatedFiles));
        Long slackFiles = zeroIfNull(DataSourceInfoUtilities.getCountsOfSlackFiles().get(dataSourceId));
        Long directories = zeroIfNull(DataSourceInfoUtilities.getCountsOfDirectories().get(dataSourceId));

        return new Object[][]{
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByCategoryTableModel_all_row(), fileCount),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByCategoryTableModel_allocated_row(), allocatedFiles),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByCategoryTableModel_unallocated_row(), unallocatedFiles),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByCategoryTableModel_slack_row(), slackFiles),
            pair(Bundle.DataSourceSummaryCountsPanel_FilesByCategoryTableModel_directory_row(), directories)
        };
    }


    private static Long zeroIfNull(Long origValue) {
        return origValue == null ? 0 : origValue;
    }


    private static long getAllocatedCount(Long allFilesCount, Long unallocatedFilesCount) {
        if (allFilesCount == null) {
            return 0;
        } else if (unallocatedFilesCount == null) {
            return allFilesCount;
        } else {
            return allFilesCount - unallocatedFilesCount;
        }
    }
    
    private static Object[][] getArtifactCountsModel(DataSource selectedDataSource) {
        Long dataSourceId = selectedDataSource == null ? null : selectedDataSource.getId();
        Map<String, Long> artifactMapping = DataSourceInfoUtilities.getCountsOfArtifactsByType().get(dataSourceId);
        if (artifactMapping == null) {
            return new Object[][]{};
        }
        
        return artifactMapping.entrySet().stream()
                .filter((entrySet) -> entrySet != null && entrySet.getKey() != null)
                .sorted((a,b) -> a.getKey().compareTo(b.getKey()))
                .map((entrySet) -> new Object[]{entrySet.getKey(), entrySet.getValue()})
                .toArray(Object[][]::new);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileCountsByMimeTypeScrollPane = new javax.swing.JScrollPane();
        fileCountsByMimeTypeTable = new javax.swing.JTable();
        byMimeTypeLabel = new javax.swing.JLabel();
        fileCountsByCategoryScrollPane = new javax.swing.JScrollPane();
        fileCountsByCategoryTable = new javax.swing.JTable();
        byCategoryLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        artifactCountsScrollPane = new javax.swing.JScrollPane();
        artifactCountsTable = new javax.swing.JTable();

        fileCountsByMimeTypeScrollPane.setViewportView(fileCountsByMimeTypeTable);

        org.openide.awt.Mnemonics.setLocalizedText(byMimeTypeLabel, org.openide.util.NbBundle.getMessage(DataSourceSummaryCountsPanel.class, "DataSourceSummaryCountsPanel.byMimeTypeLabel.text")); // NOI18N

        fileCountsByCategoryScrollPane.setViewportView(fileCountsByCategoryTable);

        org.openide.awt.Mnemonics.setLocalizedText(byCategoryLabel, org.openide.util.NbBundle.getMessage(DataSourceSummaryCountsPanel.class, "DataSourceSummaryCountsPanel.byCategoryLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataSourceSummaryCountsPanel.class, "DataSourceSummaryCountsPanel.jLabel1.text")); // NOI18N

        artifactCountsTable.setAutoCreateRowSorter(true);
        artifactCountsScrollPane.setViewportView(artifactCountsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fileCountsByMimeTypeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addComponent(byMimeTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(byCategoryLabel)
                    .addComponent(fileCountsByCategoryScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(artifactCountsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {fileCountsByCategoryScrollPane, fileCountsByMimeTypeScrollPane});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(byMimeTypeLabel)
                    .addComponent(byCategoryLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(artifactCountsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileCountsByMimeTypeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fileCountsByCategoryScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fileCountsByCategoryScrollPane, fileCountsByMimeTypeScrollPane});

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane artifactCountsScrollPane;
    private javax.swing.JTable artifactCountsTable;
    private javax.swing.JLabel byCategoryLabel;
    private javax.swing.JLabel byMimeTypeLabel;
    private javax.swing.JScrollPane fileCountsByCategoryScrollPane;
    private javax.swing.JTable fileCountsByCategoryTable;
    private javax.swing.JScrollPane fileCountsByMimeTypeScrollPane;
    private javax.swing.JTable fileCountsByMimeTypeTable;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
