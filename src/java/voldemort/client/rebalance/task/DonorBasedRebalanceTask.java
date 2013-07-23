/*
 * Copyright 2013 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package voldemort.client.rebalance.task;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import voldemort.client.protocol.admin.AdminClient;
import voldemort.client.rebalance.RebalanceBatchPlanProgressBar;
import voldemort.client.rebalance.RebalancePartitionsInfo;
import voldemort.client.rebalance.RebalanceTaskInfo;
import voldemort.store.UnreachableStoreException;

/**
 * Immutable class that executes a {@link RebalancePartitionsInfo} instance on
 * the rebalance client side
 * 
 * This is run from the donor nodes perspective
 */
public class DonorBasedRebalanceTask extends RebalanceTask {

    protected static final Logger logger = Logger.getLogger(DonorBasedRebalanceTask.class);

    private final int donorNodeId;

    public DonorBasedRebalanceTask(final int batchId,
                                   final int taskId,
                                   final List<RebalanceTaskInfo> stealInfos,
                                   final Semaphore donorPermit,
                                   final AdminClient adminClient,
                                   final RebalanceBatchPlanProgressBar progressBar) {
        super(batchId, taskId, stealInfos, donorPermit, adminClient, progressBar, logger);

        // TODO (Sid) : Comment this for replica type cleanup. Decide on donor based later.
        // RebalanceUtils.assertSameDonor(stealInfos, -1);
        this.donorNodeId = stealInfos.get(0).getDonorId();

        taskLog(toString());
    }

    @Override
    public void run() {
        int rebalanceAsyncId = INVALID_REBALANCE_ID;

        try {
            acquirePermit(donorNodeId);
            // TODO (Sid) : Comment this for replica type cleanup. Decide on donor based later.
            // Start rebalance task and then wait.
            // rebalanceAsyncId = adminClient.rebalanceOps.rebalanceNode(stealInfos);
            // taskStart(rebalanceAsyncId);
            //
            // adminClient.rpcOps.waitForCompletion(donorNodeId, rebalanceAsyncId);
            // taskDone(rebalanceAsyncId);

        } catch(UnreachableStoreException e) {
            exception = e;
            logger.error("Donor node " + donorNodeId
                                 + " is unreachable, please make sure it is up and running : "
                                 + e.getMessage(),
                         e);
        } catch(Exception e) {
            exception = e;
            logger.error("Rebalance failed : " + e.getMessage(), e);
        } finally {
            donorPermit.release();
            isComplete.set(true);
        }
    }

    @Override
    public String toString() {
        return "Donor based rebalance task on donor node " + donorNodeId + " : " + getStealInfos();
    }
}