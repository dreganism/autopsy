/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019-2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.experimental.autoingest;

import org.sleuthkit.autopsy.casemodule.multiusercases.CaseNodeData;
import org.sleuthkit.autopsy.progress.ProgressIndicator;

/**
 * A task to delete the auto ingest job coordination service nodes and the
 * output for a case produced via auto ingest, while leaving the auto ingest job
 * input directories intact. The use case is reprocessing a case with a clean
 * slate without having to restore the input directories.
 */
final class DeleteCaseOutputTask extends DeleteCaseTask {

    /**
     * Constructs a task to delete the auto ingest job coordination service
     * nodes and the output for a case produced via auto ingest, while leaving
     * the auto ingest job input directories intact. The use case is
     * reprocessing a case with a clean slate without having to restore the
     * input directories.
     *
     * @param caseNodeData The case directory lock coordination service node
     *                     data for the case.
     * @param progress     A progress indicator.
     */
    DeleteCaseOutputTask(CaseNodeData caseNodeData, ProgressIndicator progress) {
        super(caseNodeData, progress);
    }

    @Override
    void deleteWhileHoldingAllLocks() throws InterruptedException {
        deleteCaseOutput();
    }

    @Override
    void deleteAfterCaseDirectoryLockReleased() throws InterruptedException {
    }

    @Override
    void deleteAfterManifestLocksReleased() throws InterruptedException {
        deleteManifestFileLockNodes();
    }

    @Override
    void deleteAfterCaseNameLockReleased() throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
