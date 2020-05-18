/*
 * Autopsy Forensic Browser
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
package org.sleuthkit.autopsy.contentviewers;

import java.awt.Component;
import java.util.logging.Level;
import javax.swing.JScrollPane;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * This is a viewer for TSK_CALLLOG artifacts.
 * 
 * 
 */
public class CallLogArtifactViewer extends javax.swing.JPanel implements ArtifactContentViewer {

    private final static Logger logger = Logger.getLogger(CallLogArtifactViewer.class.getName());
    private static final long serialVersionUID = 1L;

    
    /**
     * Creates new form CalllogArtifactViewer
     */
    public CallLogArtifactViewer() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        callDetailsPanel = new javax.swing.JPanel();
        toOrFromNumberLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        toOrFromNameLabel = new javax.swing.JLabel();
        directionLabel = new javax.swing.JLabel();
        dateTimeLabel = new javax.swing.JLabel();
        mediaTypeLabel = new javax.swing.JLabel();
        durationLabel = new javax.swing.JLabel();
        lowerHalfPanel = new javax.swing.JPanel();
        otherParticipantsPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        otherParticipantsLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        callDetailsPanel.setPreferredSize(new java.awt.Dimension(400, 150));

        org.openide.awt.Mnemonics.setLocalizedText(toOrFromNumberLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.toOrFromNumberLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(toOrFromNameLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.toOrFromNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(directionLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.directionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dateTimeLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.dateTimeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mediaTypeLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.mediaTypeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(durationLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.durationLabel.text")); // NOI18N

        javax.swing.GroupLayout callDetailsPanelLayout = new javax.swing.GroupLayout(callDetailsPanel);
        callDetailsPanel.setLayout(callDetailsPanelLayout);
        callDetailsPanelLayout.setHorizontalGroup(
            callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(callDetailsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(callDetailsPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(toOrFromNameLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(callDetailsPanelLayout.createSequentialGroup()
                        .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(callDetailsPanelLayout.createSequentialGroup()
                                .addComponent(toOrFromNumberLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, callDetailsPanelLayout.createSequentialGroup()
                                .addComponent(directionLabel)
                                .addGap(18, 18, 18)
                                .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(callDetailsPanelLayout.createSequentialGroup()
                                        .addComponent(durationLabel)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(callDetailsPanelLayout.createSequentialGroup()
                                        .addComponent(dateTimeLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                                        .addComponent(mediaTypeLabel)))))
                        .addGap(31, 31, 31))))
        );
        callDetailsPanelLayout.setVerticalGroup(
            callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(callDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toOrFromNumberLabel)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toOrFromNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(callDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directionLabel)
                    .addComponent(dateTimeLabel)
                    .addComponent(mediaTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(durationLabel)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        add(callDetailsPanel, java.awt.BorderLayout.PAGE_START);

        lowerHalfPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.jButton3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(otherParticipantsLabel, org.openide.util.NbBundle.getMessage(CallLogArtifactViewer.class, "CallLogArtifactViewer.otherParticipantsLabel.text")); // NOI18N

        javax.swing.GroupLayout otherParticipantsPanelLayout = new javax.swing.GroupLayout(otherParticipantsPanel);
        otherParticipantsPanel.setLayout(otherParticipantsPanelLayout);
        otherParticipantsPanelLayout.setHorizontalGroup(
            otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherParticipantsPanelLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(otherParticipantsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 173, Short.MAX_VALUE)
                        .addComponent(jButton3))
                    .addGroup(otherParticipantsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2)))
                .addGap(38, 38, 38))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, otherParticipantsPanelLayout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(otherParticipantsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        otherParticipantsPanelLayout.setVerticalGroup(
            otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherParticipantsPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(otherParticipantsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(otherParticipantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jLabel9))
                .addContainerGap())
        );

        lowerHalfPanel.add(otherParticipantsPanel, java.awt.BorderLayout.CENTER);

        add(lowerHalfPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel callDetailsPanel;
    private javax.swing.JLabel dateTimeLabel;
    private javax.swing.JLabel directionLabel;
    private javax.swing.JLabel durationLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel lowerHalfPanel;
    private javax.swing.JLabel mediaTypeLabel;
    private javax.swing.JLabel otherParticipantsLabel;
    private javax.swing.JPanel otherParticipantsPanel;
    private javax.swing.JLabel toOrFromNameLabel;
    private javax.swing.JLabel toOrFromNumberLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setArtifact(BlackboardArtifact artifact) {
        
        this.removeAll();
        this.initComponents();
        
        
        String toOrFromNumber = null;
        String name;
        String direction;
        String mediaType;
        
        try {
        toOrFromNumber= getToOrFromId(artifact);
        
        //direction = getDirection(artifact);
        
        //name = 
        
        
        }
        catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error getting attributes for Calllog artifact (artifact_id=%d, obj_id=%d)", artifact.getArtifactID(), artifact.getObjectID()), ex);
        }
        
        if (toOrFromNumber != null) {
        
            this.toOrFromNumberLabel.setText(toOrFromNumber);
        }
        
        // TBD: updateOtherParticipantsPanel(TBD) }
        
        
        // repaint
        this.revalidate();
    }

    @Override
    public Component getComponent() {
       return new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    @Override
    public boolean isSupported(BlackboardArtifact artifact) {
        return artifact.getArtifactTypeID() == BlackboardArtifact.ARTIFACT_TYPE.TSK_CALLLOG.getTypeID();
    }
    
    private String getToOrFromId(BlackboardArtifact artifact) throws TskCoreException {

        BlackboardAttribute numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM));

        if (numberAttr == null) {
            numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO));
        }

        if (numberAttr == null) {
            numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER));
        }

        if (numberAttr == null) {
            numberAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ID));
        }

        if (numberAttr != null) {

            // TBD: check if it's a list of numbers and and if so, return only the first one. and put the others in otherParticicpants....
            return numberAttr.getValueString();
        }

        return null;
    }
    
}
