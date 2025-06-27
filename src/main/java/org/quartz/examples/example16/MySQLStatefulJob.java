/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.quartz.examples.example16;

import java.util.Date;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL 有状态任务
 *
 * <p>
 * 这是一个有状态的任务实现，演示了在 MySQL 集群环境下的状态持久化功能。
 * 该任务使用 JobDataMap 来维护状态信息，并在每次执行后将状态持久化到数据库中。
 * </p>
 *
 * <p>
 * 关键注解说明：
 * - @PersistJobDataAfterExecution: 任务执行完成后，将 JobDataMap 的更改持久化到数据库
 * - @DisallowConcurrentExecution: 禁止同一任务的并发执行，确保状态一致性
 * </p>
 *
 * <p>
 * 状态管理特性：
 * - 维护执行计数器
 * - 记录上次执行时间
 * - 累计执行总时长
 * - 跨集群实例共享状态
 * </p>
 *
 * <p>
 * 集群环境下的状态持久化：
 * 当任务在不同的调度器实例间迁移时，状态信息会自动同步，
 * 确保任务状态的一致性和连续性。
 * </p>
 *
 * @author Quartz Examples Team
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class MySQLStatefulJob implements Job {

    private static Logger _log = LoggerFactory.getLogger(MySQLStatefulJob.class);

    // JobDataMap 中的键名常量
    public static final String EXECUTION_COUNT = "executionCount";
    public static final String LAST_EXECUTION_TIME = "lastExecutionTime";
    public static final String TOTAL_EXECUTION_TIME = "totalExecutionTime";
    public static final String LAST_INSTANCE_ID = "lastInstanceId";

    /**
     * 有状态任务执行方法
     *
     * <p>
     * 该方法会读取和更新任务的状态信息，并在执行完成后
     * 自动将状态持久化到 MySQL 数据库中。
     * </p>
     *
     * @param context 任务执行上下文
     * @throws JobExecutionException 如果任务执行过程中发生错误
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobKey jobKey = context.getJobDetail().getKey();
        String currentInstanceId = null;
        try {
            currentInstanceId = context.getScheduler().getSchedulerInstanceId();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // 记录任务开始执行
        long startTime = System.currentTimeMillis();
        Date currentTime = new Date(startTime);

        _log.info("===== MySQL 有状态任务开始执行 =====");
        _log.info("任务标识: {}", jobKey);
        _log.info("当前调度器实例: {}", currentInstanceId);
        _log.info("执行时间: {}", currentTime);

        // 读取当前状态（如果键不存在则使用默认值）
        int executionCount = dataMap.containsKey(EXECUTION_COUNT) ? dataMap.getInt(EXECUTION_COUNT) : 0;
        long lastExecutionTime = dataMap.containsKey(LAST_EXECUTION_TIME) ? dataMap.getLong(LAST_EXECUTION_TIME) : 0L;
        long totalExecutionTime = dataMap.containsKey(TOTAL_EXECUTION_TIME) ? dataMap.getLong(TOTAL_EXECUTION_TIME) : 0L;
        String lastInstanceId = dataMap.containsKey(LAST_INSTANCE_ID) ? dataMap.getString(LAST_INSTANCE_ID) : null;

        _log.info("当前状态信息:");
        _log.info("  执行次数: {}", executionCount);
        _log.info("  上次执行时间: {}", lastExecutionTime > 0 ? new Date(lastExecutionTime) : "首次执行");
        _log.info("  累计执行时长: {} 毫秒", totalExecutionTime);
        _log.info("  上次执行实例: {}", lastInstanceId != null ? lastInstanceId : "首次执行");

        // 检查是否发生了实例迁移
        if (lastInstanceId != null && !lastInstanceId.equals(currentInstanceId)) {
            _log.warn("检测到任务实例迁移: {} -> {}", lastInstanceId, currentInstanceId);
            _log.info("这演示了集群环境下的任务故障转移功能");
        }

        try {
            // 模拟业务处理
            _log.info("正在执行有状态业务逻辑...");

            // 模拟处理时间（2-5秒）
            long processingTime = 2000 + (long)(Math.random() * 3000);
            Thread.sleep(processingTime);

            // 模拟基于状态的业务逻辑
            if (executionCount % 3 == 0 && executionCount > 0) {
                _log.info("执行特殊处理逻辑（每3次执行一次）");
                // 额外处理时间
                Thread.sleep(1000);
                processingTime += 1000;
            }

            // 更新状态信息
            executionCount++;
            totalExecutionTime += processingTime;

            // 将更新后的状态保存到 JobDataMap
            dataMap.put(EXECUTION_COUNT, executionCount);
            dataMap.put(LAST_EXECUTION_TIME, startTime);
            dataMap.put(TOTAL_EXECUTION_TIME, totalExecutionTime);
            dataMap.put(LAST_INSTANCE_ID, currentInstanceId);

            _log.info("业务逻辑执行完成，本次耗时: {} 毫秒", processingTime);
            _log.info("更新后的状态信息:");
            _log.info("  执行次数: {}", executionCount);
            _log.info("  累计执行时长: {} 毫秒", totalExecutionTime);
            _log.info("  平均执行时长: {} 毫秒", totalExecutionTime / executionCount);

        } catch (InterruptedException e) {
            _log.warn("有状态任务执行被中断: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new JobExecutionException("有状态任务执行被中断", e);
        } catch (Exception e) {
            _log.error("有状态任务执行过程中发生错误: {}", e.getMessage(), e);
            throw new JobExecutionException("有状态任务执行失败", e);
        }

        _log.info("===== MySQL 有状态任务执行完成 =====");
        _log.info("状态信息已自动持久化到 MySQL 数据库");
    }
}
